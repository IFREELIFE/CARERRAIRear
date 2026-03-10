package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileInitRequest {
    @NotBlank
    private String techSkills;

    @NotBlank
    private String mbti;

    private boolean isGuaranteed;
}
