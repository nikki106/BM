import dayjs from 'dayjs'

export const fmtTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-')

export const fmtPrice = (v) => Number(v || 0).toFixed(2)

export const borrowStatus = {
  0: { text: '借阅中', type: 'warning' },
  1: { text: '已归还', type: 'success' },
  2: { text: '已逾期', type: 'danger' }
}
