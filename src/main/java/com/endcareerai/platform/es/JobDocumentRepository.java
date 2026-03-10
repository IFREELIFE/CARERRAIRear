package com.endcareerai.platform.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDocumentRepository extends ElasticsearchRepository<JobDocument, Long> {
    List<JobDocument> findByTitleContainingOrLocationContaining(String title, String location);
    List<JobDocument> findByStatus(String status);
    List<JobDocument> findByLocationAndStatus(String location, String status);
}
