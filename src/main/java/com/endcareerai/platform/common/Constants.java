package com.endcareerai.platform.common;

/**
 * 全局常量定义类
 * 包含角色标识、RabbitMQ 配置、Redis 键前缀、ES 索引名和匹配阈值等
 */
public class Constants {
    /** 角色：学生 */
    public static final String ROLE_STUDENT = "STUDENT";
    /** 角色：学校 */
    public static final String ROLE_SCHOOL = "SCHOOL";
    /** 角色：企业 */
    public static final String ROLE_ENTERPRISE = "ENTERPRISE";
    /** 角色：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";

    // RabbitMQ
    /** LLM 任务交换机名称 */
    public static final String MQ_EXCHANGE_LLM = "llm.exchange";
    /** LLM 任务队列名称 */
    public static final String MQ_QUEUE_LLM_TASK = "llm.task.queue";
    /** LLM 任务路由键 */
    public static final String MQ_ROUTING_KEY_LLM = "llm.task";

    // Redis key prefixes
    /** Redis 用户缓存前缀 */
    public static final String REDIS_USER_PREFIX = "user:";
    /** Redis 岗位缓存前缀 */
    public static final String REDIS_JOB_PREFIX = "job:";
    /** Redis 教师时间段缓存前缀 */
    public static final String REDIS_TEACHER_SLOTS_PREFIX = "teacher:slots:";

    // Elasticsearch index
    /** Elasticsearch 岗位索引名称 */
    public static final String ES_INDEX_JOBS = "jobs";

    // Match threshold
    /** 职业匹配度拦截阈值（低于此值需确认后才能生成方案） */
    public static final int MATCH_THRESHOLD = 70;

    private Constants() {}
}
