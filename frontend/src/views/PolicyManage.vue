<template>
  <div>
    <h1>策略配置</h1>
    <el-card style="margin-top: 16px;">
      <el-button type="primary" @click="openDialog()">新增策略</el-button>
      <el-table :data="list" style="margin-top: 16px;" border stripe>
        <el-table-column prop="name" label="策略名" width="140" />
        <el-table-column prop="effect" label="效果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.effect === 'allow' ? 'success' : 'danger'" size="small">{{ row.effect === 'allow' ? '允许' : '拒绝' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="enabled" label="启用" width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="toggleEnabled(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="ruleExpression" label="规则（摘要）" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑策略' : '新增策略'" width="640" @close="form = {}">
      <el-form :model="form" label-width="100px">
        <el-form-item label="策略名" required>
          <el-input v-model="form.name" placeholder="如：管理员可访问报表" />
        </el-form-item>
        <el-form-item label="效果" required>
          <el-radio-group v-model="form.effect">
            <el-radio value="allow">允许</el-radio>
            <el-radio value="deny">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="1000" />
          <span class="tip">数字越小越先匹配</span>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="规则表达式" required>
          <div class="rule-editor">
            <div v-for="(r, i) in ruleList" :key="i" class="rule-row">
              <el-select v-model="r.attribute" placeholder="属性" filterable style="width: 180px;">
                <el-option v-for="a in attributeOptions" :key="a.key" :label="a.label" :value="a.key" />
              </el-select>
              <el-select v-model="r.op" placeholder="操作符" style="width: 100px;">
                <el-option label="等于" value="eq" />
                <el-option label="不等于" value="neq" />
                <el-option label="属于" value="in" />
                <el-option label="不属于" value="not_in" />
                <el-option label="大于" value="gt" />
                <el-option label="大于等于" value="gte" />
                <el-option label="小于" value="lt" />
                <el-option label="小于等于" value="lte" />
                <el-option label="存在" value="exists" />
                <el-option label="不存在" value="not_exists" />
              </el-select>
              <el-input v-if="!['exists','not_exists'].includes(r.op)" v-model="r.value" placeholder="值（in 用逗号分隔）" style="width: 160px;" />
              <el-button type="danger" link @click="ruleList.splice(i, 1)">删</el-button>
            </div>
            <el-button type="primary" link @click="ruleList.push({ attribute: '', op: 'eq', value: '' })">+ 添加条件</el-button>
          </div>
          <el-input v-model="form.ruleExpression" type="textarea" rows="3" placeholder="或直接编辑 JSON" style="margin-top: 8px;" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" rows="2" />
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
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { policyApi, attributeApi } from '../api'

const list = ref([])
const attributes = ref([])
const dialogVisible = ref(false)
const form = ref({})
const ruleList = ref([])

const attributeOptions = ref([])

function buildAttributeOptions() {
  const opts = []
  for (const a of attributes.value) {
    const key = `${a.category}.${a.name}`
    opts.push({ key, label: `${a.category}.${a.name}` })
  }
  attributeOptions.value = opts
}

function openDialog(row) {
  form.value = row ? { ...row, enabled: row.enabled !== false } : { name: '', effect: 'allow', priority: 100, enabled: true, ruleExpression: '[]', description: '' }
  try {
    ruleList.value = form.value.ruleExpression ? JSON.parse(form.value.ruleExpression) : []
    if (!Array.isArray(ruleList.value)) ruleList.value = []
  } catch {
    ruleList.value = []
  }
  dialogVisible.value = true
}

function ruleListToExpression() {
  const arr = ruleList.value.filter(r => r.attribute && r.op)
  const valArr = arr.map(r => {
    const v = r.value
    if (r.op === 'in' || r.op === 'not_in') {
      const list = (typeof v === 'string' ? v.split(',').map(s => s.trim()).filter(Boolean) : Array.isArray(v) ? v : [])
      return { attribute: r.attribute, op: r.op, value: list }
    }
    return { attribute: r.attribute, op: r.op, value: v }
  })
  form.value.ruleExpression = JSON.stringify(valArr, null, 2)
}

watch(ruleList, () => ruleListToExpression(), { deep: true })

async function load() {
  const [p, a] = await Promise.all([policyApi.list(), attributeApi.list()])
  list.value = p.data || []
  attributes.value = a.data || []
  buildAttributeOptions()
}

async function submit() {
  ruleListToExpression()
  if (!form.value.name?.trim()) {
    ElMessage.warning('请填写策略名')
    return
  }
  if (!form.value.ruleExpression?.trim() || form.value.ruleExpression === '[]') {
    ElMessage.warning('请至少添加一条规则')
    return
  }
  try {
    if (form.value.id) {
      await policyApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await policyApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function toggleEnabled(row) {
  try {
    await policyApi.update(row.id, { ...row, enabled: row.enabled })
    ElMessage.success('已更新')
  } catch (e) {
    row.enabled = !row.enabled
    ElMessage.error('更新失败')
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该策略？', '提示', { type: 'warning' })
  await policyApi.delete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
h1 { margin-bottom: 8px; color: #303133; }
.tip { margin-left: 8px; color: #909399; font-size: 12px; }
.rule-editor .rule-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
</style>
