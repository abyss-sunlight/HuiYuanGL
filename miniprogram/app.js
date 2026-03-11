// 小程序入口文件
const { checkLoginAndRedirect } = require('./utils/auth')

App({
  globalData: {
    // 后端地址（开发环境可改成你本机IP，例如 http://192.168.1.10:8080）
    baseUrl: 'http://localhost:8080',
    // 成员列表搜索参数（用于页面间传递）
    memberSearchParams: null
  },

  onLaunch() {
    // 小程序启动时不再强制检查登录状态
    // 用户可以在主页浏览，需要时再登录
    console.log('会员管理系统启动')
  },

  onShow() {
    // 每次显示时也不再强制检查登录状态
    console.log('小程序显示')
  }
})
