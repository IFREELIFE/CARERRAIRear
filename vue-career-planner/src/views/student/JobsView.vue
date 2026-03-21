<template>
  <div class="jobs-view">
    <h2>招聘信息</h2>
    
    <div class="jobs-container">
      <!-- 岗位列表 -->
      <div class="jobs-list" :class="{ 'expanded': selectedJob }">
        <div class="search-bar">
          <el-input placeholder="搜索岗位" prefix-icon="Search" v-model="searchKeyword" />
          <el-select v-model="jobType" placeholder="岗位类型" style="margin-left: 12px; width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="技术" value="技术" />
            <el-option label="产品" value="产品" />
            <el-option label="设计" value="设计" />
          </el-select>
        </div>
        
        <div class="jobs-grid">
          <div class="job-card" v-for="(job, index) in filteredJobs" :key="index" @click="selectJob(job)">
            <div class="job-header">
              <h3>{{ job.title }}</h3>
              <span class="job-salary">{{ job.salary }}</span>
            </div>
            <div class="job-company">{{ job.company }}</div>
            <div class="job-info">
              <span class="job-tag">{{ job.city }}</span>
              <span class="job-tag">{{ job.experience }}</span>
              <span class="job-tag">{{ job.education }}</span>
            </div>
            <div class="job-desc">{{ job.desc }}</div>
            <div class="job-footer">
              <span class="job-date">{{ job.date }}</span>
              <span class="job-match">匹配度: {{ job.matchScore }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 岗位详情和AI智能体 -->
      <div class="job-detail" v-if="selectedJob">
        <div class="detail-header">
          <h3>{{ selectedJob.title }}</h3>
          <el-button type="text" @click="closeDetail">&times;</el-button>
        </div>
        
        <div class="detail-content">
          <div class="job-info-section">
            <div class="info-row">
              <span class="info-label">公司名称:</span>
              <span class="info-value">{{ selectedJob.company }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">薪资范围:</span>
              <span class="info-value">{{ selectedJob.salary }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">工作地点:</span>
              <span class="info-value">{{ selectedJob.city }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">工作经验:</span>
              <span class="info-value">{{ selectedJob.experience }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">学历要求:</span>
              <span class="info-value">{{ selectedJob.education }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ selectedJob.date }}</span>
            </div>
          </div>
          
          <div class="job-description">
            <h4>职位描述</h4>
            <p>{{ selectedJob.description }}</p>
          </div>
          
          <div class="job-requirements">
            <h4>任职要求</h4>
            <ul>
              <li v-for="(req, idx) in selectedJob.requirements" :key="idx">{{ req }}</li>
            </ul>
          </div>
        </div>
        
        <!-- AI智能体 -->
        <div class="ai-assistant">
          <div class="ai-header">
            <div class="ai-avatar">
              <el-avatar :size="40" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=AI%20assistant%20robot%20icon&image_size=square" />
            </div>
            <h4>AI职业顾问</h4>
          </div>
          
          <div class="ai-chat">
            <div class="ai-message">
              <div class="message-content">
                <p>您好！我是您的AI职业顾问。根据您的人物画像和该岗位的要求，我正在分析您的匹配度...</p>
              </div>
            </div>
            <div class="ai-message" v-if="aiAnalysis">
              <div class="message-content">
                <h5>匹配度分析</h5>
                <p>匹配度: <strong>{{ aiAnalysis.matchScore }}%</strong></p>
                <p>{{ aiAnalysis.analysis }}</p>
                <p>{{ aiAnalysis.suggestion }}</p>
              </div>
            </div>
            <div class="user-message" v-for="(msg, idx) in userMessages" :key="idx">
              <div class="message-content">
                <p>{{ msg }}</p>
              </div>
            </div>
          </div>
          
          <div class="ai-input">
            <el-input 
              v-model="aiInput" 
              placeholder="请输入您的问题..." 
              @keyup.enter="sendMessage"
              suffix-icon="ChatLineRound"
            />
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 岗位数据
const jobs = ref([
  {
    id: 1,
    title: '前端开发工程师',
    company: '字节跳动',
    salary: '15K-25K',
    city: '北京',
    experience: '1-3年',
    education: '本科及以上',
    desc: '负责公司产品的前端开发，使用Vue、React等技术栈',
    date: '2026-03-10',
    matchScore: 85,
    description: '1. 负责公司产品的前端开发和维护\n2. 使用Vue、React等现代前端框架\n3. 参与产品需求分析和技术方案设计\n4. 与后端团队协作，实现前后端交互',
    requirements: [
      '本科及以上学历，计算机相关专业',
      '1-3年前端开发经验',
      '熟悉Vue、React等前端框架',
      '了解Node.js和后端技术',
      '良好的沟通能力和团队协作精神'
    ]
  },
  {
    id: 2,
    title: '产品经理',
    company: '腾讯',
    salary: '20K-30K',
    city: '深圳',
    experience: '3-5年',
    education: '本科及以上',
    desc: '负责产品规划和设计，与开发团队协作实现产品功能',
    date: '2026-03-09',
    matchScore: 72,
    description: '1. 负责产品的规划、设计和迭代\n2. 与用户和业务方沟通，收集需求\n3. 撰写产品文档和需求规格说明\n4. 与开发和设计团队协作，推动产品落地',
    requirements: [
      '本科及以上学历，专业不限',
      '3-5年产品经理经验',
      '良好的逻辑思维和沟通能力',
      '熟悉产品开发流程',
      '有互联网产品经验优先'
    ]
  },
  {
    id: 3,
    title: 'UI设计师',
    company: '阿里巴巴',
    salary: '12K-20K',
    city: '杭州',
    experience: '1-3年',
    education: '本科及以上',
    desc: '负责产品的UI设计，提升用户体验',
    date: '2026-03-08',
    matchScore: 68,
    description: '1. 负责产品的UI设计和视觉风格定义\n2. 制作设计原型和交互流程图\n3. 与产品和开发团队协作，确保设计落地\n4. 持续优化产品的用户体验',
    requirements: [
      '本科及以上学历，设计相关专业',
      '1-3年UI设计经验',
      '熟练使用Figma、Sketch等设计工具',
      '良好的审美能力和创意思维',
      '了解前端技术实现原理'
    ]
  },
  {
    id: 4,
    title: '后端开发工程师',
    company: '美团',
    salary: '18K-28K',
    city: '北京',
    experience: '3-5年',
    education: '本科及以上',
    desc: '负责后端系统的开发和维护，使用Java、Go等技术',
    date: '2026-03-07',
    matchScore: 78,
    description: '1. 负责后端系统的设计、开发和维护\n2. 优化系统性能和稳定性\n3. 与前端团队协作，提供API接口\n4. 参与技术选型和架构设计',
    requirements: [
      '本科及以上学历，计算机相关专业',
      '3-5年后端开发经验',
      '熟悉Java、Go等后端语言',
      '了解分布式系统和微服务架构',
      '良好的问题解决能力'
    ]
  }
])

const searchKeyword = ref('')
const jobType = ref('')
const selectedJob = ref<any>(null)
const aiInput = ref('')
const userMessages = ref<string[]>([])
const aiAnalysis = ref<any>(null)

// 过滤岗位
const filteredJobs = computed(() => {
  return jobs.value.filter(job => {
    const matchesKeyword = job.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) || 
                         job.company.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return matchesKeyword
  })
})

// 选择岗位
const selectJob = (job: any) => {
  selectedJob.value = job
  userMessages.value = []
  // 模拟AI分析
  setTimeout(() => {
    aiAnalysis.value = {
      matchScore: job.matchScore,
      analysis: `您的技能与${job.title}岗位的匹配度为${job.matchScore}%。`,
      suggestion: job.matchScore >= 70 
        ? '您的技能与该岗位较为匹配，建议您投递简历。' 
        : '您的技能与该岗位有一定差距，建议您提升相关技能后再投递。'
    }
  }, 1000)
}

// 关闭详情
const closeDetail = () => {
  selectedJob.value = null
  aiAnalysis.value = null
  userMessages.value = []
}

// 发送消息
const sendMessage = () => {
  if (aiInput.value.trim()) {
    userMessages.value.push(aiInput.value)
    // 模拟AI回复
    setTimeout(() => {
      userMessages.value.push('这是一个关于就业和学习的回答。')
    }, 1000)
    aiInput.value = ''
  }
}
</script>

<style scoped lang="scss">
.jobs-view {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .jobs-container {
    display: flex;
    gap: 20px;
    
    .jobs-list {
      flex: 1;
      transition: all 0.3s ease;
      
      &.expanded {
        flex: 2;
      }
      
      .search-bar {
        display: flex;
        margin-bottom: 20px;
        
        .el-input {
          flex: 1;
        }
      }
      
      .jobs-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 16px;
        
        .job-card {
          padding: 20px;
          border: 1px solid #f0f0f0;
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }
          
          .job-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;
            
            h3 {
              margin: 0;
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
            
            .job-salary {
              font-size: 16px;
              font-weight: 600;
              color: #f56c6c;
            }
          }
          
          .job-company {
            font-size: 14px;
            color: #666;
            margin-bottom: 12px;
          }
          
          .job-info {
            display: flex;
            gap: 8px;
            margin-bottom: 12px;
            
            .job-tag {
              font-size: 12px;
              color: #999;
              background-color: #f5f7fa;
              padding: 2px 8px;
              border-radius: 4px;
            }
          }
          
          .job-desc {
            font-size: 14px;
            color: #666;
            line-height: 1.4;
            margin-bottom: 12px;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }
          
          .job-footer {
            display: flex;
            justify-content: space-between;
            font-size: 12px;
            color: #999;
            
            .job-match {
              color: #1890ff;
              font-weight: 500;
            }
          }
        }
      }
    }
    
    .job-detail {
      width: 400px;
      background: #ffffff;
      border: 1px solid #f0f0f0;
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      display: flex;
      flex-direction: column;
      
      .detail-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
        border-bottom: 1px solid #f0f0f0;
        
        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #333;
        }
        
        .el-button {
          font-size: 20px;
          padding: 0;
          width: 30px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      
      .detail-content {
        padding: 20px;
        border-bottom: 1px solid #f0f0f0;
        overflow-y: auto;
        max-height: 400px;
        
        .job-info-section {
          margin-bottom: 20px;
          
          .info-row {
            display: flex;
            margin-bottom: 8px;
            
            .info-label {
              width: 80px;
              font-size: 14px;
              color: #999;
            }
            
            .info-value {
              flex: 1;
              font-size: 14px;
              color: #333;
            }
          }
        }
        
        .job-description,
        .job-requirements {
          margin-bottom: 20px;
          
          h4 {
            margin: 0 0 12px;
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }
          
          p {
            margin: 0;
            font-size: 14px;
            color: #666;
            line-height: 1.5;
          }
          
          ul {
            margin: 0;
            padding-left: 20px;
            
            li {
              font-size: 14px;
              color: #666;
              line-height: 1.5;
              margin-bottom: 4px;
            }
          }
        }
      }
      
      .ai-assistant {
        flex: 1;
        display: flex;
        flex-direction: column;
        
        .ai-header {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px 20px;
          border-bottom: 1px solid #f0f0f0;
          
          .ai-avatar {
            flex-shrink: 0;
          }
          
          h4 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }
        }
        
        .ai-chat {
          flex: 1;
          padding: 16px 20px;
          overflow-y: auto;
          
          .ai-message,
          .user-message {
            margin-bottom: 16px;
            
            .message-content {
              max-width: 80%;
              padding: 12px;
              border-radius: 8px;
              
              p {
                margin: 0 0 8px;
                font-size: 14px;
                line-height: 1.5;
              }
              
              h5 {
                margin: 0 0 8px;
                font-size: 14px;
                font-weight: 600;
                color: #333;
              }
            }
          }
          
          .ai-message {
            .message-content {
              background-color: #f5f7fa;
              color: #333;
              border-top-left-radius: 0;
            }
          }
          
          .user-message {
            display: flex;
            justify-content: flex-end;
            
            .message-content {
              background-color: #e6f7ff;
              color: #1890ff;
              border-top-right-radius: 0;
            }
          }
        }
        
        .ai-input {
          display: flex;
          gap: 8px;
          padding: 16px 20px;
          border-top: 1px solid #f0f0f0;
          
          .el-input {
            flex: 1;
          }
        }
      }
    }
  }
}
</style>
