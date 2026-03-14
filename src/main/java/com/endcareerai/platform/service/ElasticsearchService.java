package com.endcareerai.platform.service;

import com.endcareerai.platform.entity.Job;
import com.endcareerai.platform.es.JobDocument;
import com.endcareerai.platform.es.JobDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 搜索服务
 * 负责岗位文档的索引同步、删除和全文检索，
 * 支持按关键词搜索（标题/地点）、按状态过滤和按地点筛选
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final JobDocumentRepository jobDocumentRepository;

    /**
     * 将岗位实体同步到 Elasticsearch 索引
     * 用于岗位创建、更新后的搜索数据同步
     *
     * @param job 岗位实体对象
     */
    public void syncJobToEs(Job job) {
        JobDocument doc = new JobDocument();
        doc.setId(job.getId());
        doc.setEnterpriseId(job.getEnterpriseId());
        doc.setJobCode(job.getJobCode());
        doc.setTitle(job.getTitle());
        doc.setLocation(job.getLocation());
        doc.setSalaryRange(job.getSalaryRange());
        doc.setRawDescription(job.getRawDescription());
        doc.setStatus(job.getStatus());
        doc.setAiExtractedProfile(job.getAiExtractedProfile());
        jobDocumentRepository.save(doc);
        log.info("Job synced to ES: id={}, title={}", job.getId(), job.getTitle());
    }

    /**
     * 从 Elasticsearch 索引中删除指定岗位
     * 用于岗位关闭/下架时移除搜索数据
     *
     * @param jobId 岗位ID
     */
    public void removeJobFromEs(Long jobId) {
        jobDocumentRepository.deleteById(jobId);
        log.info("Job removed from ES: id={}", jobId);
    }

    /**
     * 按关键词搜索岗位（匹配标题或工作地点）
     * 基于 Elasticsearch 全文检索，支持中文分词（ik_max_word / ik_smart）
     *
     * @param keyword 搜索关键词
     * @return 匹配的岗位文档列表
     */
    public List<JobDocument> searchJobs(String keyword) {
        return jobDocumentRepository.findByTitleContainingOrLocationContaining(keyword, keyword);
    }

    /**
     * 按地点搜索状态为 ACTIVE 的岗位
     *
     * @param location 工作地点
     * @return 匹配的 ACTIVE 状态岗位文档列表
     */
    public List<JobDocument> searchActiveJobsByLocation(String location) {
        return jobDocumentRepository.findByLocationAndStatus(location, "ACTIVE");
    }

    /**
     * 获取所有状态为 ACTIVE 的上架岗位
     *
     * @return ACTIVE 状态的岗位文档列表
     */
    public List<JobDocument> getActiveJobs() {
        return jobDocumentRepository.findByStatus("ACTIVE");
    }
}
