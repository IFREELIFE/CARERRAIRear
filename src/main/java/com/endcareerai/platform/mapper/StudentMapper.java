package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生档案表 Mapper 接口
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
