package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerMatchResponse {
    private int matchScore;
    private boolean recommend;
    private String reason;
    private String pdfUrl;
}
