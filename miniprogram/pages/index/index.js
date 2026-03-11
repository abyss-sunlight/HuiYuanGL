const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: null,
    stats: {
      totalMembers: 0,
      todayNew: 0,
      activeUsers: 0
    }
  },

  onLoad() {
    this.setData({ userInfo: getUserInfo() })
    this.loadStats()
  },

  onShow() {
    // 每次显示页面时重新加载统计数据
    this.setData({ userInfo: getUserInfo() })
    this.loadStats()
  },

  loadStats() {
    // 加载统计数据（这里先用模拟数据，后续可以调用真实接口）
    this.setData({
      stats: {
        totalMembers: 156,
        todayNew: 8,
        activeUsers: 42
      }
    })
  },

  goToRecords() {
    // 检查登录状态和权限
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看会员记录',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    if (userInfo.permissionLevel > 2) {
      wx.showToast({
        title: '权限不足，只有员工及以上权限可查看',
        icon: 'none'
      })
      return
    }

    wx.switchTab({ url: '/pages/records/records' })
  },

  goToProfile() {
    wx.switchTab({ url: '/pages/profile/profile' })
  },

  goToMembers() {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看成员列表',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    if (!userInfo || userInfo.permissionLevel > 2) {
      wx.showToast({
        title: '权限不足，只有员工及以上权限可查看',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({ url: '/pages/members/members' })
  },

  goToDiscountManagement() {
    console.log('点击充值折扣管理')
    
    if (!isLoggedIn()) {
      console.log('用户未登录')
      wx.showModal({
        title: '提示',
        content: '请先登录后管理充值折扣',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    console.log('用户信息:', userInfo)
    
    if (!userInfo) {
      console.log('用户信息为空')
      wx.showToast({
        title: '用户信息获取失败',
        icon: 'none'
      })
      return
    }
    
    if (userInfo.permissionLevel !== 1) {
      console.log('权限不足:', userInfo.permissionLevel)
      wx.showToast({
        title: '权限不足，只有店长权限可管理',
        icon: 'none'
      })
      return
    }

    console.log('准备跳转到充值折扣管理页面')
    wx.navigateTo({ 
      url: '/pages/discount-management/discount-management',
      success: () => {
        console.log('跳转成功')
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        })
      }
    })
  },

  viewReports() {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看报表',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    wx.showToast({
      title: '报表功能开发中',
      icon: 'none'
    })
  }
})
