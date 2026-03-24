-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ai_recruit_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ai_recruit_platform`;

-- ==========================================
-- 1. 用户与权限表
-- ==========================================
CREATE TABLE `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(100) UNIQUE COMMENT '登录邮箱(学生/学校)',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '加密密码',
  `role` ENUM('STUDENT', 'SCHOOL', 'ENTERPRISE', 'ADMIN') NOT NULL COMMENT '角色',
  `avatar_url` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '0禁用, 1正常',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统全局用户表';

-- ==========================================
-- 2. 学生端相关表
-- ==========================================
CREATE TABLE `students` (
  `user_id` BIGINT PRIMARY KEY,
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `grade` VARCHAR(20) COMMENT '年级(如: 2025届)',
  `education_level` VARCHAR(20) COMMENT '学历(本科/硕士等)',
  `school_name` VARCHAR(100) COMMENT '来自哪个学校',
  `tech_skills_raw` TEXT COMMENT '初始填写的技术与能力',
  `mbti_result` VARCHAR(10) COMMENT 'MBTI测试结果',
  `ai_12_dim_radar` JSON COMMENT 'AI生成的12维人物画像',
  `gap_json` JSON COMMENT '人岗匹配差距清单(AI与面试反馈生成)',
  `target_city` VARCHAR(50) COMMENT '目标城市',
  `target_job` VARCHAR(50) COMMENT '目标职业',
  `daily_active_score` INT DEFAULT 0 COMMENT '活跃度积分',
  `last_active_date` DATE COMMENT '最后活跃日期',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='学生信息与画像表';

CREATE TABLE `student_achievements` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_id` BIGINT NOT NULL,
  `achievement_name` VARCHAR(50) NOT NULL COMMENT '成就名称(如:勤奋-连续签到10天)',
  `unlocked_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='学生成就记录表';

-- ==========================================
-- 3. 企业与岗位表 (深度适配所提供的 XLS 格式)
-- ==========================================
CREATE TABLE `enterprises` (
  `user_id` BIGINT PRIMARY KEY,
  `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
  `credit_code` VARCHAR(50) UNIQUE COMMENT '统一社会信用代码',
  `legal_representative` VARCHAR(50) COMMENT '法人代表姓名（天眼查三要素验证）',
  `industry` VARCHAR(100) COMMENT '所属行业(如:计算机软件,互联网)',
  `company_size` VARCHAR(50) COMMENT '公司规模(如:20-99人)',
  `company_type` VARCHAR(50) COMMENT '公司类型(如:天使轮/民营)',
  `company_description` TEXT COMMENT '公司详情',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='企业信息表';

CREATE TABLE `jobs` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `enterprise_id` BIGINT COMMENT '关联企业(若是大盘导入可能暂无企业账号)',
  `job_code` VARCHAR(100) UNIQUE NOT NULL COMMENT '岗位编码(含站点前缀用于去重, 如 CC6685...)',
  `title` VARCHAR(100) NOT NULL COMMENT '岗位名称',
  `location` VARCHAR(100) COMMENT '地址(如:东莞-虎门镇)',
  `salary_range` VARCHAR(50) COMMENT '薪资范围(如:3000-4000元)',
  `raw_description` TEXT COMMENT '岗位详情(包含原始html或长文本)',
  `source_url` VARCHAR(255) COMMENT '岗位来源地址(大盘数据的原始链接)',
  
  -- AI 处理字段
  `ai_extracted_profile` JSON COMMENT 'AI清洗后的结构化画像(学历、技能要求等)',
  `confidence_score` DECIMAL(5,2) COMMENT 'AI提取置信度(0-100)',
  
  `status` ENUM('PENDING_AI', 'PENDING_REVIEW', 'ACTIVE', 'CLOSED', 'REJECTED') DEFAULT 'PENDING_AI' COMMENT '状态(ACTIVE上架, CLOSED手动停止招聘)',
  `source_update_date` VARCHAR(50) COMMENT 'XLS中的更新日期',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='招聘岗位表(支持系统导入与企业自建)';

-- ==========================================
-- 4. 投递与面试闭环表
-- ==========================================
CREATE TABLE `job_applications` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_id` BIGINT NOT NULL,
  `job_id` BIGINT NOT NULL,
  `enterprise_id` BIGINT NOT NULL,
  `is_authorized` TINYINT(1) DEFAULT 1 COMMENT '是否授权企业查看详细画像',
  `status` ENUM('APPLIED', 'INTERVIEWING', 'REJECTED', 'OFFER') DEFAULT 'APPLIED',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='简历投递与授权记录表';

CREATE TABLE `interview_feedbacks` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `application_id` BIGINT NOT NULL,
  `result` ENUM('PASS', 'FAIL') NOT NULL COMMENT '面试结果',
  `feedback_tags` JSON COMMENT 'HR打的具体标签(如:["八股文背诵痕迹重"])',
  `feedback_notes` TEXT COMMENT '详细备注',
  `is_synced_to_ai` TINYINT(1) DEFAULT 0 COMMENT '是否已汇入学生的Gap_JSON中',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='真实面试反馈闭环表';

-- ==========================================
-- 5. 学校端与 1V1 辅导表
-- ==========================================
CREATE TABLE `teachers` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `school_user_id` BIGINT NOT NULL COMMENT '创建该老师的学校账号ID',
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20)
) ENGINE=InnoDB COMMENT='辅导老师表';

CREATE TABLE `counseling_appointments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_id` BIGINT NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `appointment_time` DATETIME NOT NULL COMMENT '预约时间',
  `location` VARCHAR(100) NOT NULL COMMENT '指导地点',
  `status` ENUM('PENDING', 'COMPLETED', 'CANCELED') DEFAULT 'PENDING',
  `teacher_evaluation` TEXT COMMENT '老师填写的辅导纪要(优劣势)',
  `teacher_tags` JSON COMMENT '老师打的标签',
  `is_rag_processed` TINYINT(1) DEFAULT 0 COMMENT '是否已触发RAG反哺调整AI得分',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='线下1V1辅导预约与AI反哺记录表';

-- ==========================================
-- 6. 管理端与 RabbitMQ / LLMOps 任务表
-- ==========================================
CREATE TABLE `llm_tasks` (
  `task_id` VARCHAR(50) PRIMARY KEY COMMENT '任务ID(MQ Message ID)',
  `task_type` ENUM('EXTRACT_JOB_XLS', 'GEN_STUDENT_PROFILE', 'RAG_RECALCULATE') NOT NULL,
  `target_id` BIGINT COMMENT '关联的业务主键(如job_id或student_id)',
  `status` ENUM('QUEUED', 'PROCESSING', 'SUCCESS', 'FAILED') DEFAULT 'QUEUED',
  `prompt_version` VARCHAR(50) COMMENT '提示词版本',
  `error_log` TEXT COMMENT '错误或置信度低的日志',
  `manual_correction` TEXT COMMENT '管理员人工质检备注(错题本)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='异步任务与LLMOps质检纠偏表';