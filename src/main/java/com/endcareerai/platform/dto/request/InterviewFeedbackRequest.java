package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 面试反馈请求DTO，包含面试结果（PASS/FAIL）、标签和备注
 */
@Data
public class InterviewFeedbackRequest {
    @NotBlank
    private String result;   // PASS | FAIL

    private List<String> tags;

    private String notes;
}
