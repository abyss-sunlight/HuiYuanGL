const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: {},
    filters: {
      phone: '',
      lastName: '',
      permissionLevel: null
    },
    permissionOptions: [
      { label: '全部', value: null },
      { label: '店长', value: 1 },
      { label: '员工', value: 2 },
      { label: '会员', value: 3 },
      { label: '游客', value: 4 }
    ],
    permissionIndex: 0,
    list: [],
    statusText: ''
  },

  onLoad() {
    if (!isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }

    const userInfo = getUserInfo()
    if (!userInfo || userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足，只有员工及以上可查看', icon: 'none' })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
      return
    }

    // 设置用户信息到 data 中，用于权限控制
    this.setData({ userInfo })

    // 检查是否有预设的搜索参数
    const app = getApp()
    const searchParams = app.globalData.memberSearchParams
    
    if (searchParams) {
      // 应用预设的搜索参数
      const filters = {
        phone: searchParams.phone || '',
        lastName: searchParams.lastName || '',
        permissionLevel: null
      }
      
      // 设置权限选择器的索引
      let permissionIndex = 0
      if (searchParams.permissionLevel !== undefined) {
        permissionIndex = this.data.permissionOptions.findIndex(opt => opt.value === searchParams.permissionLevel)
        if (permissionIndex === -1) permissionIndex = 0
      }
      
      this.setData({
        filters,
        permissionIndex
      })
      
      // 清除全局搜索参数
      app.globalData.memberSearchParams = null
      
      // 执行搜索
      this.search()
    } else {
      // 没有预设参数，正常加载
      this.search()
    }
  },

  refreshData() {
    this.search()
  },

  onPhoneInput(e) {
    this.setData({ 'filters.phone': e.detail.value })
  },

  onLastNameInput(e) {
    this.setData({ 'filters.lastName': e.detail.value })
  },

  onPermissionChange(e) {
    const idx = Number(e.detail.value || 0)
    const opt = this.data.permissionOptions[idx]
    this.setData({
      permissionIndex: idx,
      'filters.permissionLevel': opt ? opt.value : null
    })
  },

  resetFilters() {
    this.setData({
      filters: { phone: '', lastName: '', permissionLevel: null },
      permissionIndex: 0
    })
    this.search()
  },

  // 添加美睫记录
  addEyelashRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 存储用户信息到全局数据，供records页面使用
    const app = getApp()
    app.globalData.memberForEyelash = {
      phone: user.phone,
      lastName: user.lastName,
      userId: user.userId
    }
    
    // 跳转到records页面，并设置参数
    wx.switchTab({
      url: '/pages/records/records',
      success: () => {
        // 延迟执行，确保页面已加载
        setTimeout(() => {
          // 通过事件通知records页面显示添加弹窗
          const pages = getCurrentPages()
          const recordsPage = pages.find(page => page.route === 'pages/records/records')
          if (recordsPage) {
            recordsPage.showAddEyelashFromMember({
              phone: user.phone,
              lastName: user.lastName
            })
          }
        }, 100)
      }
    })
  },

  // 添加会员消费记录
  addConsumeRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 存储用户信息到全局数据，供records页面使用
    const app = getApp()
    app.globalData.memberForConsume = {
      phone: user.phone,
      lastName: user.lastName,
      balance: user.balance || 0,
      userId: user.userId,
      type: 'consume' // 标记为消费模式
    }
    
    // 跳转到records页面
    wx.switchTab({
      url: '/pages/records/records',
      success: () => {
        // 延迟执行，确保页面已加载
        setTimeout(() => {
          const pages = getCurrentPages()
          const recordsPage = pages.find(page => page.route === 'pages/records/records')
          if (recordsPage) {
            recordsPage.showConsumeModal({
              phone: user.phone,
              lastName: user.lastName,
              balance: user.balance || 0
            })
          }
        }, 100)
      }
    })
  },

  // 添加充值记录
  addRechargeRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 存储用户信息到全局数据，供records页面使用
    const app = getApp()
    app.globalData.memberForRecharge = {
      phone: user.phone,
      lastName: user.lastName,
      balance: 0, // 游客余额为0
      userId: user.userId,
      type: 'recharge' // 标记为充值模式
    }
    
    // 跳转到records页面
    wx.switchTab({
      url: '/pages/records/records',
      success: () => {
        // 延迟执行，确保页面已加载
        setTimeout(() => {
          const pages = getCurrentPages()
          const recordsPage = pages.find(page => page.route === 'pages/records/records')
          if (recordsPage) {
            recordsPage.showRechargeModal({
              phone: user.phone,
              lastName: user.lastName
            })
          }
        }, 100)
      }
    })
  },

  // 编辑成员
  editMember(e) {
    const user = e.currentTarget.dataset.user
    wx.showToast({ title: '编辑功能开发中', icon: 'none' })
  },

  // 删除成员
  deleteMember(e) {
    const user = e.currentTarget.dataset.user
    wx.showModal({
      title: '确认删除',
      content: `确定要删除成员 ${user.username} 吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showToast({ title: '删除功能开发中', icon: 'none' })
        }
      }
    })
  },

  search() {
    this.setData({ statusText: '加载中...' })

    const { phone, lastName, permissionLevel } = this.data.filters
    const params = []

    if (phone) params.push(`phone=${encodeURIComponent(phone)}`)
    if (lastName) params.push(`lastName=${encodeURIComponent(lastName)}`)
    if (permissionLevel !== null && permissionLevel !== undefined) {
      params.push(`permissionLevel=${permissionLevel}`)
    }

    const url = params.length ? `/api/users?${params.join('&')}` : '/api/users'

    return request({ url })
      .then((data) => {
        const list = Array.isArray(data) ? data : []
        this.setData({ list, statusText: '' })
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '请求失败'
        this.setData({ statusText: '失败：' + msg })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  }
})
