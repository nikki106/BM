<template>
  <div>
    <div class="welcome">
      <div class="hello">{{ greeting }}，{{ userStore.name }}</div>
      <div class="date">{{ today }}</div>
    </div>

    <div class="stat-row">
      <div v-for="s in statCards" :key="s.label" class="stat-card">
        <div class="stat-icon" :style="{ background: s.color }">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div>
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="15">
        <el-card shadow="never" header="最近借阅">
          <el-table :data="borrowList" size="small">
            <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
            <el-table-column prop="readerName" label="读者" width="90" />
            <el-table-column label="借阅时间" width="155">
              <template #default="{ row }">{{ fmtTime(row.borrowTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="85">
              <template #default="{ row }">
                <el-tag size="small" :type="borrowStatus[row.status]?.type">{{ borrowStatus[row.status]?.text }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="never" header="最新公告">
          <div v-for="n in noticeList" :key="n.noticeId" class="notice-item">
            <div class="notice-title">{{ n.title }}</div>
            <div class="notice-time">{{ fmtTime(n.publishTime) }}</div>
          </div>
          <el-empty v-if="!noticeList.length" description="暂无公告" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useUserStore } from '@/store/user'
import { getStats, getBorrowPage, getNoticePage } from '@/api'
import { fmtTime, borrowStatus } from '@/utils/format'

dayjs.locale('zh-cn')

const userStore = useUserStore()

const today = dayjs().format('YYYY年M月D日 dddd')
const hour = new Date().getHours()
const greeting = hour < 6 ? '夜深了' : hour < 12 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'

const stats = ref({})
const borrowList = ref([])
const noticeList = ref([])

const statCards = computed(() => [
  { label: '馆藏图书', value: stats.value.bookTotal ?? 0, icon: 'Notebook', color: '#409eff' },
  { label: '读者总数', value: stats.value.readerTotal ?? 0, icon: 'User', color: '#67c23a' },
  { label: '借出中', value: stats.value.borrowing ?? 0, icon: 'Reading', color: '#e6a23c' },
  { label: '逾期未还', value: stats.value.overdue ?? 0, icon: 'Warning', color: '#f56c6c' }
])

onMounted(() => {
  getStats()
    .then((data) => (stats.value = data))
    .catch(() => {})

  const borrowParams = { pageNum: 1, pageSize: 6 }
  if (!userStore.isAdmin) borrowParams.readerId = userStore.readerId
  getBorrowPage(borrowParams)
    .then((data) => (borrowList.value = data.list))
    .catch(() => {})

  getNoticePage({ pageNum: 1, pageSize: 5 })
    .then((data) => (noticeList.value = data.list))
    .catch(() => {})
})
</script>

<style scoped>
.welcome {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.hello {
  font-size: 20px;
  font-weight: 600;
}

.date {
  font-size: 13px;
  color: #909399;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-num {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.notice-item {
  padding: 12px 4px;
  border-bottom: 1px solid #f0f2f5;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-size: 14px;
  margin-bottom: 6px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}
</style>
