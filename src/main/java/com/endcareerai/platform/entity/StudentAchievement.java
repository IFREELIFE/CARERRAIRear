package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生成就表实体（student_achievements），记录学生获得的证书、奖项、项目经历等成就信息
 */
@Data
@TableName("student_achievements")
public class StudentAchievement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("achievement_name")
    private String achievementName;

    @TableField("unlocked_at")
    private LocalDateTime unlockedAt;
}
