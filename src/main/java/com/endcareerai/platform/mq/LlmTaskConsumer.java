package com.endcareerai.platform.mq;

import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.entity.LlmTask;
import com.endcareerai.platform.mapper.LlmTaskMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTaskConsumer {

    private final LlmTaskMapper llmTaskMapper;

    @RabbitListener(queues = Constants.MQ_QUEUE_LLM_TASK)
    public void processTask(LlmTaskMessage message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String taskId = message.getTaskId();
        log.info("Received LLM task: taskId={}, type={}", taskId, message.getTaskType());

        LlmTask task = llmTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("Task not found: {}", taskId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            // Update status to PROCESSING
            task.setStatus("PROCESSING");
            task.setUpdatedAt(LocalDateTime.now());
            llmTaskMapper.updateById(task);

            // Process based on task type
            switch (message.getTaskType()) {
                case "EXTRACT_JOB_XLS":
                    processJobExtraction(message);
                    break;
                case "GEN_STUDENT_PROFILE":
                    processStudentProfile(message);
                    break;
                case "RAG_RECALCULATE":
                    processRagRecalculate(message);
                    break;
                default:
                    log.warn("Unknown task type: {}", message.getTaskType());
            }

            // Mark as success
            task.setStatus("SUCCESS");
            task.setUpdatedAt(LocalDateTime.now());
            llmTaskMapper.updateById(task);

            channel.basicAck(deliveryTag, false);
            log.info("LLM task completed: taskId={}", taskId);

        } catch (Exception e) {
            log.error("LLM task failed: taskId={}", taskId, e);

            task.setStatus("FAILED");
            task.setErrorLog(e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            llmTaskMapper.updateById(task);

            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Process job extraction from Excel data via LLM
     * In production, this would call an LLM API to extract structured data from raw job descriptions
     */
    private void processJobExtraction(LlmTaskMessage message) {
        log.info("Processing job extraction for targetId={}, prompt version={}",
                message.getTargetId(), message.getPromptVersion());
        // TODO: Integrate actual LLM API call for job data extraction
        // This would:
        // 1. Fetch the raw job description from jobs table
        // 2. Send to LLM for structured extraction (skills, education, experience requirements)
        // 3. Update jobs.ai_extracted_profile with the result
        // 4. Set confidence_score
        // 5. Update status to PENDING_REVIEW or ACTIVE
    }

    /**
     * Generate student 12-dimension profile via LLM
     */
    private void processStudentProfile(LlmTaskMessage message) {
        log.info("Generating student profile for targetId={}", message.getTargetId());
        // TODO: Integrate actual LLM API call for student profile generation
        // This would:
        // 1. Fetch student's tech_skills_raw and mbti_result
        // 2. Send to LLM to generate 12-dimension radar chart data
        // 3. Update students.ai_12_dim_radar with JSON result
    }

    /**
     * Re-calculate student profile with RAG (teacher evaluation feedback)
     */
    private void processRagRecalculate(LlmTaskMessage message) {
        log.info("Processing RAG recalculate for targetId={}", message.getTargetId());
        // TODO: Integrate actual LLM + RAG pipeline
        // This would:
        // 1. Retrieve teacher evaluations from vector DB
        // 2. Combine with existing student profile
        // 3. Re-evaluate soft skill scores via LLM
        // 4. Update students.ai_12_dim_radar
    }
}
