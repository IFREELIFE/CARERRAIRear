package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 辅导预约表实体（counseling_appointments），记录学生与老师的辅导预约及评价反馈
 */
@Data
@TableName(value = "counseling_appointments", autoResultMap = true)
public class CounselingAppointment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("appointment_time")
    private LocalDateTime appointmentTime;

    @TableField("location")
    private String location;

    @TableField("status")
    private String status;

    @TableField("teacher_evaluation")
    private String teacherEvaluation;

    @TableField(value = "teacher_tags", typeHandler = JacksonTypeHandler.class)
    private String teacherTags;

    @TableField("is_rag_processed")
    private Integer isRagProcessed;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
