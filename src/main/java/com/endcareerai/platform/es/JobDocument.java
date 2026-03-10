package com.endcareerai.platform.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "jobs")
public class JobDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long enterpriseId;

    @Field(type = FieldType.Keyword)
    private String jobCode;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String location;

    @Field(type = FieldType.Keyword)
    private String salaryRange;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String rawDescription;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String aiExtractedProfile;
}
