package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class InterviewFeedbackRequest {
    @NotBlank
    private String result;   // PASS | FAIL

    private List<String> tags;

    private String notes;
}
