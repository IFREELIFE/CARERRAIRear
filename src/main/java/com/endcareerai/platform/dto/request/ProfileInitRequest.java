package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生档案初始化请求DTO，包含技术技能、MBTI结果和信息保证确认
 */
@Data
public class ProfileInitRequest {
    @NotBlank
    private String techSkills;

    @NotBlank
    private String mbti;

    private boolean isGuaranteed;
}
