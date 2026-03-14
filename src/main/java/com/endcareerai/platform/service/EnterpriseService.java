package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.InterviewFeedbackRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业服务接口
 * 提供岗位 Excel 批量导入、岗位关闭和面试反馈（自愈闭环）功能
 */
public interface EnterpriseService {

    /**
     * 批量导入岗位 Excel 表格
     * 解析表格后去重、入库，并推送 LLM 异步提取任务到 RabbitMQ
     *
     * @param enterpriseUserId 企业用户ID
     * @param file             Excel 文件
     */
    void importJobsExcel(Long enterpriseUserId, MultipartFile file);

    /**
     * 关闭/下架招聘岗位
     * 将岗位状态从 ACTIVE 改为 CLOSED，同时从 ES 中移除
     *
     * @param jobId 岗位ID
     */
    void closeJob(Long jobId);

    /**
     * 提交真实面试反馈（FAIL 时触发自愈闭环追加到学生 Gap_JSON）
     *
     * @param applicationId 投递申请ID
     * @param request       面试反馈请求
     */
    void submitInterviewFeedback(Long applicationId, InterviewFeedbackRequest request);
}
