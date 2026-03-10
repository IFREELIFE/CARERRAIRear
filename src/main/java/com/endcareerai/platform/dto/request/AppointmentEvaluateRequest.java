package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AppointmentEvaluateRequest {
    private List<String> tags;

    @NotBlank
    private String teacherEvaluation;
}
