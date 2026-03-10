package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobChatRequest {
    @NotBlank
    private String jobCode;

    @NotBlank
    private String question;
}
