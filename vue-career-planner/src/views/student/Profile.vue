<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <div class="profile-header">
        <el-avatar :size="80" :src="userInfo?.avatar || ''" :icon="UserFilled" class="profile-avatar" />
        <div class="profile-info">
          <h2 class="profile-name">{{ userInfo?.username || '用户' }}</h2>
          <p class="profile-role">{{ userInfo?.role === 'STUDENT' ? '学生' : '未知角色' }}</p>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userStats.courses }}</span>
              <span class="stat-label">已学课程</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.achievements }}</span>
              <span class="stat-label">获得成就</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.points }}</span>
              <span class="stat-label">积分</span>
            </div>
          </div>
        </div>
      </div>
      <el-divider />
      <div class="profile-details">
        <div class="detail-item">
          <span class="detail-label">姓名</span>
          <span class="detail-value">{{ userInfo?.name || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">性别</span>
          <span class="detail-value">{{ userInfo?.gender || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">年龄</span>
          <span class="detail-value">{{ userInfo?.age || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">学校</span>
          <span class="detail-value">{{ userInfo?.school || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">专业</span>
          <span class="detail-value">{{ userInfo?.major || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ userInfo?.email || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">手机号码</span>
          <span class="detail-value">{{ userInfo?.phone || '未设置' }}</span>
        </div>
      </div>
      <el-button type="primary" class="edit-btn" @click="handleEdit">编辑个人信息</el-button>
    </el-card>

    <!-- 学习进度卡片 -->
    <el-card class="progress-card">
      <template #header>
        <div class="card-header">
          <span>学习进度</span>
          <el-button type="text" @click="viewAllProgress">查看全部</el-button>
        </div>
      </template>
      <div class="progress-list">
        <div v-for="course in recentCourses" :key="course.id" class="progress-item">
          <div class="course-info">
            <h3 class="course-name">{{ course.name }}</h3>
            <p class="course-description">{{ course.description }}</p>
          </div>
          <div class="course-progress">
            <el-progress :percentage="course.progress" />
            <span class="progress-text">{{ course.progress }}%</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 最近成就卡片 -->
    <el-card class="achievements-card">
      <template #header>
        <div class="card-header">
          <span>最近成就</span>
          <el-button type="text" @click="viewAllAchievements">查看全部</el-button>
        </div>
      </template>
      <div class="achievements-list">
        <div v-for="achievement in recentAchievements" :key="achievement.id" class="achievement-item">
          <el-avatar :size="40" :icon="Trophy" class="achievement-icon" :style="{ backgroundColor: achievement.color }" />
          <div class="achievement-info">
            <h4 class="achievement-title">{{ achievement.title }}</h4>
            <p class="achievement-description">{{ achievement.description }}</p>
            <span class="achievement-date">{{ achievement.date }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 职业规划卡片 -->
    <el-card class="career-card">
      <template #header>
        <div class="card-header">
          <span>职业规划</span>
          <el-button type="text" @click="updateCareerGoal">更新目标</el-button>
        </div>
      </template>
      <div class="career-info">
        <div class="career-item">
          <span class="career-label">目标职业</span>
          <span class="career-value">{{ careerGoal.occupation || '未设置' }}</span>
        </div>
        <div class="career-item">
          <span class="career-label">目标城市</span>
          <span class="career-value">{{ careerGoal.city || '未设置' }}</span>
        </div>
        <div class="career-item">
          <span class="career-label">匹配度</span>
          <span class="career-value match-score">{{ careerGoal.matchScore || '未评估' }}</span>
        </div>
        <div class="career-plan">
          <h4>学习计划</h4>
          <p>{{ careerGoal.plan || '未生成学习计划' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input v-model.number="editForm.age" type="number" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="editForm.school" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" type="email" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Trophy } from '@element-plus/icons-vue'

const $router = useRouter()

// 用户信息
const userInfo = ref({
  id: 1,
  username: '张三',
  name: '张三',
  gender: '男',
  age: 20,
  school: '北京大学',
  major: '计算机科学与技术',
  email: 'zhangsan@example.com',
  phone: '13800138000',
  avatar: '',
  role: 'STUDENT'
})

// 用户统计数据
const userStats = ref({
  courses: 12,
  achievements: 8,
  points: 1250
})

// 最近课程
const recentCourses = ref([
  {
    id: 1,
    name: '前端开发基础',
    description: 'HTML、CSS、JavaScript基础',
    progress: 85
  },
  {
    id: 2,
    name: 'Vue3实战',
    description: 'Vue3框架学习与项目实战',
    progress: 60
  },
  {
    id: 3,
    name: '算法与数据结构',
    description: '常见算法与数据结构',
    progress: 45
  }
])

// 最近成就
const recentAchievements = ref([
  {
    id: 1,
    title: '学习达人',
    description: '连续学习7天',
    date: '2026-03-10',
    color: '#409EFF'
  },
  {
    id: 2,
    title: '课程完成',
    description: '完成前端开发基础课程',
    date: '2026-03-08',
    color: '#67C23A'
  },
  {
    id: 3,
    title: '首次登录',
    description: '首次登录系统',
    date: '2026-03-01',
    color: '#E6A23C'
  }
])

// 职业目标
const careerGoal = ref({
  occupation: '前端开发工程师',
  city: '北京',
  matchScore: '85%',
  plan: '建议学习Vue3、React等前端框架，掌握TypeScript，提升项目实战能力。'
})

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({ ...userInfo.value })

onMounted(() => {
  // 从本地存储获取用户信息
  const userStr = localStorage.getItem('user')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
    editForm.value = { ...userInfo.value }
  }
})

// 编辑个人信息
const handleEdit = () => {
  editForm.value = { ...userInfo.value }
  editDialogVisible.value = true
}

// 保存用户信息
const saveUserInfo = () => {
  userInfo.value = { ...editForm.value }
  localStorage.setItem('user', JSON.stringify(userInfo.value))
  ElMessage.success('个人信息已更新')
  editDialogVisible.value = false
}

// 查看全部进度
const viewAllProgress = () => {
  // 跳转到学习进度页面
  ElMessage.info('查看全部学习进度')
}

// 查看全部成就
const viewAllAchievements = () => {
  // 跳转到成就页面
  $router.push('/student/achievements')
}

// 更新职业目标
const updateCareerGoal = () => {
  // 跳转到职业目标页面
  $router.push('/student/career-goal')
}
</script>

<style scoped>
.profile-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  grid-column: 1 / -1;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.profile-header {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-avatar {
  margin-right: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 5px 0;
}

.profile-role {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 15px 0;
}

.profile-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

.profile-details {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-label {
  color: #909399;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.edit-btn {
  margin-top: 20px;
  width: 100%;
}

.progress-card,
.achievements-card,
.career-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.progress-item:last-child {
  border-bottom: none;
}

.course-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 5px 0;
}

.course-description {
  font-size: 14px;
  color: #909399;
  margin: 0 0 10px 0;
}

.course-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  font-size: 14px;
  font-weight: 500;
  color: #409EFF;
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.achievement-item:last-child {
  border-bottom: none;
}

.achievement-icon {
  margin-right: 15px;
}

.achievement-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 5px 0;
}

.achievement-description {
  font-size: 12px;
  color: #909399;
  margin: 0 0 5px 0;
}

.achievement-date {
  font-size: 11px;
  color: #c0c4cc;
}

.career-info {
  padding: 10px 0;
}

.career-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.career-item:last-child {
  border-bottom: none;
}

.career-label {
  color: #909399;
  font-size: 14px;
}

.career-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.match-score {
  color: #67C23A;
  font-weight: 600;
}

.career-plan {
  margin-top: 20px;
}

.career-plan h4 {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 10px 0;
}

.career-plan p {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
  
  .profile-details {
    grid-template-columns: 1fr;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .profile-stats {
    justify-content: center;
  }
}
</style>