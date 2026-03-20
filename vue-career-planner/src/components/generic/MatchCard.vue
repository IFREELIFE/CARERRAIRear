<template>
  <div class="match-card">
    <el-card :header="title">
      <div class="match-info">
        <div class="match-score">
          <span class="score-label">匹配度：</span>
          <span class="score-value">{{ matchScore }}%</span>
          <el-progress
            :percentage="matchScore"
            :color="getScoreColor(matchScore)"
            :stroke-width="10"
            style="margin-top: 10px"
          />
        </div>
        
        <div class="match-details" v-if="details">
          <h4>匹配详情</h4>
          <el-table :data="details" style="width: 100%" size="small">
            <el-table-column prop="name" label="维度" width="120" />
            <el-table-column prop="score" label="匹配度" width="100">
              <template #default="scope">
                {{ scope.row.score }}%
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
          </el-table>
        </div>
        
        <div class="match-actions" v-if="showActions">
          <el-button type="primary" @click="handleView">查看详情</el-button>
          <el-button @click="handleApply" v-if="showApply">申请</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Detail {
  name: string
  score: number
  description: string
}

interface Props {
  title: string
  matchScore: number
  details?: Detail[]
  showActions?: boolean
  showApply?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  details: undefined,
  showActions: true,
  showApply: false
})

const emit = defineEmits<{
  (e: 'view'): void
  (e: 'apply'): void
}>()

const getScoreColor = (score: number) => {
  if (score >= 80) {
    return '#67C23A'
  } else if (score >= 60) {
    return '#E6A23C'
  } else if (score >= 40) {
    return '#F56C6C'
  } else {
    return '#909399'
  }
}

const handleView = () => {
  emit('view')
}

const handleApply = () => {
  emit('apply')
}
</script>

<style scoped>
.match-card {
  width: 100%;
}

.match-info {
  padding: 10px 0;
}

.match-score {
  margin-bottom: 20px;
}

.score-label {
  font-size: 16px;
  font-weight: 500;
  margin-right: 10px;
}

.score-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.match-details {
  margin: 20px 0;
}

.match-details h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 500;
}

.match-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>