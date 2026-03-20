<template>
  <div class="questionnaire-container">
    <div class="questionnaire-card">
      <div class="questionnaire-header">
        <h1>确认信息</h1>
        <p>请确认您填写的信息真实准确，这将为您的职业画像生成与个性化规划服务提供可靠依据。</p>
      </div>
      <div class="confirm-content">
        <div class="certificate">
          <h2>信息确认保证书</h2>
          <p>本人郑重确认：所填写的技术技能与 MBTI 性格测试信息均真实、准确、完整，不存在任何虚假或隐瞒。</p>
          <p>本人充分知晓并同意，上述信息将用于生成专属职业画像，为我提供精准的职业规划与求职建议，若因信息不实导致相关服务偏差，本人愿自行承担责任。</p>
          <el-checkbox v-model="agreed">我已阅读并确认以上信息准确无误</el-checkbox>
        </div>
      </div>
      <div class="questionnaire-actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!agreed">确认完成</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const $router = useRouter()
const agreed = ref(false)

const handleBack = () => {
  $router.push('/student/questionnaire/mbti')
}

const handleConfirm = () => {
  if (agreed.value) {
    // 标记问卷完成
    localStorage.setItem('questionnaireCompleted', 'true')
    // 跳转到学生端首页
    $router.push('/student')
  }
}
</script>

<style scoped>
.questionnaire-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  overflow-y: auto;
}

.questionnaire-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 40px;
  max-width: 600px;
  width: 100%;
  backdrop-filter: blur(10px);
}

.questionnaire-header {
  margin-bottom: 30px;
  text-align: center;
}

.questionnaire-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.questionnaire-header p {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.confirm-content {
  margin-bottom: 40px;
}

.certificate {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 30px;
}

.certificate h2 {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 16px 0;
  text-align: center;
}

.certificate p {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin: 0 0 12px 0;
  text-align: left;
}

.certificate p:last-child {
  margin-bottom: 24px;
}

.questionnaire-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 40px;
}

.questionnaire-actions .el-button {
  min-width: 120px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.questionnaire-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.questionnaire-actions .el-button--primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4391 100%);
}

@media (max-width: 768px) {
  .questionnaire-card {
    padding: 30px 20px;
  }
  
  .questionnaire-header h1 {
    font-size: 20px;
  }
  
  .certificate h2 {
    font-size: 16px;
  }
  
  .questionnaire-actions {
    flex-direction: column;
  }
  
  .questionnaire-actions .el-button {
    width: 100%;
  }
}
</style>