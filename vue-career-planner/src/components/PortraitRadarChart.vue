<template>
  <div class="portrait-radar-chart">
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array as () => Array<{ dimension: string; score: number }>,
    default: () => []
  },
  title: {
    type: String,
    default: '12维能力评估'
  }
})

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return
  
  const option = {
    title: {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: props.data.map(item => ({
        name: item.dimension,
        max: 100
      })),
      shape: 'circle',
      splitNumber: 5,
      axisName: {
        color: '#666',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(24, 144, 255, 0.05)', 'rgba(24, 144, 255, 0.1)', 'rgba(24, 144, 255, 0.15)', 'rgba(24, 144, 255, 0.2)', 'rgba(24, 144, 255, 0.25)']
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.2)'
        }
      }
    },
    series: [{
      name: '能力评估',
      type: 'radar',
      data: [{
        value: props.data.map(item => item.score),
        name: '学生能力',
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.2)'
        },
        lineStyle: {
          color: '#1890ff',
          width: 2
        },
        itemStyle: {
          color: '#1890ff'
        }
      }]
    }]
  }
  
  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.portrait-radar-chart {
  width: 100%;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 400px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
}
</style>