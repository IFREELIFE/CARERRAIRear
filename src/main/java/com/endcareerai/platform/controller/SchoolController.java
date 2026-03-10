package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.AppointmentEvaluateRequest;
import com.endcareerai.platform.dto.response.TeacherSlotResponse;
import com.endcareerai.platform.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping("/teachers/available-slots")
    public Result<List<TeacherSlotResponse>> getAvailableSlots() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TeacherSlotResponse> slots = schoolService.getAvailableSlots(userId);
        return Result.success(slots);
    }

    @PostMapping("/appointments/{appointmentId}/evaluate")
    public Result<Void> evaluateAppointment(@PathVariable Long appointmentId,
                                            @RequestBody @Valid AppointmentEvaluateRequest request) {
        schoolService.evaluateAppointment(appointmentId, request);
        return Result.success();
    }
}
