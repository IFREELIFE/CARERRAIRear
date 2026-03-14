package com.endcareerai.platform.service.impl;

import com.endcareerai.platform.service.LlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * LLM 服务实现类
 * 通过 OpenAI 兼容接口调用大语言模型（支持 OpenAI / DeepSeek / 通义千问等）
 */
@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    private final RestTemplate llmRestTemplate;
    private final ObjectMapper objectMapper;

    // TODO: 请修改为你的 LLM API 基础地址（支持 OpenAI / DeepSeek / 通义千问等兼容接口）
    @Value("${llm.base-url:https://api.openai.com}")
    private String baseUrl;

    // TODO: 请修改为你要使用的 LLM 模型名称
    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.temperature:0.3}")
    private double temperature;

    @Value("${llm.max-tokens:2000}")
    private int maxTokens;

    /**
     * 构造方法，注入 LLM 专用的 RestTemplate 和 JSON 序列化工具
     *
     * @param llmRestTemplate LLM 专用的 RestTemplate（由 LlmConfig 创建）
     * @param objectMapper    JSON 序列化/反序列化工具
     */
    public LlmServiceImpl(@Qualifier("llmRestTemplate") RestTemplate llmRestTemplate,
                           ObjectMapper objectMapper) {
        this.llmRestTemplate = llmRestTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 从岗位原始描述中提取结构化画像（技能要求、学历、经验等）
     * 通过 System Prompt 指引 LLM 输出标准 JSON 格式
     *
     * @param rawDescription   岗位原始描述
     * @param correctionPrompt 可选的人工修正提示（重试时使用）
     * @return JSON 格式的结构化岗位画像
     */
    @Override
    public String extractJobProfile(String rawDescription, String correctionPrompt) {
        String systemPrompt = "你是一个专业的岗位信息提取助手。请从岗位描述中提取结构化信息，以JSON格式返回。"
                + "提取字段包括：required_skills(技能要求数组), education(学历要求), "
                + "experience_years(经验年限), responsibilities(职责描述数组), "
                + "preferred_skills(加分技能数组), work_type(工作类型:全职/兼职/实习)。"
                + "只返回JSON，不要其他内容。";

        String userPrompt = "请提取以下岗位描述的结构化信息：\n\n" + rawDescription;

        if (correctionPrompt != null && !correctionPrompt.isBlank()) {
            userPrompt += "\n\n人工修正建议：" + correctionPrompt;
        }

        return callLlmApi(systemPrompt, userPrompt);
    }

    /**
     * 根据学生技能和 MBTI 生成12维能力雷达图数据
     * 12维度包括：编程、算法、系统设计、沟通、团队协作、领导力、学习力、问题解决、创新、抗压、时间管理、职业素养
     *
     * @param techSkills       技术技能原始文本
     * @param mbtiResult       MBTI 测试结果
     * @param correctionPrompt 可选的人工修正提示
     * @return JSON 格式的12维雷达图数据
     */
    @Override
    public String generateStudentProfile(String techSkills, String mbtiResult, String correctionPrompt) {
        String systemPrompt = "你是一个专业的职业能力评估师。请根据学生的技术技能和MBTI性格类型，生成12维能力雷达图数据。"
                + "12个维度为：coding_ability(编程能力), algorithm(算法能力), system_design(系统设计), "
                + "communication(沟通能力), teamwork(团队协作), leadership(领导力), "
                + "learning_ability(学习能力), problem_solving(问题解决), creativity(创新能力), "
                + "stress_tolerance(抗压能力), time_management(时间管理), professional_ethics(职业素养)。"
                + "每个维度评分0-100，以JSON格式返回，如：{\"coding_ability\":85,\"algorithm\":70,...}。"
                + "只返回JSON，不要其他内容。";

        String userPrompt = "学生信息：\n技术技能：" + techSkills + "\nMBTI类型：" + mbtiResult;

        if (correctionPrompt != null && !correctionPrompt.isBlank()) {
            userPrompt += "\n\n人工修正建议：" + correctionPrompt;
        }

        return callLlmApi(systemPrompt, userPrompt);
    }

    /**
     * 结合教师评价反馈重新计算学生画像（RAG 反哺流程）
     * 教师反馈权重合理融入，不会大幅偏离原始评分
     *
     * @param currentRadar     当前12维画像 JSON
     * @param teacherFeedback  教师评价内容
     * @param correctionPrompt 可选的人工修正提示
     * @return 更新后的12维雷达图 JSON
     */
    @Override
    public String ragRecalculateProfile(String currentRadar, String teacherFeedback, String correctionPrompt) {
        String systemPrompt = "你是一个专业的职业能力评估师。请根据教师的评价反馈，结合学生当前的12维能力画像，"
                + "重新评估并更新画像数据。12个维度为：coding_ability, algorithm, system_design, "
                + "communication, teamwork, leadership, learning_ability, problem_solving, "
                + "creativity, stress_tolerance, time_management, professional_ethics。"
                + "每个维度评分0-100。重新评估时需合理考虑教师反馈的权重，不要大幅偏离原始分数。"
                + "以JSON格式返回更新后的12维数据，只返回JSON，不要其他内容。";

        String userPrompt = "当前画像数据：\n" + currentRadar
                + "\n\n教师评价反馈：\n" + teacherFeedback;

        if (correctionPrompt != null && !correctionPrompt.isBlank()) {
            userPrompt += "\n\n人工修正建议：" + correctionPrompt;
        }

        return callLlmApi(systemPrompt, userPrompt);
    }

    /**
     * 岗位详情页 AI Agent 智能问答
     * 将岗位画像和学生画像作为上下文注入 LLM，针对性回答学生提问
     *
     * @param jobTitle     岗位名称
     * @param jobProfile   岗位 AI 提取画像
     * @param studentRadar 学生12维画像
     * @param question     用户提问
     * @return AI 回答内容
     */
    @Override
    public String jobChat(String jobTitle, String jobProfile, String studentRadar, String question) {
        String systemPrompt = "你是EndCareerAi智能求职助手，帮助学生了解岗位信息并提供求职建议。"
                + "请根据岗位信息和学生画像，针对学生的问题给出专业、具体、有建设性的回答。"
                + "回答要简洁明了，控制在300字以内。";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("岗位名称：").append(jobTitle).append("\n");
        if (jobProfile != null && !jobProfile.isBlank()) {
            userPrompt.append("岗位画像：").append(jobProfile).append("\n");
        }
        if (studentRadar != null && !studentRadar.isBlank()) {
            userPrompt.append("学生能力画像：").append(studentRadar).append("\n");
        }
        userPrompt.append("\n学生提问：").append(question);

        return callLlmApi(systemPrompt, userPrompt.toString());
    }

    /**
     * 职业匹配度分析
     * 对比学生12维画像与目标岗位要求，返回匹配分数和详细分析
     *
     * @param studentRadar 学生12维画像 JSON
     * @param targetCity   目标城市
     * @param targetJob    目标岗位
     * @return JSON 格式的匹配分析结果
     */
    @Override
    public String careerMatchAnalysis(String studentRadar, String targetCity, String targetJob) {
        String systemPrompt = "你是一个专业的职业匹配分析师。请根据学生的12维能力画像和目标岗位，"
                + "分析匹配度并给出建议。以JSON格式返回：{\"matchScore\":75,\"reason\":\"匹配度分析...\","
                + "\"strengths\":[\"优势1\",\"优势2\"],\"gaps\":[\"不足1\",\"不足2\"],"
                + "\"suggestions\":[\"建议1\",\"建议2\"]}。"
                + "matchScore为0-100的整数。只返回JSON，不要其他内容。";

        String userPrompt = "学生能力画像：\n" + studentRadar
                + "\n\n目标城市：" + targetCity
                + "\n目标岗位：" + targetJob;

        return callLlmApi(systemPrompt, userPrompt);
    }

    /**
     * 调用 LLM API（OpenAI Chat Completions 兼容接口）
     * 构造请求体包含 model、temperature、max_tokens 和 messages，
     * 解析返回的 choices[0].message.content 作为结果
     *
     * @param systemPrompt 系统提示（指导 LLM 角色和输出格式）
     * @param userPrompt   用户提示（具体任务内容）
     * @return LLM 回答内容
     */
    private String callLlmApi(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/v1/chat/completions";

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            requestBody.set("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Calling LLM API: url={}, model={}", url, model);
            ResponseEntity<String> response = llmRestTemplate.postForEntity(url, entity, String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("LLM API returned empty response");
            }

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new RuntimeException("LLM API returned no choices");
            }

            JsonNode firstChoice = choices.get(0);
            if (firstChoice == null) {
                throw new RuntimeException("LLM API choices array is empty");
            }
            JsonNode messageNode = firstChoice.path("message");
            if (messageNode.isMissingNode()) {
                throw new RuntimeException("LLM API response missing message field");
            }
            JsonNode contentNode = messageNode.path("content");
            if (contentNode.isMissingNode()) {
                throw new RuntimeException("LLM API response missing content field");
            }

            String content = contentNode.asText();
            log.debug("LLM API response received, content length={}", content.length());
            return content.trim();

        } catch (Exception e) {
            log.error("Failed to call LLM API: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("LLM API调用失败: " + e.getMessage(), e);
        }
    }
}
