<template>
  <div class="welcome-container">
    <div class="welcome-card">
      <div class="welcome-header">
        <el-avatar :size="64" :icon="UserFilled" class="welcome-avatar" />
        <h1>欢迎使用职业规划平台</h1>
      </div>
      <div class="welcome-content">
        <p>为了给您提供更精准的职业规划服务，我们建议您完成一份简短的问卷，帮助我们了解您的技术背景和能力水平。</p>
        <p>问卷内容包括：</p>
        <ul>
          <li>您所学过的技术和技能</li>
          <li>您的MBTI性格测试</li>
        </ul>
        <p>完成后，我们将为您生成个性化的职业画像，为您的职业规划提供更有针对性的建议。</p>
      </div>
      <div class="welcome-actions">
        <el-button type="default" @click="handleSkip">跳过</el-button>
        <el-button type="primary" @click="handleStart">马上填写</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'

const $router = useRouter()

const handleSkip = () => {
  // 跳过问卷，直接进入首页
  localStorage.setItem('questionnaireCompleted', 'false')
  $router.push('/student')
}

const handleStart = () => {
  // 开始填写问卷
  $router.push('/student/questionnaire/skills')
}
</script>

<style scoped>
.welcome-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  overflow-y: auto;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 40px;
  max-width: 600px;
  width: 100%;
  text-align: center;
  backdrop-filter: blur(10px);
}

.welcome-header {
  margin-bottom: 30px;
}

.welcome-avatar {
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.welcome-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.welcome-content {
  margin-bottom: 40px;
  text-align: left;
}

.welcome-content p {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin: 0 0 15px 0;
}

.welcome-content ul {
  margin: 0 0 15px 20px;
  padding: 0;
}

.welcome-content li {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.welcome-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.welcome-actions .el-button {
  min-width: 120px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.welcome-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.welcome-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4391 100%);
}

@media (max-width: 768px) {
  .welcome-card {
    padding: 30px 20px;
  }
  
  .welcome-header h1 {
    font-size: 20px;
  }
  
  .welcome-actions {
    flex-direction: column;
  }
  
  .welcome-actions .el-button {
    width: 100%;
  }
}
</style>