package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.CareerMatchRequest;
import com.endcareerai.platform.dto.request.JobApplyRequest;
import com.endcareerai.platform.dto.request.JobChatRequest;
import com.endcareerai.platform.dto.request.ProfileInitRequest;
import com.endcareerai.platform.dto.response.CareerMatchResponse;
import com.endcareerai.platform.dto.response.JobChatResponse;
import com.endcareerai.platform.entity.JobApplication;
import com.endcareerai.platform.es.JobDocument;
import com.endcareerai.platform.service.ElasticsearchService;
import com.endcareerai.platform.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final ElasticsearchService elasticsearchService;

    @PostMapping("/profile/init")
    public Result<Void> initProfile(@RequestBody @Valid ProfileInitRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        studentService.initProfile(userId, request);
        return Result.success();
    }

    @PostMapping("/career/match-and-plan")
    public Result<CareerMatchResponse> matchAndPlan(@RequestBody @Valid CareerMatchRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CareerMatchResponse response = studentService.matchAndPlan(userId, request);
        return Result.success(response);
    }

    @PostMapping("/agent/job-chat")
    public Result<JobChatResponse> jobChat(@RequestBody @Valid JobChatRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobChatResponse response = studentService.jobChat(userId, request);
        return Result.success(response);
    }

    @PostMapping("/jobs/{jobId}/apply")
    public Result<JobApplication> applyJob(@PathVariable Long jobId,
                                           @RequestBody @Valid JobApplyRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobApplication application = studentService.applyJob(userId, jobId, request);
        return Result.success(application);
    }

    @GetMapping("/jobs/search")
    public Result<List<JobDocument>> searchJobs(@RequestParam String keyword) {
        List<JobDocument> results = elasticsearchService.searchJobs(keyword);
        return Result.success(results);
    }

    @GetMapping("/jobs/active")
    public Result<List<JobDocument>> getActiveJobs() {
        List<JobDocument> results = elasticsearchService.getActiveJobs();
        return Result.success(results);
    }
}
