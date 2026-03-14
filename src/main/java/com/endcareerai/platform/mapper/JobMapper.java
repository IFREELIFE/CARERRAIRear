package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位表 Mapper 接口
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
