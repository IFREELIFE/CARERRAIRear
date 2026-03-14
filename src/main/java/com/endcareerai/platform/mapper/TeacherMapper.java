package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 辅导教师表 Mapper 接口
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
