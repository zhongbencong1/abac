<template>
  <div>
    <h1>审计日志</h1>
    <el-card style="margin-top: 16px;">
      <el-table :data="logs" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 'allow' ? 'success' : 'danger'" size="small">{{ row.result === 'allow' ? '允许' : '拒绝' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="policyName" label="匹配策略" width="140" show-overflow-tooltip />
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="subjectAttrs" label="主体属性" width="140" show-overflow-tooltip />
        <el-table-column prop="resourceAttrs" label="资源属性" width="140" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px;"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { auditApi } from '../api'

const logs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

function formatTime(str) {
  if (!str) return ''
  try {
    const d = new Date(str)
    return isNaN(d.getTime()) ? str : d.toLocaleString('zh-CN')
  } catch {
    return str
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await auditApi.logs(page.value - 1, size.value)
    logs.value = data?.content ?? []
    total.value = data?.totalElements ?? 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
h1 { margin-bottom: 8px; color: #303133; }
</style>
