<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <div class="page-title">公告列表</div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">发布公告</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="noticeId" label="编号" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑公告' : '发布公告'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="dialog.form.title" placeholder="请输入公告标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="dialog.form.content"
            type="textarea"
            :rows="5"
            placeholder="请输入公告内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getNoticePage, saveNotice, deleteNotice } from '@/api'
import { fmtTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  const data = await getNoticePage(query).catch(() => null)
  loading.value = false
  if (!data) return
  list.value = data.list
  total.value = data.total
}

const search = () => {
  query.pageNum = 1
  load()
}

const dialog = reactive({ visible: false, isEdit: false, form: {} })
const formRef = ref()
const saving = ref(false)

const openDialog = (row) => {
  dialog.isEdit = !!row
  dialog.form = row ? { ...row } : { title: '', content: '' }
  dialog.visible = true
}

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  const res = await saveNotice(dialog.form).catch(() => null)
  saving.value = false
  if (!res) return
  ElMessage.success(dialog.isEdit ? '修改成功' : '发布成功')
  dialog.visible = false
  load()
}

const remove = async (row) => {
  const ok = await ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '删除公告', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '再想想'
  })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await deleteNotice(row.noticeId)
    .then(() => {
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
}
</style>
