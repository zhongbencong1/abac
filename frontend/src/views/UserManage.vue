<template>
  <div>
    <h1>用户管理</h1>
    <p class="desc">维护用户及其主体属性，鉴权时可将用户作为主体参与 ABAC 校验。</p>
    <el-card style="margin-top: 16px;">
      <el-button type="primary" @click="openDialog()">新增用户</el-button>
      <el-table :data="list" style="margin-top: 16px;" border stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="displayName" label="显示名" width="120" />
        <el-table-column prop="subjectAttrs" label="主体属性（摘要）" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520" @close="form = {}">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="登录名，唯一" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="form.displayName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="主体属性 (JSON)" required>
          <el-input v-model="form.subjectAttrs" type="textarea" rows="5" placeholder='{"role":"admin","department":"IT"}' />
          <div class="help-block">
            <p><strong>怎么填？</strong></p>
            <p>主体属性表示「这个人」在鉴权时具备的属性，填一个 <strong>JSON 对象</strong>，键值对随意，但要和策略里用到的 <code>subject.xxx</code> 对应。</p>
            <p><strong>示例：</strong></p>
            <ul>
              <li>策略里有 <code>subject.role eq admin</code> → 这里就要有 <code>"role": "admin"</code></li>
              <li>策略里有 <code>subject.department in ["IT","HR"]</code> → 这里就要有 <code>"department": "IT"</code> 等</li>
            </ul>
            <p>常见写法：<code>{"role":"admin","department":"IT"}</code> 或 <code>{"role":"user","level":2}</code>。键名用英文，值为字符串或数字，整体必须是合法 JSON。</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({})

function openDialog(row) {
  form.value = row ? { ...row } : { username: '', displayName: '', subjectAttrs: '{}' }
  dialogVisible.value = true
}

async function load() {
  const { data } = await userApi.list()
  list.value = data || []
}

async function submit() {
  if (!form.value.username?.trim()) {
    ElMessage.warning('请填写用户名')
    return
  }
  const attrs = form.value.subjectAttrs?.trim()
  if (!attrs) {
    ElMessage.warning('请填写主体属性 JSON')
    return
  }
  try {
    JSON.parse(attrs)
  } catch (e) {
    ElMessage.warning('主体属性必须是合法 JSON 对象')
    return
  }
  try {
    if (form.value.id) {
      await userApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      const payload = {
        username: form.value.username?.trim(),
        displayName: form.value.displayName?.trim() || null,
        subjectAttrs: form.value.subjectAttrs?.trim() || '{}',
      }
      await userApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    const msg = e.response?.data?.message || (e.response?.data ? JSON.stringify(e.response.data) : null) || (e.message || '操作失败')
    if (!e.response) {
      ElMessage.error('请求失败，请确认后端已启动（http://localhost:8080）')
    } else {
      ElMessage.error(typeof msg === 'string' ? msg : '操作失败')
    }
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
  await userApi.delete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
h1 { margin-bottom: 8px; color: #303133; }
.desc { color: #606266; font-size: 13px; margin-bottom: 8px; }
.tip { color: #909399; font-size: 12px; margin-top: 4px; }
.help-block { margin-top: 8px; padding: 10px; background: #f5f7fa; border-radius: 4px; font-size: 12px; color: #606266; line-height: 1.6; }
.help-block p { margin: 0 0 6px 0; }
.help-block p:last-of-type { margin-bottom: 0; }
.help-block ul { margin: 4px 0 0 0; padding-left: 18px; }
.help-block li { margin-bottom: 2px; }
.help-block code { background: #e4e7ed; padding: 1px 6px; border-radius: 3px; font-size: 12px; }
</style>
