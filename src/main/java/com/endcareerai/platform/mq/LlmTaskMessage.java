package com.endcareerai.platform.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * LLM 异步任务 MQ 消息体
 * 在 RabbitMQ 中传递，包含任务元信息和可选的重试修正参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmTaskMessage implements Serializable {
    /** 任务唯一标识 */
    private String taskId;
    /** 任务类型：EXTRACT_JOB_XLS / GEN_STUDENT_PROFILE / RAG_RECALCULATE */
    private String taskType;
    /** 业务关联ID（job_id 或 student_id） */
    private Long targetId;
    /** Prompt 版本号 */
    private String promptVersion;
    /** 人工修正提示（重试时使用） */
    private String correctionPrompt;
    /** 部分重试字段列表（重试时使用，null 表示全部重试） */
    private List<String> partialRetryFields;
}
