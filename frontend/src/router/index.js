import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/career-paths',
    name: 'CareerPaths',
    component: () => import('../views/career-paths/Index.vue')
  },
  {
    path: '/career-paths/:type',
    name: 'CareerPathDetail',
    component: () => import('../views/career-paths/Detail.vue')
  },
  {
    path: '/career-paths/:type/self-introduction',
    name: 'SelfIntroduction',
    component: () => import('../views/career-paths/SelfIntroduction.vue')
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('../views/users/Index.vue')
  },
  {
    path: '/career-evaluation',
    name: 'CareerEvaluation',
    component: () => import('../views/career-evaluation/Index.vue')
  },
  {
    path: '/career-plan',
    name: 'CareerPlan',
    component: () => import('../views/career-evaluation/CareerPlan.vue')
  },
  {
    path: '/company-reviews',
    name: 'CompanyReviews',
    component: () => import('../views/company-reviews/Index.vue')
  },
  {
    path: '/company-reviews/senior-advice/form',
    name: 'SeniorAdviceForm',
    component: () => import('../views/company-reviews/SeniorAdviceForm.vue')
  },
  {
    path: '/interview',
    name: 'Interview',
    component: () => import('../views/interview/Index.vue')
  },
  {
    path: '/resume',
    name: 'Resume',
    component: () => import('../views/resume/Index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 始终滚动到顶部
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register']
  const selfIntroductionPath = /^\/career-paths\/[^\/]+\/self-introduction$/
  const careerPathDetailPath = /^\/career-paths\/[^\/]+$/  
  
  if (!token && !publicPaths.includes(to.path) && !selfIntroductionPath.test(to.path) && !careerPathDetailPath.test(to.path)) {
    next('/login')
  } else {
    next()
  }
})

export default router
