package com.endcareerai.platform.service;

/**
 * LLM 服务接口
 * 封装对大语言模型 API 的调用，支持多种任务类型
 */
public interface LlmService {

    /**
     * 从岗位原始描述中提取结构化信息（技能要求、学历要求、经验要求等）
     *
     * @param rawDescription  岗位原始描述
     * @param correctionPrompt 可选的人工修正提示（重试时使用）
     * @return JSON 格式的结构化岗位画像
     */
    String extractJobProfile(String rawDescription, String correctionPrompt);

    /**
     * 根据学生技能和 MBTI 生成12维能力画像
     *
     * @param techSkills       技术技能原始文本
     * @param mbtiResult       MBTI 测试结果
     * @param correctionPrompt 可选的人工修正提示（重试时使用）
     * @return JSON 格式的12维雷达图数据
     */
    String generateStudentProfile(String techSkills, String mbtiResult, String correctionPrompt);

    /**
     * 结合教师评价反馈重新计算学生画像（RAG 流程）
     *
     * @param currentRadar     当前12维画像 JSON
     * @param teacherFeedback  教师评价内容
     * @param correctionPrompt 可选的人工修正提示（重试时使用）
     * @return 更新后的12维雷达图 JSON
     */
    String ragRecalculateProfile(String currentRadar, String teacherFeedback, String correctionPrompt);

    /**
     * 岗位智能问答（AI Agent）
     *
     * @param jobTitle         岗位名称
     * @param jobProfile       岗位 AI 提取画像
     * @param studentRadar     学生12维画像
     * @param question         用户提问
     * @return AI 回答内容
     */
    String jobChat(String jobTitle, String jobProfile, String studentRadar, String question);

    /**
     * 职业匹配度分析
     *
     * @param studentRadar 学生12维画像 JSON
     * @param targetCity   目标城市
     * @param targetJob    目标岗位
     * @return JSON 格式的匹配分析结果，包含 matchScore 和 reason
     */
    String careerMatchAnalysis(String studentRadar, String targetCity, String targetJob);
}
