<template>
  <div class="persona-card">
    <el-card :header="title">
      <div class="persona-dimensions">
        <div
          v-for="(dimension, index) in dimensions"
          :key="index"
          class="dimension-item"
        >
          <div class="dimension-header">
            <span class="dimension-name">{{ dimension.name }}</span>
            <span class="dimension-value">{{ dimension.value }}%</span>
          </div>
          <el-progress
            :percentage="dimension.value"
            :color="getDimensionColor(dimension.value)"
            :stroke-width="8"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
interface Dimension {
  name: string
  value: number
}

interface Props {
  title: string
  dimensions: Dimension[]
}

const props = defineProps<Props>()

const getDimensionColor = (value: number) => {
  if (value >= 80) {
    return '#67C23A'
  } else if (value >= 60) {
    return '#E6A23C'
  } else if (value >= 40) {
    return '#F56C6C'
  } else {
    return '#909399'
  }
}
</script>

<style scoped>
.persona-card {
  width: 100%;
}

.persona-dimensions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.dimension-item {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dimension-name {
  font-weight: 500;
  color: #303133;
}

.dimension-value {
  font-weight: 600;
  color: #409eff;
}

.el-progress {
  margin-top: 5px;
}
</style>
