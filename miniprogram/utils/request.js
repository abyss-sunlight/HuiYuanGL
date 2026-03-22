// 统一请求封装
const app = getApp()
const { getToken, isLoggedIn } = require('./auth')

function request({ url, method = 'GET', data, header }) {
  return new Promise((resolve, reject) => {
    // 添加token到请求头
    const authHeader = {}
    if (isLoggedIn()) {
      authHeader['Authorization'] = `Bearer ${getToken()}`
    }

    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: {
        'content-type': 'application/json',
        ...authHeader,
        ...(header || {})
      },
      success(res) {
        // 后端约定返回：{ code, message, data } 或直接返回业务数据
        const body = res.data
        
        // 检查是否是直接返回的业务数据（如LoginResponse）
        if (body && typeof body === 'object' && body.token) {
          // 直接返回业务数据（这种情况发生在后端直接返回LoginResponse等对象）
          resolve(body)
          return
        }
        
        // 检查是否是ApiResponse包装的格式
        if (body && body.code === 0) {
          resolve(body.data)
          return
        }
        
        // 处理微信登录新用户需要补充信息的情况
        if (body && body.code === 201) {
          resolve(body)  // 直接返回完整的响应，让前端处理
          return
        }
        
        // 处理认证失败
        if (body && (body.code === 40101 || body.code === 40102 || body.code === 40103)) {
          // token失效，清除本地存储并跳转到个人中心页面
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.reLaunch({ url: '/pages/profile/profile' })
        }
        
        reject(body || { code: -1, message: '请求失败' })
      },
      fail(err) {
        reject({ code: -2, message: '网络错误', err })
      }
    })
  })
}

module.exports = {
  request
}
