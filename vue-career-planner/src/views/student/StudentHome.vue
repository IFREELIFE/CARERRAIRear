<template>
  <div class="student-home">
    <div class="home-header">
      <h2>欢迎回来，张三</h2>
      <p>今天是 {{ today }}，祝你职业规划顺利！</p>
    </div>
    <div class="home-content">
      <!-- 个人概览 -->
      <div class="stats-card">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人概览</span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">1</div>
              <div class="stat-label">已上传简历</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">12</div>
              <div class="stat-label">匹配岗位</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">2</div>
              <div class="stat-label">已生成报告</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">3</div>
              <div class="stat-label">待完成任务</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">7</div>
              <div class="stat-label">连续签到</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">85</div>
              <div class="stat-label">画像完整度</div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 快速操作 -->
      <div class="quick-actions">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="actions-grid">
            <el-button type="primary" @click="navigateTo('/student/persona')">
              <el-icon><UserFilled /></el-icon>
              我的画像
            </el-button>
            <el-button type="success" @click="navigateTo('/student/appointment')">
              <el-icon><Calendar /></el-icon>
              预约指导
            </el-button>
            <el-button type="info" @click="navigateTo('/student/career-goal')">
              <el-icon><StarFilled /></el-icon>
              目标设定
            </el-button>
            <el-button type="warning" @click="navigateTo('/student/jobs')">
              <el-icon><Briefcase /></el-icon>
              招聘信息
            </el-button>
            <el-button type="danger" @click="navigateTo('/student/achievements')">
              <el-icon><Medal /></el-icon>
              我的成就
            </el-button>
            <el-button type="default" @click="navigateTo('/student/announcements')">
              <el-icon><Bell /></el-icon>
              通告中心
            </el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 推荐岗位 -->
      <div class="recommended-jobs">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>推荐岗位</span>
              <el-button type="text" @click="navigateTo('/student/jobs')">查看更多</el-button>
            </div>
          </template>
          <div class="jobs-list">
            <div class="job-item" v-for="(job, index) in recommendedJobs" :key="index">
              <div class="job-info">
                <h4>{{ job.title }}</h4>
                <p class="job-company">{{ job.company }}</p>
                <div class="job-tags">
                  <el-tag size="small" effect="light">{{ job.salary }}</el-tag>
                  <el-tag size="small" effect="light">{{ job.city }}</el-tag>
                  <el-tag size="small" effect="light">{{ job.experience }}</el-tag>
                </div>
              </div>
              <div class="job-match">
                <span class="match-value">{{ job.matchScore }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 最近通告 -->
      <div class="recent-announcements">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近通告</span>
              <el-button type="text" @click="navigateTo('/student/announcements')">查看全部</el-button>
            </div>
          </template>
          <div class="announcements-list">
            <div class="announcement-item" v-for="(announcement, index) in recentAnnouncements" :key="index" :class="announcement.type">
              <div class="announcement-type" :class="announcement.type">
                {{ getTypeLabel(announcement.type) }}
              </div>
              <div class="announcement-content">
                <h5>{{ announcement.title }}</h5>
                <p>{{ announcement.content }}</p>
              </div>
              <div class="announcement-date">{{ announcement.date }}</div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 学习进度 -->
      <div class="learning-progress">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学习进度</span>
            </div>
          </template>
          <div class="progress-list">
            <div class="progress-item" v-for="(item, index) in learningProgress" :key="index">
              <div class="progress-header">
                <span class="progress-title">{{ item.title }}</span>
                <span class="progress-percentage">{{ item.progress }}%</span>
              </div>
              <el-progress :percentage="item.progress" :stroke-width="8" :color="item.color" />
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 最近活动 -->
      <div class="recent-activity">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <div class="activity-list">
            <div class="activity-item" v-for="(activity, index) in recentActivities" :key="index">
              <div class="activity-icon" :class="activity.type">
                <el-icon :size="20"><component :is="activity.icon" /></el-icon>
              </div>
              <div class="activity-content">
                <p class="activity-text">{{ activity.text }}</p>
                <p class="activity-time">{{ activity.time }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Calendar, StarFilled, Briefcase, Medal, Bell, Upload, Document, Clock, Check, Message } from '@element-plus/icons-vue'

const router = useRouter()

// 今天的日期
const today = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 推荐岗位
const recommendedJobs = ref([
  {
    id: 1,
    title: '前端开发工程师',
    company: '字节跳动',
    salary: '15K-25K',
    city: '北京',
    experience: '1-3年',
    matchScore: 85
  },
  {
    id: 2,
    title: '全栈开发工程师',
    company: '腾讯',
    salary: '20K-30K',
    city: '深圳',
    experience: '3-5年',
    matchScore: 78
  },
  {
    id: 3,
    title: '前端开发工程师',
    company: '阿里巴巴',
    salary: '18K-28K',
    city: '杭州',
    experience: '1-3年',
    matchScore: 82
  }
])

// 最近通告
const recentAnnouncements = ref([
  {
    id: 1,
    title: '学习进度预警',
    content: '您的专业技能学习进度落后于平均水平，建议加强相关技能的学习。',
    type: 'ai',
    date: '2026-03-10'
  },
  {
    id: 2,
    title: '职业规划建议',
    content: '根据您的人物画像分析，建议您考虑前端开发工程师方向，该方向与您的技能匹配度较高。',
    type: 'teacher',
    date: '2026-03-09'
  },
  {
    id: 3,
    title: '鼓励信息',
    content: '您的学习态度非常认真，继续保持！相信您会在职业发展道路上取得优异成绩。',
    type: 'encouragement',
    date: '2026-03-08'
  }
])

// 学习进度
const learningProgress = ref([
  {
    title: '专业技能',
    progress: 75,
    color: '#409eff'
  },
  {
    title: '沟通能力',
    progress: 60,
    color: '#67c23a'
  },
  {
    title: '团队协作',
    progress: 80,
    color: '#e6a23c'
  },
  {
    title: '问题解决',
    progress: 70,
    color: '#f56c6c'
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    text: '完成了职业测评',
    time: '2026-03-10 14:30',
    type: 'success',
    icon: Check
  },
  {
    id: 2,
    text: '上传了简历',
    time: '2026-03-09 09:15',
    type: 'info',
    icon: Upload
  },
  {
    id: 3,
    text: '生成了职业规划报告',
    time: '2026-03-08 16:45',
    type: 'success',
    icon: Document
  },
  {
    id: 4,
    text: '收到了老师的指导建议',
    time: '2026-03-07 11:20',
    type: 'warning',
    icon: Message
  },
  {
    id: 5,
    text: '连续签到7天',
    time: '2026-03-06 08:00',
    type: 'success',
    icon: Clock
  }
])

// 导航到指定页面
const navigateTo = (path: string) => {
  router.push(path)
}

// 获取通告类型标签
const getTypeLabel = (type: string) => {
  const labels = {
    teacher: '老师预警',
    ai: 'AI预警',
    encouragement: '鼓励信息'
  }
  return labels[type as keyof typeof labels] || type
}
</script>

<style scoped lang="scss">
.student-home {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.home-header {
  margin-bottom: 30px;
  h2 {
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #303133;
  }
  p {
    font-size: 16px;
    color: #606266;
  }
}

.home-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;

  .stats-card,
  .quick-actions {
    grid-column: 1 / -1;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  span {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-top: 20px;

  .stat-item {
    text-align: center;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1890ff;
      margin-bottom: 8px;
    }
    .stat-label {
      font-size: 14px;
      color: #606266;
    }
  }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 20px;

  .el-button {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 10px;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .el-icon {
      margin-bottom: 8px;
      font-size: 20px;
    }
  }
}

.jobs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  
  .job-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
    
    .job-info {
      flex: 1;
      
      h4 {
        margin: 0 0 8px;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      
      .job-company {
        margin: 0 0 8px;
        font-size: 14px;
        color: #606266;
      }
      
      .job-tags {
        display: flex;
        gap: 8px;
      }
    }
    
    .job-match {
      .match-value {
        font-size: 18px;
        font-weight: 600;
        color: #1890ff;
      }
    }
  }
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  
  .announcement-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 8px;
    
    &.teacher {
      border-left: 4px solid #f56c6c;
    }
    
    &.ai {
      border-left: 4px solid #409eff;
    }
    
    &.encouragement {
      border-left: 4px solid #67c23a;
    }
    
    .announcement-type {
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      white-space: nowrap;
      
      &.teacher {
        background-color: #fef0f0;
        color: #f56c6c;
      }
      
      &.ai {
        background-color: #ecf5ff;
        color: #409eff;
      }
      
      &.encouragement {
        background-color: #f0f9eb;
        color: #67c23a;
      }
    }
    
    .announcement-content {
      flex: 1;
      
      h5 {
        margin: 0 0 4px;
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }
      
      p {
        margin: 0;
        font-size: 13px;
        color: #606266;
        line-height: 1.4;
      }
    }
    
    .announcement-date {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
    }
  }
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
  
  .progress-item {
    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .progress-title {
        font-size: 14px;
        color: #303133;
      }
      
      .progress-percentage {
        font-size: 14px;
        font-weight: 600;
        color: #606266;
      }
    }
  }
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  
  .activity-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    background-color: #f8f9fa;
    border-radius: 8px;
    
    .activity-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      &.success {
        background-color: #f0f9eb;
        color: #67c23a;
      }
      
      &.info {
        background-color: #ecf5ff;
        color: #409eff;
      }
      
      &.warning {
        background-color: #fdf6ec;
        color: #e6a23c;
      }
      
      &.danger {
        background-color: #fef0f0;
        color: #f56c6c;
      }
    }
    
    .activity-content {
      flex: 1;
      
      .activity-text {
        margin: 0 0 4px;
        font-size: 14px;
        color: #303133;
      }
      
      .activity-time {
        margin: 0;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .home-content {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .actions-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
