package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 任务重试请求DTO，包含管理员修正提示和部分重试字段列表
 */
@Data
public class TaskRetryRequest {
    @NotBlank
    private String correctionPrompt;

    private List<String> partialRetryFields;
}
