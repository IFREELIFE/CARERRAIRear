package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.CareerMatchRequest;
import com.endcareerai.platform.dto.request.JobApplyRequest;
import com.endcareerai.platform.dto.request.JobChatRequest;
import com.endcareerai.platform.dto.request.ProfileInitRequest;
import com.endcareerai.platform.dto.response.CareerMatchResponse;
import com.endcareerai.platform.dto.response.JobChatResponse;
import com.endcareerai.platform.entity.JobApplication;

public interface StudentService {
    void initProfile(Long userId, ProfileInitRequest request);
    CareerMatchResponse matchAndPlan(Long userId, CareerMatchRequest request);
    JobChatResponse jobChat(Long userId, JobChatRequest request);
    JobApplication applyJob(Long userId, Long jobId, JobApplyRequest request);
}
