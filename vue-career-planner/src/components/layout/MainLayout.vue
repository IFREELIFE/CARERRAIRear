<template>
  <div class="main-layout">
    <!-- 根据角色动态渲染侧边栏 -->
    <component :is="currentSidebar" />
    
    <!-- 主内容区 -->
    <div class="main-content" :class="{ 'has-sidebar': currentSidebar }">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <div class="user-menu-container">
            <div class="user-info" @click="toggleUserMenu">
              <el-avatar :size="32" :src="userInfo?.avatar || ''" :icon="UserFilled" />
              <span class="username">{{ userInfo?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </div>
            <div v-if="userMenuVisible" class="user-menu-dropdown">
              <div class="menu-item" @click="handleCommand('profile')">
                <el-icon><user /></el-icon>
                <span>个人中心</span>
              </div>
              <div class="menu-divider"></div>
              <div class="menu-item" @click="handleCommand('logout')">
                <el-icon><switch-button /></el-icon>
                <span>退出登录</span>
              </div>
            </div>
          </div>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, UserFilled, User, SwitchButton } from '@element-plus/icons-vue'
import { logout } from '../../api/auth'

import StudentSidebar from './StudentSidebar.vue'
import CompanySidebar from './CompanySidebar.vue'
import SchoolSidebar from './SchoolSidebar.vue'
import PlatformSidebar from './PlatformSidebar.vue'

const $route = useRoute()
const $router = useRouter()

const userInfo = ref<any>(null)
const userMenuVisible = ref(false)

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
  }
})

const currentSidebar = computed(() => {
  const role = userInfo.value?.role
  
  // 检查是否在问卷相关页面
  const isQuestionnairePage = $route.path.includes('/questionnaire') || $route.path.includes('/welcome')
  
  // 如果是学生角色且在问卷相关页面，不显示侧边栏
  if (role === 'STUDENT' && isQuestionnairePage) {
    return null
  }
  
  switch (role) {
    case 'STUDENT':
      return StudentSidebar
    case 'COMPANY_ADMIN':
      return CompanySidebar
    case 'SCHOOL_ADMIN':
      return SchoolSidebar
    case 'PLATFORM_ADMIN':
      return PlatformSidebar
    default:
      return null
  }
})

const pageTitle = computed(() => {
  return $route.meta?.title || '职业规划系统'
})

const toggleUserMenu = () => {
  userMenuVisible.value = !userMenuVisible.value
}

const handleCommand = async (command: string) => {
  // 先关闭菜单
  userMenuVisible.value = false
  
  switch (command) {
    case 'profile':
      $router.push('/student/profile')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          try {
            await logout(refreshToken)
          } catch (error) {
            console.error('Logout API error:', error)
          }
        }
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        ElMessage.success('已退出登录')
        $router.push('/login')
      }).catch(() => {})
      break
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  background-color: #f5f7fa;
}
</style>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.main-content {
  flex: 1;
  margin-left: 0;
  transition: margin-left 0.3s;
  min-width: 0;
}

.main-content.has-sidebar {
  margin-left: 210px;
}

.header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.breadcrumb {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  z-index: 1000;
}

.user-info {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-info:hover {
  color: #409EFF;
  background-color: #ecf5ff;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.user-menu {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 8px 0;
}

.menu-item el-icon {
  font-size: 16px;
  color: #606266;
}

.menu-item span {
  font-size: 14px;
  color: #303133;
}

.user-menu-container {
  position: relative;
}

.user-menu-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 1001;
}

.user-menu-dropdown .menu-item {
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-menu-dropdown .menu-item:hover {
  background-color: #f5f7fa;
}

.user-menu-dropdown .menu-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 8px 0;
}

.content {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
  width: 100%;
  box-sizing: border-box;
}
</style>
