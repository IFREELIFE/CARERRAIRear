package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投递申请表 Mapper 接口
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {
}
