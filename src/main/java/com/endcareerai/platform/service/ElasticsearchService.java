package com.endcareerai.platform.service;

import com.endcareerai.platform.entity.Job;
import com.endcareerai.platform.es.JobDocument;
import com.endcareerai.platform.es.JobDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final JobDocumentRepository jobDocumentRepository;

    /**
     * Sync a job entity to Elasticsearch
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
     * Remove a job from Elasticsearch
     */
    public void removeJobFromEs(Long jobId) {
        jobDocumentRepository.deleteById(jobId);
        log.info("Job removed from ES: id={}", jobId);
    }

    /**
     * Search jobs by keyword (matches title or location)
     */
    public List<JobDocument> searchJobs(String keyword) {
        return jobDocumentRepository.findByTitleContainingOrLocationContaining(keyword, keyword);
    }

    /**
     * Search active jobs by location
     */
    public List<JobDocument> searchActiveJobsByLocation(String location) {
        return jobDocumentRepository.findByLocationAndStatus(location, "ACTIVE");
    }

    /**
     * Get all active jobs
     */
    public List<JobDocument> getActiveJobs() {
        return jobDocumentRepository.findByStatus("ACTIVE");
    }
}
