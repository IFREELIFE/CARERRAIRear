package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口，继承 MyBatis-Plus BaseMapper 提供基础 CRUD 操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
