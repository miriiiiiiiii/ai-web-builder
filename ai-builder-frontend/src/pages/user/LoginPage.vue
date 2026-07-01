<template>
  <div id="loginPage">
    <div class="login-container">
      <div class="login-header">
        <!-- logo + 标题 -->
        <div class="header-top-row">
          <a-image :src="logoUrl" :width="56" :preview="false" class="login-logo" />
          <h1 class="login-title">AI Builder</h1>
        </div>
        <!-- 副标题 -->
        <p class="login-subtitle">不写一行代码，生成完整前端应用</p>
      </div>

      <a-tabs v-model:activeKey="activeTab" centered class="login-tabs">
        <a-tab-pane key="account" tab="账号登录">
          <a-form
            :model="accountForm"
            :rules="accountRules"
            layout="vertical"
            class="login-form"
            @finish="handleAccountLogin"
          >
            <a-form-item name="userAccount">
              <a-input
                v-model:value="accountForm.userAccount"
                size="large"
                placeholder="请输入账号"
                allow-clear
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="userPassword">
              <a-input-password
                v-model:value="accountForm.userPassword"
                size="large"
                placeholder="请输入密码"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <div class="form-extra">
              <RouterLink to="/user/register">点击注册</RouterLink>
            </div>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting">
                登录
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="email" tab="邮箱登录">
          <a-form
            :model="emailForm"
            :rules="emailRules"
            layout="vertical"
            class="login-form"
            @finish="handleEmailLogin"
          >
            <a-form-item name="userEmail">
              <a-input
                v-model:value="emailForm.userEmail"
                size="large"
                placeholder="请输入邮箱"
                allow-clear
              >
                <template #prefix>
                  <MailOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="code">
              <div class="code-row">
                <a-input
                  v-model:value="emailForm.code"
                  size="large"
                  placeholder="请输入验证码"
                  class="code-input"
                >
                  <template #prefix>
                    <SafetyOutlined class="input-icon" />
                  </template>
                </a-input>
                <a-button
                  size="large"
                  class="code-btn"
                  :disabled="countdown > 0 || sendingCode"
                  :loading="sendingCode"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s 后重试` : '获取验证码' }}
                </a-button>
              </div>
            </a-form-item>

            <div class="form-extra">
              <RouterLink to="/user/register">点击注册</RouterLink>
            </div>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting">
                登录
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { loginByAccount, loginByEmail, sendEmailCode } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import logoUrl from '@/assets/logo.png'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const activeTab = ref('account')
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const accountForm = reactive<API.AccountLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const emailForm = reactive<API.EmailLoginRequest>({
  userEmail: '',
  code: '',
})

const accountRules: Record<string, Rule[]> = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const emailRules: Record<string, Rule[]> = {
  userEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const redirectAfterLogin = async () => {
  const redirect = route.query.redirect as string | undefined
  if (redirect) {
    window.location.href = redirect
  } else {
    await router.push('/')
  }
}

const handleLoginSuccess = async (data: API.LoginUserVO) => {
  loginUserStore.setLoginUser(data)
  message.success('登录成功')
  await redirectAfterLogin()
}

const handleAccountLogin = async () => {
  submitting.value = true
  try {
    const res = await loginByAccount({
      userAccount: accountForm.userAccount,
      userPassword: accountForm.userPassword,
    })

    if (res.data.code === 0 && res.data.data) {
      await handleLoginSuccess(res.data.data)
      return
    }

    message.error(res.data.message || '登录失败')
  } catch {
    message.error('登录失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleEmailLogin = async () => {
  submitting.value = true
  try {
    const res = await loginByEmail({
      userEmail: emailForm.userEmail,
      code: emailForm.code,
    })

    if (res.data.code === 0 && res.data.data) {
      await handleLoginSuccess(res.data.data)
      return
    }

    message.error(res.data.message || '登录失败')
  } catch {
    message.error('登录失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!emailForm.userEmail) {
    message.warning('请先输入邮箱')
    return
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(emailForm.userEmail)) {
    message.warning('请输入正确的邮箱格式')
    return
  }

  if (countdown.value > 0) {
    return
  }

  sendingCode.value = true
  try {
    const res = await sendEmailCode({ userEmail: emailForm.userEmail })

    if (res.data.code === 0) {
      message.success('验证码已发送')
      startCountdown()
      return
    }

    message.error(res.data.message || '验证码发送失败')
  } catch {
    message.error('验证码发送失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
#loginPage {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: linear-gradient(160deg, #eef4ff 0%, #f8fbff 35%, #e8f0ff 70%, #dce8ff 100%);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center; /* 整体全部水平居中 */
  gap: 8px; /* 标题行 和 副标题 的上下间距 */
  margin-bottom: 36px;
}

/* logo 和标题同行，垂直居中对齐 */
.header-top-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px; /* logo 和标题文字的左右间距 */
}

.login-logo {
  flex-shrink: 0;
}

.login-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.88);
  line-height: 1.2;
}

.login-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
  line-height: 1.5;
}

.login-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 28px;
}

.login-tabs :deep(.ant-tabs-tab) {
  font-size: 15px;
  padding: 8px 0;
}

.login-form {
  width: 100%;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  background: #fff;
  border-radius: 6px;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex-shrink: 0;
  min-width: 118px;
  background: #fff;
}

.form-extra {
  display: flex;
  justify-content: flex-end;
  margin: -4px 0 20px;
  font-size: 14px;
}

.form-extra a {
  color: #1677ff;
}

.form-extra a:hover {
  color: #4096ff;
}

.input-icon {
  color: rgba(0, 0, 0, 0.25);
}

@media (max-width: 576px) {
  #loginPage {
    padding: 24px 12px;
  }

  .login-title {
    font-size: 22px;
  }

  .login-subtitle {
    font-size: 13px;
  }

  .code-btn {
    min-width: 104px;
    padding-inline: 12px;
    font-size: 13px;
  }
}
</style>
