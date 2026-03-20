<template>
  <div class="sidebar">
    <div class="logo">
      <div class="logo-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.32 6l-4.32 2.37.94 2.33L14 14.26l-2.94-1.56.94-2.33L5.68 9H18.32zM12 13.12l1.88-1.05.94 2.34-1.88 1.05-.94-2.34z"/>
        </svg>
      </div>
      <h3>高校端</h3>
    </div>
    <el-menu
      :default-active="$route.path"
      router
      class="sidebar-menu"
      background-color="#ffffff"
      text-color="#333333"
      active-text-color="#1890ff"
    >
      <el-menu-item index="/school/profile">
        <el-icon><Edit /></el-icon>
        <span>学生档案</span>
      </el-menu-item>
      <el-menu-item index="/school/employment-analysis">
        <el-icon><DataLine /></el-icon>
        <span>就业分析</span>
      </el-menu-item>
      <el-menu-item index="/school/guidance-management">
        <el-icon><Calendar /></el-icon>
        <span>辅导管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
  Edit,
  DataLine,
  Calendar
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
