package com.endcareerai.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("enterprises")
public class Enterprise {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @TableField("company_name")
    private String companyName;

    @TableField("credit_code")
    private String creditCode;

    @TableField("industry")
    private String industry;

    @TableField("company_size")
    private String companySize;

    @TableField("company_type")
    private String companyType;

    @TableField("company_description")
    private String companyDescription;
}
