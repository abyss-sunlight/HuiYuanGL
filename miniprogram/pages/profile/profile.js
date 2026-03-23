// 导入认证相关工具函数
const { isLoggedIn, getUserInfo, logout } = require('../../utils/auth')

// 导入各功能模块
const authModule = require('./profile.auth')
const userModule = require('./profile.user')
const wechatModule = require('./profile.wechat')
const modalModule = require('./profile.modal')
const setPasswordModule = require('./profile.set-password')
const changePhoneModule = require('./profile.change-phone')

// 页面对象定义
Page(Object.assign(
  // 基础数据
  {
    data: {
      isLoggedIn: false,           // 登录状态标识
      userInfo: null,             // 用户信息对象
      
      // 合并各模块数据
      ...authModule.authData,
      ...userModule.userData,
      ...wechatModule.wechatData,
      ...setPasswordModule.setPasswordData,
      ...changePhoneModule.changePhoneData
    },

    // 页面生命周期：页面加载时执行
    onLoad() {
      // 检查当前登录状态
      this.checkLoginStatus()
    },

    // 页面生命周期：页面显示时执行
    onShow() {
      // 每次显示页面都检查登录状态，确保状态同步
      this.checkLoginStatus()
    },

    // 检查登录状态方法
    // 从本地存储获取登录状态和用户信息，更新页面显示
    checkLoginStatus() {
      const loggedIn = isLoggedIn()    // 获取登录状态
      const userInfo = getUserInfo()   // 获取用户信息
      // 更新页面数据
      this.setData({
        isLoggedIn: loggedIn,
        userInfo: userInfo
      })
    },

    // 测试方法 - 验证模块导入
    testChangePhone() {
      console.log('测试方法被调用')
      console.log('changePhoneModule:', changePhoneModule)
      if (changePhoneModule && changePhoneModule.showChangePhone) {
        changePhoneModule.showChangePhone.call(this)
      } else {
        console.error('changePhoneModule 或 showChangePhone 方法不存在')
      }
    }
  },
  
  // 认证模块方法
  (() => {
    const authMethods = { ...authModule }
    delete authMethods.authData  // 移除数据定义
    return authMethods
  })(),
  
  // 用户管理模块方法
  (() => {
    const userMethods = { ...userModule }
    delete userMethods.userData  // 移除数据定义
    return userMethods
  })(),
  
  // 微信登录模块方法
  (() => {
    const wechatMethods = { ...wechatModule }
    delete wechatMethods.wechatData  // 移除数据定义
    return wechatMethods
  })(),
  
  // 弹窗管理模块方法
  modalModule,
  
  // 密码设置模块方法
  (() => {
    const setPasswordMethods = { ...setPasswordModule }
    delete setPasswordMethods.setPasswordData  // 移除数据定义
    return setPasswordMethods
  })(),
  
  // 手机号修改模块方法
  (() => {
    const changePhoneMethods = { ...changePhoneModule }
    delete changePhoneMethods.changePhoneData  // 移除数据定义
    return changePhoneMethods
  })()
))
