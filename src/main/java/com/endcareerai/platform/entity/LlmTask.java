package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM异步任务表实体（llm_tasks），记录所有发送到MQ的AI任务状态和执行日志
 */
@Data
@TableName("llm_tasks")
public class LlmTask {

    @TableId(value = "task_id", type = IdType.INPUT)
    private String taskId;

    @TableField("task_type")
    private String taskType;

    @TableField("target_id")
    private Long targetId;

    @TableField("status")
    private String status;

    @TableField("prompt_version")
    private String promptVersion;

    @TableField("error_log")
    private String errorLog;

    @TableField("manual_correction")
    private String manualCorrection;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
