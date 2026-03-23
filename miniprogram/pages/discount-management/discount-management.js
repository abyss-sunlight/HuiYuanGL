/**
 * 充值折扣管理页面
 * 仅店长权限可访问
 */

const { isLoggedIn, getUserInfo } = require('../../utils/auth')

// 导入各功能模块
const apiModule = require('./discount-management.api')
const formModule = require('./discount-management.form')

Page(Object.assign(
  // 基础数据和生命周期
  {
    data: {
      userInfo: null,
      loading: false,
      
      // 折扣列表
      discountList: [],
      
      // 合并表单模块数据
      ...formModule.formData
    },

    onLoad() {
      console.log('充值折扣管理页面加载')
      this.checkLoginStatus()
      this.loadDiscountList()
    },

    onShow() {
      // 每次显示页面时检查登录状态
      this.checkLoginStatus()
    },

    /**
     * 检查登录状态和权限
     */
    checkLoginStatus() {
      const loggedIn = isLoggedIn()
      if (!loggedIn) {
        wx.showModal({
          title: '提示',
          content: '请先登录',
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              wx.switchTab({ url: '/pages/profile/profile' })
            } else {
              wx.navigateBack()
            }
          }
        })
        return
      }

      const userInfo = getUserInfo()
      this.setData({ userInfo })
      
      // 检查权限 - 只有店长可以访问
      if (userInfo.permissionLevel !== 1) {
        wx.showModal({
          title: '权限不足',
          content: '只有店长权限可以管理充值折扣',
          showCancel: false,
          success: () => {
            wx.navigateBack()
          }
        })
        return
      }
    }
  },
  
  // API模块方法
  (() => {
    const apiMethods = { ...apiModule }
    return apiMethods
  })(),
  
  // 表单模块方法
  (() => {
    const formMethods = { ...formModule }
    delete formMethods.formData  // 移除数据定义
    return formMethods
  })()
))
