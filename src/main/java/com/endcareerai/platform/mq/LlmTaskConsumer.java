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

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTaskConsumer {

    private final LlmTaskMapper llmTaskMapper;
    private final JobMapper jobMapper;
    private final StudentMapper studentMapper;
    private final CounselingAppointmentMapper counselingAppointmentMapper;
    private final LlmService llmService;
    private final RedisService redisService;

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
     * 通过 LLM 从岗位原始描述中提取结构化画像
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

        // Invalidate cache
        redisService.delete(Constants.REDIS_JOB_PREFIX + jobId);

        log.info("Job extraction completed: jobId={}, profileLength={}", jobId, extractedProfile.length());
    }

    /**
     * 通过 LLM 根据学生技能和 MBTI 生成12维能力画像
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
     * 结合教师评价反馈通过 LLM + RAG 重新计算学生画像
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
