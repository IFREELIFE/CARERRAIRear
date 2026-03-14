package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.dto.request.InterviewFeedbackRequest;
import com.endcareerai.platform.entity.Enterprise;
import com.endcareerai.platform.entity.InterviewFeedback;
import com.endcareerai.platform.entity.Job;
import com.endcareerai.platform.entity.JobApplication;
import com.endcareerai.platform.entity.Student;
import com.endcareerai.platform.mapper.EnterpriseMapper;
import com.endcareerai.platform.mapper.InterviewFeedbackMapper;
import com.endcareerai.platform.mapper.JobApplicationMapper;
import com.endcareerai.platform.mapper.JobMapper;
import com.endcareerai.platform.mapper.StudentMapper;
import com.endcareerai.platform.mq.LlmTaskProducer;
import com.endcareerai.platform.service.ElasticsearchService;
import com.endcareerai.platform.service.EnterpriseService;
import com.endcareerai.platform.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 企业服务实现类
 * 实现岗位 Excel 批量导入、岗位关闭和面试反馈（自愈闭环）逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewFeedbackMapper interviewFeedbackMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final StudentMapper studentMapper;
    private final LlmTaskProducer llmTaskProducer;
    private final ElasticsearchService elasticsearchService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /**
     * 批量导入岗位 Excel 表格
     * 处理流程：
     * 1. 校验企业信息存在
     * 2. 逐行解析 Excel（跳过表头），按 job_code 去重
     * 3. 每个岗位入库（状态为 PENDING_AI），推送 EXTRACT_JOB_XLS 任务到 MQ
     * 4. 同步岗位到 Elasticsearch（后续 LLM 处理完成后再次同步为 ACTIVE）
     *
     * @param enterpriseUserId 企业用户ID
     * @param file             Excel 文件
     */
    @Override
    @Transactional
    public void importJobsExcel(Long enterpriseUserId, MultipartFile file) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseUserId);
        if (enterprise == null) {
            throw new BusinessException("企业信息不存在");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int imported = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String jobCode = getCellStringValue(row, 0);
                if (jobCode == null || jobCode.isBlank()) continue;

                // Check for duplicate
                Long duplicateCount = jobMapper.selectCount(
                        new QueryWrapper<Job>().eq("job_code", jobCode));
                if (duplicateCount > 0) {
                    log.info("Skipping duplicate job_code: {}", jobCode);
                    continue;
                }

                Job job = new Job();
                job.setEnterpriseId(enterpriseUserId);
                job.setJobCode(jobCode);
                job.setTitle(getCellStringValue(row, 1));
                job.setLocation(getCellStringValue(row, 2));
                job.setSalaryRange(getCellStringValue(row, 3));
                job.setRawDescription(getCellStringValue(row, 4));
                job.setSourceUrl(getCellStringValue(row, 5));
                job.setSourceUpdateDate(getCellStringValue(row, 6));
                job.setStatus("PENDING_AI");
                job.setCreatedAt(LocalDateTime.now());
                jobMapper.insert(job);

                // Send LLM extraction task
                llmTaskProducer.sendTask("EXTRACT_JOB_XLS", job.getId(), "v1");

                // Sync to Elasticsearch
                elasticsearchService.syncJobToEs(job);

                imported++;
            }

            log.info("Excel import completed: enterpriseUserId={}, imported={}", enterpriseUserId, imported);
        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            throw new BusinessException("Excel文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 关闭/下架招聘岗位
     * 将岗位状态从 ACTIVE 改为 CLOSED，从 Elasticsearch 中移除该岗位，并清除 Redis 缓存
     *
     * @param jobId 岗位ID
     */
    @Override
    @Transactional
    public void closeJob(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        if (!"ACTIVE".equals(job.getStatus())) {
            throw new BusinessException("只能关闭状态为 ACTIVE 的岗位");
        }

        job.setStatus("CLOSED");
        jobMapper.updateById(job);

        // Remove from Elasticsearch
        elasticsearchService.removeJobFromEs(jobId);

        // Invalidate Redis cache
        redisService.delete(Constants.REDIS_JOB_PREFIX + jobId);

        log.info("Job closed: jobId={}", jobId);
    }

    /**
     * 提交真实面试反馈（自愈闭环核心）
     * 处理流程：
     * 1. 校验投递记录存在
     * 2. 创建面试反馈记录，保存结果、标签和备注
     * 3. 若结果为 FAIL，自动将 HR 的标签和备注追加到学生的 Gap_JSON（匹配差距清单）
     * 4. 标记反馈已同步给 AI 系统
     *
     * @param applicationId 投递申请ID
     * @param request       包含面试结果（PASS/FAIL）、标签和备注的请求体
     */
    @Override
    @Transactional
    public void submitInterviewFeedback(Long applicationId, InterviewFeedbackRequest request) {
        JobApplication application = jobApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setApplicationId(applicationId);
        feedback.setResult(request.getResult());
        feedback.setFeedbackNotes(request.getNotes());
        feedback.setIsSyncedToAi(0);
        feedback.setCreatedAt(LocalDateTime.now());

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                feedback.setFeedbackTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize feedback tags", e);
                throw new BusinessException("标签序列化失败");
            }
        }

        interviewFeedbackMapper.insert(feedback);

        // If result is FAIL, update student gap info
        if ("FAIL".equals(request.getResult())) {
            Student student = studentMapper.selectById(application.getStudentId());
            if (student != null) {
                String existingGap = student.getGapJson();
                String newGapEntry;
                try {
                    newGapEntry = objectMapper.writeValueAsString(
                            Map.of(
                                    "applicationId", applicationId,
                                    "tags", request.getTags() != null ? request.getTags() : Collections.emptyList(),
                                    "notes", request.getNotes() != null ? request.getNotes() : "",
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize gap entry", e);
                    throw new BusinessException("Gap信息序列化失败");
                }

                if (existingGap == null || existingGap.isBlank()) {
                    student.setGapJson("[" + newGapEntry + "]");
                } else {
                    // Deserialize existing array, append new entry, serialize back
                    try {
                        List<?> gapList = objectMapper.readValue(existingGap, List.class);
                        List<Object> mutableList = new ArrayList<>(gapList);
                        mutableList.add(objectMapper.readValue(newGapEntry, Object.class));
                        student.setGapJson(objectMapper.writeValueAsString(mutableList));
                    } catch (JsonProcessingException ex) {
                        log.warn("Existing gap_json is malformed, overwriting: {}", existingGap);
                        student.setGapJson("[" + newGapEntry + "]");
                    }
                }
                studentMapper.updateById(student);

                feedback.setIsSyncedToAi(1);
                interviewFeedbackMapper.updateById(feedback);
            }
        }

        log.info("Interview feedback submitted: applicationId={}, result={}", applicationId, request.getResult());
    }

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * 从 Excel 行中提取指定列的字符串值
     *
     * @param row       Excel 行对象
     * @param cellIndex 列索引（从0开始）
     * @return 单元格字符串值，空值返回 null
     */
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = DATA_FORMATTER.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }
}
