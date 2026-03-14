package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 职业匹配响应DTO，包含匹配分数、推荐状态、分析原因和PDF链接
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerMatchResponse {
    private int matchScore;
    private boolean recommend;
    private String reason;
    private String pdfUrl;
}
