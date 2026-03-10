# EndCareerAi - 智能求职匹配平台

## 技术栈

- **框架**: Spring Boot 3.2.5 + Java 17
- **ORM**: MyBatis-Plus 3.5.6
- **消息队列**: RabbitMQ (异步LLM任务处理)
- **缓存**: Redis (用户/岗位/教师时段缓存)
- **搜索引擎**: Elasticsearch (岗位全文检索)
- **认证**: JWT + Spring Security (角色权限控制)
- **Excel导入**: Apache POI 5.2.5

## 项目结构

```
src/main/java/com/endcareerai/platform/
├── EndCareerAiApplication.java          # 启动类
├── common/                              # 通用组件
│   ├── Result.java                      # 统一响应包装
│   ├── Constants.java                   # 常量定义
│   ├── BusinessException.java           # 业务异常
│   └── GlobalExceptionHandler.java      # 全局异常处理
├── config/                              # 配置类
│   ├── SecurityConfig.java              # Spring Security + JWT
│   ├── RedisConfig.java                 # Redis序列化配置
│   ├── RabbitMQConfig.java              # RabbitMQ交换机/队列/绑定
│   ├── ElasticsearchConfig.java         # ES仓库扫描
│   └── MyBatisPlusConfig.java           # 分页插件
├── security/                            # JWT认证
│   ├── JwtTokenProvider.java            # Token生成/验证
│   └── JwtAuthenticationFilter.java     # 请求过滤器
├── entity/                              # 数据库实体(10个表)
├── mapper/                              # MyBatis-Plus Mapper接口
├── dto/                                 # 数据传输对象
│   ├── request/                         # 请求DTO
│   └── response/                        # 响应DTO
├── service/                             # 业务层
│   ├── impl/                            # 服务实现
│   ├── RedisService.java                # Redis缓存服务
│   └── ElasticsearchService.java        # ES搜索服务
├── controller/                          # REST控制器(5个模块)
├── mq/                                  # RabbitMQ消息
│   ├── LlmTaskMessage.java             # 消息体
│   ├── LlmTaskProducer.java            # 消息生产者
│   └── LlmTaskConsumer.java            # 消息消费者
└── es/                                  # Elasticsearch
    ├── JobDocument.java                 # ES文档映射
    └── JobDocumentRepository.java       # ES仓库
```

## API 模块

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/v1/auth` | 多端注册(学生/学校/企业) |
| 学生端 | `/v1/student` | 档案初始化、职业匹配、AI对话、投递 |
| 学校端 | `/v1/school` | 教师时段、辅导评价(RAG反哺) |
| 企业端 | `/v1/enterprise` | Excel导入、关闭岗位、面试反馈(自愈闭环) |
| 管理端 | `/v1/admin` | LLM任务监控、精准重试 |

## 快速开始

### 前置条件
- JDK 17+
- MySQL 8.0+
- Redis 7+
- RabbitMQ 3.12+
- Elasticsearch 8.x

### 启动步骤

1. 执行 `schema_Version2 (1).sql` 创建数据库和表
2. 修改 `src/main/resources/application.yml` 中的数据库、Redis、RabbitMQ、ES连接配置
3. 构建并运行:
```bash
mvn clean package -DskipTests
java -jar target/platform-0.0.1-SNAPSHOT.jar
```

### 核心API

```bash
# 注册
POST /v1/auth/register

# 学生初始化档案(触发AI生成12维画像)
POST /v1/student/profile/init

# 职业匹配与规划PDF生成
POST /v1/student/career/match-and-plan

# AI智能体对话
POST /v1/student/agent/job-chat

# 企业批量上传岗位Excel
POST /v1/jobs/import/excel

# 面试反馈(自愈闭环)
POST /v1/enterprise/interviews/{applicationId}/feedback

# LLM任务监控
GET /v1/admin/llmops/tasks
```