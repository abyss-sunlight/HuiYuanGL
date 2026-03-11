// 导入网络请求工具
const { request } = require('../../utils/request')
// 导入认证相关工具函数
const { getUserInfo } = require('../../utils/auth')

Page({
  data: {
    phone: '',
    code: '',
    newPassword: '',

    codeSending: false,
    codeText: '获取验证码',
    submitting: false
  },

  onLoad() {
    const userInfo = getUserInfo()
    if (userInfo && userInfo.phone) {
      this.setData({ phone: userInfo.phone })
    }
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ code: e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ newPassword: e.detail.value })
  },

  sendSmsCode() {
    const phone = this.data.phone

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    this.setData({ codeSending: true })

    request({
      url: '/api/auth/send-sms',
      method: 'POST',
      data: { phone }
    })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.startCountdown()
      })
      .catch((err) => {
        this.setData({ codeSending: false })
        wx.showToast({
          title: err.message || '发送失败',
          icon: 'none'
        })
      })
  },

  startCountdown() {
    let count = 60
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        clearInterval(timer)
        this.setData({
          codeText: '获取验证码',
          codeSending: false
        })
      } else {
        this.setData({ codeText: `${count}秒后重发` })
      }
    }, 1000)
  },

  submit() {
    const { phone, code, newPassword } = this.data

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    if (!code || code.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }

    if (!newPassword || newPassword.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    request({
      url: '/api/auth/set-password',
      method: 'POST',
      data: {
        phone,
        code,
        newPassword
      }
    })
      .then(() => {
        wx.showToast({ title: '设置成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 600)
      })
      .catch((err) => {
        wx.showToast({
          title: err.message || '设置失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ submitting: false })
      })
  }
})
