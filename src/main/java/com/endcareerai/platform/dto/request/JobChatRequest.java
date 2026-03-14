package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI岗位问答请求DTO，包含岗位编码和用户提问内容
 */
@Data
public class JobChatRequest {
    @NotBlank
    private String jobCode;

    @NotBlank
    private String question;
}
