package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.CareerMatchRequest;
import com.endcareerai.platform.dto.request.JobApplyRequest;
import com.endcareerai.platform.dto.request.JobChatRequest;
import com.endcareerai.platform.dto.request.ProfileInitRequest;
import com.endcareerai.platform.dto.response.CareerMatchResponse;
import com.endcareerai.platform.dto.response.JobChatResponse;
import com.endcareerai.platform.entity.JobApplication;

/**
 * 学生服务接口
 * 提供学生端核心业务：档案初始化、职业匹配、AI 问答和岗位投递
 */
public interface StudentService {

    /**
     * 初始化学生档案（技术技能 + MBTI）
     * 保存学生信息并推送 LLM 异步任务生成12维画像
     *
     * @param userId  当前用户ID
     * @param request 档案初始化请求
     */
    void initProfile(Long userId, ProfileInitRequest request);

    /**
     * 目标职业匹配与 PDF 规划方案生成
     * 调用 LLM 分析匹配度，若低于阈值则拦截，否则生成职业规划 PDF
     *
     * @param userId  当前用户ID
     * @param request 职业匹配请求
     * @return 匹配结果响应
     */
    CareerMatchResponse matchAndPlan(Long userId, CareerMatchRequest request);

    /**
     * 岗位详情页 AI Agent 智能问答
     * 注入岗位画像和学生画像上下文，调用 LLM 回答学生提问
     *
     * @param userId  当前用户ID
     * @param request AI 问答请求
     * @return AI 回答响应
     */
    JobChatResponse jobChat(Long userId, JobChatRequest request);

    /**
     * 岗位投递与企业授权
     * 创建投递记录并根据学生授权意愿设置企业查看画像的权限
     *
     * @param userId  当前用户ID
     * @param jobId   岗位ID
     * @param request 投递请求
     * @return 投递记录
     */
    JobApplication applyJob(Long userId, Long jobId, JobApplyRequest request);
}
