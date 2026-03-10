package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName(value = "students", autoResultMap = true)
public class Student {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @TableField("real_name")
    private String realName;

    @TableField("grade")
    private String grade;

    @TableField("education_level")
    private String educationLevel;

    @TableField("school_name")
    private String schoolName;

    @TableField("tech_skills_raw")
    private String techSkillsRaw;

    @TableField("mbti_result")
    private String mbtiResult;

    @TableField(value = "ai_12_dim_radar", typeHandler = JacksonTypeHandler.class)
    private String ai12DimRadar;

    @TableField(value = "gap_json", typeHandler = JacksonTypeHandler.class)
    private String gapJson;

    @TableField("target_city")
    private String targetCity;

    @TableField("target_job")
    private String targetJob;

    @TableField("daily_active_score")
    private Integer dailyActiveScore;

    @TableField("last_active_date")
    private LocalDate lastActiveDate;
}
