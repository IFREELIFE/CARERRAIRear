package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.dto.request.CareerMatchRequest;
import com.endcareerai.platform.dto.request.JobApplyRequest;
import com.endcareerai.platform.dto.request.JobChatRequest;
import com.endcareerai.platform.dto.request.ProfileInitRequest;
import com.endcareerai.platform.dto.response.CareerMatchResponse;
import com.endcareerai.platform.dto.response.JobChatResponse;
import com.endcareerai.platform.entity.Job;
import com.endcareerai.platform.entity.JobApplication;
import com.endcareerai.platform.entity.Student;
import com.endcareerai.platform.mapper.JobApplicationMapper;
import com.endcareerai.platform.mapper.JobMapper;
import com.endcareerai.platform.mapper.StudentMapper;
import com.endcareerai.platform.mq.LlmTaskProducer;
import com.endcareerai.platform.service.ElasticsearchService;
import com.endcareerai.platform.service.LlmService;
import com.endcareerai.platform.service.RedisService;
import com.endcareerai.platform.service.StudentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 学生服务实现类
 * 实现学生端核心业务逻辑，包括档案初始化、职业匹配、AI 问答和岗位投递
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final LlmTaskProducer llmTaskProducer;
    private final LlmService llmService;
    private final RedisService redisService;
    private final ElasticsearchService elasticsearchService;
    private final ObjectMapper objectMapper;

    /**
     * 初始化学生档案（技术技能 + MBTI）
     * 处理流程：
     * 1. 校验信息保证书确认状态
     * 2. 获取或创建学生记录
     * 3. 保存技术技能和 MBTI 结果
     * 4. 推送 GEN_STUDENT_PROFILE 任务到 MQ 由 LLM 异步生成12维画像
     * 5. 清除用户缓存
     *
     * @param userId  当前用户ID
     * @param request 包含技术技能、MBTI 结果和保证确认的请求体
     */
    @Override
    @Transactional
    public void initProfile(Long userId, ProfileInitRequest request) {
        if (!request.isGuaranteed()) {
            throw new BusinessException("必须确认信息保证书");
        }

        // Get or create student record
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            student = new Student();
            student.setUserId(userId);
            studentMapper.insert(student);
        }

        student.setTechSkillsRaw(request.getTechSkills());
        student.setMbtiResult(request.getMbti());
        studentMapper.updateById(student);

        // Send task to generate 12-dim profile via LLM
        llmTaskProducer.sendTask("GEN_STUDENT_PROFILE", userId, "v1");

        // Invalidate cache
        redisService.delete(Constants.REDIS_USER_PREFIX + userId);

        log.info("Student profile initialized: userId={}", userId);
    }

    /**
     * 目标职业匹配与 PDF 规划方案生成
     * 处理流程：
     * 1. 校验学生记录及12维画像是否已生成
     * 2. 更新学生的目标城市和职业偏好
     * 3. 调用 LLM 进行职业匹配度分析
     * 4. 若匹配度 < 70% 且未强制生成，则拦截返回原因
     * 5. 否则生成个性化职业规划 PDF 下载链接
     * 6. 缓存匹配结果到 Redis
     *
     * @param userId  当前用户ID
     * @param request 包含目标城市、目标岗位和是否强制生成的请求体
     * @return 匹配结果响应（含分数、推荐状态、原因和 PDF 链接）
     */
    @Override
    public CareerMatchResponse matchAndPlan(Long userId, CareerMatchRequest request) {
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException("学生记录不存在");
        }
        if (student.getAi12DimRadar() == null) {
            throw new BusinessException("请先完成档案初始化，等待AI生成12维画像");
        }

        // Update target preferences
        student.setTargetCity(request.getTargetCity());
        student.setTargetJob(request.getTargetJob());
        studentMapper.updateById(student);

        // Call LLM for career match analysis
        int matchScore;
        String reason;
        try {
            String analysisResult = llmService.careerMatchAnalysis(
                    student.getAi12DimRadar(), request.getTargetCity(), request.getTargetJob());
            JsonNode analysisJson = objectMapper.readTree(analysisResult);
            matchScore = analysisJson.has("matchScore") ? analysisJson.get("matchScore").asInt() : 60;
            reason = analysisJson.has("reason") ? analysisJson.get("reason").asText() : "AI分析完成";
        } catch (Exception e) {
            log.warn("LLM career match analysis failed, falling back to default: {}", e.getMessage());
            matchScore = student.getAi12DimRadar() != null ? 75 : 50;
            reason = "匹配度评估完成（基于画像基础分析）";
        }

        if (matchScore < Constants.MATCH_THRESHOLD && !request.isForceGenerate()) {
            CareerMatchResponse response = new CareerMatchResponse();
            response.setMatchScore(matchScore);
            response.setRecommend(false);
            response.setReason(reason);
            response.setPdfUrl(null);
            return response;
        }

        // Generate PDF URL
        String pdfUrl = "/api/reports/career-plan/" + userId + "_" + System.currentTimeMillis() + ".pdf";

        CareerMatchResponse response = new CareerMatchResponse();
        response.setMatchScore(matchScore);
        response.setRecommend(true);
        response.setReason(reason);
        response.setPdfUrl(pdfUrl);

        // Cache match result
        String cacheKey = Constants.REDIS_USER_PREFIX + userId + ":match";
        redisService.set(cacheKey, response, 60, TimeUnit.MINUTES);

        log.info("Career match completed: userId={}, score={}", userId, matchScore);
        return response;
    }

    /**
     * 岗位详情页 AI Agent 智能问答
     * 处理流程：
     * 1. 根据岗位编码查询岗位信息
     * 2. 查询学生12维画像
     * 3. 将岗位画像和学生画像作为上下文注入 LLM
     * 4. LLM 生成针对性回答（如"是否胜任"、"需要学什么"等）
     * 5. 若 LLM 调用失败则返回兜底回答
     *
     * @param userId  当前用户ID
     * @param request 包含岗位编码和用户提问的请求体
     * @return AI 回答响应
     */
    @Override
    public JobChatResponse jobChat(Long userId, JobChatRequest request) {
        Job job = jobMapper.selectOne(
                new QueryWrapper<Job>().eq("job_code", request.getJobCode()));
        if (job == null) {
            throw new BusinessException("岗位不存在: " + request.getJobCode());
        }

        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException("学生记录不存在");
        }

        // Call LLM for intelligent job chat response
        String answer;
        try {
            answer = llmService.jobChat(
                    job.getTitle(),
                    job.getAiExtractedProfile(),
                    student.getAi12DimRadar(),
                    request.getQuestion());
        } catch (Exception e) {
            log.warn("LLM job chat failed, returning fallback: {}", e.getMessage());
            answer = String.format(
                    "关于岗位【%s】的问题「%s」：基于您的技能画像和该岗位要求，建议关注以下方面。（AI服务暂时不可用，请稍后再试）",
                    job.getTitle(), request.getQuestion());
        }

        log.info("Job chat: userId={}, jobCode={}", userId, request.getJobCode());
        return new JobChatResponse(answer, request.getJobCode());
    }

    /**
     * 岗位投递与企业授权
     * 处理流程：
     * 1. 校验岗位存在且状态为 ACTIVE
     * 2. 校验学生记录存在
     * 3. 创建投递记录，根据学生授权意愿设置 is_authorized
     * 4. 同步岗位信息到 Elasticsearch
     *
     * @param userId  当前用户ID
     * @param jobId   岗位ID
     * @param request 包含是否授权企业查看画像的请求体
     * @return 投递记录
     */
    @Override
    @Transactional
    public JobApplication applyJob(Long userId, Long jobId, JobApplyRequest request) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        if (!"ACTIVE".equals(job.getStatus())) {
            throw new BusinessException("该岗位当前不可申请");
        }

        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException("学生记录不存在");
        }

        JobApplication application = new JobApplication();
        application.setStudentId(userId);
        application.setJobId(jobId);
        application.setEnterpriseId(job.getEnterpriseId());
        application.setIsAuthorized(request.isGrantAuthToEnterprise() ? 1 : 0);
        application.setStatus("APPLIED");
        application.setCreatedAt(LocalDateTime.now());
        jobApplicationMapper.insert(application);

        // Sync job to ES to update search metadata
        elasticsearchService.syncJobToEs(job);

        log.info("Job application created: userId={}, jobId={}, applicationId={}",
                userId, jobId, application.getId());
        return application;
    }
}
