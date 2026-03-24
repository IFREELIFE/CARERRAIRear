package com.endcareerai.platform.service.impl;

import com.endcareerai.platform.service.GraphProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基于 Neo4j 的画像图谱实现：将 AI 画像写入图数据库，用于后续检索与联动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphProfileServiceImpl implements GraphProfileService {

    private static final String REL_INTEREST_IN = "INTEREST_IN";
    private final Neo4jClient neo4jClient;

    @Override
    public void upsertJobProfile(Long jobId, String jobCode, String title, String location, String profileJson) {
        if (jobId == null || jobCode == null || profileJson == null) {
            return;
        }
        runSafely("upsert job profile", () -> {
            Map<String, Object> params = new HashMap<>();
            params.put("jobId", jobId);
            params.put("jobCode", jobCode);
            params.put("title", title);
            params.put("location", location);
            params.put("profile", profileJson);
            neo4jClient.query(
                            "MERGE (j:Job {jobId:$jobId}) " +
                                    "SET j.jobCode=$jobCode, j.title=$title, j.location=$location, " +
                                    "j.profile=$profile, j.updatedAt=datetime()")
                    .bindAll(params)
                    .run();
            return null;
        });
    }

    @Override
    public void upsertStudentProfile(Long studentId, String realName, String mbti, String radarJson) {
        if (studentId == null || radarJson == null) {
            return;
        }
        runSafely("upsert student profile", () -> {
            Map<String, Object> params = new HashMap<>();
            params.put("studentId", studentId);
            params.put("realName", realName);
            params.put("mbti", mbti);
            params.put("radar", radarJson);
            // realName 可能为空， 保留已有姓名； mbti 与 radar 使用最新 AI 结果覆盖
            neo4jClient.query(
                            "MERGE (s:Student {studentId:$studentId}) " +
                                    "SET s.realName=coalesce($realName, s.realName), " +
                                    "s.mbti=$mbti, s.radar=$radar, s.updatedAt=datetime()")
                    .bindAll(params)
                    .run();
            return null;
        });
    }

    @Override
    public void linkStudentToJob(Long studentId, Long jobId) {
        if (studentId == null || jobId == null) {
            return;
        }
        runSafely("link student to job", () -> {
            Map<String, Object> params = new HashMap<>();
            params.put("studentId", studentId);
            params.put("jobId", jobId);
            // Cypher 不支持参数化关系类型，这里使用常量拼接以保持语义一致
            neo4jClient.query(
                            "MERGE (s:Student {studentId:$studentId}) " +
                                    "MERGE (j:Job {jobId:$jobId}) " +
                                    "MERGE (s)-[r:" + REL_INTEREST_IN + "]->(j) " +
                                    "SET r.updatedAt=datetime()")
                    .bindAll(params)
                    .run();
            return null;
        });
    }

    @Override
    public String fetchJobProfileByCode(String jobCode) {
        if (jobCode == null) {
            return null;
        }
        return runSafely("fetch job profile", () ->
                neo4jClient.query("MATCH (j:Job {jobCode:$jobCode}) RETURN j.profile as profile LIMIT 1")
                        .bind(jobCode).to("jobCode")
                        .fetchAs(String.class)
                        .first()
                        .orElse(null));
    }

    @Override
    public String fetchStudentProfile(Long studentId) {
        if (studentId == null) {
            return null;
        }
        return runSafely("fetch student profile", () ->
                neo4jClient.query("MATCH (s:Student {studentId:$studentId}) RETURN s.radar as radar LIMIT 1")
                        .bind(studentId).to("studentId")
                        .fetchAs(String.class)
                        .first()
                        .orElse(null));
    }

    private <T> T runSafely(String action, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DataAccessResourceFailureException e) {
            log.warn("Neo4j {} skipped (connection not ready): {}", action, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Neo4j {} failed: {}", action, e.getMessage());
            return null;
        }
    }
}
