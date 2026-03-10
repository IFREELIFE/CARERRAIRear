package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.dto.request.AppointmentEvaluateRequest;
import com.endcareerai.platform.dto.response.TeacherSlotResponse;
import com.endcareerai.platform.entity.CounselingAppointment;
import com.endcareerai.platform.entity.Teacher;
import com.endcareerai.platform.mapper.CounselingAppointmentMapper;
import com.endcareerai.platform.mapper.TeacherMapper;
import com.endcareerai.platform.mq.LlmTaskProducer;
import com.endcareerai.platform.service.RedisService;
import com.endcareerai.platform.service.SchoolService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final TeacherMapper teacherMapper;
    private final CounselingAppointmentMapper counselingAppointmentMapper;
    private final LlmTaskProducer llmTaskProducer;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public List<TeacherSlotResponse> getAvailableSlots(Long schoolUserId) {
        List<Teacher> teachers = teacherMapper.selectList(
                new QueryWrapper<Teacher>().eq("school_user_id", schoolUserId));

        List<TeacherSlotResponse> result = new ArrayList<>();
        for (Teacher teacher : teachers) {
            List<CounselingAppointment> pendingAppointments = counselingAppointmentMapper.selectList(
                    new QueryWrapper<CounselingAppointment>()
                            .eq("teacher_id", teacher.getId())
                            .eq("status", "PENDING"));

            List<String> availableTimes = new ArrayList<>();
            for (CounselingAppointment appointment : pendingAppointments) {
                availableTimes.add(appointment.getAppointmentTime().toString());
            }

            // Derive location from pending appointments if available
            String location = pendingAppointments.isEmpty()
                    ? null
                    : pendingAppointments.get(0).getLocation();

            TeacherSlotResponse slot = new TeacherSlotResponse(
                    teacher.getId(),
                    teacher.getName(),
                    location,
                    availableTimes);
            result.add(slot);
        }

        // Cache the result
        String cacheKey = Constants.REDIS_TEACHER_SLOTS_PREFIX + schoolUserId;
        redisService.set(cacheKey, result, 15, TimeUnit.MINUTES);

        log.info("Available slots queried: schoolUserId={}, teacherCount={}", schoolUserId, result.size());
        return result;
    }

    @Override
    @Transactional
    public void evaluateAppointment(Long appointmentId, AppointmentEvaluateRequest request) {
        CounselingAppointment appointment = counselingAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("咨询预约不存在");
        }

        appointment.setTeacherEvaluation(request.getTeacherEvaluation());

        // Convert tags list to JSON string
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                appointment.setTeacherTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize teacher tags", e);
                throw new BusinessException("标签序列化失败");
            }
        }

        appointment.setStatus("COMPLETED");
        appointment.setIsRagProcessed(0);
        counselingAppointmentMapper.updateById(appointment);

        // Send RAG recalculate task for the student
        llmTaskProducer.sendTask("RAG_RECALCULATE", appointment.getStudentId(), "v1");

        log.info("Appointment evaluated: appointmentId={}, studentId={}",
                appointmentId, appointment.getStudentId());
    }
}
