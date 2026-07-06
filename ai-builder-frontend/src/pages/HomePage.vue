<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <h1 class="hero-title">
        一句话生成你的<span class="blue-text">专属网站</span>
      </h1>
      <p class="hero-subtitle">不写一行代码，自动生成页面</p>

      <div class="prompt-box">
        <a-textarea
          v-model:value="promptText"
          :auto-size="{ minRows: 3, maxRows: 6 }"
          placeholder="使用 AI Builder 创建一个有趣的小游戏，游戏玩法是......"
          class="prompt-textarea"
          @keydown.enter.ctrl="handleCreate"
        />
        <div class="prompt-actions">
          <div class="prompt-left-actions">
            <a-button size="small" class="action-btn" disabled>
              <PaperClipOutlined />
              上传
            </a-button>
            <a-button size="small" class="action-btn" disabled>
              <HighlightOutlined />
              优化
            </a-button>
          </div>
          <a-button
            type="primary"
            shape="circle"
            :loading="creating"
            class="send-btn"
            @click="handleCreate"
          >
            <ArrowUpOutlined />
          </a-button>
        </div>
      </div>

      <div class="quick-tags">
        <a-button
          v-for="tag in quickTags"
          :key="tag.title"
          class="quick-tag"
          @click="promptText = tag.prompt"
        >
          {{ tag.title }}
        </a-button>
      </div>
    </section>

    <!-- 我的作品 -->
    <section v-if="loginUserStore.loginUser.id" class="apps-section">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
        <a-input-search
          v-model:value="mySearchName"
          placeholder="搜索应用名称"
          allow-clear
          class="section-search"
          @search="handleMySearch"
        />
      </div>
      <a-spin :spinning="myLoading">
        <div v-if="myApps.length" class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            mode="my"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <a-empty v-else description="暂无作品，快去创建一个吧" />
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myPageNum"
            :page-size="myPageSize"
            :total="myTotal"
            :show-size-changer="false"
            :show-total="(total:number)=>`共 ${total} 个应用`"
            @change="onMyPageChange"
          />
        </div>
      </a-spin>
    </section>

    <!-- 精选案例 -->
    <section class="apps-section">
      <div class="section-header">
        <h2 class="section-title">精选案例</h2>
        <a-input-search
          v-model:value="goodSearchName"
          placeholder="搜索应用名称"
          allow-clear
          class="section-search"
          @search="handleGoodSearch"
        />
      </div>
      <a-spin :spinning="goodLoading">
        <div v-if="goodApps.length" class="app-grid">
          <AppCard
            v-for="app in goodApps"
            :key="app.id"
            :app="app"
            mode="featured"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <a-empty v-else description="暂无精选案例" />
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="goodPageNum"
            :page-size="goodPageSize"
            :total="goodTotal"
            :show-size-changer="false"
            :show-total="(total:number)=>`共 ${total} 个应用`"
            @change="onGoodPageChange"
          />
        </div>
      </a-spin>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowUpOutlined,
  HighlightOutlined,
  PaperClipOutlined,
} from '@ant-design/icons-vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'
import { useLoginUserStore } from '@/stores/loginUser'
import logoUrl from '@/assets/logo.png'
import {getDeployUrl} from "../config/env";
import { quickTags } from '@/utils/quickTags'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const promptText = ref('')
const creating = ref(false)


// 我的作品
const myApps = ref<API.AppVO[]>([])
const myLoading = ref(false)
const myPageNum = ref(1)
const myPageSize = 6
const myTotal = ref(0)
const mySearchName = ref('')

// 精选案例
const goodApps = ref<API.AppVO[]>([])
const goodLoading = ref(false)
const goodPageNum = ref(1)
const goodPageSize = 6
const goodTotal = ref(0)
const goodSearchName = ref('')

// 获取我的应用列表
const fetchMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }
  myLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myPageNum.value,
      pageSize: myPageSize,
      appName: mySearchName.value.trim() || undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    myLoading.value = false
  }
}

// 获取精选应用列表
const fetchGoodApps = async () => {
  goodLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodPageNum.value,
      pageSize: goodPageSize,
      appName: goodSearchName.value.trim() || undefined,
      sortField: 'priority',
      sortOrder: 'desc',
    })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    goodLoading.value = false
  }
}

const onMyPageChange = (page:number)=>{
  myPageNum.value = page
  fetchMyApps()
}

const onGoodPageChange = (page:number)=>{
  goodPageNum.value = page
  fetchGoodApps()
}

const handleMySearch = ()=>{
  myPageNum.value = 1
  fetchMyApps()
}

const handleGoodSearch = ()=>{
  goodPageNum.value = 1
  fetchGoodApps()
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

const handleCreate = async () => {
  const prompt = promptText.value.trim()
  if (!prompt) {
    message.warning('请输入提示词')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push(`/user/login?redirect=${encodeURIComponent('/')}`)
    return
  }

  creating.value = true
  try {
    const res = await addApp({ initPrompt: prompt })
    if (res.data.code === 0 && res.data.data) {
      await router.push(`/app/chat/${res.data.data}?autoSend=1`)
    } else {
      message.error('创建失败，' + res.data.message)
    }
  } catch {
    message.error('创建失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<style scoped>
.home-page {
  margin: -24px;
  width: calc(100% + 48px);
}
.home-page {
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  min-height: 100vh;

  background: linear-gradient(
    180deg,
    #d8ebff 0%,
    #edf7ff 45%,
    #ffffff 100%
  );

  overflow-x: hidden;
}

.hero-section {
  padding: 60px 24px 48px;
  text-align: center;
  position: relative;
}

.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E");
  pointer-events: none;
}

.hero-title {
  position: relative;
  font-size: 48px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.88);
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.hero-logo {
  width: 44px;
  height: 44px;
}

.hero-subtitle {
  position: relative;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.45);
  margin: 0 0 36px;
}

.prompt-box {
  position: relative;
  max-width: 720px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.prompt-textarea {
  border: none !important;
  box-shadow: none !important;
  resize: none;
  font-size: 15px;
  padding: 0;
}

.prompt-textarea:focus {
  box-shadow: none !important;
}

.prompt-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.prompt-left-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  border-radius: 20px;
  font-size: 13px;
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-tags {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
  max-width: 720px;
  margin-left: auto;
  margin-right: auto;
}

.quick-tag {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}

.quick-tag:hover {
  background: rgba(255, 255, 255, 0.95);
  color: rgba(0, 0, 0, 0.88);
}

.apps-section {
  padding: 40px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  margin: 0;
}

.section-search {
  width: 240px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 992px) {
  .app-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .home-page {
    margin: -12px;
    width: calc(100% + 24px);
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-logo {
    width: 32px;
    height: 32px;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }

  .section-search {
    width: 100%;
  }
}

.blue-text {
  color: #1677ff;
  font-weight: 700;
}
</style>
