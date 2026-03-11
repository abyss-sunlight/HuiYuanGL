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
