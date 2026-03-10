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
import com.endcareerai.platform.service.RedisService;
import com.endcareerai.platform.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final LlmTaskProducer llmTaskProducer;
    private final RedisService redisService;
    private final ElasticsearchService elasticsearchService;

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

        // Calculate match score (mock: 75 if radar exists, 50 otherwise)
        int matchScore = student.getAi12DimRadar() != null ? 75 : 50;

        if (matchScore < Constants.MATCH_THRESHOLD && !request.isForceGenerate()) {
            CareerMatchResponse response = new CareerMatchResponse();
            response.setMatchScore(matchScore);
            response.setRecommend(false);
            response.setReason("匹配度低于阈值(" + Constants.MATCH_THRESHOLD + ")，建议调整目标城市或岗位");
            response.setPdfUrl(null);
            return response;
        }

        // Generate mock PDF URL
        String pdfUrl = "/api/reports/career-plan/" + userId + "_" + System.currentTimeMillis() + ".pdf";

        CareerMatchResponse response = new CareerMatchResponse();
        response.setMatchScore(matchScore);
        response.setRecommend(true);
        response.setReason("匹配度良好，已生成职业规划报告");
        response.setPdfUrl(pdfUrl);

        // Cache match result
        String cacheKey = Constants.REDIS_USER_PREFIX + userId + ":match";
        redisService.set(cacheKey, response, 60, TimeUnit.MINUTES);

        log.info("Career match completed: userId={}, score={}", userId, matchScore);
        return response;
    }

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

        // Build mock AI response (in production, would call LLM)
        String answer = String.format(
                "关于岗位【%s】的问题「%s」：基于您的技能画像和该岗位要求，建议关注以下方面。(AI分析结果将在LLM集成后提供)",
                job.getTitle(), request.getQuestion());

        log.info("Job chat: userId={}, jobCode={}", userId, request.getJobCode());
        return new JobChatResponse(answer, request.getJobCode());
    }

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
