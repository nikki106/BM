import request from '@/utils/request'

// 登录
export const login = (data) => request.post('/login', data)

// 图书
export const getBookPage = (params) => request.get('/book/page', { params })
export const getAllBooks = () => request.get('/book/all')
export const saveBook = (data) => (data.bookId ? request.put('/book', data) : request.post('/book', data))
export const deleteBook = (id) => request.delete(`/book/${id}`)

// 分类
export const getCategories = () => request.get('/category/list')
export const addCategory = (data) => request.post('/category', data)
export const deleteCategory = (id) => request.delete(`/category/${id}`)

// 读者
export const getReaderPage = (params) => request.get('/reader/page', { params })
export const getAllReaders = () => request.get('/reader/all')
export const saveReader = (data) => (data.readerId ? request.put('/reader', data) : request.post('/reader', data))
export const deleteReader = (id) => request.delete(`/reader/${id}`)

// 借阅
export const getBorrowPage = (params) => request.get('/borrow/page', { params })
export const addBorrow = (data) => request.post('/borrow', data)
export const returnBorrow = (id) => request.put(`/borrow/return/${id}`)
export const renewBorrow = (id) => request.put(`/borrow/renew/${id}`)

// 公告
export const getNoticePage = (params) => request.get('/notice/page', { params })
export const saveNotice = (data) => (data.noticeId ? request.put('/notice', data) : request.post('/notice', data))
export const deleteNotice = (id) => request.delete(`/notice/${id}`)

// 首页统计
export const getStats = () => request.get('/stats')
