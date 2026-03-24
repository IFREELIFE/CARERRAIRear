package com.endcareerai.platform.service;

/**
 * 画像图谱服务：将 AI 生成的岗位画像与人物画像写入 Neo4j，便于后续检索与联动
 */
public interface GraphProfileService {

    /**
     * 将岗位画像写入/更新到 Neo4j 图谱
     *
     * @param jobId       数据库岗位ID
     * @param jobCode     平台岗位编码
     * @param title       岗位标题
     * @param location    工作地点
     * @param profileJson AI 提取的结构化岗位画像
     */
    void upsertJobProfile(Long jobId, String jobCode, String title, String location, String profileJson);

    /**
     * 将学生画像写入/更新到 Neo4j 图谱
     *
     * @param studentId   学生ID
     * @param realName    学生姓名
     * @param mbti        MBTI 类型
     * @param radarJson   AI 生成的12维画像
     */
    void upsertStudentProfile(Long studentId, String realName, String mbti, String radarJson);

    /**
     * 记录学生与岗位的兴趣/对话关系，方便下次追踪画像联动
     *
     * @param studentId 学生ID
     * @param jobId     岗位ID
     */
    void linkStudentToJob(Long studentId, Long jobId);

    /**
     * 从图谱读取岗位画像（若存在）
     *
     * @param jobCode 平台岗位编码
     * @return Neo4j 中存储的岗位画像 JSON
     */
    String fetchJobProfileByCode(String jobCode);

    /**
     * 从图谱读取学生画像（若存在）
     *
     * @param studentId 学生ID
     * @return Neo4j 中存储的学生画像 JSON
     */
    String fetchStudentProfile(Long studentId);
}
