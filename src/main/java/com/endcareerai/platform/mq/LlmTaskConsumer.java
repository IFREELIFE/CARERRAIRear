package com.endcareerai.platform.mq;

import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.entity.CounselingAppointment;
import com.endcareerai.platform.entity.Job;
import com.endcareerai.platform.entity.LlmTask;
import com.endcareerai.platform.entity.Student;
import com.endcareerai.platform.mapper.CounselingAppointmentMapper;
import com.endcareerai.platform.mapper.JobMapper;
import com.endcareerai.platform.mapper.LlmTaskMapper;
import com.endcareerai.platform.mapper.StudentMapper;
import com.endcareerai.platform.service.ElasticsearchService;
import com.endcareerai.platform.service.LlmService;
import com.endcareerai.platform.service.RedisService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ 消费者 —— 异步处理 LLM 任务
 * 监听 llm.task.queue 队列，根据任务类型分发到对应处理方法，
 * 支持岗位画像提取、学生画像生成、RAG反哺重算三种任务类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTaskConsumer {

    private final LlmTaskMapper llmTaskMapper;
    private final JobMapper jobMapper;
    private final StudentMapper studentMapper;
    private final CounselingAppointmentMapper counselingAppointmentMapper;
    private final LlmService llmService;
    private final ElasticsearchService elasticsearchService;
    private final RedisService redisService;

    /**
     * 监听 RabbitMQ 队列，接收并处理 LLM 异步任务消息
     * 处理流程：更新任务状态为 PROCESSING → 根据类型分发处理 → 标记 SUCCESS/FAILED
     * 使用手动 ACK 模式确保消息可靠消费
     *
     * @param message     MQ 消息体，包含任务ID、类型、目标ID等
     * @param channel     RabbitMQ Channel，用于手动确认/拒绝消息
     * @param deliveryTag 消息投递标签，用于 ACK/NACK
     */
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
     * 处理岗位画像提取任务（EXTRACT_JOB_XLS）
     * 从岗位原始描述中通过 LLM 提取结构化画像信息（技能要求、学历、经验等），
     * 更新岗位状态为 ACTIVE 并同步到 Elasticsearch 使其可被搜索
     *
     * @param message MQ 消息体，包含 targetId（即 job_id）和可选的修正提示
     */
    private void processJobExtraction(LlmTaskMessage message) {
        Long jobId = message.getTargetId();
        log.info("Processing job extraction for jobId={}, prompt version={}", jobId, message.getPromptVersion());

        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new RuntimeException("Job not found: " + jobId);
        }

        String rawDescription = job.getRawDescription();
        if (rawDescription == null || rawDescription.isBlank()) {
            throw new RuntimeException("Job raw description is empty for jobId=" + jobId);
        }

        // Call LLM to extract structured job profile
        String extractedProfile = llmService.extractJobProfile(rawDescription, message.getCorrectionPrompt());

        // Update job with AI-extracted profile
        job.setAiExtractedProfile(extractedProfile);
        job.setConfidenceScore(new BigDecimal("0.85"));
        job.setStatus("ACTIVE");
        jobMapper.updateById(job);

        // Sync updated job to Elasticsearch so it can be searched as ACTIVE
        elasticsearchService.syncJobToEs(job);

        // Invalidate cache
        redisService.delete(Constants.REDIS_JOB_PREFIX + jobId);

        log.info("Job extraction completed: jobId={}, profileLength={}", jobId, extractedProfile.length());
    }

    /**
     * 处理学生画像生成任务（GEN_STUDENT_PROFILE）
     * 根据学生填写的技术技能和 MBTI 类型，通过 LLM 生成12维能力雷达图数据，
     * 12个维度包括：编程能力、算法、系统设计、沟通、团队协作、领导力等
     *
     * @param message MQ 消息体，包含 targetId（即 student_id / user_id）和可选的修正提示
     */
    private void processStudentProfile(LlmTaskMessage message) {
        Long studentId = message.getTargetId();
        log.info("Generating student profile for studentId={}", studentId);

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("Student not found: " + studentId);
        }

        String techSkills = student.getTechSkillsRaw();
        String mbtiResult = student.getMbtiResult();
        if (techSkills == null || techSkills.isBlank()) {
            throw new RuntimeException("Student tech skills are empty for studentId=" + studentId);
        }

        // Call LLM to generate 12-dimension radar profile
        String radarProfile = llmService.generateStudentProfile(techSkills, mbtiResult, message.getCorrectionPrompt());

        // Update student with AI-generated 12-dim radar
        student.setAi12DimRadar(radarProfile);
        studentMapper.updateById(student);

        // Invalidate cache
        redisService.delete(Constants.REDIS_USER_PREFIX + studentId);

        log.info("Student profile generated: studentId={}, radarLength={}", studentId, radarProfile.length());
    }

    /**
     * 处理 RAG 反哺重算任务（RAG_RECALCULATE）
     * 收集该学生所有未处理的教师辅导评价，结合当前12维画像，
     * 通过 LLM + RAG 流程重新评估并微调学生的软技能得分（如沟通、抗压等），
     * 处理完成后标记相关预约记录为已反哺
     *
     * @param message MQ 消息体，包含 targetId（即 student_id）和可选的修正提示
     */
    private void processRagRecalculate(LlmTaskMessage message) {
        Long studentId = message.getTargetId();
        log.info("Processing RAG recalculate for studentId={}", studentId);

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("Student not found: " + studentId);
        }

        String currentRadar = student.getAi12DimRadar();
        if (currentRadar == null || currentRadar.isBlank()) {
            throw new RuntimeException("Student has no existing radar profile for studentId=" + studentId);
        }

        // Retrieve the latest teacher evaluation for this student
        List<CounselingAppointment> completedAppointments = counselingAppointmentMapper.selectList(
                new QueryWrapper<CounselingAppointment>()
                        .eq("student_id", studentId)
                        .eq("status", "COMPLETED")
                        .eq("is_rag_processed", 0)
                        .orderByDesc("appointment_time"));

        StringBuilder feedbackBuilder = new StringBuilder();
        for (CounselingAppointment appointment : completedAppointments) {
            if (appointment.getTeacherEvaluation() != null) {
                feedbackBuilder.append("评价: ").append(appointment.getTeacherEvaluation());
                if (appointment.getTeacherTags() != null) {
                    feedbackBuilder.append(" 标签: ").append(appointment.getTeacherTags());
                }
                feedbackBuilder.append("\n");
            }
        }

        String teacherFeedback = feedbackBuilder.toString();
        if (teacherFeedback.isBlank()) {
            log.info("No unprocessed teacher feedback for studentId={}", studentId);
            return;
        }

        // Call LLM to recalculate profile with teacher feedback
        String updatedRadar = llmService.ragRecalculateProfile(currentRadar, teacherFeedback, message.getCorrectionPrompt());

        // Update student radar
        student.setAi12DimRadar(updatedRadar);
        studentMapper.updateById(student);

        // Mark appointments as RAG processed
        for (CounselingAppointment appointment : completedAppointments) {
            appointment.setIsRagProcessed(1);
            counselingAppointmentMapper.updateById(appointment);
        }

        // Invalidate cache
        redisService.delete(Constants.REDIS_USER_PREFIX + studentId);

        log.info("RAG recalculate completed: studentId={}, feedbackCount={}", studentId, completedAppointments.size());
    }
}
