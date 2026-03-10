package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.TaskRetryRequest;
import com.endcareerai.platform.dto.response.LlmTaskStatsResponse;
import com.endcareerai.platform.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/llmops/tasks")
    public Result<LlmTaskStatsResponse> getTaskStats() {
        LlmTaskStatsResponse stats = adminService.getTaskStats();
        return Result.success(stats);
    }

    @PostMapping("/llmops/tasks/{taskId}/retry")
    public Result<Void> retryTask(@PathVariable String taskId,
                                  @RequestBody @Valid TaskRetryRequest request) {
        adminService.retryTask(taskId, request);
        return Result.success();
    }
}
