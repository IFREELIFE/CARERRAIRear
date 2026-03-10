package com.endcareerai.platform.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmTaskMessage implements Serializable {
    private String taskId;
    private String taskType;   // EXTRACT_JOB_XLS, GEN_STUDENT_PROFILE, RAG_RECALCULATE
    private Long targetId;     // business key (job_id, student_id, etc.)
    private String promptVersion;
    private String correctionPrompt;           // for retry
    private List<String> partialRetryFields;   // for partial retry
}
