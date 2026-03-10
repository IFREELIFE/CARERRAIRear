package com.endcareerai.platform.dto.response;

import com.endcareerai.platform.entity.LlmTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
