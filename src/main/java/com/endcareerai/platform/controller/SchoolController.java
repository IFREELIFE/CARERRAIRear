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

/**
 * 学校端控制器（模块三）
 * 提供辅导老师时间段查询和辅导评价提交功能，支持 RAG 反哺机制
 */
@Slf4j
@RestController
@RequestMapping("/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * 获取可用辅导老师与时间段列表
     * 学校创建多位辅导老师后，学生可通过此接口获取所有老师的排班、空闲时段和指导地点，
     * 用于前端日历预约展示
     *
     * @return 包含老师ID、姓名、地点和可用时间列表的响应
     */
    @GetMapping("/teachers/available-slots")
    public Result<List<TeacherSlotResponse>> getAvailableSlots() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TeacherSlotResponse> slots = schoolService.getAvailableSlots(userId);
        return Result.success(slots);
    }

    /**
     * 老师提交辅导纪要与评价（触发 RAG 反哺）
     * 线下 1V1 辅导结束后，老师调用此接口提交辅导纪要。核心机制：提交后系统将评价向量化存入向量库（RAG），
     * 下次 AI 重新评估学生画像时会强制检索老师评价，实现人机协同智能微调学生软技能得分（沟通、抗压等）
     *
     * @param appointmentId 咨询预约ID
     * @param request       包含标签和辅导纪要的请求体
     * @return 操作成功标识
     */
    @PostMapping("/appointments/{appointmentId}/evaluate")
    public Result<Void> evaluateAppointment(@PathVariable Long appointmentId,
                                            @RequestBody @Valid AppointmentEvaluateRequest request) {
        schoolService.evaluateAppointment(appointmentId, request);
        return Result.success();
    }
}
