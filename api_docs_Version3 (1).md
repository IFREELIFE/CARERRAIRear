# 智能求职匹配平台 核心 API 接口文档

## 全局规范
- **Base URL:** `https://api.domain.com/v1`
- **Content-Type:** `application/json` (文件上传接口除外)
- **鉴权方式:** Header 中附带 `Authorization: Bearer <JWT_TOKEN>`

---

## 模块一：认证与基础信息 (Auth)

### 1. 多端账号注册
- **POST** `/auth/register`
- **接口说明:** 
  处理学生、学校、企业三端的注册逻辑。学生端只需邮箱和基础学籍信息；学校端需进行edu邮箱后缀校验；企业端接入天眼查API，通过传入的企业三要素（企业名称、信用代码、法人）进行真实性核验。
- **Request Body:**
  ```json
  {
    "role": "STUDENT", // STUDENT | SCHOOL | ENTERPRISE
    "email": "student@edu.cn",
    "password": "hashed_password",
    "credit_code": "91110108551385082Q", // 企业端必填
    "company_name": "某科技公司" // 企业端必填
  }
  ```

---

## 模块二：学生端 (Student)

### 1. 提交初始技术能力与 MBTI 测试
- **POST** `/student/profile/init`
- **接口说明:** 
  学生首次登录时的必经节点。学生填写的原始技术栈和 MBTI 结果提交后，后端会将其推入消息队列，由大模型异步生成核心的“12维能力雷达图”和初始人物画像。此接口要求前端必须传入 `is_guaranteed=true`（保证书确认）。
- **Request Body:**
  ```json
  {
    "tech_skills": "Java, SpringBoot, 熟悉MySQL",
    "mbti": "INTJ",
    "is_guaranteed": true
  }
  ```

### 2. 目标职业匹配与生成规划 PDF
- **POST** `/student/career/match-and-plan`
- **接口说明:** 
  学生选定目标城市和职业后，AI 会将【学生的12维画像】与【大盘该岗位的平均画像】进行对比。如果匹配度低于70%，系统会阻断并返回不推荐原因；如果学生在二次确认弹窗中选择坚持（`force_generate: true`），或者匹配度本就高于70%，系统将调用大模型生成专属的职业规划与培养方案，并转化为可下载的 PDF 链接。
- **Request Body:**
  ```json
  {
    "target_city": "东莞",
    "target_job": "前端开发",
    "force_generate": false 
  }
  ```
- **Response:**
  ```json
  {
    "match_score": 65,
    "recommend": false,
    "reason": "你的12维画像中前端基础较弱，缺少项目实际落地经验。",
    "pdf_url": null 
  }
  ```

### 3. 职位详情页 AI 智能体对话 (1/4 屏幕)
- **POST** `/student/agent/job-chat`
- **接口说明:** 
  学生在职位详情页右下角唤起的 AI 问答接口。后端会将当前页面的【岗位清洗画像】和当前用户的【12维人物画像】作为 System Prompt 的上下文注入，使得 LLM 能够精准回答“我符合这个岗位吗？”、“我还需要学什么？”等就业相关问题。
- **Request Body:**
  ```json
  {
    "job_code": "CC668565120J40736166805",
    "question": "这个岗位要求的技术栈我匹配吗？"
  }
  ```

### 4. 职位投递与企业授权
- **POST** `/student/jobs/{job_id}/apply`
- **接口说明:** 
  学生投递简历。调用此接口后，不仅会生成一条投递记录，还会将 `is_authorized` 状态置为 true。这意味着该岗位的对应企业 / HR 获得了查看该学生详细 12维画像和原始档案的权限。
- **Request Body:**
  ```json
  {
    "grant_auth_to_enterprise": true 
  }
  ```

---

## 模块三：学校端 (School)

### 1. 获取可选辅导老师与时间段
- **GET** `/school/teachers/available-slots`
- **接口说明:** 
  学校端创建了多位辅导老师后，学生可通过此接口获取所有老师的排班表、空闲时段和指导地点，用于前端展示预约日历。
- **Response:**
  ```json
  [
    {
      "teacher_id": 1,
      "name": "张老师",
      "location": "就业指导中心 301",
      "available_times": ["2026-03-12 14:00:00", "2026-03-12 15:00:00"]
    }
  ]
  ```

### 2. 老师提交辅导纪要与评价 (触发 RAG 反哺)
- **POST** `/school/appointments/{appointment_id}/evaluate`
- **接口说明:** 
  线下 1v1 辅导结束后，老师调用此接口提交面谈纪要。**核心亮点**：提交后，系统会将这段人工评语向量化并存入向量数据库（RAG）。当 AI 下次重新评估该学生画像时，会强制检索这段老师的评语，实现对学生软素质得分（如沟通能力、抗压能力）的智能微调与人机协同。
- **Request Body:**
  ```json
  {
    "tags": ["表达内向", "逻辑清晰"],
    "teacher_evaluation": "学生底层逻辑不错，但是模拟面试时非常紧张，表达不流畅，建议下调沟通得分。"
  }
  ```

---

## 模块四：企业端 (Enterprise)

### 1. 批量上传岗位 XLS 表格
- **POST** `/jobs/import/excel`
- **接口说明:** 
  支持平台管理端导入大盘数据，也支持企业自行上传招聘模板 Excel。接口采用异步处理设计，解析出表格中的长文本后，系统会按照“站点+岗位编码”进行去重，然后按行拆分成独立的任务扔进 RabbitMQ 消息队列，由后台 LLM 慢慢进行清洗提取，防止接口超时。
- **Content-Type:** `multipart/form-data`

### 2. 手动关闭/下线招聘岗位
- **PUT** `/enterprise/jobs/{job_id}/close`
- **接口说明:** 
  企业 HR 招满人员后，调用此接口将岗位状态从 `ACTIVE` 变更为 `CLOSED`。关闭后的岗位将立即从学生端的信息大厅中隐藏，同时停止接收新的投递。

### 3. 企业录入真实面试反馈 (触发自愈)
- **POST** `/enterprise/interviews/{application_id}/feedback`
- **接口说明:** 
  **整个平台最核心的自愈闭环接口**。面试结束后，HR 录入面试结果。如果是“淘汰(FAIL)”，后端会自动抓取 HR 填写的 tags 和 notes，并将其静默追加到该学生账户下的 `Gap_JSON`（匹配差距清单）字段中。这样学生就能知道具体的“死因”，同时系统对该生能力的认知也会变得更加精准。
- **Request Body:**
  ```json
  {
    "result": "FAIL",
    "tags": ["八股文背诵痕迹重", "底层原理不懂"],
    "notes": "问到MySQL索引结构时只会背B+树，无法结合真实业务场景说明。"
  }
  ```

---

## 模块五：管理端 (Admin / LLMOps)

### 1. 获取 AI 异步任务全流程监控列表
- **GET** `/admin/llmops/tasks`
- **接口说明:** 
  供管理员在后台实时查看 RabbitMQ 任务体系的执行情况。返回数据包含当前正在排队、执行中、成功和失败的任务数量，以及各提示词（Prompt）版本的成功率统计，用于质量指标大屏展示。

### 2. 错题本人工纠偏与精准重试 (节省 Token)
- **POST** `/admin/llmops/tasks/{task_id}/retry`
- **接口说明:** 
  当管理员在人工自检中发现 AI 提取字段出错（如置信度低，或把“了解”当成了“精通”）时调用此接口。管理员附带纠偏备注，系统要求大模型**仅重新抽取错误的字段**（通过 `partial_retry_fields` 指定），其余正确的字段直接沿用上次的结果，从而大幅节省大模型的 API Token 算力成本，并实现 AI 质量的持续进化。
- **Request Body:**
  ```json
  {
    "correction_prompt": "注意：不要把熟悉当成精通。仅重新抽取【技能】字段，薪资和学历保持不变。",
    "partial_retry_fields": ["ai_extracted_skills"]
  }
  ```