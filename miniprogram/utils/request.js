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
        // 后端约定返回：{ code, message, data }
        const body = res.data
        if (body && body.code === 0) {
          resolve(body.data)
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
