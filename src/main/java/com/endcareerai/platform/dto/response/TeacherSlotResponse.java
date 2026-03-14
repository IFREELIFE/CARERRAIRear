package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教师可用时间段响应DTO，包含教师ID、姓名、地点和可用时间列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSlotResponse {
    private Long teacherId;
    private String name;
    private String location;
    private List<String> availableTimes;
}
