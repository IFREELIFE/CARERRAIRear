<template>
  <div class="interview-feedback-entry">
    <div class="page-header">
      <h1>面试反馈管理</h1>
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <div class="content-container">
      <!-- 左侧：面试反馈表单 -->
      <div class="form-section">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>面试结果登记</span>
              <el-badge v-if="isUpdated" type="success" value="已更新面试反馈" />
            </div>
          </template>
          
          <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="120px">
            <!-- 面试结果 -->
            <el-form-item label="面试结果" prop="result">
              <el-radio-group v-model="feedbackForm.result">
                <el-radio label="通过">通过</el-radio>
                <el-radio label="淘汰">淘汰</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 评价标签 -->
            <el-form-item label="评价标签" prop="tags" v-if="feedbackForm.result === '淘汰'">
              <div class="tags-container">
                <el-tag
                  v-for="tag in presetTags"
                  :key="tag"
                  :effect="feedbackForm.tags.includes(tag) ? 'dark' : 'plain'"
                  @click="toggleTag(tag)"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-model="newTag"
                  placeholder="输入自定义标签"
                  class="tag-input"
                  @keyup.enter="addCustomTag"
                >
                  <template #append>
                    <el-button @click="addCustomTag">添加</el-button>
                  </template>
                </el-input>
              </div>
              <div class="selected-tags">
                <el-tag
                  v-for="tag in feedbackForm.tags"
                  :key="tag"
                  type="info"
                  closable
                  @close="removeTag(tag)"
                  class="selected-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </el-form-item>
            
            <!-- 备注 -->
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="feedbackForm.remark"
                type="textarea"
                :rows="4"
                placeholder="请输入备注信息"
              />
            </el-form-item>
            
            <!-- 提交按钮 -->
            <el-form-item>
              <el-button type="primary" @click="submitFeedback" :loading="loading">提交反馈</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 面试反馈历史 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="card-header">
              <span>面试反馈历史</span>
            </div>
          </template>
          
          <el-table :data="feedbackHistory" style="width: 100%">
            <el-table-column prop="interviewTime" label="面试时间" width="180" />
            <el-table-column prop="interviewer" label="面试官" width="120" />
            <el-table-column prop="result" label="面试结果">
              <template #default="scope">
                <el-tag :type="scope.row.result === '通过' ? 'success' : 'danger'">
                  {{ scope.row.result }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="coreTags" label="核心标签" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="viewFeedback(scope.row)">查看</el-button>
                <el-button size="small" type="primary" @click="editFeedback(scope.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <!-- 右侧：差距清单 -->
      <div class="gap-section">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>差距清单</span>
              <el-badge v-if="isUpdated" type="success" value="已更新" />
            </div>
          </template>
          
          <div v-if="loadingGap" class="loading-container">
            <el-loading v-model="loadingGap" text="加载中..." />
          </div>
          <div v-else>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in gapList"
                :key="index"
                :timestamp="item.time"
                :type="item.type"
              >
                <el-card :body-style="{ padding: '10px' }">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.content }}</p>
                  <div v-if="item.changes" class="gap-changes">
                    <p class="change-title">变化：</p>
                    <ul>
                      <li v-for="(change, idx) in item.changes" :key="idx">{{ change }}</li>
                    </ul>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const loadingGap = ref(false)
const isUpdated = ref(false)
const feedbackFormRef = ref()

// 预设标签
const presetTags = [
  '八股文背诵痕迹重',
  '底层原理不懂',
  '项目经验不足',
  '沟通能力有待提高',
  '技术栈不符合要求',
  '缺乏团队协作经验',
  '学习能力不足',
  '问题解决能力弱'
]

// 表单数据
const feedbackForm = ref({
  result: '',
  tags: [] as string[],
  remark: ''
})

// 新标签输入
const newTag = ref('')

// 表单验证规则
const feedbackRules = {
  result: [{ required: true, message: '请选择面试结果', trigger: 'change' }],
  tags: [
    {
      required: true,
      message: '请选择评价标签',
      trigger: 'change',
      validator: (_rule: any, value: any, callback: any) => {
        if (feedbackForm.value.result === '淘汰' && value.length === 0) {
          callback(new Error('淘汰状态下必须选择评价标签'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 面试反馈历史（假数据）
const feedbackHistory = ref([
  {
    id: '1',
    interviewTime: '2024-03-10 14:00',
    interviewer: '张经理',
    result: '通过',
    coreTags: '技术基础扎实,沟通能力强',
    remark: '候选人技术基础扎实，沟通能力强，建议进入下一轮面试'
  },
  {
    id: '2',
    interviewTime: '2024-03-08 10:00',
    interviewer: '李主管',
    result: '淘汰',
    coreTags: '八股文背诵痕迹重,底层原理不懂',
    remark: '候选人对底层原理理解不够深入，建议加强基础知识学习'
  }
])

// 差距清单（假数据）
const gapList = ref([
  {
    time: '2024-03-10 14:30',
    title: '技术能力差距',
    content: '底层原理理解不够深入，需要加强计算机基础理论学习',
    type: 'warning',
    changes: [
      '新增：对Vue3源码理解不够深入',
      '修改：沟通能力从"良好"调整为"优秀"'
    ]
  },
  {
    time: '2024-03-08 10:30',
    title: '项目经验差距',
    content: '缺乏大型项目经验，需要积累更多实际项目经验',
    type: 'info'
  }
])

// 切换标签
const toggleTag = (tag: string) => {
  const index = feedbackForm.value.tags.indexOf(tag)
  if (index > -1) {
    feedbackForm.value.tags.splice(index, 1)
  } else {
    feedbackForm.value.tags.push(tag)
  }
}

// 添加自定义标签
const addCustomTag = () => {
  if (newTag.value && !feedbackForm.value.tags.includes(newTag.value)) {
    feedbackForm.value.tags.push(newTag.value)
    newTag.value = ''
  }
}

// 移除标签
const removeTag = (tag: string) => {
  const index = feedbackForm.value.tags.indexOf(tag)
  if (index > -1) {
    feedbackForm.value.tags.splice(index, 1)
  }
}

// 提交反馈
const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  feedbackFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 模拟成功响应
        ElMessage.success('面试反馈提交成功')
        
        // 更新状态
        isUpdated.value = true
        
        // 刷新差距清单
        await loadGapList()
        
        // 添加到历史记录
        const newFeedback = {
          id: Date.now().toString(),
          interviewTime: new Date().toLocaleString('zh-CN'),
          interviewer: '当前用户',
          result: feedbackForm.value.result,
          coreTags: feedbackForm.value.tags.join(','),
          remark: feedbackForm.value.remark
        }
        feedbackHistory.value.unshift(newFeedback)
        
        // 重置表单
        feedbackForm.value = {
          result: '',
          tags: [],
          remark: ''
        }
      } catch (error) {
        ElMessage.error('面试反馈提交失败')
        console.error('提交失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 加载差距清单
const loadGapList = async () => {
  try {
    loadingGap.value = true
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟更新后的差距清单
    gapList.value = [
      {
        time: new Date().toLocaleString('zh-CN'),
        title: '技术能力差距',
        content: '底层原理理解不够深入，需要加强计算机基础理论学习',
        type: 'warning',
        changes: [
          '新增：对Vue3源码理解不够深入',
          '修改：沟通能力从"良好"调整为"优秀"'
        ]
      },
      {
        time: '2024-03-08 10:30',
        title: '项目经验差距',
        content: '缺乏大型项目经验，需要积累更多实际项目经验',
        type: 'info'
      }
    ]
  } catch (error) {
    console.error('加载差距清单失败:', error)
  } finally {
    loadingGap.value = false
  }
}

// 查看反馈
const viewFeedback = (_feedback: any) => {
  // 这里可以实现查看详细反馈的逻辑
  ElMessage.info('查看反馈详情')
}

// 编辑反馈
const editFeedback = (_feedback: any) => {
  // 这里可以实现编辑反馈的逻辑
  ElMessage.info('编辑反馈')
}

// 返回列表
const goBack = () => {
  router.push('/company/job-candidate')
}

// 组件挂载时加载数据
onMounted(() => {
  loadGapList()
})
</script>

<style scoped lang="scss">
.interview-feedback-entry {
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
  h1 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
}

.content-container {
  display: flex;
  gap: 20px;
  .form-section {
    flex: 1;
  }
  .gap-section {
    width: 400px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s ease;
  &:hover {
    transform: translateY(-2px);
  }
}

.tag-input {
  width: 200px;
  margin-top: 10px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.selected-tag {
  margin-bottom: 5px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.gap-changes {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e8e8e8;
  .change-title {
    font-weight: 500;
    margin-bottom: 5px;
  }
  ul {
    margin: 0;
    padding-left: 20px;
    li {
      margin-bottom: 2px;
    }
  }
}

.mt-20 {
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .content-container {
    flex-direction: column;
  }
  .gap-section {
    width: 100%;
    margin-top: 20px;
  }
}
</style>
