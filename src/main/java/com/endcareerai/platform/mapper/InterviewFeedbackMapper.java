package com.endcareerai.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endcareerai.platform.entity.InterviewFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试反馈表 Mapper 接口
 */
@Mapper
public interface InterviewFeedbackMapper extends BaseMapper<InterviewFeedback> {
}
