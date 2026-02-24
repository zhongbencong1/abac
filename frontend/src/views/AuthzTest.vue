<template>
  <div>
    <h1>鉴权校验</h1>
    <el-card style="margin-top: 16px;">
      <el-form :model="request" label-width="120px" style="max-width: 640px;">
        <el-form-item label="按用户加载主体">
          <el-select
            v-model="selectedUserId"
            placeholder="选择用户，将自动填充主体属性"
            clearable
            filterable
            style="width: 100%;"
            @change="onUserSelect"
          >
            <el-option
              v-for="u in userList"
              :key="u.id"
              :label="u.displayName || u.username"
              :value="u.id"
            >
              <span>{{ u.username }}</span>
              <span v-if="u.displayName" style="color: #909399; margin-left: 8px;">（{{ u.displayName }}）</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作 (action)">
          <el-input v-model="request.action" placeholder="如 read、write、delete" />
        </el-form-item>
        <el-form-item label="主体属性 (subject)">
          <el-input v-model="subjectJson" type="textarea" rows="4" placeholder='{"role":"admin","department":"IT"}' />
        </el-form-item>
        <el-form-item label="资源属性 (resource)">
          <el-input v-model="resourceJson" type="textarea" rows="4" placeholder='{"type":"report","sensitivity":"low"}' />
        </el-form-item>
        <el-form-item label="环境属性 (environment)">
          <el-input v-model="environmentJson" type="textarea" rows="2" placeholder='{} 可选' />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="check">发起鉴权</el-button>
        </el-form-item>
      </el-form>

      <el-divider>鉴权结果</el-divider>
      <el-alert v-if="result !== null" :title="result.allowed ? '允许' : '拒绝'" :type="result.allowed ? 'success' : 'error'" show-icon>
        <p>{{ result.reason }}</p>
        <p v-if="result.policyName">匹配策略：{{ result.policyName }}</p>
      </el-alert>
      <el-empty v-else description="填写上方信息后点击「发起鉴权」" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authzApi, userApi } from '../api'

const request = ref({ action: 'read' })
const subjectJson = ref('{"role":"admin","department":"IT"}')
const resourceJson = ref('{"type":"report","sensitivity":"low"}')
const environmentJson = ref('{}')
const loading = ref(false)
const result = ref(null)
const userList = ref([])
const selectedUserId = ref(null)

function parseJson(str, def = {}) {
  if (!str?.trim()) return def
  try {
    return JSON.parse(str)
  } catch {
    return def
  }
}

async function loadUsers() {
  try {
    const { data } = await userApi.list()
    userList.value = data || []
  } catch (e) {
    console.error(e)
  }
}

async function onUserSelect(userId) {
  if (!userId) return
  try {
    const { data } = await userApi.getSubjectAttrs(userId)
    subjectJson.value = JSON.stringify(data != null ? data : {}, null, 2)
    ElMessage.success('已从用户加载主体属性')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载失败')
  }
}

async function check() {
  const subject = parseJson(subjectJson.value)
  const resource = parseJson(resourceJson.value)
  const environment = parseJson(environmentJson.value)
  loading.value = true
  result.value = null
  try {
    const { data } = await authzApi.check({
      subject,
      resource,
      action: request.value.action || 'read',
      environment,
    })
    result.value = data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '鉴权请求失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<style scoped>
h1 { margin-bottom: 8px; color: #303133; }
</style>
