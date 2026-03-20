<template>
  <div class="content-audit">
    <div class="page-header">
      <h1>内容审核发布</h1>
      <p>审核AI提取的岗位信息，确保数据准确性</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-card shadow="hover">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="置信度区间">
            <el-select v-model="filterForm.confidenceRange" placeholder="选择置信度区间">
              <el-option label="全部" value="all"></el-option>
              <el-option label="< 80%" value="low"></el-option>
              <el-option label="80%-95%" value="medium"></el-option>
              <el-option label="≥ 95%" value="high"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="filterForm.status" placeholder="选择审核状态">
              <el-option label="全部" value="all"></el-option>
              <el-option label="待审核" value="pending"></el-option>
              <el-option label="已通过" value="approved"></el-option>
              <el-option label="已驳回" value="rejected"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="岗位编码/名称">
            <el-input v-model="filterForm.keyword" placeholder="输入岗位编码或名称" style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 数据列表 -->
    <div class="data-section">
      <el-card shadow="hover">
        <el-table
          :data="filteredJobs"
          style="width: 100%"
          @row-click="handleRowClick"
          v-loading="loading"
        >
          <el-table-column prop="jobCode" label="岗位编码" width="120"></el-table-column>
          <el-table-column prop="jobName" label="岗位名称" min-width="180"></el-table-column>
          <el-table-column prop="companyName" label="公司名称" min-width="150"></el-table-column>
          <el-table-column label="置信度" width="120">
            <template #default="scope">
              <el-tag :type="getConfidenceType(scope.row.confidence)">
                {{ scope.row.confidence }}%
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="handleApprove(scope.row)"
                :disabled="!isAdmin || scope.row.status !== 'pending'"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click.stop="handleReject(scope.row)"
                :disabled="scope.row.status !== 'pending'"
              >
                驳回
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
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="岗位详情审核"
      size="80%"
    >
      <div v-if="selectedJob" class="job-detail">
        <div class="detail-header">
          <h3>{{ selectedJob.jobName }}</h3>
          <p>{{ selectedJob.companyName }} | {{ selectedJob.jobCode }}</p>
          <el-tag :type="getConfidenceType(selectedJob.confidence)">
            置信度: {{ selectedJob.confidence }}%
          </el-tag>
        </div>

        <div class="detail-content">
          <el-tabs type="border-card">
            <el-tab-pane label="AI提取的结构化字段">
              <el-form :model="selectedJob" label-width="120px">
                <el-form-item label="岗位名称">
                  <el-input v-model="selectedJob.jobName" disabled></el-input>
                </el-form-item>
                <el-form-item label="公司名称">
                  <el-input v-model="selectedJob.companyName" disabled></el-input>
                </el-form-item>
                <el-form-item label="岗位编码">
                  <el-input v-model="selectedJob.jobCode" disabled></el-input>
                </el-form-item>
                <el-form-item label="薪资范围">
                  <el-input v-model="selectedJob.salary" disabled></el-input>
                </el-form-item>
                <el-form-item label="工作地点">
                  <el-input v-model="selectedJob.location" disabled></el-input>
                </el-form-item>
                <el-form-item label="工作经验">
                  <el-input v-model="selectedJob.experience" disabled></el-input>
                </el-form-item>
                <el-form-item label="学历要求">
                  <el-input v-model="selectedJob.education" disabled></el-input>
                </el-form-item>
                <el-form-item label="岗位描述">
                  <el-input type="textarea" :rows="5" v-model="selectedJob.description" disabled></el-input>
                </el-form-item>
                <el-form-item label="任职要求">
                  <el-input type="textarea" :rows="5" v-model="selectedJob.requirements" disabled></el-input>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="原始岗位描述">
              <div class="original-description">
                {{ selectedJob.originalDescription }}
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <div class="detail-footer">
          <el-button @click="drawerVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleApprove(selectedJob)"
            :disabled="!isAdmin || selectedJob.status !== 'pending'"
          >
            通过
          </el-button>
          <el-button 
            type="danger" 
            @click="handleReject(selectedJob)"
            :disabled="selectedJob.status !== 'pending'"
          >
            驳回
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 驳回对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回原因"
      width="400px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input type="textarea" :rows="4" v-model="rejectForm.reason" placeholder="请填写驳回原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 筛选表单
const filterForm = ref({
  confidenceRange: 'all',
  status: 'all',
  keyword: ''
})

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)
const loading = ref(false)

// 抽屉状态
const drawerVisible = ref(false)
const selectedJob = ref<any>(null)

// 驳回对话框
const rejectDialogVisible = ref(false)
const rejectForm = ref({
  reason: ''
})
const jobToReject = ref<any>(null)

// 权限控制
const isAdmin = ref(true) // 模拟管理员权限

// 假数据
const jobs = ref([
  {
    id: 1,
    jobCode: 'JOB001',
    jobName: '前端开发工程师',
    companyName: '科技有限公司',
    confidence: 75,
    status: 'pending',
    salary: '15k-25k',
    location: '北京',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责公司前端开发工作，使用Vue3框架',
    requirements: '熟悉Vue3、TypeScript，有大型项目经验',
    originalDescription: '招聘前端开发工程师，要求熟悉Vue3、TypeScript，有3-5年工作经验，本科及以上学历，薪资15k-25k，工作地点北京。',
    createdAt: '2026-03-10 10:00:00'
  },
  {
    id: 2,
    jobCode: 'JOB002',
    jobName: '后端开发工程师',
    companyName: '互联网公司',
    confidence: 92,
    status: 'pending',
    salary: '20k-30k',
    location: '上海',
    experience: '5-10年',
    education: '本科及以上',
    description: '负责公司后端系统开发，使用Java技术栈',
    requirements: '熟悉Java、Spring Boot，有分布式系统经验',
    originalDescription: '招聘后端开发工程师，要求熟悉Java、Spring Boot，有5-10年工作经验，本科及以上学历，薪资20k-30k，工作地点上海。',
    createdAt: '2026-03-10 09:30:00'
  },
  {
    id: 3,
    jobCode: 'JOB003',
    jobName: '产品经理',
    companyName: '科技公司',
    confidence: 98,
    status: 'pending',
    salary: '18k-28k',
    location: '深圳',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责产品规划和设计，协调研发团队',
    requirements: '有产品规划经验，熟悉用户体验设计',
    originalDescription: '招聘产品经理，要求有3-5年产品规划经验，熟悉用户体验设计，本科及以上学历，薪资18k-28k，工作地点深圳。',
    createdAt: '2026-03-10 09:00:00'
  },
  {
    id: 4,
    jobCode: 'JOB004',
    jobName: 'UI设计师',
    companyName: '设计公司',
    confidence: 85,
    status: 'pending',
    salary: '12k-20k',
    location: '广州',
    experience: '2-4年',
    education: '大专及以上',
    description: '负责公司UI设计工作，包括界面设计和交互设计',
    requirements: '熟悉Photoshop、Sketch，有良好的设计感',
    originalDescription: '招聘UI设计师，要求熟悉Photoshop、Sketch，有2-4年工作经验，大专及以上学历，薪资12k-20k，工作地点广州。',
    createdAt: '2026-03-09 16:00:00'
  },
  {
    id: 5,
    jobCode: 'JOB005',
    jobName: '数据分析师',
    companyName: '金融科技公司',
    confidence: 78,
    status: 'pending',
    salary: '15k-25k',
    location: '杭州',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责公司数据分析工作，提供数据洞察',
    requirements: '熟悉SQL、Python，有数据分析经验',
    originalDescription: '招聘数据分析师，要求熟悉SQL、Python，有3-5年工作经验，本科及以上学历，薪资15k-25k，工作地点杭州。',
    createdAt: '2026-03-09 14:30:00'
  },
  {
    id: 6,
    jobCode: 'JOB006',
    jobName: '测试工程师',
    companyName: '软件公司',
    confidence: 90,
    status: 'pending',
    salary: '12k-20k',
    location: '成都',
    experience: '2-4年',
    education: '本科及以上',
    description: '负责公司软件测试工作，保证产品质量',
    requirements: '熟悉测试方法，有自动化测试经验',
    originalDescription: '招聘测试工程师，要求熟悉测试方法，有2-4年工作经验，本科及以上学历，薪资12k-20k，工作地点成都。',
    createdAt: '2026-03-09 11:00:00'
  },
  {
    id: 7,
    jobCode: 'JOB007',
    jobName: '运维工程师',
    companyName: '云计算公司',
    confidence: 95,
    status: 'approved',
    salary: '15k-25k',
    location: '北京',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责公司服务器运维工作，保障系统稳定',
    requirements: '熟悉Linux、Docker，有云平台经验',
    originalDescription: '招聘运维工程师，要求熟悉Linux、Docker，有3-5年工作经验，本科及以上学历，薪资15k-25k，工作地点北京。',
    createdAt: '2026-03-08 16:00:00'
  },
  {
    id: 8,
    jobCode: 'JOB008',
    jobName: '销售经理',
    companyName: '科技公司',
    confidence: 82,
    status: 'rejected',
    salary: '10k-20k+提成',
    location: '上海',
    experience: '2-4年',
    education: '大专及以上',
    description: '负责公司产品销售，开拓市场',
    requirements: '有销售经验，良好的沟通能力',
    originalDescription: '招聘销售经理，要求有2-4年销售经验，良好的沟通能力，大专及以上学历，薪资10k-20k+提成，工作地点上海。',
    createdAt: '2026-03-08 14:00:00'
  }
])

// 过滤后的数据
const filteredJobs = computed(() => {
  let result = [...jobs.value]
  
  // 按置信度排序（低置信度优先）
  result.sort((a, b) => a.confidence - b.confidence)
  
  // 按置信度区间筛选
  if (filterForm.value.confidenceRange !== 'all') {
    result = result.filter(job => {
      if (filterForm.value.confidenceRange === 'low') {
        return job.confidence < 80
      } else if (filterForm.value.confidenceRange === 'medium') {
        return job.confidence >= 80 && job.confidence < 95
      } else if (filterForm.value.confidenceRange === 'high') {
        return job.confidence >= 95
      }
      return true
    })
  }
  
  // 按审核状态筛选
  if (filterForm.value.status !== 'all') {
    result = result.filter(job => job.status === filterForm.value.status)
  }
  
  // 按关键词筛选
  if (filterForm.value.keyword) {
    const keyword = filterForm.value.keyword.toLowerCase()
    result = result.filter(job => 
      job.jobCode.toLowerCase().includes(keyword) || 
      job.jobName.toLowerCase().includes(keyword)
    )
  }
  
  // 分页
  total.value = result.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 获取置信度标签类型
const getConfidenceType = (confidence: number) => {
  if (confidence < 80) return 'danger'
  if (confidence < 95) return 'warning'
  return 'success'
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  if (status === 'pending') return 'info'
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  return ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  if (status === 'pending') return '待审核'
  if (status === 'approved') return '已通过'
  if (status === 'rejected') return '已驳回'
  return ''
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    confidenceRange: 'all',
    status: 'all',
    keyword: ''
  }
  currentPage.value = 1
}

// 处理行点击
const handleRowClick = (row: any) => {
  selectedJob.value = { ...row }
  drawerVisible.value = true
}

// 处理通过
const handleApprove = (job: any) => {
  const index = jobs.value.findIndex(item => item.id === job.id)
  if (index !== -1 && jobs.value[index]) {
    jobs.value[index].status = 'approved'
    ElMessage.success('审核通过')
    drawerVisible.value = false
  }
}

// 处理驳回
const handleReject = (job: any) => {
  jobToReject.value = job
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

// 确认驳回
const confirmReject = () => {
  if (!rejectForm.value.reason) {
    ElMessage.error('请填写驳回原因')
    return
  }
  
  if (jobToReject.value) {
    const index = jobs.value.findIndex(item => item.id === jobToReject.value.id)
    if (index !== -1 && jobs.value[index]) {
      jobs.value[index].status = 'rejected'
      ElMessage.success('已驳回')
      rejectDialogVisible.value = false
      drawerVisible.value = false
    }
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
}

// 生命周期
onMounted(() => {
  // 模拟数据加载
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
.content-audit {
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

.filter-section {
  margin-bottom: 24px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.data-section {
  margin-bottom: 24px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.job-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.detail-header p {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
}

.detail-content {
  margin-bottom: 24px;
}

.original-description {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.5;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}
</style>