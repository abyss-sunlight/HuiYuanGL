// 导入网络请求工具
const { request } = require('../../utils/request')
// 导入认证相关工具函数
const { getUserInfo } = require('../../utils/auth')

// 密码设置弹窗模块
module.exports = {
  // 弹窗数据
  setPasswordData: {
    showSetPasswordModal: false,
    phone: '',
    code: '',
    newPassword: '',
    codeSending: false,
    codeText: '获取验证码',
    submitting: false,
    showPassword: false
  },

  // 弹窗控制方法
  // 显示密码设置弹窗
  showSetPassword() {
    const userInfo = getUserInfo()
    const phone = userInfo && userInfo.phone ? userInfo.phone : ''
    
    this.setData({
      'setPasswordData.showSetPasswordModal': true,
      'setPasswordData.phone': phone,
      'setPasswordData.code': '',
      'setPasswordData.newPassword': '',
      'setPasswordData.codeSending': false,
      'setPasswordData.codeText': '获取验证码',
      'setPasswordData.submitting': false,
      'setPasswordData.showPassword': false
    })
  },

  // 隐藏密码设置弹窗
  hideSetPassword() {
    this.setData({
      'setPasswordData.showSetPasswordModal': false
    })
  },

  // 点击遮罩层关闭弹窗
  onSetPasswordModalMask() {
    this.hideSetPassword()
  },

  // 手机号输入处理
  onSetPasswordPhoneInput(e) {
    this.setData({
      'setPasswordData.phone': e.detail.value
    })
  },

  // 验证码输入处理
  onSetPasswordCodeInput(e) {
    this.setData({
      'setPasswordData.code': e.detail.value
    })
  },

  // 新密码输入处理
  onSetPasswordNewPasswordInput(e) {
    this.setData({
      'setPasswordData.newPassword': e.detail.value
    })
  },

  // 切换密码显示/隐藏
  toggleSetPasswordVisibility() {
    this.setData({
      'setPasswordData.showPassword': !this.data.setPasswordData.showPassword
    })
  },

  // 发送短信验证码
  sendSetPasswordSmsCode() {
    const phone = this.data.setPasswordData.phone

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    this.setData({ 'setPasswordData.codeSending': true })

    request({
      url: '/api/auth/send-sms',
      method: 'POST',
      data: { phone }
    })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.startSetPasswordCountdown()
      })
      .catch((err) => {
        this.setData({ 'setPasswordData.codeSending': false })
        wx.showToast({
          title: err.message || '发送失败',
          icon: 'none'
        })
      })
  },

  // 开始倒计时
  startSetPasswordCountdown() {
    let count = 60
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        clearInterval(timer)
        this.setData({
          'setPasswordData.codeText': '获取验证码',
          'setPasswordData.codeSending': false
        })
      } else {
        this.setData({ 'setPasswordData.codeText': `${count}秒后重发` })
      }
    }, 1000)
  },

  // 提交密码设置
  submitSetPassword() {
    const { phone, code, newPassword } = this.data.setPasswordData

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

    this.setData({ 'setPasswordData.submitting': true })

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
          this.hideSetPassword()
        }, 600)
      })
      .catch((err) => {
        wx.showToast({
          title: err.message || '设置失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ 'setPasswordData.submitting': false })
      })
  }
}
