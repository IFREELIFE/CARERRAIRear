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

/**
 * 企业端控制器（模块四）
 * 提供岗位 Excel 批量导入、岗位关闭和面试反馈提交功能
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    /**
     * 批量上传岗位 Excel 表格
     * 支持平台管理员导入大盘数据和企业上传招聘模板 Excel。
     * 采用异步设计：解析表格长文本后按"站点+岗位编码"去重，
     * 拆分为独立任务推入 RabbitMQ 队列，由后端 LLM 逐步清洗提取，防止超时
     *
     * @param file Excel 文件（multipart/form-data 格式）
     * @return 操作成功标识
     */
    @PostMapping("/jobs/import/excel")
    public Result<Void> importJobsExcel(@RequestParam("file") MultipartFile file) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        enterpriseService.importJobsExcel(userId, file);
        return Result.success();
    }

    /**
     * 手动关闭/下架招聘岗位
     * 企业 HR 调用此接口将岗位状态从 ACTIVE 改为 CLOSED，
     * 关闭后岗位立即从学生信息枢纽中隐藏并停止接收新投递
     *
     * @param jobId 岗位ID
     * @return 操作成功标识
     */
    @PutMapping("/enterprise/jobs/{jobId}/close")
    public Result<Void> closeJob(@PathVariable Long jobId) {
        enterpriseService.closeJob(jobId);
        return Result.success();
    }

    /**
     * 企业录入真实面试反馈（触发自愈闭环）
     * 平台最核心的自愈闭环端点。面试结束后 HR 录入结果，
     * 若结果为 FAIL，后端自动抓取 HR 的标签和备注，静默追加到学生的 Gap_JSON（匹配差距清单），
     * 帮助学生了解具体失败原因，同时系统获得更精准的学生能力认知
     *
     * @param applicationId 投递申请ID
     * @param request       包含面试结果（PASS/FAIL）、标签和备注的请求体
     * @return 操作成功标识
     */
    @PostMapping("/enterprise/interviews/{applicationId}/feedback")
    public Result<Void> submitInterviewFeedback(@PathVariable Long applicationId,
                                                @RequestBody @Valid InterviewFeedbackRequest request) {
        enterpriseService.submitInterviewFeedback(applicationId, request);
        return Result.success();
    }
}
