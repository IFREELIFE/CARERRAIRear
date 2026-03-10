package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.AppointmentEvaluateRequest;
import com.endcareerai.platform.dto.response.TeacherSlotResponse;

import java.util.List;

public interface SchoolService {
    List<TeacherSlotResponse> getAvailableSlots(Long schoolUserId);
    void evaluateAppointment(Long appointmentId, AppointmentEvaluateRequest request);
}
