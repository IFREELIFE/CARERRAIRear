<template>
  <div class="task-scheduler">
    <div class="page-header">
      <h1>任务调度中心</h1>
      <p>异步任务全流程管控，支持RabbitMQ任务监控和精准重试</p>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-card shadow="hover">
        <div class="action-buttons">
          <el-button type="primary" @click="handleBatchPause" :disabled="selectedTasks.length === 0">
            <el-icon><Timer /></el-icon>
            批量暂停
          </el-button>
          <el-button type="success" @click="handleBatchResume" :disabled="selectedTasks.length === 0">
            <el-icon><VideoPlay /></el-icon>
            批量恢复
          </el-button>
          <el-button type="danger" @click="handleBatchCancel" :disabled="selectedTasks.length === 0">
            <el-icon><Delete /></el-icon>
            批量取消
          </el-button>
          <el-button @click="refreshTasks">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="task-stats">
          <div class="stat-item">
            <span class="stat-label">总任务数</span>
            <span class="stat-value">{{ tasks.length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">执行中</span>
            <span class="stat-value status-running">{{ runningTasks }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">成功</span>
            <span class="stat-value status-success">{{ successTasks }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">失败</span>
            <span class="stat-value status-failed">{{ failedTasks }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">排队中</span>
            <span class="stat-value status-queued">{{ queuedTasks }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 任务列表 -->
    <div class="task-section">
      <el-card shadow="hover">
        <el-table
          v-loading="loading"
          :data="tasks"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="任务ID" width="180"></el-table-column>
          <el-table-column prop="jobCode" label="岗位编码" width="120"></el-table-column>
          <el-table-column prop="jobName" label="岗位名称" min-width="180"></el-table-column>
          <el-table-column label="执行状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="180">
            <template #default="scope">
              <div v-if="scope.row.status === 'running'" class="progress-container">
                <el-progress 
                  :percentage="scope.row.progress" 
                  :color="getProgressColor(scope.row.progress)"
                  :stroke-width="10"
                />
                <span class="progress-text">{{ scope.row.progress }}%</span>
              </div>
              <span v-else class="progress-text">
                {{ scope.row.status === 'success' ? '100%' : '0%' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180"></el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                @click="handleRetry(scope.row)"
                :disabled="scope.row.status !== 'failed'"
              >
                精准重试
              </el-button>
              <el-button 
                size="small" 
                @click="handleViewLog(scope.row)"
                :disabled="scope.row.status !== 'failed'"
              >
                查看日志
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="tasks.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 精准重试弹窗 -->
    <el-dialog
      v-model="retryDialogVisible"
      title="精准重试"
      width="500px"
    >
      <div v-if="selectedTask" class="retry-dialog">
        <p class="retry-task-info">
          任务ID: {{ selectedTask.id }} | 岗位编码: {{ selectedTask.jobCode }}
        </p>
        <p class="retry-field-title">请选择需要重试的字段：</p>
        <el-checkbox-group v-model="selectedFields">
          <el-checkbox label="jobName">岗位名称</el-checkbox>
          <el-checkbox label="companyName">公司名称</el-checkbox>
          <el-checkbox label="salary">薪资范围</el-checkbox>
          <el-checkbox label="location">工作地点</el-checkbox>
          <el-checkbox label="experience">工作经验</el-checkbox>
          <el-checkbox label="education">学历要求</el-checkbox>
          <el-checkbox label="description">岗位描述</el-checkbox>
          <el-checkbox label="requirements">任职要求</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="retryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRetry" :disabled="selectedFields.length === 0">确认重试</el-button>
      </template>
    </el-dialog>

    <!-- 日志查看弹窗 -->
    <el-dialog
      v-model="logDialogVisible"
      title="任务日志"
      width="800px"
      height="600px"
    >
      <div v-if="selectedTask" class="log-dialog">
        <p class="log-task-info">
          任务ID: {{ selectedTask.id }} | 岗位编码: {{ selectedTask.jobCode }}
        </p>
        <el-tabs type="border-card">
          <el-tab-pane label="错误信息">
            <div class="log-content error-log">
              {{ selectedTask.errorLog || '无错误信息' }}
            </div>
          </el-tab-pane>
          <el-tab-pane label="Prompt内容">
            <div class="log-content prompt-log">
              {{ selectedTask.promptLog || '无Prompt内容' }}
            </div>
          </el-tab-pane>
          <el-tab-pane label="执行详情">
            <div class="log-content detail-log">
              <pre>{{ JSON.stringify(selectedTask, null, 2) }}</pre>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Timer, VideoPlay, Delete, Refresh } from '@element-plus/icons-vue'

// 任务数据
const tasks = ref([
  {
    id: 'TASK-20260312-001',
    jobCode: 'JOB001',
    jobName: '前端开发工程师',
    status: 'running',
    progress: 65,
    createdAt: '2026-03-12 10:00:00',
    updatedAt: '2026-03-12 10:05:30',
    errorLog: '',
    promptLog: ''
  },
  {
    id: 'TASK-20260312-002',
    jobCode: 'JOB002',
    jobName: '后端开发工程师',
    status: 'success',
    progress: 100,
    createdAt: '2026-03-12 09:30:00',
    updatedAt: '2026-03-12 09:45:00',
    errorLog: '',
    promptLog: ''
  },
  {
    id: 'TASK-20260312-003',
    jobCode: 'JOB003',
    jobName: '产品经理',
    status: 'failed',
    progress: 0,
    createdAt: '2026-03-12 09:00:00',
    updatedAt: '2026-03-12 09:10:00',
    errorLog: 'LLM调用超时：API请求超过30秒未响应',
    promptLog: '请从以下岗位描述中提取结构化信息：\n\n招聘产品经理，要求有3-5年产品规划经验，熟悉用户体验设计，本科及以上学历，薪资18k-28k，工作地点深圳。'
  },
  {
    id: 'TASK-20260312-004',
    jobCode: 'JOB004',
    jobName: 'UI设计师',
    status: 'queued',
    progress: 0,
    createdAt: '2026-03-12 08:30:00',
    updatedAt: '2026-03-12 08:30:00',
    errorLog: '',
    promptLog: ''
  },
  {
    id: 'TASK-20260312-005',
    jobCode: 'JOB005',
    jobName: '数据分析师',
    status: 'failed',
    progress: 0,
    createdAt: '2026-03-12 08:00:00',
    updatedAt: '2026-03-12 08:05:00',
    errorLog: '字段提取失败：无法识别薪资范围格式',
    promptLog: '请从以下岗位描述中提取结构化信息：\n\n招聘数据分析师，要求熟悉SQL、Python，有3-5年工作经验，本科及以上学历，薪资15k-25k，工作地点杭州。'
  },
  {
    id: 'TASK-20260312-006',
    jobCode: 'JOB006',
    jobName: '测试工程师',
    status: 'running',
    progress: 35,
    createdAt: '2026-03-12 07:30:00',
    updatedAt: '2026-03-12 07:40:00',
    errorLog: '',
    promptLog: ''
  },
  {
    id: 'TASK-20260312-007',
    jobCode: 'JOB007',
    jobName: '运维工程师',
    status: 'success',
    progress: 100,
    createdAt: '2026-03-12 07:00:00',
    updatedAt: '2026-03-12 07:15:00',
    errorLog: '',
    promptLog: ''
  },
  {
    id: 'TASK-20260312-008',
    jobCode: 'JOB008',
    jobName: '销售经理',
    status: 'queued',
    progress: 0,
    createdAt: '2026-03-12 06:30:00',
    updatedAt: '2026-03-12 06:30:00',
    errorLog: '',
    promptLog: ''
  }
])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 选中的任务
const selectedTasks = ref<any[]>([])

// 精准重试相关
const retryDialogVisible = ref(false)
const selectedTask = ref<any>(null)
const selectedFields = ref<string[]>([])

// 日志查看相关
const logDialogVisible = ref(false)

// 任务统计
const runningTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'running').length
})

const successTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'success').length
})

const failedTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'failed').length
})

const queuedTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'queued').length
})

// 获取状态标签类型
const getStatusType = (status: string) => {
  if (status === 'queued') return 'info'
  if (status === 'running') return 'warning'
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  return ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  if (status === 'queued') return '排队中'
  if (status === 'running') return '执行中'
  if (status === 'success') return '成功'
  if (status === 'failed') return '失败'
  return ''
}

// 获取进度条颜色
const getProgressColor = (progress: number) => {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

// 处理选择变化
const handleSelectionChange = (val: any[]) => {
  selectedTasks.value = val
}

// 批量暂停
const handleBatchPause = () => {
  ElMessage.success(`已暂停 ${selectedTasks.value.length} 个任务`)
  // 这里可以添加实际的暂停逻辑
}

// 批量恢复
const handleBatchResume = () => {
  ElMessage.success(`已恢复 ${selectedTasks.value.length} 个任务`)
  // 这里可以添加实际的恢复逻辑
}

// 批量取消
const handleBatchCancel = () => {
  ElMessage.success(`已取消 ${selectedTasks.value.length} 个任务`)
  // 这里可以添加实际的取消逻辑
}

// 刷新任务
const refreshTasks = () => {
  loading.value = true
  // 模拟数据刷新
  setTimeout(() => {
    // 随机更新执行中任务的进度
    tasks.value.forEach(task => {
      if (task.status === 'running') {
        task.progress = Math.min(100, task.progress + Math.floor(Math.random() * 10))
        if (task.progress === 100) {
          task.status = 'success'
        }
        task.updatedAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
    })
    loading.value = false
    ElMessage.success('任务列表已刷新')
  }, 500)
}

// 精准重试
const handleRetry = (task: any) => {
  selectedTask.value = task
  selectedFields.value = []
  retryDialogVisible.value = true
}

// 确认重试
const confirmRetry = () => {
  if (selectedTask.value && selectedFields.value.length > 0) {
    ElMessage.success(`已提交精准重试请求，将重试以下字段：${selectedFields.value.join(', ')}`)
    // 这里可以添加实际的重试逻辑
    retryDialogVisible.value = false
  }
}

// 查看日志
const handleViewLog = (task: any) => {
  selectedTask.value = task
  logDialogVisible.value = true
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
}

// 模拟实时更新
onMounted(() => {
  // 模拟SSE监听任务状态变化
  setInterval(() => {
    tasks.value.forEach(task => {
      if (task.status === 'running') {
        task.progress = Math.min(100, task.progress + 1)
        if (task.progress === 100) {
          task.status = 'success'
        }
        task.updatedAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
    })
  }, 5000)
})
</script>

<style scoped>
.task-scheduler {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.page-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.action-section {
  margin-bottom: 24px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.task-stats {
  display: flex;
  gap: 32px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.status-running {
  color: #e6a23c;
}

.status-success {
  color: #67c23a;
}

.status-failed {
  color: #f56c6c;
}

.status-queued {
  color: #409eff;
}

.task-section {
  margin-bottom: 24px;
}

.progress-container {
  position: relative;
  padding-right: 50px;
}

.progress-text {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #666;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.retry-dialog {
  padding: 20px 0;
}

.retry-task-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.retry-field-title {
  margin-bottom: 16px;
  font-weight: 500;
}

.log-dialog {
  padding: 20px 0;
}

.log-task-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.log-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.error-log {
  color: #f56c6c;
  white-space: pre-wrap;
}

.prompt-log {
  white-space: pre-wrap;
}

.detail-log pre {
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
}
</style>