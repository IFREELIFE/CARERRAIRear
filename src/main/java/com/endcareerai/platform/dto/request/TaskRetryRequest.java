package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TaskRetryRequest {
    @NotBlank
    private String correctionPrompt;

    private List<String> partialRetryFields;
}
