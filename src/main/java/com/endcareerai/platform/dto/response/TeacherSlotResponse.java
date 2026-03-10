package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSlotResponse {
    private Long teacherId;
    private String name;
    private String location;
    private List<String> availableTimes;
}
