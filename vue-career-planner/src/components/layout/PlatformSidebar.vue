<template>
  <div class="sidebar">
    <div class="logo">
      <div class="logo-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
          <path d="M19 10v4H5v-4h14m0-2H5c-1.1 0-2 .9-2 2v4c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-4c0-1.1-.9-2-2-2zm0 8H5v2h14v-2z"/>
        </svg>
      </div>
      <h3>平台管理</h3>
    </div>
    <el-menu
      :default-active="$route.path"
      router
      class="sidebar-menu"
      background-color="#ffffff"
      text-color="#333333"
      active-text-color="#1890ff"
    >
      <el-menu-item index="/platform/dashboard">
        <el-icon><DataAnalysis /></el-icon>
        <span>质量指标驾驶舱</span>
      </el-menu-item>
      <el-menu-item index="/platform/data-source">
        <el-icon><Document /></el-icon>
        <span>数据源管理</span>
      </el-menu-item>
      <el-menu-item index="/platform/content-audit">
        <el-icon><Check /></el-icon>
        <span>内容审核发布</span>
      </el-menu-item>
      <el-menu-item index="/platform/task-scheduler">
        <el-icon><Timer /></el-icon>
        <span>任务调度中心</span>
      </el-menu-item>
      <el-menu-item index="/platform/model-quality">
        <el-icon><TrendCharts /></el-icon>
        <span>模型质量优化</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
  DataAnalysis,
  Document,
  Check,
  Timer,
  TrendCharts
} from '@element-plus/icons-vue'

const $route = useRoute()
</script>

<style scoped>
.sidebar {
  width: 210px;
  height: 100vh;
  background-color: #ffffff;
  position: fixed;
  left: 0;
  top: 0;
  box-shadow: 0 0 20px rgba(24, 144, 255, 0.1);
  border-right: 1px solid #e6f7ff;
  transition: all 0.3s ease;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  border-bottom: 1px solid #e6f7ff;
  padding: 0 20px;
  gap: 10px;
  background-color: #f0f9ff;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1890ff, #69c0ff);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
}

.logo h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
  padding-top: 16px;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 0 12px 8px;
  border-radius: 10px;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 400;
}

:deep(.el-menu-item:hover) {
  background-color: #f0f9ff !important;
  color: #1890ff !important;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

:deep(.el-menu-item.is-active) {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
  box-shadow: 0 2px 12px rgba(24, 144, 255, 0.2);
  font-weight: 500;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 16px;
  color: #69c0ff;
  transition: color 0.3s ease;
}

:deep(.el-menu-item:hover .el-icon) {
  color: #1890ff;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #1890ff;
}
</style>
