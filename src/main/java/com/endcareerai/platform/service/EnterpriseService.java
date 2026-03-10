package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.InterviewFeedbackRequest;
import org.springframework.web.multipart.MultipartFile;

public interface EnterpriseService {
    void importJobsExcel(Long enterpriseUserId, MultipartFile file);
    void closeJob(Long jobId);
    void submitInterviewFeedback(Long applicationId, InterviewFeedbackRequest request);
}
