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

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTaskProducer {

    private final RabbitTemplate rabbitTemplate;
    private final LlmTaskMapper llmTaskMapper;

    /**
     * Send a new LLM task to RabbitMQ and record in DB
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
     * Re-send a task for retry with correction info
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
