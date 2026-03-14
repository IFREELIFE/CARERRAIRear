package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.CareerMatchRequest;
import com.endcareerai.platform.dto.request.JobApplyRequest;
import com.endcareerai.platform.dto.request.JobChatRequest;
import com.endcareerai.platform.dto.request.ProfileInitRequest;
import com.endcareerai.platform.dto.response.CareerMatchResponse;
import com.endcareerai.platform.dto.response.JobChatResponse;
import com.endcareerai.platform.entity.JobApplication;
import com.endcareerai.platform.es.JobDocument;
import com.endcareerai.platform.service.ElasticsearchService;
import com.endcareerai.platform.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端控制器（模块二）
 * 提供学生档案初始化、职业匹配、AI 问答、岗位投递和岗位搜索等功能
 */
@Slf4j
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final ElasticsearchService elasticsearchService;

    /**
     * 提交初始技术能力与 MBTI 测试结果
     * 学生首次登录后的必填节点，提交后后端推入消息队列由 LLM 异步生成12维能力雷达图
     * 前端必须传入 is_guaranteed=true（确认信息保证书）
     *
     * @param request 包含技术技能、MBTI 结果和保证确认的请求体
     * @return 操作成功标识
     */
    @PostMapping("/profile/init")
    public Result<Void> initProfile(@RequestBody @Valid ProfileInitRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        studentService.initProfile(userId, request);
        return Result.success();
    }

    /**
     * 目标职业匹配与 PDF 规划方案生成
     * AI 对比学生12维画像与目标岗位市场平均画像，若匹配度 < 70% 则拦截并返回原因；
     * 若学生强制确认（force_generate=true）或匹配度 >= 70%，则生成个性化职业规划 PDF
     *
     * @param request 包含目标城市、目标岗位和是否强制生成的请求体
     * @return 匹配分数、推荐状态、分析原因和 PDF 下载链接
     */
    @PostMapping("/career/match-and-plan")
    public Result<CareerMatchResponse> matchAndPlan(@RequestBody @Valid CareerMatchRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CareerMatchResponse response = studentService.matchAndPlan(userId, request);
        return Result.success(response);
    }

    /**
     * 岗位详情页 AI Agent 智能问答（1/4屏）
     * 后端将清洗后的岗位画像和学生12维画像作为 System Prompt 上下文注入 LLM，
     * 使其能精准回答"我是否胜任该岗位"、"需要学习什么"等问题
     *
     * @param request 包含岗位编码和用户提问的请求体
     * @return AI 回答内容和对应岗位编码
     */
    @PostMapping("/agent/job-chat")
    public Result<JobChatResponse> jobChat(@RequestBody @Valid JobChatRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobChatResponse response = studentService.jobChat(userId, request);
        return Result.success(response);
    }

    /**
     * 岗位投递与企业授权
     * 学生提交简历，生成投递记录并设置 is_authorized 授权状态，
     * 授权后对应企业/HR 可查看学生的详细12维画像和原始档案
     *
     * @param jobId   目标岗位ID
     * @param request 包含是否授权企业查看画像的请求体
     * @return 投递记录详情
     */
    @PostMapping("/jobs/{jobId}/apply")
    public Result<JobApplication> applyJob(@PathVariable Long jobId,
                                           @RequestBody @Valid JobApplyRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobApplication application = studentService.applyJob(userId, jobId, request);
        return Result.success(application);
    }

    /**
     * 通过关键词搜索岗位（基于 Elasticsearch 全文检索）
     * 匹配岗位名称和工作地点
     *
     * @param keyword 搜索关键词
     * @return 匹配的岗位文档列表
     */
    @GetMapping("/jobs/search")
    public Result<List<JobDocument>> searchJobs(@RequestParam String keyword) {
        List<JobDocument> results = elasticsearchService.searchJobs(keyword);
        return Result.success(results);
    }

    /**
     * 获取所有状态为 ACTIVE 的上架岗位列表
     *
     * @return ACTIVE 状态的岗位文档列表
     */
    @GetMapping("/jobs/active")
    public Result<List<JobDocument>> getActiveJobs() {
        List<JobDocument> results = elasticsearchService.getActiveJobs();
        return Result.success(results);
    }
}
