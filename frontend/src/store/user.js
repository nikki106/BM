import { defineStore } from 'pinia'

const KEY = 'bm_user'

export const useUserStore = defineStore('user', {
  state: () => {
    const saved = JSON.parse(localStorage.getItem(KEY) || 'null')
    return saved || { token: '', username: '', role: '', name: '', readerId: null }
  },
  getters: {
    isAdmin: (s) => s.role === 'ADMIN'
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.username = data.username
      this.role = data.role
      this.name = data.name
      this.readerId = data.readerId ?? null
      localStorage.setItem(
        KEY,
        JSON.stringify({ token: this.token, username: this.username, role: this.role, name: this.name, readerId: this.readerId })
      )
    },
    logout() {
      localStorage.removeItem(KEY)
      this.$reset()
    }
  }
})
