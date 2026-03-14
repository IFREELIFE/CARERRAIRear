package com.endcareerai.platform.mq;

import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.entity.LlmTask;
import com.endcareerai.platform.mapper.LlmTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RabbitMQ 生产者 —— 发送 LLM 异步任务消息
 * 负责创建任务记录到数据库，并将任务消息推送到 RabbitMQ 队列，
 * 同时支持失败任务的人工纠偏重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTaskProducer {

    private final RabbitTemplate rabbitTemplate;
    private final LlmTaskMapper llmTaskMapper;

    /**
     * 发送新的 LLM 异步任务到 RabbitMQ 并记录到数据库
     * 生成唯一任务ID，创建 QUEUED 状态的任务记录，然后将消息推入 MQ 队列
     *
     * @param taskType      任务类型（EXTRACT_JOB_XLS / GEN_STUDENT_PROFILE / RAG_RECALCULATE）
     * @param targetId      业务关联ID（job_id 或 student_id）
     * @param promptVersion Prompt 版本号
     * @return 生成的任务ID
     */
    public String sendTask(String taskType, Long targetId, String promptVersion) {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        // Save task record to DB
        LlmTask task = new LlmTask();
        task.setTaskId(taskId);
        task.setTaskType(taskType);
        task.setTargetId(targetId);
        task.setStatus("QUEUED");
        task.setPromptVersion(promptVersion);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        llmTaskMapper.insert(task);

        // Send message to RabbitMQ
        LlmTaskMessage message = new LlmTaskMessage();
        message.setTaskId(taskId);
        message.setTaskType(taskType);
        message.setTargetId(targetId);
        message.setPromptVersion(promptVersion);

        rabbitTemplate.convertAndSend(
                Constants.MQ_EXCHANGE_LLM,
                Constants.MQ_ROUTING_KEY_LLM,
                message
        );

        log.info("LLM task sent to MQ: taskId={}, type={}, targetId={}", taskId, taskType, targetId);
        return taskId;
    }

    /**
     * 重新发送失败任务到 RabbitMQ（人工纠偏重试）
     * 更新任务状态为 QUEUED，附加修正提示和部分重试字段信息后重新推入 MQ 队列
     *
     * @param existingTask       已存在的失败任务实体
     * @param correctionPrompt   管理员提供的修正提示
     * @param partialRetryFields 需要重试的字段列表（null 表示全部重试）
     */
    public void retryTask(LlmTask existingTask, String correctionPrompt, java.util.List<String> partialRetryFields) {
        existingTask.setStatus("QUEUED");
        existingTask.setManualCorrection(correctionPrompt);
        existingTask.setUpdatedAt(LocalDateTime.now());
        llmTaskMapper.updateById(existingTask);

        LlmTaskMessage message = new LlmTaskMessage();
        message.setTaskId(existingTask.getTaskId());
        message.setTaskType(existingTask.getTaskType());
        message.setTargetId(existingTask.getTargetId());
        message.setPromptVersion(existingTask.getPromptVersion());
        message.setCorrectionPrompt(correctionPrompt);
        message.setPartialRetryFields(partialRetryFields);

        rabbitTemplate.convertAndSend(
                Constants.MQ_EXCHANGE_LLM,
                Constants.MQ_ROUTING_KEY_LLM,
                message
        );

        log.info("LLM task retry sent to MQ: taskId={}", existingTask.getTaskId());
    }
}
