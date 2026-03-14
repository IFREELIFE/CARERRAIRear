package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递申请表实体（job_applications），记录学生投递岗位的记录和企业画像授权状态
 */
@Data
@TableName("job_applications")
public class JobApplication {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("job_id")
    private Long jobId;

    @TableField("enterprise_id")
    private Long enterpriseId;

    @TableField("is_authorized")
    private Integer isAuthorized;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
