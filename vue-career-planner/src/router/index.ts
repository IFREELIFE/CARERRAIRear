import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../components/layout/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  // 学生端路由
  {
    path: '/student',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: 'dashboard',
        name: 'PlatformDashboard',
        component: () => import('../views/platform/Dashboard.vue'),
        meta: { title: '质量指标驾驶舱' }
      },
      {
        path: '',
        name: 'StudentHome',
        component: () => import('../views/student/StudentHome.vue'),
        meta: { title: '学生首页' }
      },
      {
        path: 'persona',
        name: 'StudentPersona',
        component: () => import('../views/student/StudentPersonaView.vue'),
        meta: { title: '我的画像' }
      },
      {
        path: 'appointment',
        name: 'StudentAppointment',
        component: () => import('../views/student/AppointmentView.vue'),
        meta: { title: '预约指导' }
      },
      {
        path: 'career-goal',
        name: 'StudentCareerGoal',
        component: () => import('../views/student/CareerGoalView.vue'),
        meta: { title: '目标设定' }
      },
      {
        path: 'jobs',
        name: 'StudentJobs',
        component: () => import('../views/student/JobsView.vue'),
        meta: { title: '招聘信息' }
      },
      {
        path: 'achievements',
        name: 'StudentAchievements',
        component: () => import('../views/student/AchievementsView.vue'),
        meta: { title: '我的成就' }
      },
      {
        path: 'announcements',
        name: 'StudentAnnouncements',
        component: () => import('../views/student/AnnouncementsView.vue'),
        meta: { title: '通告中心' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'welcome',
        name: 'StudentWelcome',
        component: () => import('../views/student/WelcomeNotification.vue'),
        meta: { title: '欢迎' }
      },
      {
        path: 'questionnaire/skills',
        name: 'StudentSkillsQuestionnaire',
        component: () => import('../views/student/questionnaire/SkillsQuestionnaire.vue'),
        meta: { title: '技术技能问卷' }
      },
      {
        path: 'questionnaire/mbti',
        name: 'StudentMBTIQuestionnaire',
        component: () => import('../views/student/questionnaire/MBTIQuestionnaire.vue'),
        meta: { title: 'MBTI性格测试' }
      },
      {
        path: 'questionnaire/confirm',
        name: 'StudentConfirmQuestionnaire',
        component: () => import('../views/student/questionnaire/ConfirmQuestionnaire.vue'),
        meta: { title: '确认信息' }
      }
    ]
  },
  // 企业端路由
  {
    path: '/company',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'ENTERPRISE' },
    children: [
      {
        path: 'jobs',
        name: 'CompanyJobs',
        component: () => import('../views/company/JobManagement.vue'),
        meta: { title: '职位管理' }
      },
      {
        path: 'job-candidate',
        name: 'CompanyJobCandidate',
        component: () => import('../views/company/JobCandidateManagement.vue'),
        meta: { title: '岗位投递管理' }
      },
      {
        path: 'interview-feedback/:candidateId',
        name: 'CompanyInterviewFeedback',
        component: () => import('../views/company/InterviewFeedbackEntry.vue'),
        meta: { title: '面试反馈管理' }
      },
      {
        path: 'intelligent-resume/:candidateId',
        name: 'CompanyIntelligentResume',
        component: () => import('../views/company/IntelligentResume.vue'),
        meta: { title: '智能简历筛选' }
      }
    ]
  },
  // 高校端路由
  {
    path: '/school',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'SCHOOL' },
    children: [

      {
        path: 'profile',
        name: 'SchoolProfile',
        component: () => import('../views/school/StudentProfile.vue'),
        meta: { title: '学生档案' }
      },
      {
        path: 'employment-analysis',
        name: 'SchoolEmploymentAnalysis',
        component: () => import('../views/school/EmploymentAnalysis.vue'),
        meta: { title: '就业分析' }
      },
      {
        path: 'guidance-management',
        name: 'SchoolGuidanceManagement',
        component: () => import('../views/school/GuidanceManagement.vue'),
        meta: { title: '辅导管理' }
      }
    ]
  },
  // 平台管理端路由
  {
    path: '/platform',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'PlatformDashboard',
        component: () => import('../views/platform/Dashboard.vue'),
        meta: { title: '质量指标驾驶舱' }
      },
      {
        path: 'data-source',
        name: 'PlatformDataSource',
        component: () => import('../views/platform/DataSourceManagement.vue'),
        meta: { title: '数据源管理' }
      },
      {
        path: 'content-audit',
        name: 'PlatformContentAudit',
        component: () => import('../views/platform/ContentAudit.vue'),
        meta: { title: '内容审核发布' }
      },
      {
        path: 'task-scheduler',
        name: 'PlatformTaskScheduler',
        component: () => import('../views/platform/TaskScheduler.vue'),
        meta: { title: '任务调度中心' }
      },
      {
        path: 'model-quality',
        name: 'PlatformModelQuality',
        component: () => import('../views/platform/ModelQuality.vue'),
        meta: { title: '模型质量优化' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth !== false
  const token = localStorage.getItem('accessToken')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  if (requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.role && user && to.meta.role !== user.role) {
    ElMessage.error('无权访问该页面')
    next('/login')
    return
  }

  next()
})

export default router
