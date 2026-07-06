<template>
  <div id="registerPage">
    <div class="register-container">
      <div class="register-header">
        <div class="header-top-row">
          <a-image :src="logoUrl" :width="56" :preview="false" class="register-logo" />
          <h1 class="register-title">AI Builder</h1>
        </div>
        <p class="register-subtitle">不写一行代码，生成完整前端应用</p>
      </div>

      <a-form
        :model="formState"
        :rules="rules"
        layout="vertical"
        class="register-form"
        @finish="handleRegister"
      >
        <a-form-item name="nickName">
          <a-input
            v-model:value="formState.nickName"
            size="large"
            placeholder="请输入昵称"
            allow-clear
          >
            <template #prefix>
              <SmileOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="userAccount">
          <a-input
            v-model:value="formState.userAccount"
            size="large"
            placeholder="请输入账号"
            allow-clear
          >
            <template #prefix>
              <UserOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="userEmail">
          <a-input
            v-model:value="formState.userEmail"
            size="large"
            placeholder="请输入邮箱"
            allow-clear
          >
            <template #prefix>
              <MailOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            size="large"
            placeholder="请输入密码"
          >
            <template #prefix>
              <LockOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="checkPassword">
          <a-input-password
            v-model:value="formState.checkPassword"
            size="large"
            placeholder="请确认密码"
          >
            <template #prefix>
              <LockOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <div class="form-extra">
          <RouterLink to="/user/login">已有账号？去登录</RouterLink>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block :loading="submitting">
            注册
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { LockOutlined, MailOutlined, SmileOutlined, UserOutlined } from '@ant-design/icons-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { register } from '@/api/userController'
import logoUrl from '@/assets/logo.png'

const router = useRouter()
const submitting = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  nickName: '',
  userAccount: '',
  userEmail: '',
  userPassword: '',
  checkPassword: '',
})

const validateCheckPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请确认密码！')
  }
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules: Record<string, Rule[]> = {
  nickName: [
    { required: true, message: '昵称是必填项！' },
    { min: 4, max: 20, message: '昵称长度需为 4-20 个字符' },
  ],
  userAccount: [
    { required: true, message: '账号是必填项！' },
    {
      pattern: /^(?=.*[A-Za-z])[A-Za-z\d]{4,16}$/,
      message: '账号需为 4-16 位纯字母/字母数字的组合，且不能全为数字',
    },
  ],
  userEmail: [
    { required: true, message: '邮箱是必填项！', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确！', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '密码是必填项！' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/,
      message: '密码需为 8-20 位，且至少包含字母和数字',
    },
  ],
  checkPassword: [{ validator: validateCheckPassword, trigger: 'blur' }],
}

const handleRegister = async () => {
  submitting.value = true
  try {
    const res = await register({
      nickName: formState.nickName,
      userAccount: formState.userAccount,
      userEmail: formState.userEmail,
      userPassword: formState.userPassword,
      checkPassword: formState.checkPassword,
    })

    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      await router.push('/user/login')
      return
    }

    message.error(res.data.message || '注册失败')
  } catch {
    message.error('注册失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#registerPage {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: linear-gradient(160deg, #eef4ff 0%, #f8fbff 35%, #e8f0ff 70%, #dce8ff 100%);
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 36px;
}

.header-top-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.register-logo {
  flex-shrink: 0;
}

.register-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.88);
  line-height: 1.2;
}

.register-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
  line-height: 1.5;
}

.register-form {
  width: 100%;
}

.register-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.ant-input-affix-wrapper),
.register-form :deep(.ant-input) {
  background: #fff;
  border-radius: 6px;
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
  #registerPage {
    padding: 24px 12px;
  }

  .register-title {
    font-size: 22px;
  }

  .register-subtitle {
    font-size: 13px;
  }
}
</style>
