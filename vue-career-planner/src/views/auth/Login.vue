<template>
  <div class="login-page">
    <!-- 背景装饰层 -->
    <div class="bg-effects">
      <!-- 扫描线 -->
      <div class="scan-line"></div>
      <!-- 数据流 -->
      <div class="data-flow">
        <div class="data-line data-line-1"></div>
        <div class="data-line data-line-2"></div>
        <div class="data-line data-line-3"></div>
      </div>
      <!-- 粒子效果 -->
      <div class="particles">
        <div class="particle particle-1"></div>
        <div class="particle particle-2"></div>
        <div class="particle particle-3"></div>
        <div class="particle particle-4"></div>
        <div class="particle particle-5"></div>
        <div class="particle particle-6"></div>
      </div>

      <!-- 发光圆环 -->
      <div class="glow-ring glow-ring-1"></div>
      <div class="glow-ring glow-ring-2"></div>
    </div>
    
    <!-- 几何图形装饰 -->
    <div class="geometric-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>
    
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <!-- 顶部图标 -->
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
            </svg>
          </div>
          <h2>职业规划平台</h2>
          <p class="slogan">欢迎回来，请登录您的账户</p>
        </div>
      </template>
      <div class="login-form">
        <div class="input-group">
          <label class="input-label">用户名</label>
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            :prefix-icon="User"
            size="large"
          />
        </div>
        <div class="input-group">
          <label class="input-label">密码</label>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            :prefix-icon="Lock" 
            show-password 
            size="large"
            @keyup.enter="handleLogin"
          />
        </div>
        <div class="form-actions">
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading" 
            size="large"
          >立即登录</el-button>
        </div>
        <div class="register-link">
          还没有账号？<a href="#" @click.prevent="handleRegister" class="register-btn">立即注册</a>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../../api/auth'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const getRedirectPath = (role: string): string => {
  const rolePathMap: Record<string, string> = {
    'STUDENT': '/student',
    'SCHOOL': '/school/profile',
    'ENTERPRISE': '/company/jobs',
    'ADMIN': '/platform/dashboard'
  }
  return rolePathMap[role] || '/'
}

const handleLogin = async () => {
  // 简单表单验证
  if (!loginForm.username) {
    ElMessage.error('请输入用户名/手机号')
    return
  }
  if (!loginForm.password) {
    ElMessage.error('请输入密码')
    return
  }
  
  try {
    loading.value = true

    const response = await login({
      username: loginForm.username,
      password: loginForm.password
    })
    const loginData = response.data

    localStorage.setItem('accessToken', loginData.accessToken)
    localStorage.setItem('refreshToken', loginData.refreshToken)
    localStorage.setItem('user', JSON.stringify({ 
      role: loginData.role,
      username: loginForm.username
    }))
    
    ElMessage.success('登录成功')

    router.push(getRedirectPath(loginData.role))
  } catch (error: any) {
    console.error('Login error:', error)
    const errorMsg = error.message || '登录失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  ElMessage.info('注册功能开发中')
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>

<style scoped>
.login-page {
  background: linear-gradient(135deg, #0c1929 0%, #122a4a 30%, #163d66 60%, #0c1929 100%);
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 背景装饰层 */
.login-page .bg-effects {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  overflow: hidden;
}

/* 扫描线效果 */
.login-page .scan-line {
  position: absolute;
  top: 0;
  left: -100%;
  width: 200%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(100, 180, 255, 0.08) 45%,
    rgba(100, 180, 255, 0.15) 50%,
    rgba(100, 180, 255, 0.08) 55%,
    transparent 100%
  );
  animation: scanMove 8s linear infinite;
}

@keyframes scanMove {
  0% { transform: translateX(-25%); }
  100% { transform: translateX(25%); }
}

/* 数据流效果 */
.login-page .data-flow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.login-page .data-line {
  position: absolute;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(100, 180, 255, 0.7), transparent);
  animation: dataFlow 4s linear infinite;
}

.login-page .data-line-1 {
  top: 20%;
  left: -100%;
  width: 50%;
  animation-delay: 0s;
}

.login-page .data-line-2 {
  top: 50%;
  left: -100%;
  width: 70%;
  animation-delay: 1.5s;
}

.login-page .data-line-3 {
  top: 80%;
  left: -100%;
  width: 40%;
  animation-delay: 3s;
}

@keyframes dataFlow {
  0% { left: -100%; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { left: 100%; opacity: 0; }
}

/* 粒子效果 */
.login-page .particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.login-page .particle {
  position: absolute;
  width: 5px;
  height: 5px;
  background: rgba(100, 180, 255, 0.9);
  border-radius: 50%;
  box-shadow: 0 0 12px rgba(100, 180, 255, 0.9), 0 0 24px rgba(100, 180, 255, 0.5);
  animation: particleFloat 6s ease-in-out infinite;
}

.login-page .particle-1 { top: 15%; left: 20%; animation-delay: 0s; }
.login-page .particle-2 { top: 35%; right: 25%; animation-delay: 1s; }
.login-page .particle-3 { top: 55%; left: 35%; animation-delay: 2s; }
.login-page .particle-4 { top: 70%; right: 40%; animation-delay: 3s; }
.login-page .particle-5 { top: 85%; left: 50%; animation-delay: 4s; }
.login-page .particle-6 { top: 45%; left: 70%; animation-delay: 5s; }

@keyframes particleFloat {
  0%, 100% { 
    transform: translateY(0) scale(1); 
    opacity: 0.5; 
  }
  50% { 
    transform: translateY(-30px) scale(1.5); 
    opacity: 1; 
  }
}



/* 发光圆环 */
.login-page .glow-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(100, 180, 255, 0.4);
  animation: ringRotate 20s linear infinite;
}

.login-page .glow-ring-1 {
  top: -10%;
  left: -10%;
  width: 60%;
  height: 60%;
  animation-direction: normal;
}

.login-page .glow-ring-2 {
  bottom: -20%;
  right: -10%;
  width: 50%;
  height: 50%;
  animation-direction: reverse;
}

.login-page .glow-ring::before {
  content: '';
  position: absolute;
  top: 10%;
  left: 10%;
  right: 10%;
  bottom: 10%;
  border-radius: 50%;
  border: 1px solid rgba(100, 180, 255, 0.2);
}

@keyframes ringRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 中心光晕 */
.login-page::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 800px;
  background: radial-gradient(
    circle,
    rgba(100, 180, 255, 0.2) 0%,
    rgba(100, 180, 255, 0.08) 30%,
    transparent 70%
  );
  animation: centerGlow 8s ease-in-out infinite;
  z-index: 0;
}

@keyframes centerGlow {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 0.8; 
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.2); 
    opacity: 1; 
  }
}

/* 几何图形装饰 */
.login-page .geometric-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
}

.login-page .shape {
  position: absolute;
  background: rgba(100, 180, 255, 0.08);
  border: 1px solid rgba(100, 180, 255, 0.25);
  backdrop-filter: blur(10px);
  animation: shapeFloat 15s ease-in-out infinite;
}

.login-page .shape-1 {
  top: 10%;
  left: 10%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  animation-delay: 0s;
}

.login-page .shape-2 {
  top: 60%;
  right: 15%;
  width: 80px;
  height: 80px;
  transform: rotate(45deg);
  animation-delay: 2s;
}

.login-page .shape-3 {
  bottom: 10%;
  left: 20%;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  animation-delay: 4s;
}

.login-page .shape-4 {
  top: 30%;
  right: 25%;
  width: 60px;
  height: 60px;
  transform: rotate(30deg);
  animation-delay: 6s;
}

.login-page > * {
  position: relative;
  z-index: 2;
}

@keyframes shapeFloat {
  0% {
    transform: translateY(0) translateX(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) translateX(20px) rotate(180deg);
    opacity: 0.6;
  }
  100% {
    transform: translateY(0) translateX(0) rotate(360deg);
    opacity: 0.3;
  }
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 32px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: none;
}



.login-form {
  width: 100%;
}

.input-group {
  margin-bottom: 16px;
}

.input-label {
  display: block;
  font-size: 12px;
  font-weight: 400;
  color: #666;
  margin-bottom: 6px;
}

/* Element Plus 输入框样式 */
.input-group :deep(.el-input__wrapper) {
  border-radius: 8px !important;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  box-shadow: none;
}

.input-group :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.input-group :deep(.el-input__wrapper.is-focus) {
  border-color: #dcdfe6 !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1) !important;
}

.input-group :deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

.form-actions {
  margin-top: 16px;
}

/* Element Plus 按钮样式 */
.form-actions :deep(.el-button) {
  border-radius: 8px !important;
  height: 40px;
  font-size: 14px;
  font-weight: 600;
  width: 100%;
  background: #1890ff;
  border: none;
}

.form-actions :deep(.el-button:hover) {
  background: #40a9ff;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.register-link .register-btn {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.register-link .register-btn:hover {
  color: #096dd9;
  text-decoration: underline;
}

.login-header {
  text-align: center;
  margin-bottom: 4px;
  padding: 16px 0 8px;
}

.login-header .logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
}

.login-header .logo-icon svg {
  color: white;
  width: 32px;
  height: 32px;
}

.login-header h2 {
  margin: 0 0 8px;
  color: #1890ff;
  font-size: 24px;
  font-weight: 600;
}

.login-header .slogan {
  margin: 0;
  color: #666;
  font-size: 14px;
  opacity: 0.8;
}
</style>
