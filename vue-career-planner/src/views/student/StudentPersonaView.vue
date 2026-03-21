<template>
  <div class="student-persona-view">
    <div class="page-header">
      <h2>我的画像</h2>
      <el-button type="primary" @click="generateResume" :loading="loading">
        <el-icon><Document /></el-icon>
        一键生成简历
      </el-button>
    </div>
    
    <el-card shadow="hover" class="persona-card">
      <div class="persona-header">
        <div class="persona-avatar">
          <el-avatar :size="80" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20student%20avatar%20portrait&image_size=square" />
        </div>
        <div class="persona-info">
          <h3>张三</h3>
          <p class="persona-desc">计算机科学与技术专业 | 大三</p>
        </div>
      </div>
      
      <div class="persona-dimensions">
        <h4>12维能力评估</h4>
        <div ref="chartRef" class="dimension-chart"></div>
        <div class="dimension-legend">
          <div class="legend-item" v-for="(dimension, index) in dimensions" :key="index">
            <span class="legend-color" :style="{ backgroundColor: colors[index % colors.length] }"></span>
            <span class="legend-name">{{ dimension.name }}</span>
            <span class="legend-score">{{ dimension.score }}%</span>
          </div>
        </div>
      </div>
      
      <div class="persona-tags">
        <h4>个人标签</h4>
        <div class="tags-container">
          <el-tag v-for="(tag, index) in tags" :key="index" size="medium" effect="light">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

// 不同的颜色方案
const colors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
  '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9',
  '#F8C471', '#82E0AA'
]

// 12维能力数据
const dimensions = ref([
  { name: '专业技能', score: 85 },
  { name: '沟通能力', score: 78 },
  { name: '团队协作', score: 82 },
  { name: '问题解决', score: 75 },
  { name: '创新思维', score: 68 },
  { name: '学习能力', score: 90 },
  { name: '领导力', score: 65 },
  { name: '时间管理', score: 72 },
  { name: '抗压能力', score: 70 },
  { name: '适应能力', score: 80 },
  { name: '分析能力', score: 88 },
  { name: '执行能力', score: 76 }
])

// 个人标签
const tags = ref([
  '勤奋好学', '团队协作', '技术达人', '问题解决者', '创新思维'
])

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    legend: {
      show: false
    },
    series: [
      {
        type: 'pie',
        radius: '70%',
        center: ['50%', '50%'],
        data: dimensions.value.map((item, index) => ({
          name: item.name,
          value: item.score,
          itemStyle: {
            color: colors[index % colors.length]
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          },
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{c}%',
          fontSize: 12,
          color: '#333'
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 20
        }
      }
    ]
  }
  
  chart.setOption(option)
}

// 一键生成简历
const generateResume = () => {
  loading.value = true
  // 模拟生成简历
  setTimeout(() => {
    loading.value = false
    ElMessage.success('简历生成成功，正在下载...')
    // 模拟下载
    setTimeout(() => {
      ElMessage.success('简历已下载到本地')
    }, 1000)
  }, 2000)
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped lang="scss">
.student-persona-view {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #333;
      font-size: 24px;
      font-weight: 600;
    }
  }
  
  .persona-card {
    border-radius: 12px;
    overflow: hidden;
    
    .persona-header {
      display: flex;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      
      .persona-avatar {
        margin-right: 20px;
      }
      
      .persona-info {
        flex: 1;
        
        h3 {
          margin: 0 0 8px;
          font-size: 20px;
          font-weight: 600;
          color: #333;
        }
        
        .persona-desc {
          margin: 0;
          color: #666;
          font-size: 14px;
        }
      }
    }
    
    .persona-dimensions {
      padding: 20px;
      
      h4 {
        margin: 0 0 16px;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      
      .dimension-chart {
        width: 100%;
        height: 400px;
        margin-bottom: 20px;
      }
      
      .dimension-legend {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 12px;
        
        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 50%;
          }
          
          .legend-name {
            flex: 1;
            font-size: 14px;
            color: #666;
          }
          
          .legend-score {
            font-size: 14px;
            font-weight: 600;
            color: #1890ff;
          }
        }
      }
    }
    
    .persona-tags {
      padding: 0 20px 20px;
      
      h4 {
        margin: 0 0 16px;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      
      .tags-container {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }
}
</style>
