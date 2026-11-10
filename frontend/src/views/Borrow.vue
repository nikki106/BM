<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-form inline @submit.prevent>
          <el-form-item>
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px" @change="search">
              <el-option v-for="(v, k) in borrowStatus" :key="k" :label="v.text" :value="Number(k)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="query.keyword"
              placeholder="书名 / 读者姓名"
              clearable
              style="width: 200px"
              @keyup.enter="search"
              @clear="search"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          </el-form-item>
        </el-form>
        <el-button v-if="userStore.isAdmin" type="primary" :icon="Plus" @click="openBorrow">办理借书</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="borrowId" label="编号" width="70" />
        <el-table-column prop="bookTitle" label="书名" min-width="170" show-overflow-tooltip />
        <el-table-column label="读者" width="130">
          <template #default="{ row }">
            <div>{{ row.readerName }}</div>
            <div class="card-no">{{ row.cardNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="借阅时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column label="应还时间" width="160">
          <template #default="{ row }">
            <div>{{ fmtTime(row.dueTime) }}</div>
            <div v-if="row.status === 2" class="overdue">已逾期 {{ overdueDays(row) }} 天</div>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.returnTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="85">
          <template #default="{ row }">
            <el-tag size="small" :type="borrowStatus[row.status]?.type">{{ borrowStatus[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="userStore.isAdmin" label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 1" link type="success" @click="returnBook(row)">还书</el-button>
            <el-button v-if="row.status === 0" link type="warning" @click="renew(row)">续借</el-button>
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

    <el-dialog v-model="borrowVisible" title="办理借书" width="480px" destroy-on-close>
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="70px">
        <el-form-item label="图书" prop="bookId">
          <el-select v-model="borrowForm.bookId" filterable placeholder="搜索书名" style="width: 100%">
            <el-option
              v-for="b in bookOptions"
              :key="b.bookId"
              :value="b.bookId"
              :label="`${b.title}（${b.author}）`"
              :disabled="b.stock === 0"
            >
              <div class="option-row">
                <span>{{ b.title }}</span>
                <span class="option-right">{{ b.stock > 0 ? `库存 ${b.stock}` : '缺货' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="borrowForm.readerId" filterable placeholder="搜索姓名 / 证号" style="width: 100%">
            <el-option
              v-for="r in readerOptions"
              :key="r.readerId"
              :value="r.readerId"
              :label="`${r.name}（${r.cardNo}）`"
            >
              <div class="option-row">
                <span>{{ r.name }} {{ r.cardNo }}</span>
                <span class="option-right">已借 {{ r.borrowing }}/{{ r.maxBorrow }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="借期" prop="days">
          <el-select v-model="borrowForm.days" style="width: 100%">
            <el-option :value="30" label="30 天" />
            <el-option :value="60" label="60 天" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认借书</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getBorrowPage, getAllBooks, getAllReaders, addBorrow, returnBorrow, renewBorrow } from '@/api'
import { useUserStore } from '@/store/user'
import { fmtTime, borrowStatus } from '@/utils/format'

const userStore = useUserStore()

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10, status: null, keyword: '', readerId: null })

const load = async () => {
  loading.value = true
  const data = await getBorrowPage(query).catch(() => null)
  loading.value = false
  if (!data) return
  list.value = data.list
  total.value = data.total
}

const search = () => {
  query.pageNum = 1
  load()
}

const overdueDays = (row) => dayjs().diff(dayjs(row.dueTime), 'day')

const returnBook = async (row) => {
  const ok = await ElMessageBox.confirm(`确认《${row.bookTitle}》已经归还？`, '还书确认', {
    type: 'warning',
    confirmButtonText: '确认归还',
    cancelButtonText: '取消'
  })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await returnBorrow(row.borrowId)
    .then(() => {
      ElMessage.success('归还成功')
      load()
    })
    .catch(() => {})
}

const renew = async (row) => {
  const newDue = dayjs(row.dueTime).add(30, 'day').format('YYYY-MM-DD')
  const ok = await ElMessageBox.confirm(`为《${row.bookTitle}》续借 30 天？新的应还日期为 ${newDue}`, '续借确认', { type: 'info' })
    .then(() => true)
    .catch(() => false)
  if (!ok) return
  await renewBorrow(row.borrowId)
    .then(() => {
      ElMessage.success('续借成功')
      load()
    })
    .catch(() => {})
}

// 办理借书
const borrowVisible = ref(false)
const borrowFormRef = ref()
const submitting = ref(false)
const bookOptions = ref([])
const readerOptions = ref([])

const borrowForm = reactive({ bookId: null, readerId: null, days: 30 })

const borrowRules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  readerId: [{ required: true, message: '请选择读者', trigger: 'change' }]
}

const openBorrow = async () => {
  borrowVisible.value = true
  const [books, readers] = await Promise.all([
    getAllBooks().catch(() => null),
    getAllReaders().catch(() => null)
  ])
  if (books) bookOptions.value = books
  if (readers) readerOptions.value = readers
}

const submit = async () => {
  const valid = await borrowFormRef.value.validate().catch(() => false)
  if (!valid || submitting.value) return
  submitting.value = true
  addBorrow({ bookId: borrowForm.bookId, readerId: borrowForm.readerId, days: borrowForm.days })
    .then(() => {
      ElMessage.success('借阅成功')
      borrowVisible.value = false
      load()
    })
    .catch(() => {})
    .finally(() => {
      submitting.value = false
    })
}

onMounted(() => {
  if (!userStore.isAdmin) query.readerId = userStore.readerId
  load()
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

.card-no {
  font-size: 12px;
  color: #909399;
}

.overdue {
  font-size: 12px;
  color: #f56c6c;
}
</style>
