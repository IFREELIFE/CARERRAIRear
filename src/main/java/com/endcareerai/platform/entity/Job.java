package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位表实体（jobs），存储企业发布的岗位信息、AI提取的结构化画像和置信度分数
 */
@Data
@TableName(value = "jobs", autoResultMap = true)
public class Job {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("enterprise_id")
    private Long enterpriseId;

    @TableField("job_code")
    private String jobCode;

    @TableField("title")
    private String title;

    @TableField("location")
    private String location;

    @TableField("salary_range")
    private String salaryRange;

    @TableField("raw_description")
    private String rawDescription;

    @TableField("source_url")
    private String sourceUrl;

    @TableField(value = "ai_extracted_profile", typeHandler = JacksonTypeHandler.class)
    private String aiExtractedProfile;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("status")
    private String status;

    @TableField("source_update_date")
    private String sourceUpdateDate;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
