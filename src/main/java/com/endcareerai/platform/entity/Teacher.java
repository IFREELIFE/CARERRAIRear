package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 辅导教师表实体（teachers），存储学校辅导老师的姓名、地点和空闲时段
 */
@Data
@TableName("teachers")
public class Teacher {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("school_user_id")
    private Long schoolUserId;

    @TableField("name")
    private String name;

    @TableField("phone")
    private String phone;
}
