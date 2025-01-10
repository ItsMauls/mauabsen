import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import AttendanceView from '../views/AttendanceView.vue'
import EmployeesView from '../views/EmployeesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/attendance',
      name: 'attendance',
      component: AttendanceView,
      meta: { requiresAuth: true }
    },
    {
      path: '/employees',
      name: 'employees',
      component: EmployeesView,
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && role !== 'ROLE_ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
