package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.InterviewFeedbackRequest;
import com.endcareerai.platform.service.EnterpriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @PostMapping("/jobs/import/excel")
    public Result<Void> importJobsExcel(@RequestParam("file") MultipartFile file) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        enterpriseService.importJobsExcel(userId, file);
        return Result.success();
    }

    @PutMapping("/enterprise/jobs/{jobId}/close")
    public Result<Void> closeJob(@PathVariable Long jobId) {
        enterpriseService.closeJob(jobId);
        return Result.success();
    }

    @PostMapping("/enterprise/interviews/{applicationId}/feedback")
    public Result<Void> submitInterviewFeedback(@PathVariable Long applicationId,
                                                @RequestBody @Valid InterviewFeedbackRequest request) {
        enterpriseService.submitInterviewFeedback(applicationId, request);
        return Result.success();
    }
}
