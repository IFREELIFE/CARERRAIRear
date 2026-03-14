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

/**
 * 管理端服务实现类
 * 提供 LLMOps 任务监控仪表盘数据查询和失败任务人工纠偏重试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final LlmTaskMapper llmTaskMapper;
    private final LlmTaskProducer llmTaskProducer;

    /**
     * 获取 AI 异步任务统计信息
     * 分别统计 QUEUED、PROCESSING、SUCCESS、FAILED 四种状态的任务数量，
     * 并返回最近100条任务记录用于仪表盘展示
     *
     * @return 任务统计响应（含各状态计数和最近任务列表）
     */
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

    /**
     * 人工纠偏精准重试失败任务
     * 校验任务存在且状态为 FAILED 后，将修正提示和部分重试字段信息重新推入 MQ 队列
     *
     * @param taskId  任务ID
     * @param request 重试请求（包含修正提示和部分重试字段）
     */
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
