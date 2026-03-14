package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI岗位问答响应DTO，包含AI回答内容和岗位编码
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobChatResponse {
    private String answer;
    private String jobCode;
}
