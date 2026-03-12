const { isLoggedIn } = require('../../utils/auth')

module.exports = {
  onLoad(options) {
    // 处理URL参数
    if (options.tab) {
      this.setData({ currentTab: options.tab })
    }
    
    if (options.userId && options.lastName && options.phone) {
      // 从成员列表跳转过来，预填用户信息
      const prefillData = {
        userId: options.userId,
        lastName: options.lastName,
        phone: options.phone
      }
      
      if (options.amount) {
        prefillData.amount = options.amount
      }
      
      this.setData({ prefillData })
    }
    
    this.checkLoginStatus()
  },

  onShow() {
    this.checkLoginStatus()
    
    // 检查是否有从成员列表传递过来的用户信息
    const app = getApp()
    if (app.globalData && app.globalData.memberForEyelash) {
      const memberInfo = app.globalData.memberForEyelash
      
      // 显示添加睫毛记录弹窗并预填信息
      if (this.showAddEyelashFromMember) {
        this.showAddEyelashFromMember({
          phone: memberInfo.phone,
          lastName: memberInfo.lastName
        })
      }
      
      // 清除全局数据
      app.globalData.memberForEyelash = null
    }
    
    // 检查充值记录
    if (app.globalData && app.globalData.memberForRecharge) {
      const memberInfo = app.globalData.memberForRecharge
      
      // 显示充值弹窗并预填信息
      if (this.showRechargeModal) {
        this.showRechargeModal({
          phone: memberInfo.phone,
          lastName: memberInfo.lastName
        })
      }
      
      // 清除全局数据
      app.globalData.memberForRecharge = null
    }
    
    // 检查消费记录
    if (app.globalData && app.globalData.memberForConsume) {
      const memberInfo = app.globalData.memberForConsume
      
      // 显示消费弹窗并预填信息
      if (this.showConsumeModal) {
        this.showConsumeModal({
          phone: memberInfo.phone,
          lastName: memberInfo.lastName,
          balance: memberInfo.balance
        })
      }
      
      // 清除全局数据
      app.globalData.memberForConsume = null
    }
  },

  checkLoginStatus() {
    const loggedIn = isLoggedIn()
    this.setData({ isLoggedIn: loggedIn })

    if (loggedIn) {
      this.loadUserInfo()
      if (this.data.currentTab === 'eyelash') {
        this.loadRecords()
      } else if (this.data.currentTab === 'consume') {
        this.loadConsumeRecords()
      }
    }
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    this.setData({ userInfo })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ currentTab: tab })

    if (tab === 'eyelash') {
      this.loadRecords()
    } else if (tab === 'consume') {
      this.loadConsumeRecords()
    }
  }
}
