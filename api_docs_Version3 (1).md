# 智能求职匹配平台 API 接口文档

## 全局规范
- **Base Path:** `/`（无版本前缀，如有网关可自行追加）
- **Content-Type:** `application/json`（文件上传接口除外）
- **鉴权方式:** 除 `/auth/register`、`/auth/login`、`/auth/refresh` 公开，其余接口需在 Header 中附带 `Authorization: Bearer <JWT_TOKEN>`
- **角色与路径（基于 SecurityConfig）:**
  - 学生：访问 `/student/**`
  - 学校：访问 `/school/**`（学生也可访问）
  - 企业：访问 `/jobs/**`、`/enterprise/**`、`/jobs/import/**`
  - 管理员：访问 `/admin/**`
- **统一响应结构（Result<T>):**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```
  - `code=200` 表示成功；业务或校验错误返回相应错误码与 `message` 描述，`data` 可能为 `null`。
  - 校验异常示例：`{"code":400,"message":"field: must not be blank","data":null}`

---

## 模块一：认证与基础信息 (Auth) `/auth`

### 1. 多端账号注册
- **POST** `/auth/register` （公开）
- **Body:**
  ```json
  {
    "role": "STUDENT | SCHOOL | ENTERPRISE",
    "email": "student@edu.cn",
    "password": "plain_or_hashed",
    "creditCode": "91110108551385082Q",   // 企业端可选
    "companyName": "某科技公司"            // 企业端可选
  }
  ```
- **Response:** `LoginResponse`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "accessToken": "jwt-access",
      "refreshToken": "jwt-refresh",
      "expiresIn": 3600,
      "token": "jwt-access",
      "role": "STUDENT",
      "userId": 1
    }
  }
  ```
  - 说明：`token` 字段与 `accessToken` 等价，保留用于兼容旧版前端。

### 2. 登录
- **POST** `/auth/login` （公开）
- **Body:**
  ```json
  { "username": "student@edu.cn", "password": "******" }
  ```
- **Response:** `LoginResponse`（同上）

### 3. 退出登录
- **POST** `/auth/logout` （需登录）
- **Body:**
  ```json
  { "refreshToken": "jwt-refresh-token" }
  ```
- **Response:** 成功返回 `data=null`

### 4. 刷新 Token
- **POST** `/auth/refresh` （公开）
- **Body:**
  ```json
  { "refreshToken": "jwt-refresh-token" }
  ```
- **Response:** `LoginResponse`（同上）

### 5. 修改密码
- **POST** `/auth/change-password` （需登录）
- **Body:**
  ```json
  {
    "oldPassword": "old-pass",
    "newPassword": "new-pass"
  }
  ```
- **Response:** 成功返回 `data=null`

---

## 模块二：学生端 (Student) `/student`（角色：STUDENT）

### 1. 提交初始技术能力与 MBTI
- **POST** `/student/profile/init`
- **Body:** `guaranteed` 字段必须为 `true`
  ```json
  {
    "techSkills": "Java, SpringBoot, MySQL",
    "mbti": "INTJ",
    "guaranteed": true
  }
  ```
- **Response:** 成功返回 `data=null`；若未勾选保证书返回 400 业务错误。

### 2. 目标职业匹配与生成规划 PDF
- **POST** `/student/career/match-and-plan`
- **Body:**
  ```json
  {
    "targetCity": "深圳",
    "targetJob": "前端开发",
    "forceGenerate": false
  }
  ```
- **Response:** `CareerMatchResponse`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "matchScore": 75,
      "recommend": true,
      "reason": "AI分析结果...",
      "pdfUrl": "/api/reports/career-plan/1_1716960000000.pdf"
    }
  }
  ```

### 3. 职位详情页 AI 智能体对话
- **POST** `/student/agent/job-chat`
- **Body:**
  ```json
  {
    "jobCode": "CC668565120J40736166805",
    "question": "这个岗位要求的技术栈我匹配吗？"
  }
  ```
- **Response:** `JobChatResponse`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "answer": "基于您的12维画像...",
      "jobCode": "CC668565120J40736166805"
    }
  }
  ```

### 4. 职位投递与企业授权
- **POST** `/student/jobs/{jobId}/apply`
- **Body:**（默认授权企业查看画像）
  ```json
  { "grantAuthToEnterprise": true }
  ```
- **Response:** `JobApplication`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 10,
      "studentId": 1,
      "jobId": 22,
      "enterpriseId": 5,
      "isAuthorized": 1,
      "status": "APPLIED",
      "createdAt": "2026-03-28T08:30:00"
    }
  }
  ```

### 5. 关键词搜索岗位
- **GET** `/student/jobs/search?keyword=后端`
- **Response:** `JobDocument[]`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 22,
        "enterpriseId": 5,
        "jobCode": "J20240328001",
        "title": "后端开发工程师",
        "location": "深圳",
        "salaryRange": "15k-25k",
        "rawDescription": "...",
        "status": "ACTIVE",
        "aiExtractedProfile": "{...}"
      }
    ]
  }
  ```

### 6. 获取所有上架岗位
- **GET** `/student/jobs/active`
- **Response:** `JobDocument[]`（同上，仅返回 `status=ACTIVE` 的岗位）

---

## 模块三：学校端 (School) `/school`（角色：SCHOOL 或 STUDENT）

### 1. 获取可选辅导老师与时间段
- **GET** `/school/teachers/available-slots`
- **Response:** `TeacherSlotResponse[]`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "teacherId": 1,
        "name": "张老师",
        "location": "就业指导中心 301",
        "availableTimes": ["2026-03-12 14:00:00", "2026-03-12 15:00:00"]
      }
    ]
  }
  ```

### 2. 老师提交辅导纪要与评价
- **POST** `/school/appointments/{appointmentId}/evaluate`
- **Body:**
  ```json
  {
    "tags": ["表达内向", "逻辑清晰"],
    "teacherEvaluation": "学生底层逻辑不错，但表达紧张..."
  }
  ```
- **Response:** 成功返回 `data=null`

---

## 模块四：企业端 (Enterprise)（角色：ENTERPRISE）

### 1. 批量上传岗位 Excel
- **POST** `/jobs/import/excel`
- **Content-Type:** `multipart/form-data`，表单字段名 `file`
- **Response:** 成功返回 `data=null`

### 2. 手动关闭/下线招聘岗位
- **PUT** `/enterprise/jobs/{jobId}/close`
- **Response:** 成功返回 `data=null`

### 3. 企业录入真实面试反馈
- **POST** `/enterprise/interviews/{applicationId}/feedback`
- **Body:**
  ```json
  {
    "result": "FAIL",        // PASS | FAIL
    "tags": ["八股文背诵痕迹重", "底层原理不懂"],
    "notes": "问到MySQL索引结构时无法结合业务说明"
  }
  ```
- **Response:** 成功返回 `data=null`

---

## 模块五：管理端 (Admin / LLMOps) `/admin`（角色：ADMIN）

### 1. 获取 AI 异步任务全流程监控列表
- **GET** `/admin/llmops/tasks`
- **Response:** `LlmTaskStatsResponse`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "queued": 3,
      "processing": 2,
      "success": 18,
      "failed": 1,
      "tasks": [
        {
          "taskId": "task-001",
          "taskType": "GEN_STUDENT_PROFILE",
          "targetId": 1,
          "status": "PROCESSING",
          "promptVersion": "v1",
          "errorLog": null,
          "manualCorrection": null,
          "createdAt": "2026-03-28T08:00:00",
          "updatedAt": "2026-03-28T08:05:00"
        }
      ]
    }
  }
  ```

### 2. 错题本人工纠偏与精准重试
- **POST** `/admin/llmops/tasks/{taskId}/retry`
- **Body:**
  ```json
  {
    "correctionPrompt": "仅重新抽取【技能】字段，薪资和学历保持不变。",
    "partialRetryFields": ["ai_extracted_skills"]
  }
  ```
- **Response:** 成功返回 `data=null`
