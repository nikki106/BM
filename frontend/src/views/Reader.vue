<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-form inline @submit.prevent>
          <el-form-item>
            <el-input
              v-model="query.keyword"
              placeholder="姓名 / 证号 / 电话"
              clearable
              style="width: 220px"
              @keyup.enter="search"
              @clear="search"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增读者</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="cardNo" label="读者证号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="联系电话" width="130">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.phone }">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxBorrow" label="可借上限" width="90" align="center" />
        <el-table-column label="当前借阅" width="90" align="center">
          <template #default="{ row }">{{ row.borrowing }} / {{ row.maxBorrow }}</template>
        </el-table-column>
        <el-table-column label="违约次数" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.violationCount > 0" size="small" type="danger">{{ row.violationCount }} 次</el-tag>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑读者' : '新增读者'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="90px">
        <el-form-item label="读者证号" prop="cardNo">
          <el-input v-model="dialog.form.cardNo" placeholder="如 R20260006" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialog.form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="dialog.form.phone" placeholder="联系电话（可选）" />
        </el-form-item>
        <el-form-item label="可借上限" prop="maxBorrow">
          <el-input-number v-model="dialog.form.maxBorrow" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getReaderPage, saveReader, deleteReader } from '@/api'
import { fmtTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const load = async () => {
  loading.value = true
  const data = await getReaderPage(query).catch(() => null)
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
  dialog.form = row
    ? { ...row }
    : { cardNo: '', name: '', phone: '', maxBorrow: 5 }
  dialog.visible = true
}

const rules = {
  cardNo: [{ required: true, message: '请输入读者证号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  const res = await saveReader(dialog.form).catch(() => null)
  saving.value = false
  if (!res) return
  ElMessage.success(dialog.isEdit ? '修改成功' : '新增成功')
  dialog.visible = false
  load()
}

const remove = async (row) => {
  if (row.borrowing > 0) return ElMessage.warning('该读者还有未归还的图书，不能删除')
  const ok = await ElMessageBox.confirm(`确定要删除读者「${row.name}」吗？`, '删除读者', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '再想想'
  })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await deleteReader(row.readerId)
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
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.toolbar :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
