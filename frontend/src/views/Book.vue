<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-form inline @submit.prevent>
          <el-form-item>
            <el-input
              v-model="query.keyword"
              placeholder="书名 / 作者 / ISBN"
              clearable
              style="width: 220px"
              @keyup.enter="search"
              @clear="search"
            />
          </el-form-item>
          <el-form-item>
            <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 140px" @change="search">
              <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="search">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <div>
          <el-button @click="categoryVisible = true">分类管理</el-button>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增图书</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="bookId" label="编号" width="70" />
        <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="90">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="出版社" min-width="170" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" width="135" />
        <el-table-column label="价格" width="85" align="right">
          <template #default="{ row }">¥ {{ fmtPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column label="库存" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.stock > 0">{{ row.stock }}</span>
            <el-tag v-else size="small" type="danger">缺货</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入库时间" width="160">
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑图书' : '新增图书'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="80px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="dialog.form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="dialog.form.author" placeholder="作者（可选）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="dialog.form.publisher" placeholder="出版社（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="dialog.form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="dialog.form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="dialog.form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="dialog.form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryVisible" title="分类管理" width="420px">
      <div class="category-add">
        <el-input v-model="newCategory" placeholder="新分类名称" maxlength="20" @keyup.enter="saveCategory" />
        <el-button type="primary" @click="saveCategory">添加</el-button>
      </div>
      <el-table :data="categories" size="small" max-height="320">
        <el-table-column prop="categoryId" label="编号" width="70" />
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getBookPage, getCategories, saveBook, deleteBook, addCategory, deleteCategory } from '@/api'
import { fmtTime, fmtPrice } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null })

const load = async () => {
  loading.value = true
  const data = await getBookPage(query).catch(() => null)
  loading.value = false
  if (!data) return
  list.value = data.list
  total.value = data.total
}

const search = () => {
  query.pageNum = 1
  load()
}

const reset = () => {
  query.keyword = ''
  query.categoryId = null
  search()
}

const loadCategories = async () => {
  const data = await getCategories().catch(() => null)
  if (data) categories.value = data
}

// 新增 / 编辑
const dialog = reactive({ visible: false, isEdit: false, form: {} })
const formRef = ref()
const saving = ref(false)

const openDialog = (row) => {
  dialog.isEdit = !!row
  dialog.form = row
    ? { ...row }
    : { title: '', author: '', publisher: '', isbn: '', categoryId: null, price: undefined, stock: 1 }
  dialog.visible = true
}

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  const res = await saveBook(dialog.form).catch(() => null)
  saving.value = false
  if (!res) return
  ElMessage.success(dialog.isEdit ? '修改成功' : '新增成功')
  dialog.visible = false
  load()
}

const remove = async (row) => {
  const ok = await ElMessageBox.confirm(`确定要删除《${row.title}》吗？`, '删除图书', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '再想想'
  })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await deleteBook(row.bookId)
    .then(() => {
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

// 分类管理
const categoryVisible = ref(false)
const newCategory = ref('')

const saveCategory = async () => {
  const name = newCategory.value.trim()
  if (!name) return ElMessage.warning('先输入分类名称')
  const res = await addCategory({ categoryName: name }).catch(() => null)
  if (!res) return
  ElMessage.success('添加成功')
  newCategory.value = ''
  loadCategories()
}

const removeCategory = async (row) => {
  const ok = await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？`, '提示', { type: 'warning' })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await deleteCategory(row.categoryId)
    .then(() => {
      ElMessage.success('删除成功')
      loadCategories()
    })
    .catch(() => {})
}

onMounted(() => {
  load()
  loadCategories()
})
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

.category-add {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
</style>
