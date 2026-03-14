package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 职业匹配请求DTO，包含目标城市、目标岗位和是否强制生成方案
 */
@Data
public class CareerMatchRequest {
    @NotBlank
    private String targetCity;

    @NotBlank
    private String targetJob;

    private boolean forceGenerate;
}
