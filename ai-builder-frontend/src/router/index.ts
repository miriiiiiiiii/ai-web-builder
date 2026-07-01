import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import HomePage from '../pages/HomePage.vue'
import LoginPage from '../pages/user/LoginPage.vue'
import RegisterPage from '../pages/user/RegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/user/login',
      name: '用户登录',
      component: LoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: RegisterPage,
    },
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: '主页',
          component: HomePage,
        },
        {
          path: 'admin/userManage',
          name: '用户管理',
          component: UserManagePage,
        },
      ],
    },
  ],
})

export default router
