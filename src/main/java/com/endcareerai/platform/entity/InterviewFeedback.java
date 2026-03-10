package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "interview_feedbacks", autoResultMap = true)
public class InterviewFeedback {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("application_id")
    private Long applicationId;

    @TableField("result")
    private String result;

    @TableField(value = "feedback_tags", typeHandler = JacksonTypeHandler.class)
    private String feedbackTags;

    @TableField("feedback_notes")
    private String feedbackNotes;

    @TableField("is_synced_to_ai")
    private Integer isSyncedToAi;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
