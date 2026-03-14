package com.endcareerai.platform.dto.response;

import com.endcareerai.platform.entity.LlmTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM任务统计响应DTO，包含各状态任务计数和最近任务列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmTaskStatsResponse {
    private long queued;
    private long processing;
    private long success;
    private long failed;
    private List<LlmTask> tasks;
}
