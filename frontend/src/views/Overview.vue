<template>
  <div class="overview">
    <h1>ABAC 鉴权系统</h1>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户</template>
          <div class="stat">{{ stats.userCount }}</div>
          <p class="desc">已维护用户数量</p>
          <el-button type="primary" text @click="$router.push('/users')">用户管理</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>属性</template>
          <div class="stat">{{ stats.attributeCount }}</div>
          <p class="desc">已配置属性数量</p>
          <el-button type="primary" text @click="$router.push('/attributes')">管理属性</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>策略</template>
          <div class="stat">{{ stats.policyCount }}</div>
          <p class="desc">已启用策略数量</p>
          <el-button type="primary" text @click="$router.push('/policies')">配置策略</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>审计</template>
          <div class="stat">—</div>
          <p class="desc">鉴权记录与审计日志</p>
          <el-button type="primary" text @click="$router.push('/audit')">查看日志</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>快速鉴权</template>
      <p>在「鉴权校验」页填写主体、资源、操作与环境属性后发起校验，结果会记录到审计日志。</p>
      <el-button type="primary" @click="$router.push('/authz')">去鉴权</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { attributeApi, policyApi, userApi } from '../api'

const stats = ref({ userCount: 0, attributeCount: 0, policyCount: 0 })

onMounted(async () => {
  try {
    const [u, a, p] = await Promise.all([userApi.list(), attributeApi.list(), policyApi.list()])
    stats.value.userCount = u.data?.length ?? 0
    stats.value.attributeCount = a.data?.length ?? 0
    stats.value.policyCount = (p.data?.filter(x => x.enabled) ?? []).length
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.overview h1 { margin-bottom: 8px; color: #303133; }
.stat { font-size: 28px; font-weight: bold; color: #409EFF; }
.desc { color: #909399; font-size: 12px; margin: 8px 0; }
</style>
