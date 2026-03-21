import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/DataAnalytics.vue'),
        meta: { title: '数据分析', icon: 'TrendCharts' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'knowledge',
        name: 'KnowledgeManagement',
        component: () => import('@/views/backend/knowledge/ArticleManagement.vue'),
        meta: { title: '知识文章', icon: 'Document' }
      },
      {
        path: 'knowledge-category',
        name: 'KnowledgeCategoryManagement',
        component: () => import('@/views/backend/knowledge/CategoryManagement.vue'),
        meta: { title: '分类管理', icon: 'FolderOpened' }
      },
      {
        path: 'consultations',
        name: 'ConsultationManagement',
        component: () => import('@/views/backend/ConsultationManagement.vue'),
        meta: { title: '咨询记录', icon: 'ChatDotRound' }
      },
      {
        path: 'emotion-diary',
        name: 'EmotionDiaryManagement',
        component: () => import('@/views/backend/EmotionDiaryManagement.vue'),
        meta: { title: '情绪日记', icon: 'Notebook' }
      },
      {
        path: 'ai-analysis-queue',
        name: 'AiAnalysisQueue',
        component: () => import('@/views/backend/AiAnalysisQueue.vue'),
        meta: { title: 'AI分析队列', icon: 'Operation' }
      },
      {
        path: 'knowledge-test',
        name: 'KnowledgeArticleTest',
        component: () => import('@/views/backend/knowledge/ArticleTest.vue'),
        meta: { title: '文章测试', icon: 'Tools' }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人信息', icon: 'UserFilled' }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/frontend/Knowledge.vue'),
        meta: { title: '知识库' }
      },
      {
        path: 'knowledge/article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/frontend/ArticleDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'consultation',
        name: 'Consultation',
        component: () => import('@/views/frontend/Consultation.vue'),
        meta: { title: 'AI智能咨询', requiresAuth: true }
      },
      {
        path: 'psychological-test',
        name: 'PsychologicalTest',
        component: () => import('@/views/frontend/PsychologicalTest.vue'),
        meta: { title: '心理测试', requiresAuth: true }
      },
      {
        path: 'emotion-diary',
        name: 'EmotionDiary',
        component: () => import('@/views/frontend/EmotionDiary.vue'),
        meta: { title: '情绪日记', requiresAuth: true }
      },
      {
        path: 'emotion-diary/:id',
        name: 'EmotionDiaryDetail',
        component: () => import('@/views/frontend/EmotionDiaryDetail.vue'),
        meta: { title: '情绪日记详情', requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'MyFavorites',
        component: () => import('@/views/frontend/MyFavorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      }
    ] 
  },
  // 认证相关路由使用专门的认证布局
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录' }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/auth/Register.vue'),
        meta: { title: '注册' }
      },
    ]
  },
  // 兼容旧路由
  {
    path: '/login',
    redirect: '/auth/login'
  },
  {
    path: '/register',
    redirect: '/auth/register'
  },
  {
    path: '/mock-test',
    name: 'MockTest',
    component: () => import('@/views/MockTest.vue'),
    meta: { title: 'Mock测试' }
  },
  {
    path: '/knowledge-test',
    name: 'KnowledgeTest',
    component: () => import('@/views/test/KnowledgeTest.vue'),
    meta: { title: '知识库模块测试' }
  },
  {
    path: '/rich-text-editor-test',
    name: 'RichTextEditorTest',
    component: () => import('@/views/test/RichTextEditorTest.vue'),
    meta: { title: '富文本编辑器测试' }
  },
  {
    path: '/simple-font-test',
    name: 'SimpleFontTest',
    component: () => import('@/views/test/SimpleFontTest.vue'),
    meta: { title: '简化字体测试' }
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 心理健康助手`
  }

  const userStore = useUserStore()
  console.log("Current route:", to.path)
  console.log("User status:", {
    isLoggedIn: userStore.isLoggedIn,
    isUser: userStore.isUser
  })

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      next(userStore.isUser ? '/' : '/back/dashboard')
      return
    }

    if (!userStore.isUser) {
      // 非普通用户只能访问后台路由
      if (to.path.startsWith('/back')) {
        next()
      } else {
        next('/back/dashboard')
      }
      return
    } else {
      // 普通用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
      } else {
        next()
      }
      return
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
  }

  next()
})

export default router
