<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ensureCsrfToken } from '@/api/auth'
import { ApiClientError } from '@/api/http'
import ErrorState from '@/components/ErrorState.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 只接受站内绝对路径，防止 redirect 参数被用于开放重定向。
 */
function resolveRedirect(): string {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  return '/'
}

/**
 * 提交顺序：表单校验 → 确保 CSRF Cookie → 登录 → 回跳。
 * 失败时保留已填内容，只展示错误提示。
 */
async function onSubmit(): Promise<void> {
  if (submitting.value || !formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  errorMessage.value = ''
  try {
    // 登录是写请求，先确保拿到 CSRF Cookie
    await ensureCsrfToken()
    await auth.login({ username: form.username.trim(), password: form.password })
    await router.replace(resolveRedirect())
  } catch (error) {
    if (error instanceof ApiClientError) {
      // 403 通常是 CSRF token 失效：提示刷新而不是自动重试，避免重复提交
      errorMessage.value =
        error.code === 'FORBIDDEN' ? '安全校验失败，请刷新页面后重试' : error.message
    } else {
      errorMessage.value = '登录失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="login-page">
    <div class="card login-card">
      <h1 class="login-title">凳子记账</h1>
      <p class="login-subtitle">使用所有者账号登录；V1 不提供注册与找回密码。</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            maxlength="64"
            autocomplete="username"
            placeholder="请输入用户名"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            maxlength="72"
            autocomplete="current-password"
            placeholder="请输入密码"
            @keyup.enter="onSubmit"
          />
        </el-form-item>

        <ErrorState v-if="errorMessage" :message="errorMessage" :retryable="false" class="login-error" />

        <el-button
          class="login-submit"
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          {{ submitting ? '登录中…' : '登录' }}
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom));
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: var(--space-5);
}

.login-title {
  margin: 0 0 var(--space-1);
  font-size: 22px;
}

.login-subtitle {
  margin: 0 0 var(--space-5);
  color: var(--color-text-secondary);
  font-size: 13px;
}

.login-error {
  margin-bottom: var(--space-4);
}

.login-submit {
  width: 100%;
}
</style>
