<template>
  <div class="questionnaire-container">
    <div class="questionnaire-card">
      <div class="questionnaire-header">
        <h1>MBTI性格测试</h1>
        <p>请回答以下问题，帮助我们了解您的性格特点，为您生成更精准的职业画像。</p>
      </div>
      <div class="questionnaire-progress">
        <el-progress :percentage="progress" :format="formatProgress" />
        <span class="progress-text">{{ currentQuestionIndex + 1 }} / {{ questions.length }}</span>
      </div>
      <div class="question-container">
        <h2 class="question-title">{{ questions[currentQuestionIndex].question }}</h2>
        <div class="options">
          <el-radio-group v-model="answers[currentQuestionIndex]">
            <el-radio v-for="(option, index) in questions[currentQuestionIndex].options" :key="index" :label="option.value">
              {{ option.text }}
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <div class="questionnaire-actions">
        <el-button v-if="currentQuestionIndex > 0" @click="handlePrevious">上一题</el-button>
        <el-button v-if="currentQuestionIndex < questions.length - 1" type="primary" @click="handleNext">下一题</el-button>
        <el-button v-else type="primary" @click="handleSubmit">提交</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const $router = useRouter()

const questions = ref([
  {
    question: '在社交场合中，你更倾向于：',
    options: [
      { text: '主动与他人交流，享受社交活动', value: 'E' },
      { text: '观察为主，喜欢安静的环境', value: 'I' }
    ]
  },
  {
    question: '你更注重：',
    options: [
      { text: '事实和具体信息', value: 'S' },
      { text: '可能性和抽象概念', value: 'N' }
    ]
  },
  {
    question: '做决策时，你更依赖：',
    options: [
      { text: '逻辑和客观分析', value: 'T' },
      { text: '情感和个人价值观', value: 'F' }
    ]
  },
  {
    question: '你更喜欢：',
    options: [
      { text: '有计划、有条理的生活', value: 'J' },
      { text: '灵活、即兴的生活方式', value: 'P' }
    ]
  },
  {
    question: '当你遇到问题时，你更倾向于：',
    options: [
      { text: '直接面对并解决', value: 'E' },
      { text: '思考后再行动', value: 'I' }
    ]
  },
  {
    question: '你对新信息的接受方式是：',
    options: [
      { text: '通过实际体验和观察', value: 'S' },
      { text: '通过想象和理论', value: 'N' }
    ]
  },
  {
    question: '在团队中，你更看重：',
    options: [
      { text: '效率和结果', value: 'T' },
      { text: '和谐和团队关系', value: 'F' }
    ]
  },
  {
    question: '你对时间的态度是：',
    options: [
      { text: '严格遵守计划', value: 'J' },
      { text: '灵活调整计划', value: 'P' }
    ]
  },
  {
    question: '你更倾向于：',
    options: [
      { text: '向外获取能量', value: 'E' },
      { text: '向内获取能量', value: 'I' }
    ]
  },
  {
    question: '你看待事物的方式是：',
    options: [
      { text: '注重细节和现实', value: 'S' },
      { text: '注重整体和未来', value: 'N' }
    ]
  },
  {
    question: '你做决定的方式是：',
    options: [
      { text: '理性分析', value: 'T' },
      { text: '感性判断', value: 'F' }
    ]
  },
  {
    question: '你对生活的态度是：',
    options: [
      { text: '结构化和有组织', value: 'J' },
      { text: '开放和灵活', value: 'P' }
    ]
  }
])

const currentQuestionIndex = ref(0)
const answers = ref<(string | null)[]>(Array(questions.value.length).fill(null))

const progress = computed(() => {
  return Math.round(((currentQuestionIndex.value + 1) / questions.value.length) * 100)
})

const formatProgress = (percentage: number) => {
  return `${percentage}%`
}

const handlePrevious = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const handleNext = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const handleSubmit = () => {
  // 计算MBTI结果
  const counts = {
    E: 0, I: 0,
    S: 0, N: 0,
    T: 0, F: 0,
    J: 0, P: 0
  }

  answers.value.forEach(answer => {
    if (answer) {
      counts[answer as keyof typeof counts]++
    }
  })

  const mbtiResult = [
    counts.E > counts.I ? 'E' : 'I',
    counts.S > counts.N ? 'S' : 'N',
    counts.T > counts.F ? 'T' : 'F',
    counts.J > counts.P ? 'J' : 'P'
  ].join('')

  // 保存MBTI结果到本地存储
  localStorage.setItem('mbtiQuestionnaire', JSON.stringify({
    answers: answers.value,
    result: mbtiResult
  }))

  // 跳转到确认页面
  $router.push('/student/questionnaire/confirm')
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
  max-width: 800px;
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

.questionnaire-progress {
  margin-bottom: 30px;
}

.progress-text {
  display: block;
  text-align: right;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.question-container {
  margin-bottom: 40px;
}

.question-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.questionnaire-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4391 100%);
}

@media (max-width: 768px) {
  .questionnaire-card {
    padding: 30px 20px;
  }
  
  .questionnaire-header h1 {
    font-size: 20px;
  }
  
  .question-title {
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