<template>
  <div class="model-quality">
    <div class="page-header">
      <h1>模型质量优化</h1>
      <p>AI抽取质量分析与错题本闭环管理</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-card shadow="hover">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="Prompt版本">
            <el-select
              v-model="filterForm.promptVersions"
              multiple
              placeholder="选择Prompt版本"
              style="width: 240px;"
            >
              <el-option label="v1.0" value="v1.0"></el-option>
              <el-option label="v1.1" value="v1.1"></el-option>
              <el-option label="v1.2" value="v1.2"></el-option>
              <el-option label="v2.0" value="v2.0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px;"
            />
          </el-form-item>
          <el-form-item label="业务字段">
            <el-select
              v-model="filterForm.businessFields"
              multiple
              placeholder="选择业务字段"
              style="width: 240px;"
            >
              <el-option label="学历" value="education"></el-option>
              <el-option label="技能" value="skills"></el-option>
              <el-option label="工作经验" value="experience"></el-option>
              <el-option label="薪资范围" value="salary"></el-option>
              <el-option label="工作地点" value="location"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery" :loading="loading">
              查询
            </el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 标签页切换 -->
    <div class="content-section">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 抽取质量报告 -->
        <el-tab-pane label="抽取质量报告" name="quality">
          <div class="quality-report">
            <!-- 图表区域 -->
            <div class="chart-section">
              <el-row :gutter="20">
                <el-col :span="16">
                  <el-card shadow="hover">
                    <template #header>
                      <div class="card-header">
                        <span>多版本Prompt准确率对比</span>
                        <el-button-group>
                          <el-button 
                            size="small" 
                            :type="chartType === 'bar' ? 'primary' : ''"
                            @click="chartType = 'bar'"
                          >
                            柱状图
                          </el-button>
                          <el-button 
                            size="small" 
                            :type="chartType === 'radar' ? 'primary' : ''"
                            @click="chartType = 'radar'"
                          >
                            雷达图
                          </el-button>
                        </el-button-group>
                      </div>
                    </template>
                    <div ref="mainChartRef" class="chart-container"></div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover">
                    <template #header>
                      <span>最易出错字段 TOP5</span>
                    </template>
                    <div class="error-top-list">
                      <div 
                        v-for="(item, index) in errorTopList" 
                        :key="index"
                        class="error-item"
                        @click="handleFieldClick(item)"
                      >
                        <div class="error-rank">{{ index + 1 }}</div>
                        <div class="error-info">
                          <div class="error-name">{{ item.field }}</div>
                          <div class="error-rate">错误率: {{ item.errorRate }}%</div>
                        </div>
                        <el-progress 
                          :percentage="item.errorRate" 
                          :color="getErrorColor(item.errorRate)"
                          :show-text="false"
                        />
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <!-- 字段详情 -->
            <div v-if="selectedField" class="field-detail">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>字段错误详情 - {{ selectedField.field }}</span>
                    <el-button size="small" @click="selectedField = null">关闭</el-button>
                  </div>
                </template>
                <div class="detail-content">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <div class="detail-item">
                        <div class="detail-label">错误率</div>
                        <div class="detail-value">{{ selectedField.errorRate }}%</div>
                      </div>
                    </el-col>
                    <el-col :span="12">
                      <div class="detail-item">
                        <div class="detail-label">错误案例数</div>
                        <div class="detail-value">{{ selectedField.errorCount }}</div>
                      </div>
                    </el-col>
                  </el-row>
                  <div class="error-cases">
                    <div class="cases-title">典型错误案例</div>
                    <div 
                      v-for="(caseItem, index) in selectedField.cases" 
                      :key="index"
                      class="case-item"
                    >
                      <div class="case-header">
                        <span class="case-id">案例 #{{ (index as number) + 1 }}</span>
                        <span class="case-job">岗位: {{ caseItem.jobCode }}</span>
                      </div>
                      <div class="case-content">
                        <div class="case-row">
                          <span class="case-label">AI提取结果:</span>
                          <span class="case-value error">{{ caseItem.aiResult }}</span>
                        </div>
                        <div class="case-row">
                          <span class="case-label">正确结果:</span>
                          <span class="case-value correct">{{ caseItem.correctResult }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 导出按钮 -->
            <div class="export-section">
              <el-button type="primary" @click="handleExportReport">
                <el-icon><Download /></el-icon>
                导出质量报告
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 错题本 -->
        <el-tab-pane label="错题本" name="errorBook">
          <div class="error-book">
            <!-- 操作栏 -->
            <div class="action-bar">
              <div class="left-actions">
                <el-select
                  v-model="errorFilter.field"
                  placeholder="错误字段"
                  style="width: 150px;"
                  clearable
                >
                  <el-option label="学历" value="education"></el-option>
                  <el-option label="技能" value="skills"></el-option>
                  <el-option label="工作经验" value="experience"></el-option>
                  <el-option label="薪资范围" value="salary"></el-option>
                </el-select>
                <el-select
                  v-model="errorFilter.status"
                  placeholder="处理状态"
                  style="width: 150px;"
                  clearable
                >
                  <el-option label="待处理" value="pending"></el-option>
                  <el-option label="处理中" value="processing"></el-option>
                  <el-option label="已完成" value="completed"></el-option>
                </el-select>
              </div>
              <div class="right-actions">
                <el-button 
                  type="primary" 
                  @click="handleBatchCorrect"
                  :disabled="selectedErrors.length === 0"
                  v-if="isAdmin"
                >
                  批量纠偏
                </el-button>
                <el-button @click="handleExportErrors">
                  <el-icon><Download /></el-icon>
                  导出错题
                </el-button>
              </div>
            </div>

            <!-- 错题列表 -->
            <el-table
              v-loading="errorLoading"
              :data="filteredErrors"
              style="width: 100%"
              @selection-change="handleErrorSelection"
            >
              <el-table-column type="selection" width="55"></el-table-column>
              <el-table-column prop="jobCode" label="岗位编码" width="120"></el-table-column>
              <el-table-column prop="jobName" label="岗位名称" min-width="150"></el-table-column>
              <el-table-column prop="errorField" label="错误字段" width="120">
                <template #default="scope">
                  <el-tag type="danger">{{ getFieldText(scope.row.errorField) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="aiResult" label="AI提取结果" min-width="150">
                <template #default="scope">
                  <span class="error-text">{{ scope.row.aiResult }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="correctResult" label="正确结果" min-width="150">
                <template #default="scope">
                  <span class="correct-text">{{ scope.row.correctResult || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="promptVersion" label="Prompt版本" width="120"></el-table-column>
              <el-table-column prop="status" label="处理状态" width="120">
                <template #default="scope">
                  <el-tag :type="getErrorStatusType(scope.row.status)">
                    {{ getErrorStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="handleCorrect(scope.row)"
                    :disabled="scope.row.status !== 'pending'"
                    v-if="isAdmin"
                  >
                    纠偏
                  </el-button>
                  <el-button 
                    size="small" 
                    @click="handleViewErrorDetail(scope.row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="errorCurrentPage"
                v-model:page-size="errorPageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="errorTotal"
                @size-change="handleErrorSizeChange"
                @current-change="handleErrorCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 纠偏弹窗 -->
    <el-dialog
      v-model="correctDialogVisible"
      title="人工纠偏"
      width="600px"
    >
      <div v-if="selectedError" class="correct-dialog">
        <el-form :model="correctForm" label-width="100px">
          <el-form-item label="岗位编码">
            <el-input v-model="selectedError.jobCode" disabled></el-input>
          </el-form-item>
          <el-form-item label="错误字段" required>
            <el-select v-model="correctForm.errorField" placeholder="选择错误字段">
              <el-option label="学历" value="education"></el-option>
              <el-option label="技能" value="skills"></el-option>
              <el-option label="工作经验" value="experience"></el-option>
              <el-option label="薪资范围" value="salary"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="AI提取结果">
            <el-input v-model="selectedError.aiResult" disabled></el-input>
          </el-form-item>
          <el-form-item label="正确结果">
            <el-input v-model="correctForm.correctResult" placeholder="请输入正确结果"></el-input>
          </el-form-item>
          <el-form-item label="纠偏备注" required>
            <el-input
              v-model="correctForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请填写纠偏备注，如：注意区分了解和精通"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="correctDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCorrect" :loading="correctLoading">
          提交纠偏
        </el-button>
      </template>
    </el-dialog>

    <!-- 错误详情弹窗 -->
    <el-dialog
      v-model="errorDetailVisible"
      title="错误详情"
      width="600px"
    >
      <div v-if="selectedError" class="error-detail-dialog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="岗位编码">{{ selectedError.jobCode }}</el-descriptions-item>
          <el-descriptions-item label="岗位名称">{{ selectedError.jobName }}</el-descriptions-item>
          <el-descriptions-item label="错误字段">{{ getFieldText(selectedError.errorField) }}</el-descriptions-item>
          <el-descriptions-item label="Prompt版本">{{ selectedError.promptVersion }}</el-descriptions-item>
          <el-descriptions-item label="AI提取结果" :span="2">
            <span class="error-text">{{ selectedError.aiResult }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="正确结果" :span="2">
            <span class="correct-text">{{ selectedError.correctResult || '未填写' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getErrorStatusType(selectedError.status)">
              {{ getErrorStatusText(selectedError.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedError.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="纠偏备注" :span="2">
            {{ selectedError.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 筛选表单
const filterForm = ref({
  promptVersions: ['v1.0', 'v1.1', 'v2.0'],
  dateRange: [],
  businessFields: ['education', 'skills', 'experience', 'salary', 'location']
})

// 加载状态
const loading = ref(false)
const errorLoading = ref(false)
const correctLoading = ref(false)

// 标签页
const activeTab = ref('quality')

// 图表类型
const chartType = ref('bar')
const mainChartRef = ref<HTMLElement>()
let mainChart: echarts.ECharts | null = null

// 选中的字段
const selectedField = ref<any>(null)

// 权限控制
const isAdmin = ref(true)

// 错题筛选
const errorFilter = ref({
  field: '',
  status: ''
})

// 错题分页
const errorCurrentPage = ref(1)
const errorPageSize = ref(10)
const errorTotal = ref(100)

// 选中的错题
const selectedErrors = ref<any[]>([])

// 纠偏相关
const correctDialogVisible = ref(false)
const selectedError = ref<any>(null)
const correctForm = ref({
  errorField: '',
  correctResult: '',
  remark: ''
})

// 错误详情
const errorDetailVisible = ref(false)

// 最易出错字段TOP5
const errorTopList = ref([
  { field: '技能熟练度', errorRate: 23.5, errorCount: 156, cases: [
    { jobCode: 'JOB001', aiResult: '精通Vue', correctResult: '熟练Vue' },
    { jobCode: 'JOB003', aiResult: '了解React', correctResult: '熟悉React' }
  ]},
  { field: '薪资范围', errorRate: 18.2, errorCount: 121, cases: [
    { jobCode: 'JOB005', aiResult: '15k-20k', correctResult: '15k-25k' }
  ]},
  { field: '学历要求', errorRate: 15.8, errorCount: 105, cases: [
    { jobCode: 'JOB007', aiResult: '本科', correctResult: '本科及以上' }
  ]},
  { field: '工作经验', errorRate: 12.3, errorCount: 82, cases: [
    { jobCode: 'JOB009', aiResult: '3年', correctResult: '3-5年' }
  ]},
  { field: '工作地点', errorRate: 8.6, errorCount: 57, cases: [
    { jobCode: 'JOB011', aiResult: '北京', correctResult: '北京/上海' }
  ]}
])

// 错题数据
const errorList = ref([
  {
    id: 1,
    jobCode: 'JOB001',
    jobName: '前端开发工程师',
    errorField: 'skills',
    aiResult: '精通Vue3、熟悉React',
    correctResult: '熟练Vue3、熟悉React',
    promptVersion: 'v1.0',
    status: 'pending',
    remark: '',
    createdAt: '2026-03-12 10:00:00'
  },
  {
    id: 2,
    jobCode: 'JOB002',
    jobName: '后端开发工程师',
    errorField: 'salary',
    aiResult: '20k-30k',
    correctResult: '20k-35k',
    promptVersion: 'v1.1',
    status: 'processing',
    remark: '薪资范围识别不准确',
    createdAt: '2026-03-12 09:30:00'
  },
  {
    id: 3,
    jobCode: 'JOB003',
    jobName: '产品经理',
    errorField: 'education',
    aiResult: '本科',
    correctResult: '本科及以上',
    promptVersion: 'v1.2',
    status: 'completed',
    remark: '学历要求需包含"及以上"',
    createdAt: '2026-03-12 09:00:00'
  },
  {
    id: 4,
    jobCode: 'JOB004',
    jobName: 'UI设计师',
    errorField: 'experience',
    aiResult: '2年',
    correctResult: '2-4年',
    promptVersion: 'v2.0',
    status: 'pending',
    remark: '',
    createdAt: '2026-03-12 08:30:00'
  },
  {
    id: 5,
    jobCode: 'JOB005',
    jobName: '数据分析师',
    errorField: 'skills',
    aiResult: '熟悉Python、SQL',
    correctResult: '熟练Python、熟悉SQL',
    promptVersion: 'v1.0',
    status: 'pending',
    remark: '',
    createdAt: '2026-03-12 08:00:00'
  }
])

// 过滤后的错题
const filteredErrors = computed(() => {
  let result = [...errorList.value]
  
  if (errorFilter.value.field) {
    result = result.filter(item => item.errorField === errorFilter.value.field)
  }
  
  if (errorFilter.value.status) {
    result = result.filter(item => item.status === errorFilter.value.status)
  }
  
  errorTotal.value = result.length
  const start = (errorCurrentPage.value - 1) * errorPageSize.value
  const end = start + errorPageSize.value
  return result.slice(start, end)
})

// 初始化图表
const initChart = () => {
  if (!mainChartRef.value) return
  
  if (mainChart) {
    mainChart.dispose()
  }
  
  mainChart = echarts.init(mainChartRef.value)
  
  if (chartType.value === 'bar') {
    renderBarChart()
  } else {
    renderRadarChart()
  }
}

// 渲染柱状图
const renderBarChart = () => {
  if (!mainChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['v1.0', 'v1.1', 'v1.2', 'v2.0']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['学历', '技能', '工作经验', '薪资范围', '工作地点']
    },
    yAxis: {
      type: 'value',
      name: '准确率 (%)',
      max: 100
    },
    series: [
      {
        name: 'v1.0',
        type: 'bar',
        data: [85, 72, 88, 78, 92]
      },
      {
        name: 'v1.1',
        type: 'bar',
        data: [88, 78, 90, 82, 94]
      },
      {
        name: 'v1.2',
        type: 'bar',
        data: [90, 82, 92, 85, 95]
      },
      {
        name: 'v2.0',
        type: 'bar',
        data: [92, 88, 94, 90, 96]
      }
    ]
  }
  
  mainChart.setOption(option)
}

// 渲染雷达图
const renderRadarChart = () => {
  if (!mainChart) return
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: ['v1.0', 'v1.1', 'v1.2', 'v2.0']
    },
    radar: {
      indicator: [
        { name: '学历', max: 100 },
        { name: '技能', max: 100 },
        { name: '工作经验', max: 100 },
        { name: '薪资范围', max: 100 },
        { name: '工作地点', max: 100 }
      ]
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [85, 72, 88, 78, 92],
            name: 'v1.0'
          },
          {
            value: [88, 78, 90, 82, 94],
            name: 'v1.1'
          },
          {
            value: [90, 82, 92, 85, 95],
            name: 'v1.2'
          },
          {
            value: [92, 88, 94, 90, 96],
            name: 'v2.0'
          }
        ]
      }
    ]
  }
  
  mainChart.setOption(option)
}

// 查询
const handleQuery = () => {
  loading.value = true
  // 模拟请求防抖
  setTimeout(() => {
    loading.value = false
    nextTick(() => {
      initChart()
    })
    ElMessage.success('数据加载成功')
  }, 500)
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    promptVersions: ['v1.0', 'v1.1', 'v2.0'],
    dateRange: [],
    businessFields: ['education', 'skills', 'experience', 'salary', 'location']
  }
}

// 字段点击
const handleFieldClick = (field: any) => {
  selectedField.value = field
}

// 获取错误颜色
const getErrorColor = (rate: number) => {
  if (rate >= 20) return '#f56c6c'
  if (rate >= 10) return '#e6a23c'
  return '#67c23a'
}

// 获取字段文本
const getFieldText = (field: string) => {
  const fieldMap: Record<string, string> = {
    education: '学历',
    skills: '技能',
    experience: '工作经验',
    salary: '薪资范围',
    location: '工作地点'
  }
  return fieldMap[field] || field
}

// 获取错误状态类型
const getErrorStatusType = (status: string) => {
  if (status === 'pending') return 'warning'
  if (status === 'processing') return 'info'
  if (status === 'completed') return 'success'
  return ''
}

// 获取错误状态文本
const getErrorStatusText = (status: string) => {
  if (status === 'pending') return '待处理'
  if (status === 'processing') return '处理中'
  if (status === 'completed') return '已完成'
  return ''
}

// 错题选择
const handleErrorSelection = (val: any[]) => {
  selectedErrors.value = val
}

// 纠偏
const handleCorrect = (error: any) => {
  selectedError.value = error
  correctForm.value = {
    errorField: error.errorField,
    correctResult: error.correctResult || '',
    remark: ''
  }
  correctDialogVisible.value = true
}

// 提交纠偏
const submitCorrect = async () => {
  if (!correctForm.value.errorField) {
    ElMessage.error('请选择错误字段')
    return
  }
  if (!correctForm.value.remark) {
    ElMessage.error('请填写纠偏备注')
    return
  }
  
  correctLoading.value = true
  // 模拟提交
  setTimeout(() => {
    correctLoading.value = false
    correctDialogVisible.value = false
    ElMessage.success('纠偏提交成功，AI将重新执行')
    
    // 更新状态
    if (selectedError.value) {
      const index = errorList.value.findIndex(item => item.id === selectedError.value.id)
      if (index !== -1 && errorList.value[index]) {
        errorList.value[index].status = 'processing'
        errorList.value[index].correctResult = correctForm.value.correctResult
        errorList.value[index].remark = correctForm.value.remark
      }
    }
  }, 1000)
}

// 批量纠偏
const handleBatchCorrect = () => {
  ElMessageBox.confirm(
    `将对选中的 ${selectedErrors.value.length} 条数据发起重新执行，预计消耗 ${selectedErrors.value.length * 100} Token`,
    '批量纠偏确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`已提交 ${selectedErrors.value.length} 条纠偏请求`)
    // 更新状态
    selectedErrors.value.forEach(error => {
      const index = errorList.value.findIndex(item => item.id === error.id)
      if (index !== -1 && errorList.value[index]) {
        errorList.value[index].status = 'processing'
      }
    })
  }).catch(() => {
    // 取消操作
  })
}

// 查看错误详情
const handleViewErrorDetail = (error: any) => {
  selectedError.value = error
  errorDetailVisible.value = true
}

// 导出质量报告
const handleExportReport = () => {
  ElMessage.success('质量报告导出中...')
  // 模拟导出
  setTimeout(() => {
    ElMessage.success('质量报告已导出')
  }, 1000)
}

// 导出错题
const handleExportErrors = () => {
  ElMessage.success('错题列表导出中...')
  // 模拟导出
  setTimeout(() => {
    ElMessage.success('错题列表已导出')
  }, 1000)
}

// 分页处理
const handleErrorSizeChange = (size: number) => {
  errorPageSize.value = size
  errorCurrentPage.value = 1
}

const handleErrorCurrentChange = (current: number) => {
  errorCurrentPage.value = current
}

// 监听图表类型变化
watch(() => chartType.value, () => {
  nextTick(() => {
    initChart()
  })
})

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart()
  })
  
  // 模拟SSE监听
  setInterval(() => {
    errorList.value.forEach(error => {
      if (error.status === 'processing') {
        // 随机完成一些任务
        if (Math.random() > 0.7) {
          error.status = 'completed'
        }
      }
    })
  }, 5000)
})
</script>

<style scoped>
.model-quality {
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
  flex-wrap: wrap;
}

.content-section {
  margin-bottom: 24px;
}

.quality-report {
  padding: 20px 0;
}

.chart-section {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.error-top-list {
  padding: 10px 0;
}

.error-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.error-item:hover {
  background-color: #e6f7ff;
  transform: translateX(4px);
}

.error-rank {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1890ff, #69c0ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  margin-right: 12px;
}

.error-info {
  flex: 1;
}

.error-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.error-rate {
  font-size: 12px;
  color: #999;
}

.field-detail {
  margin-bottom: 24px;
}

.detail-content {
  padding: 10px 0;
}

.detail-item {
  text-align: center;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.detail-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.error-cases {
  margin-top: 20px;
}

.cases-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.case-item {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.case-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;
}

.case-content {
  font-size: 14px;
}

.case-row {
  margin-bottom: 4px;
}

.case-label {
  color: #666;
  margin-right: 8px;
}

.case-value {
  font-weight: 500;
}

.case-value.error {
  color: #f56c6c;
}

.case-value.correct {
  color: #67c23a;
}

.export-section {
  text-align: right;
}

.error-book {
  padding: 20px 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left-actions {
  display: flex;
  gap: 12px;
}

.right-actions {
  display: flex;
  gap: 12px;
}

.error-text {
  color: #f56c6c;
}

.correct-text {
  color: #67c23a;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.correct-dialog {
  padding: 20px 0;
}

.error-detail-dialog {
  padding: 20px 0;
}
</style>