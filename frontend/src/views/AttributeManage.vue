<template>
  <div>
    <h1>属性管理</h1>
    <el-card style="margin-top: 16px;">
      <el-button type="primary" @click="openDialog()">新增属性</el-button>
      <el-table :data="list" style="margin-top: 16px;" border stripe>
        <el-table-column prop="name" label="属性名" width="160" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="valueType" label="值类型" width="100" />
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑属性' : '新增属性'" width="480" @close="form = {}">
      <el-form :model="form" label-width="100px">
        <el-form-item label="属性名" required>
          <el-input v-model="form.name" placeholder="如 role、department" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%;">
            <el-option label="主体 (subject)" value="subject" />
            <el-option label="资源 (resource)" value="resource" />
            <el-option label="环境 (environment)" value="environment" />
          </el-select>
        </el-form-item>
        <el-form-item label="值类型" required>
          <el-select v-model="form.valueType" placeholder="选择类型" style="width: 100%;">
            <el-option label="字符串" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="布尔" value="boolean" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" rows="2" placeholder="可选" />
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
import { attributeApi } from '../api'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({})

const categoryLabel = (c) => ({ subject: '主体', resource: '资源', environment: '环境' })[c] || c

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', category: 'subject', valueType: 'string', description: '' }
  dialogVisible.value = true
}

async function load() {
  const { data } = await attributeApi.list()
  list.value = data || []
}

async function submit() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请填写属性名')
    return
  }
  try {
    if (form.value.id) {
      await attributeApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await attributeApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该属性？', '提示', { type: 'warning' })
  await attributeApi.delete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
h1 { margin-bottom: 8px; color: #303133; }
</style>
