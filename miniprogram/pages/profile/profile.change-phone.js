// 导入网络请求工具
const { request } = require('../../utils/request')
// 导入认证相关工具函数
const { getUserInfo } = require('../../utils/auth')

// 手机号修改弹窗模块
module.exports = {
  // 弹窗数据
  changePhoneData: {
    showChangePhoneModal: false,
    // 原手机号验证
    originalPhone: '',
    originalCode: '',
    originalCodeSending: false,
    originalCodeText: '获取验证码',
    // 新手机号验证
    newPhone: '',
    newCode: '',
    newCodeSending: false,
    newCodeText: '获取验证码',
    // 状态控制
    currentStep: 1, // 1: 验证原手机号, 2: 验证新手机号
    submitting: false
  },

  // 弹窗控制方法
  // 显示手机号修改弹窗
  showChangePhone() {
    const userInfo = getUserInfo()
    const phone = userInfo && userInfo.phone ? userInfo.phone : ''
    
    this.setData({
      'changePhoneData.showChangePhoneModal': true,
      'changePhoneData.originalPhone': phone,
      'changePhoneData.originalCode': '',
      'changePhoneData.newPhone': '',
      'changePhoneData.newCode': '',
      'changePhoneData.originalCodeSending': false,
      'changePhoneData.originalCodeText': '获取验证码',
      'changePhoneData.newCodeSending': false,
      'changePhoneData.newCodeText': '获取验证码',
      'changePhoneData.currentStep': 1,
      'changePhoneData.submitting': false
    })
  },

  // 隐藏手机号修改弹窗
  hideChangePhone() {
    this.setData({
      'changePhoneData.showChangePhoneModal': false
    })
  },

  // 点击遮罩层关闭弹窗
  onChangePhoneModalMask() {
    this.hideChangePhone()
  },

  // 原手机号验证码输入处理
  onOriginalCodeInput(e) {
    this.setData({
      'changePhoneData.originalCode': e.detail.value
    })
  },

  // 新手机号输入处理
  onNewPhoneInput(e) {
    this.setData({
      'changePhoneData.newPhone': e.detail.value
    })
  },

  // 新手机号验证码输入处理
  onNewCodeInput(e) {
    this.setData({
      'changePhoneData.newCode': e.detail.value
    })
  },

  // 发送原手机号验证码
  sendOriginalSmsCode() {
    const phone = this.data.changePhoneData.originalPhone

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    this.setData({ 'changePhoneData.originalCodeSending': true })

    request({
      url: '/api/auth/send-original-sms',
      method: 'POST',
      data: { phone }
    })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.startOriginalCountdown()
      })
      .catch((err) => {
        this.setData({ 'changePhoneData.originalCodeSending': false })
        wx.showToast({
          title: err.message || '发送失败',
          icon: 'none'
        })
      })
  },

  // 开始原验证码倒计时
  startOriginalCountdown() {
    let count = 60
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        clearInterval(timer)
        this.setData({
          'changePhoneData.originalCodeText': '获取验证码',
          'changePhoneData.originalCodeSending': false
        })
      } else {
        this.setData({ 'changePhoneData.originalCodeText': `${count}秒后重发` })
      }
    }, 1000)
  },

  // 验证原手机号
  verifyOriginalPhone() {
    const { originalPhone, originalCode } = this.data.changePhoneData

    if (!originalPhone || originalPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    if (!originalCode || originalCode.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }

    this.setData({ 'changePhoneData.submitting': true })

    request({
      url: '/api/auth/verify-original-phone',
      method: 'POST',
      data: {
        phone: originalPhone,
        code: originalCode
      }
    })
      .then(() => {
        wx.showToast({ title: '验证成功', icon: 'success' })
        this.setData({ 'changePhoneData.currentStep': 2 })
      })
      .catch((err) => {
        wx.showToast({
          title: err.message || '验证失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ 'changePhoneData.submitting': false })
      })
  },

  // 发送新手机号验证码
  sendNewSmsCode() {
    const phone = this.data.changePhoneData.newPhone

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    this.setData({ 'changePhoneData.newCodeSending': true })

    request({
      url: '/api/auth/send-new-sms',
      method: 'POST',
      data: { phone }
    })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.startNewCountdown()
      })
      .catch((err) => {
        this.setData({ 'changePhoneData.newCodeSending': false })
        wx.showToast({
          title: err.message || '发送失败',
          icon: 'none'
        })
      })
  },

  // 开始新验证码倒计时
  startNewCountdown() {
    let count = 60
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        clearInterval(timer)
        this.setData({
          'changePhoneData.newCodeText': '获取验证码',
          'changePhoneData.newCodeSending': false
        })
      } else {
        this.setData({ 'changePhoneData.newCodeText': `${count}秒后重发` })
      }
    }, 1000)
  },

  // 提交手机号修改
  submitPhoneChange() {
    const { originalPhone, originalCode, newPhone, newCode } = this.data.changePhoneData

    if (!newPhone || newPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    if (!newCode || newCode.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }

    if (originalPhone === newPhone) {
      wx.showToast({ title: '新手机号不能与原手机号相同', icon: 'none' })
      return
    }

    this.setData({ 'changePhoneData.submitting': true })

    request({
      url: '/api/auth/change-phone',
      method: 'POST',
      data: {
        originalPhone,
        originalCode,
        newPhone,
        newCode
      }
    })
      .then(() => {
        wx.showToast({ title: '修改成功', icon: 'success' })
        // 更新本地用户信息
        const userInfo = getUserInfo()
        if (userInfo) {
          userInfo.phone = newPhone
          wx.setStorageSync('userInfo', userInfo)
        }
        setTimeout(() => {
          this.hideChangePhone()
          // 刷新页面数据
          this.onLoad()
        }, 600)
      })
      .catch((err) => {
        wx.showToast({
          title: err.message || '修改失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ 'changePhoneData.submitting': false })
      })
  },

  // 返回上一步
  backToStep1() {
    this.setData({ 'changePhoneData.currentStep': 1 })
  }
}
