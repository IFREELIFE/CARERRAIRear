package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.dto.request.TaskRetryRequest;
import com.endcareerai.platform.dto.response.LlmTaskStatsResponse;
import com.endcareerai.platform.entity.LlmTask;
import com.endcareerai.platform.mapper.LlmTaskMapper;
import com.endcareerai.platform.mq.LlmTaskProducer;
import com.endcareerai.platform.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final LlmTaskMapper llmTaskMapper;
    private final LlmTaskProducer llmTaskProducer;

    @Override
    public LlmTaskStatsResponse getTaskStats() {
        long queued = llmTaskMapper.selectCount(
                new QueryWrapper<LlmTask>().eq("status", "QUEUED"));
        long processing = llmTaskMapper.selectCount(
                new QueryWrapper<LlmTask>().eq("status", "PROCESSING"));
        long success = llmTaskMapper.selectCount(
                new QueryWrapper<LlmTask>().eq("status", "SUCCESS"));
        long failed = llmTaskMapper.selectCount(
                new QueryWrapper<LlmTask>().eq("status", "FAILED"));

        List<LlmTask> tasks = llmTaskMapper.selectList(
                new QueryWrapper<LlmTask>().orderByDesc("created_at").last("LIMIT 100"));

        log.info("Task stats queried: queued={}, processing={}, success={}, failed={}",
                queued, processing, success, failed);
        return new LlmTaskStatsResponse(queued, processing, success, failed, tasks);
    }

    @Override
    @Transactional
    public void retryTask(String taskId, TaskRetryRequest request) {
        LlmTask task = llmTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在: " + taskId);
        }
        if (!"FAILED".equals(task.getStatus())) {
            throw new BusinessException("只能重试状态为 FAILED 的任务");
        }

        llmTaskProducer.retryTask(task, request.getCorrectionPrompt(), request.getPartialRetryFields());

        log.info("Task retry initiated: taskId={}", taskId);
    }
}
