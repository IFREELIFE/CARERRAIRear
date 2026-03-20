<template>
  <div class="intelligent-resume">
    <div class="page-header">
      <h1>智能简历筛选</h1>
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-loading v-model="loading" text="加载中..." />
    </div>
    
    <div v-else class="resume-content">
      <!-- 左侧：原始简历 -->
      <div class="resume-original">
        <div class="resume-header">
          <h2>原始简历</h2>
          <el-checkbox v-model="showKeywords" @change="toggleKeywords">关键词高亮</el-checkbox>
        </div>
        <div class="resume-body" ref="resumeBody">
          <div v-html="highlightedResume" class="rich-text-content"></div>
        </div>
      </div>
      
      <!-- 右侧：AI 标签 -->
      <div class="resume-tags">
        <div class="tags-header">
          <h2>核心能力标签</h2>
          <el-radio-group v-model="tagFilter" @change="filterTags">
            <el-radio label="all">全部</el-radio>
            <el-radio label="positive">正向</el-radio>
            <el-radio label="negative">负向</el-radio>
          </el-radio-group>
        </div>
        <div class="tags-body">
          <el-tag
            v-for="tag in filteredTags"
            :key="tag.id"
            :type="tag.type === 'positive' ? 'success' : 'danger'"
            :effect="'dark'"
            class="tag-item"
            :title="`置信度: ${tag.confidence}%`"
            @click="highlightTag(tag)"
          >
            {{ tag.content }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getResumeDetail } from '../../api/company'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const showKeywords = ref(true)
const tagFilter = ref('all')
const resumeBody = ref<HTMLElement>()

const candidateId = computed(() => route.params.candidateId as string)

// 简历数据
const resumeData = ref({
  id: '',
  originalContent: '',
  tags: [] as Array<{
    id: string
    type: 'positive' | 'negative'
    content: string
    confidence: number
  }>
})

// 高亮后的简历内容
const highlightedResume = computed(() => {
  if (!showKeywords.value) return resumeData.value.originalContent
  
  let content = resumeData.value.originalContent
  resumeData.value.tags.forEach(tag => {
    const regex = new RegExp(`(${tag.content})`, 'gi')
    content = content.replace(regex, '<span class="highlight">$1</span>')
  })
  return content
})

// 筛选后的标签
const filteredTags = computed(() => {
  if (tagFilter.value === 'all') return resumeData.value.tags
  return resumeData.value.tags.filter(tag => tag.type === tagFilter.value)
})

// 加载简历数据
const loadResumeData = async () => {
  try {
    loading.value = true
    // 使用假数据模拟API响应
    const mockData = {
      id: candidateId.value,
      originalContent: `
        <h1>张三</h1>
        <h2>个人信息</h2>
        <p>性别：男</p>
        <p>年龄：22岁</p>
        <p>学历：本科</p>
        <p>专业：计算机科学与技术</p>
        <p>毕业院校：北京大学</p>
        
        <h2>技能</h2>
        <ul>
          <li>Vue3 熟练</li>
          <li>React 熟悉</li>
          <li>JavaScript/TypeScript 精通</li>
          <li>Node.js 熟悉</li>
          <li>MySQL 熟悉</li>
        </ul>
        
        <h2>项目经验</h2>
        <h3>项目1：在线商城系统</h3>
        <p>负责前端开发，使用Vue3 + TypeScript + Element Plus构建</p>
        <p>实现了商品列表、购物车、订单管理等功能</p>
        
        <h3>项目2：企业管理系统</h3>
        <p>负责前端开发，使用React + Ant Design构建</p>
        <p>实现了用户管理、权限控制、数据统计等功能</p>
        
        <h2>实习经验</h2>
        <p>无大厂实习经验</p>
        
        <h2>自我评价</h2>
        <p>学习能力强，团队协作能力良好，对新技术有强烈的探索欲望</p>
      `,
      tags: [
        { id: '1', type: 'positive' as const, content: 'Vue3 熟练', confidence: 95 },
        { id: '2', type: 'positive' as const, content: 'TypeScript 精通', confidence: 90 },
        { id: '3', type: 'positive' as const, content: '学习能力强', confidence: 85 },
        { id: '4', type: 'positive' as const, content: '团队协作能力良好', confidence: 80 },
        { id: '5', type: 'negative' as const, content: '缺乏大厂实习', confidence: 95 },
        { id: '6', type: 'positive' as const, content: '计算机科学与技术专业', confidence: 100 },
        { id: '7', type: 'positive' as const, content: '北京大学毕业', confidence: 100 },
        { id: '8', type: 'positive' as const, content: 'React 熟悉', confidence: 85 },
        { id: '9', type: 'positive' as const, content: 'Node.js 熟悉', confidence: 80 },
        { id: '10', type: 'positive' as const, content: 'MySQL 熟悉', confidence: 75 },
      ]
    }
    
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    
    resumeData.value = mockData
  } catch (error) {
    console.error('加载简历失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换关键词高亮
const toggleKeywords = () => {
  // 高亮逻辑在 computed 中处理
}

// 筛选标签
const filterTags = () => {
  // 筛选逻辑在 computed 中处理
}

// 高亮标签对应的内容
const highlightTag = (tag: { content: string }) => {
  if (!resumeBody.value) return
  
  const regex = new RegExp(`(${tag.content})`, 'gi')
  const content = resumeBody.value.innerHTML
  const newContent = content.replace(regex, '<span class="tag-highlight">$1</span>')
  resumeBody.value.innerHTML = newContent
  
  // 滚动到第一个高亮位置
  const highlightElement = resumeBody.value.querySelector('.tag-highlight')
  if (highlightElement) {
    highlightElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
  
  // 3秒后移除高亮
  setTimeout(() => {
    if (resumeBody.value) {
      const highlightedElements = resumeBody.value.querySelectorAll('.tag-highlight')
      highlightedElements.forEach(el => {
        el.classList.remove('tag-highlight')
        el.classList.add('highlight')
      })
    }
  }, 3000)
}

// 返回列表
const goBack = () => {
  router.push('/company/job-candidate')
}

// 监听路由参数变化
watch(() => route.params.candidateId, () => {
  loadResumeData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadResumeData()
})
</script>

<style scoped>
.intelligent-resume {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8e8e8;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}

.resume-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 120px);
}

.resume-original {
  flex: 1;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.resume-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fafafa;
}

.resume-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.resume-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  line-height: 1.6;
}

.rich-text-content {
  font-size: 14px;
  color: #333;
}

.rich-text-content h1, .rich-text-content h2, .rich-text-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #333;
}

.rich-text-content p {
  margin-bottom: 10px;
}

.rich-text-content ul, .rich-text-content ol {
  margin-left: 20px;
  margin-bottom: 10px;
}

.resume-tags {
  width: 350px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tags-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #fafafa;
}

.tags-header h2 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.tags-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.highlight {
  background-color: #fff3cd;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 500;
}

.tag-highlight {
  background-color: #d4edda;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 600;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(72, 187, 120, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(72, 187, 120, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(72, 187, 120, 0);
  }
}

@media (max-width: 1200px) {
  .resume-content {
    flex-direction: column;
    height: auto;
  }
  
  .resume-tags {
    width: 100%;
    margin-top: 20px;
  }
  
  .tags-body {
    max-height: 300px;
  }
}
</style>