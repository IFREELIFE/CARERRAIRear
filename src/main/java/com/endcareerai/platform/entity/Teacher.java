package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
