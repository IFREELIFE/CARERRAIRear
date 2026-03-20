<template>
  <div class="employment-analysis">
    <el-card class="analysis-card">
      <template #header>
        <div class="card-header">
          <h2>就业分析</h2>
          <el-button type="primary" size="small">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </div>
      </template>
      
      <div class="chart-container">
        <!-- 整体画像分布雷达图 -->
        <div class="chart-item">
          <h3>整体画像分布</h3>
          <div ref="portraitChart" class="chart"></div>
        </div>
        
        <!-- 期望城市比例饼图 -->
        <div class="chart-item">
          <h3>期望城市比例</h3>
          <div ref="cityChart" class="chart"></div>
        </div>
        
        <!-- 意向岗位族柱状图 -->
        <div class="chart-item">
          <h3>意向岗位族</h3>
          <div ref="jobChart" class="chart"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Download } from '@element-plus/icons-vue'

// 图表实例
const portraitChart = ref<HTMLElement | null>(null)
const cityChart = ref<HTMLElement | null>(null)
const jobChart = ref<HTMLElement | null>(null)

// 图表实例对象
let portraitChartInstance: echarts.ECharts | null = null
let cityChartInstance: echarts.ECharts | null = null
let jobChartInstance: echarts.ECharts | null = null

// 假数据
const mockData = {
  portraitData: [
    { dimension: "沟通能力", score: 75 },
    { dimension: "专业技能", score: 82 },
    { dimension: "抗压能力", score: 68 },
    { dimension: "团队协作", score: 78 },
    { dimension: "创新能力", score: 65 },
    { dimension: "学习能力", score: 85 }
  ],
  cityData: [
    { name: "北京", value: 25 },
    { name: "上海", value: 20 },
    { name: "广州", value: 15 },
    { name: "深圳", value: 18 },
    { name: "杭州", value: 12 },
    { name: "其他", value: 10 }
  ],
  jobData: [
    { name: "技术开发", count: 120 },
    { name: "产品运营", count: 80 },
    { name: "市场营销", count: 60 },
    { name: "金融服务", count: 50 },
    { name: "教育培训", count: 40 },
    { name: "其他", count: 30 }
  ]
}

// 初始化雷达图
const initPortraitChart = () => {
  if (!portraitChart.value) return
  
  portraitChartInstance = echarts.init(portraitChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: mockData.portraitData.map(item => ({
        name: item.dimension,
        max: 100
      }))
    },
    series: [{
      name: '整体画像',
      type: 'radar',
      data: [{
        value: mockData.portraitData.map(item => item.score),
        name: '平均水平',
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.2)'
        },
        lineStyle: {
          color: '#1890ff'
        },
        itemStyle: {
          color: '#1890ff'
        }
      }]
    }]
  }
  
  portraitChartInstance.setOption(option)
}

// 初始化饼图
const initCityChart = () => {
  if (!cityChart.value) return
  
  cityChartInstance = echarts.init(cityChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: mockData.cityData.map(item => item.name)
    },
    series: [{
      name: '期望城市',
      type: 'pie',
      radius: '60%',
      center: ['60%', '50%'],
      data: mockData.cityData,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  cityChartInstance.setOption(option)
}

// 初始化柱状图
const initJobChart = () => {
  if (!jobChart.value) return
  
  jobChartInstance = echarts.init(jobChart.value)
  
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
      type: 'category',
      data: mockData.jobData.map(item => item.name),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '意向人数',
      type: 'bar',
      data: mockData.jobData.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#69c0ff' }
        ])
      }
    }]
  }
  
  jobChartInstance.setOption(option)
}

// 处理窗口大小变化
const handleResize = () => {
  portraitChartInstance?.resize()
  cityChartInstance?.resize()
  jobChartInstance?.resize()
}

onMounted(() => {
  // 初始化图表
  initPortraitChart()
  initCityChart()
  initJobChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  portraitChartInstance?.dispose()
  cityChartInstance?.dispose()
  jobChartInstance?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.employment-analysis {
  width: 100%;
  min-height: 100vh;
}

.analysis-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.chart-item {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.chart-item h3 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.chart {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .chart-container {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 250px;
  }
}
</style>