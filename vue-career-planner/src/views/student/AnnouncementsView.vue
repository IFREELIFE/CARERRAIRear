<template>
  <div class="announcements-view">
    <h2>通告中心</h2>
    
    <el-card shadow="hover" class="announcements-card">
      <div class="announcements-filter">
        <el-select v-model="filterType" placeholder="筛选类型" style="width: 150px;">
          <el-option label="全部" value="" />
          <el-option label="老师预警" value="teacher" />
          <el-option label="AI预警" value="ai" />
          <el-option label="鼓励信息" value="encouragement" />
        </el-select>
      </div>
      
      <div class="announcements-list">
        <div class="announcement-item" v-for="(announcement, index) in filteredAnnouncements" :key="index" :class="announcement.type">
          <div class="announcement-header">
            <div class="announcement-type" :class="announcement.type">
              {{ getTypeLabel(announcement.type) }}
            </div>
            <span class="announcement-date">{{ announcement.date }}</span>
          </div>
          <div class="announcement-content">
            <h4>{{ announcement.title }}</h4>
            <p>{{ announcement.content }}</p>
          </div>
          <div class="announcement-actions" v-if="announcement.type === 'teacher' || announcement.type === 'ai'">
            <el-button type="primary" size="small" @click="markAsRead(announcement)">
              {{ announcement.read ? '已读' : '标记为已读' }}
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-if="filteredAnnouncements.length === 0" class="empty-state">
        <el-empty description="暂无通告" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 通告数据
const announcements = ref([
  {
    id: 1,
    title: '学习进度预警',
    content: '您的专业技能学习进度落后于平均水平，建议加强相关技能的学习。',
    type: 'ai',
    date: '2026-03-10',
    read: false
  },
  {
    id: 2,
    title: '职业规划建议',
    content: '根据您的人物画像分析，建议您考虑前端开发工程师方向，该方向与您的技能匹配度较高。',
    type: 'teacher',
    date: '2026-03-09',
    read: true
  },
  {
    id: 3,
    title: '鼓励信息',
    content: '您的学习态度非常认真，继续保持！相信您会在职业发展道路上取得优异成绩。',
    type: 'encouragement',
    date: '2026-03-08',
    read: true
  },
  {
    id: 4,
    title: '技能差距预警',
    content: '您的沟通能力评分较低，建议参加相关培训或实践活动来提升。',
    type: 'ai',
    date: '2026-03-07',
    read: false
  },
  {
    id: 5,
    title: '实习机会推荐',
    content: '有适合您专业的实习机会，建议关注并积极申请。',
    type: 'teacher',
    date: '2026-03-06',
    read: true
  }
])

const filterType = ref('')

// 过滤通告
const filteredAnnouncements = computed(() => {
  if (!filterType.value) {
    return announcements.value
  }
  return announcements.value.filter(announcement => announcement.type === filterType.value)
})

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels = {
    teacher: '老师预警',
    ai: 'AI预警',
    encouragement: '鼓励信息'
  }
  return labels[type as keyof typeof labels] || type
}

// 标记为已读
const markAsRead = (announcement: any) => {
  announcement.read = true
  ElMessage.success('已标记为已读')
}
</script>

<style scoped lang="scss">
.announcements-view {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .announcements-card {
    border-radius: 12px;
    
    .announcements-filter {
      margin-bottom: 20px;
    }
    
    .announcements-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      .announcement-item {
        padding: 20px;
        border: 1px solid #f0f0f0;
        border-radius: 12px;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        }
        
        &.teacher {
          border-left: 4px solid #f56c6c;
        }
        
        &.ai {
          border-left: 4px solid #409eff;
        }
        
        &.encouragement {
          border-left: 4px solid #67c23a;
        }
        
        .announcement-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .announcement-type {
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
            
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
          
          .announcement-date {
            font-size: 12px;
            color: #999;
          }
        }
        
        .announcement-content {
          margin-bottom: 16px;
          
          h4 {
            margin: 0 0 8px;
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
        }
        
        .announcement-actions {
          display: flex;
          justify-content: flex-end;
        }
      }
    }
    
    .empty-state {
      padding: 40px 0;
      text-align: center;
    }
  }
}
</style>