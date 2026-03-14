package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.TaskRetryRequest;
import com.endcareerai.platform.dto.response.LlmTaskStatsResponse;
import com.endcareerai.platform.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端控制器（模块五 / LLMOps）
 * 提供 AI 异步任务全链路监控和人工纠偏精准重试功能
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取 AI 异步任务全链路监控列表
     * 管理员可实时查看 RabbitMQ 任务系统执行情况，
     * 返回当前排队、执行中、成功和失败的任务数量，以及最近100条任务记录，用于仪表盘展示
     *
     * @return 包含各状态任务计数和最近任务列表的统计响应
     */
    @GetMapping("/llmops/tasks")
    public Result<LlmTaskStatsResponse> getTaskStats() {
        LlmTaskStatsResponse stats = adminService.getTaskStats();
        return Result.success(stats);
    }

    /**
     * 人工纠偏与精准重试（节省 Token）
     * 管理员发现 AI 字段提取错误时（如置信度低、把"熟悉"误判为"精通"），调用此接口。
     * 管理员提供修正备注，系统要求 LLM 仅重新提取错误字段（通过 partial_retry_fields 指定），
     * 直接复用之前正确字段，大幅节省 LLM API Token 开销，实现 AI 质量持续进化
     *
     * @param taskId  任务ID
     * @param request 包含修正提示和部分重试字段列表的请求体
     * @return 操作成功标识
     */
    @PostMapping("/llmops/tasks/{taskId}/retry")
    public Result<Void> retryTask(@PathVariable String taskId,
                                  @RequestBody @Valid TaskRetryRequest request) {
        adminService.retryTask(taskId, request);
        return Result.success();
    }
}
