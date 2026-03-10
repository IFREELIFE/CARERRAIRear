package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareerMatchRequest {
    @NotBlank
    private String targetCity;

    @NotBlank
    private String targetJob;

    private boolean forceGenerate;
}
