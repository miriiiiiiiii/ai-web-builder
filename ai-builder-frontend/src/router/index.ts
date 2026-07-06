import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import HomePage from '../pages/HomePage.vue'
import LoginPage from '../pages/user/LoginPage.vue'
import RegisterPage from '../pages/user/RegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import AppManagePage from '../pages/admin/AppManagePage.vue'
import AppChatPage from '../pages/app/AppChatPage.vue'
import AppEditPage from '../pages/app/AppEditPage.vue'

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
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
      meta: { requireLogin: true },
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
          path: 'app/edit/:id',
          name: '编辑应用',
          component: AppEditPage,
          meta: { requireLogin: true },
        },
        {
          path: 'admin/userManage',
          name: '用户管理',
          component: UserManagePage,
        },
        {
          path: 'admin/appManage',
          name: '应用管理',
          component: AppManagePage,
        },
      ],
    },
  ],
})

export default router
