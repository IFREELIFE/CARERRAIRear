package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.AppointmentEvaluateRequest;
import com.endcareerai.platform.dto.response.TeacherSlotResponse;

import java.util.List;

/**
 * 学校服务接口
 * 提供辅导老师时间段查询和辅导评价（RAG 反哺）功能
 */
public interface SchoolService {

    /**
     * 获取指定学校的可用辅导老师与时间段
     *
     * @param schoolUserId 学校用户ID
     * @return 辅导老师可用时间段列表
     */
    List<TeacherSlotResponse> getAvailableSlots(Long schoolUserId);

    /**
     * 老师提交辅导纪要与评价（触发 RAG 反哺重算学生画像）
     *
     * @param appointmentId 咨询预约ID
     * @param request       评价请求
     */
    void evaluateAppointment(Long appointmentId, AppointmentEvaluateRequest request);
}
