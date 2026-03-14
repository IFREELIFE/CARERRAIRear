package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.TaskRetryRequest;
import com.endcareerai.platform.dto.response.LlmTaskStatsResponse;

/**
 * 管理端服务接口
 * 提供 LLMOps 任务监控和人工纠偏重试功能
 */
public interface AdminService {

    /**
     * 获取 AI 异步任务统计信息
     * 包含各状态任务计数和最近任务列表
     *
     * @return 任务统计响应
     */
    LlmTaskStatsResponse getTaskStats();

    /**
     * 人工纠偏精准重试失败任务
     * 管理员提供修正提示，系统仅重新提取指定字段以节省 Token
     *
     * @param taskId  任务ID
     * @param request 重试请求
     */
    void retryTask(String taskId, TaskRetryRequest request);
}
