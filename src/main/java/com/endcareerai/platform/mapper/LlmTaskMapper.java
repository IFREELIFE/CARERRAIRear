package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.LlmTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM异步任务表 Mapper 接口
 */
@Mapper
public interface LlmTaskMapper extends BaseMapper<LlmTask> {
}
