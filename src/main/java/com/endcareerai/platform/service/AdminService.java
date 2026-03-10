package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.TaskRetryRequest;
import com.endcareerai.platform.dto.response.LlmTaskStatsResponse;

public interface AdminService {
    LlmTaskStatsResponse getTaskStats();
    void retryTask(String taskId, TaskRetryRequest request);
}
