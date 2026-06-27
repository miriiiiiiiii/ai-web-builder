<template>
  <a-layout-header class="header">
    <div class="header-content">
      <!--左侧：Logo、标题和导航菜单-->
      <div class="header-left">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
          <span class="site-title">AI Builder</span>
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          class="header-menu"
          @click="handleMenuClick"
        />
      </div>
      <div>

      </div>
      <!--右侧：用户操作区域-->
      <div class="header-right">
        <a-button type="primary">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

const menuItems: MenuProps['items'] = [
  {
    key: 'home',
    label: '主页',
  },
  {
    key: 'about',
    label: '关于',
  },
]


// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0;
  height: 64px;
  line-height: 64px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 40px;
  cursor: pointer;
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
}

.header-right {
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .logo-container {
    margin-right: 16px;
  }

  .site-title {
    display: none;
  }

  .header-menu {
    flex: none;
  }
}

@media (max-width: 576px) {
  .logo-img {
    width: 32px;
    height: 32px;
  }

  .logo-container {
    margin-right: 8px;
  }
}
</style>
