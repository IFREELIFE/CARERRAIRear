package com.endcareerai.platform.common;

public class Constants {
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_SCHOOL = "SCHOOL";
    public static final String ROLE_ENTERPRISE = "ENTERPRISE";
    public static final String ROLE_ADMIN = "ADMIN";

    // RabbitMQ
    public static final String MQ_EXCHANGE_LLM = "llm.exchange";
    public static final String MQ_QUEUE_LLM_TASK = "llm.task.queue";
    public static final String MQ_ROUTING_KEY_LLM = "llm.task";

    // Redis key prefixes
    public static final String REDIS_USER_PREFIX = "user:";
    public static final String REDIS_JOB_PREFIX = "job:";
    public static final String REDIS_TEACHER_SLOTS_PREFIX = "teacher:slots:";

    // Elasticsearch index
    public static final String ES_INDEX_JOBS = "jobs";

    // Match threshold
    public static final int MATCH_THRESHOLD = 70;

    private Constants() {}
}
