<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-title">📚 图书管理系统</div>
        <div class="brand-sub">Book Management System</div>
        <div class="brand-desc">让每一本书都被好好阅读</div>
      </div>
      <div class="form-side">
        <div class="form-title">欢迎登录</div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="doLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="doLogin">登 录</el-button>
        </el-form>
        <div class="tips">默认账号：admin / 123456（管理员）· reader01 / 123456（读者）</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const doLogin = async () => {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const data = await login({ username: form.username, password: form.password }).catch(() => null)
  loading.value = false
  if (!data) return

  userStore.setLogin(data)
  ElMessage.success(`欢迎回来，${data.name}`)
  router.push('/dashboard')
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3a5f 0%, #2d5aa0 55%, #4a90d9 100%);
}

.login-card {
  width: 780px;
  height: 420px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  background: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.brand {
  width: 340px;
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(160deg, #2b5fa8, #4a90d9);
  color: #fff;
}

.brand-title {
  font-size: 26px;
  font-weight: 600;
  margin-top: 48px;
}

.brand-sub {
  font-size: 13px;
  opacity: 0.75;
  margin-top: 8px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.9;
  margin-top: auto;
  margin-bottom: 8px;
}

.form-side {
  flex: 1;
  padding: 48px 48px 40px;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
}

.tips {
  margin-top: 20px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
