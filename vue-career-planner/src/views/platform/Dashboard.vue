<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>质量指标驾驶舱</h1>
      <div class="header-actions">
        <span class="update-time">最后更新: {{ lastUpdateTime }}</span>
        <el-button type="primary" @click="refreshData" :loading="refreshing">
          <el-icon><Refresh /></el-icon>
          手动刷新
        </el-button>
      </div>
    </div>
    
    <div class="dashboard-content">
      <div class="top-section">
        <div class="metric-cards">
          <div class="metric-card" v-for="metric in coreMetrics" :key="metric.title">
            <div class="metric-icon" :style="{ background: metric.color }">
              <el-icon :size="32"><component :is="metric.icon" /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value" :class="{ 'alert': metric.isAlert }">
                {{ metric.value }}
                <span v-if="metric.unit">{{ metric.unit }}</span>
              </div>
              <div class="metric-title">{{ metric.title }}</div>
              <div class="metric-trend" :class="metric.trend > 0 ? 'up' : 'down'">
                <el-icon v-if="metric.trend > 0"><Top /></el-icon>
                <el-icon v-else><Bottom /></el-icon>
                {{ Math.abs(metric.trend) }}%
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="middle-section">
        <div class="chart-container">
          <div class="chart-card">
            <div class="chart-header">
              <h3>AI任务成功率趋势</h3>
              <el-radio-group v-model="trendTimeRange" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="trendChartRef" class="chart"></div>
          </div>
        </div>
        
        <div class="chart-container">
          <div class="chart-card">
            <div class="chart-header">
              <h3>任务类型分布</h3>
            </div>
            <div ref="taskTypeChartRef" class="chart"></div>
          </div>
        </div>
      </div>
      
      <div class="bottom-section">
        <div class="chart-container wide">
          <div class="chart-card">
            <div class="chart-header">
              <h3>失败任务 Top5 原因</h3>
              <el-tag type="danger" effect="dark" v-if="failureRateAlert">
                <el-icon><WarningFilled /></el-icon>
                失败率异常告警
              </el-tag>
            </div>
            <div ref="failureReasonChartRef" class="chart"></div>
          </div>
        </div>
        
        <div class="chart-container wide">
          <div class="chart-card">
            <div class="chart-header">
              <h3>高频换岗路线</h3>
            </div>
            <div ref="careerPathChartRef" class="chart"></div>
          </div>
        </div>
      </div>
      
      <div class="detail-section">
        <div class="table-card">
          <div class="table-header">
            <h3>最近失败任务</h3>
            <el-button text type="primary" @click="viewAllFailures">查看全部</el-button>
          </div>
          <el-table :data="recentFailures" style="width: 100%">
            <el-table-column prop="taskId" label="任务ID" width="120" />
            <el-table-column prop="type" label="任务类型" width="150" />
            <el-table-column prop="reason" label="失败原因" />
            <el-table-column prop="time" label="失败时间" width="180" />
            <el-table-column prop="retryCount" label="重试次数" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.retryCount > 3 ? 'danger' : 'warning'">
                  {{ scope.row.retryCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" type="primary" @click="retryTask(scope.row)">重试</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  Top, 
  Bottom, 
  Document,
  Share,
  CircleCheck,
  WarningFilled,
  Timer
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const refreshing = ref(false)
const lastUpdateTime = ref('')
const trendTimeRange = ref('week')
const failureRateAlert = ref(false)

const trendChartRef = ref<HTMLElement>()
const taskTypeChartRef = ref<HTMLElement>()
const failureReasonChartRef = ref<HTMLElement>()
const careerPathChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let taskTypeChart: echarts.ECharts | null = null
let failureReasonChart: echarts.ECharts | null = null
let careerPathChart: echarts.ECharts | null = null
let refreshTimer: number | null = null

const coreMetrics = ref([
  {
    title: '岗位画像总数',
    value: '12,856',
    unit: '个',
    trend: 12.5,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    icon: markRaw(Document),
    isAlert: false
  },
  {
    title: '换岗路线图数',
    value: '3,428',
    unit: '条',
    trend: 8.3,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    icon: markRaw(Share),
    isAlert: false
  },
  {
    title: '整体成功率',
    value: '94.6',
    unit: '%',
    trend: 2.1,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    icon: markRaw(CircleCheck),
    isAlert: false
  },
  {
    title: '平均响应时间',
    value: '1.2',
    unit: '秒',
    trend: -15.3,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    icon: markRaw(Timer),
    isAlert: false
  }
])

const recentFailures = ref([
  {
    taskId: 'TASK-001',
    type: '画像生成',
    reason: '数据源连接超时',
    time: '2024-03-12 14:30:25',
    retryCount: 3
  },
  {
    taskId: 'TASK-002',
    type: '路线推荐',
    reason: '算法模型加载失败',
    time: '2024-03-12 13:45:18',
    retryCount: 2
  },
  {
    taskId: 'TASK-003',
    type: '数据分析',
    reason: '内存不足',
    time: '2024-03-12 12:20:33',
    retryCount: 5
  },
  {
    taskId: 'TASK-004',
    type: '画像更新',
    reason: '数据库写入失败',
    time: '2024-03-12 11:15:42',
    retryCount: 1
  },
  {
    taskId: 'TASK-005',
    type: '报告生成',
    reason: '模板文件缺失',
    time: '2024-03-12 10:30:15',
    retryCount: 4
  }
])

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['成功率', '失败率'],
      top: 10,
      textStyle: {
        color: '#333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['03-06', '03-07', '03-08', '03-09', '03-10', '03-11', '03-12'],
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%',
        color: '#666'
      },
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '成功率',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#52c41a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        },
        data: [92.3, 93.1, 94.5, 93.8, 95.2, 94.6, 94.6]
      },
      {
        name: '失败率',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#ff4d4f'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 77, 79, 0.3)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0.05)' }
          ])
        },
        data: [7.7, 6.9, 5.5, 6.2, 4.8, 5.4, 5.4]
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initTaskTypeChart = () => {
  if (!taskTypeChartRef.value) return
  
  taskTypeChart = echarts.init(taskTypeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#333'
      }
    },
    series: [
      {
        name: '任务类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {d}%',
          color: '#333'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#333'
          }
        },
        labelLine: {
          show: true,
          length: 20,
          lineStyle: {
            color: '#999'
          }
        },
        data: [
          { value: 4520, name: '画像生成', itemStyle: { color: '#1890ff' } },
          { value: 3280, name: '路线推荐', itemStyle: { color: '#52c41a' } },
          { value: 2150, name: '数据分析', itemStyle: { color: '#faad14' } },
          { value: 1850, name: '报告生成', itemStyle: { color: '#722ed1' } },
          { value: 1056, name: '其他', itemStyle: { color: '#8c8c8c' } }
        ]
      }
    ]
  }
  
  taskTypeChart.setOption(option)
}

const initFailureReasonChart = () => {
  if (!failureReasonChartRef.value) return
  
  failureReasonChart = echarts.init(failureReasonChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '次数',
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: ['模板文件缺失', '内存不足', '数据库写入失败', '算法模型加载失败', '数据源连接超时'],
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#333'
      }
    },
    series: [
      {
        name: '失败次数',
        type: 'bar',
        data: [
          { value: 45, itemStyle: { color: '#ff7875' } },
          { value: 68, itemStyle: { color: '#ff9c6e' } },
          { value: 89, itemStyle: { color: '#ffc069' } },
          { value: 125, itemStyle: { color: '#ffd666' } },
          { value: 186, itemStyle: { color: '#ff4d4f' } }
        ],
        label: {
          show: true,
          position: 'right',
          formatter: '{c}次',
          color: '#333'
        },
        barWidth: '60%'
      }
    ]
  }
  
  failureReasonChart.setOption(option)
}

const initCareerPathChart = () => {
  if (!careerPathChartRef.value) return
  
  careerPathChart = echarts.init(careerPathChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人'
    },
    series: [
      {
        name: '换岗路线',
        type: 'funnel',
        left: '10%',
        top: 60,
        bottom: 60,
        width: '80%',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '100%',
        sort: 'descending',
        gap: 2,
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}: {c}人',
          color: '#fff'
        },
        labelLine: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            fontSize: 16,
            color: '#fff'
          }
        },
        data: [
          { value: 100, name: '前端→全栈', itemStyle: { color: '#1890ff' } },
          { value: 85, name: '测试→开发', itemStyle: { color: '#52c41a' } },
          { value: 72, name: '后端→架构师', itemStyle: { color: '#722ed1' } },
          { value: 60, name: '运营→产品', itemStyle: { color: '#faad14' } },
          { value: 48, name: '设计→前端', itemStyle: { color: '#eb2f96' } }
        ]
      }
    ]
  }
  
  careerPathChart.setOption(option)
}

const refreshData = async () => {
  try {
    refreshing.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
    const metric0 = coreMetrics.value[0]
    const metric1 = coreMetrics.value[1]
    const metric2 = coreMetrics.value[2]
    
    if (metric0) {
      metric0.value = (12856 + Math.floor(Math.random() * 100)).toLocaleString()
    }
    if (metric1) {
      metric1.value = (3428 + Math.floor(Math.random() * 50)).toLocaleString()
    }
    if (metric2) {
      metric2.value = (94 + Math.random() * 2).toFixed(1)
    }
    
    const failureRate = 5 + Math.random() * 3
    if (failureRate > 7) {
      failureRateAlert.value = true
      if (metric2) {
        metric2.isAlert = true
      }
    } else {
      failureRateAlert.value = false
      if (metric2) {
        metric2.isAlert = false
      }
    }
    
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    refreshing.value = false
  }
}

const viewAllFailures = () => {
  ElMessage.info('查看全部失败任务')
}

const retryTask = (task: any) => {
  ElMessage.success(`任务 ${task.taskId} 已重新提交`)
}

const handleResize = () => {
  trendChart?.resize()
  taskTypeChart?.resize()
  failureReasonChart?.resize()
  careerPathChart?.resize()
}

onMounted(() => {
  lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  
  initTrendChart()
  initTaskTypeChart()
  initFailureReasonChart()
  initCareerPathChart()
  
  window.addEventListener('resize', handleResize)
  
  refreshTimer = window.setInterval(() => {
    refreshData()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  trendChart?.dispose()
  taskTypeChart?.dispose()
  failureReasonChart?.dispose()
  careerPathChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  color: #333;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  h1 {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    color: #1890ff;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .update-time {
      color: #666;
      font-size: 14px;
    }
  }
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.top-section {
  .metric-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }
  
  .metric-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
    }
    
    .metric-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
    }
    
    .metric-info {
      flex: 1;
      
      .metric-value {
        font-size: 32px;
        font-weight: 700;
        color: #333;
        margin-bottom: 4px;
        
        &.alert {
          color: #ff4d4f;
          animation: blink 1s infinite;
        }
        
        span {
          font-size: 16px;
          font-weight: 400;
          color: #999;
          margin-left: 4px;
        }
      }
      
      .metric-title {
        font-size: 14px;
        color: #666;
        margin-bottom: 4px;
      }
      
      .metric-trend {
        display: flex;
        align-items: center;
        font-size: 12px;
        
        &.up {
          color: #52c41a;
        }
        
        &.down {
          color: #ff4d4f;
        }
      }
    }
  }
}

.middle-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-container {
  &.wide {
    grid-column: span 1;
  }
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  height: 100%;
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .chart {
    height: 300px;
  }
}

.detail-section {
  .table-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 20px;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }
    
    :deep(.el-table) {
      background-color: #fff;
      
      .el-table__header-wrapper th {
        background-color: #fafafa;
        color: #333;
      }
      
      .el-table__body-wrapper td {
        background-color: #fff;
        color: #333;
      }
      
      .el-table__row:hover td {
        background-color: #e6f7ff;
      }
      
      .el-table__empty-block {
        background-color: #fff;
      }
    }
  }
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@media (max-width: 1400px) {
  .top-section .metric-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .middle-section,
  .bottom-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .top-section .metric-cards {
    grid-template-columns: 1fr;
  }
  
  .dashboard-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}
</style>
