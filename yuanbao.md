// 导入网络请求工具
const { request } = require('../../utils/request')
// 导入认证相关工具函数
const { isLoggedIn, getUserInfo, logout } = require('../../utils/auth')

// 页面对象定义
Page({
  // 页面数据初始化
  data: {
    isLoggedIn: false,           // 登录状态标识
    userInfo: null,             // 用户信息对象
    showSmsForm: false,         // 短信登录表单显示状态
    showPasswordForm: false,    // 密码登录表单显示状态
    showRegisterModal: false,   // 新用户注册模态弹窗显示状态
    showWxAuthModal: false,     // 微信授权弹窗显示状态
    
    // 短信登录相关数据
    smsPhone: '',               // 短信登录手机号
    smsCode: '',                // 短信验证码
    smsCodeSending: false,      // 短信验证码发送状态
    smsCodeText: '获取验证码',  // 短信验证码按钮文本
    smsLogging: false,          // 短信登录状态
    
    // 密码登录相关数据
    passwordPhone: '',          // 密码登录手机号
    password: '',               // 密码登录密码
    showPassword: false,        // 密码显示/隐藏状态
    passwordLogging: false,     // 密码登录状态
    
    // 新用户注册相关数据
    registerForm: {
      lastName: '',            // 姓氏
      gender: 1                // 性别（默认男性）
    },
    tempUserInfo: null,        // 临时用户信息（登录成功但需要补充信息）
    
    // 微信登录相关
    wxCode: '',                // 微信登录凭证
    wxUserInfo: null,          // 微信用户信息（昵称、头像等）
    gettingWxInfo: false       // 正在获取微信信息状态
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
  checkLoginStatus() {
    const loggedIn = isLoggedIn()
    const userInfo = getUserInfo()
    this.setData({
      isLoggedIn: loggedIn,
      userInfo: userInfo
    })
  },

  // 显示短信登录表单
  showSmsLogin() {
    this.setData({ 
      showSmsForm: true,
      showPasswordForm: false
    })
  },

  // 微信登录
  wxLogin() {
    // 先获取微信登录凭证
    wx.showLoading({
      title: '登录中...',
      mask: true
    })
    
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('微信登录凭证获取成功:', loginRes.code)
          
          // 保存code，准备后续使用
          this.setData({ wxCode: loginRes.code })
          
          // 显示授权确认弹窗
          this.setData({
            showWxAuthModal: true,
            gettingWxInfo: true
          })
          
          wx.hideLoading()
        } else {
          wx.hideLoading()
          wx.showToast({
            title: '获取登录凭证失败',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        wx.hideLoading()
        console.error('微信登录失败:', err)
        wx.showToast({
          title: '微信登录失败',
          icon: 'none'
        })
      }
    })
  },

  // 取消微信授权
  cancelWxAuth() {
    this.setData({
      showWxAuthModal: false,
      gettingWxInfo: false,
      wxUserInfo: null
    })
  },

  // 获取用户信息（包括头像、昵称）
  getUserProfile() {
    wx.getUserProfile({
      desc: '完善会员资料',
      success: (res) => {
        console.log('获取微信用户信息成功:', res)
        this.setData({
          wxUserInfo: res.userInfo,
          gettingWxInfo: false
        })
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err)
        this.setData({ gettingWxInfo: false })
        wx.showToast({
          title: '获取用户信息失败，请重试',
          icon: 'none'
        })
      }
    })
  },

  // 获取手机号
  getPhoneNumber(e) {
    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      console.log('获取手机号成功:', e.detail)
      
      const { encryptedData, iv } = e.detail
      const { wxCode, wxUserInfo } = this.data
      
      if (!encryptedData || !iv) {
        wx.showToast({
          title: '获取手机号失败',
          icon: 'none'
        })
        return
      }
      
      // 发送微信登录请求（包含手机号、昵称、头像）
      this.setData({ gettingWxInfo: true })
      
      request({
        url: '/api/auth/wx-login',
        method: 'POST',
        data: {
          code: wxCode,
          encryptedData: encryptedData,
          iv: iv,
          userInfo: wxUserInfo
        }
      }).then(response => {
        this.setData({
          showWxAuthModal: false,
          gettingWxInfo: false
        })
        
        console.log('微信登录响应:', response)
        
        if (response.code === 200) {
          // 检查是否是新用户（需要补充信息）
          this.handleLoginSuccess(response)
        } else {
          wx.showToast({
            title: response.message || '登录失败',
            icon: 'none'
          })
        }
      }).catch(error => {
        this.setData({
          showWxAuthModal: false,
          gettingWxInfo: false
        })
        console.error('微信登录请求失败:', error)
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      })
    } else {
      // 用户拒绝授权手机号
      this.setData({ gettingWxInfo: false })
      wx.showToast({
        title: '需要授权手机号才能登录',
        icon: 'none'
      })
    }
  },

  // 隐藏短信登录表单
  hideSmsLogin() {
    this.setData({ 
      showSmsForm: false,
      smsPhone: '',
      smsCode: ''
    })
  },

  // 短信手机号输入
  onSmsPhoneInput(e) {
    this.setData({ smsPhone: e.detail.value })
  },

  // 短信验证码输入
  onSmsCodeInput(e) {
    this.setData({ smsCode: e.detail.value })
  },

  // 发送短信验证码
  sendSmsCode() {
    const phone = this.data.smsPhone
    
    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    this.setData({ smsCodeSending: true })
    
    request({
      url: '/api/auth/send-sms',
      method: 'POST',
      data: { phone }
    }).then(() => {
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      this.startCountdown()
    }).catch((err) => {
      this.setData({ smsCodeSending: false })
      wx.showToast({
        title: err.message || '发送失败',
        icon: 'none'
      })
    })
  },

  // 开始倒计时
  startCountdown() {
    let count = 60
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        clearInterval(timer)
        this.setData({ 
          smsCodeText: '获取验证码',
          smsCodeSending: false
        })
      } else {
        this.setData({ smsCodeText: `${count}秒后重发` })
      }
    }, 1000)
  },

  // 短信登录提交
  smsLoginSubmit() {
    const { smsPhone, smsCode } = this.data
    
    if (!smsPhone || smsPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    
    if (!smsCode || smsCode.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }

    this.setData({ smsLogging: true })
    
    request({
      url: '/api/auth/sms-login',
      method: 'POST',
      data: {
        loginType: 'sms',
        phone: smsPhone,
        code: smsCode
      }
    }).then((response) => {
      this.handleLoginSuccess(response)
    }).catch((err) => {
      this.setData({ smsLogging: false })
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
    })
  },

  // 显示密码登录表单
  showPasswordLogin() {
    this.setData({ 
      showPasswordForm: true,
      showSmsForm: false
    })
  },

  // 隐藏密码登录表单
  hidePasswordLogin() {
    this.setData({ 
      showPasswordForm: false,
      passwordPhone: '',
      password: ''
    })
  },

  // 密码登录手机号输入
  onPasswordPhoneInput(e) {
    this.setData({ passwordPhone: e.detail.value })
  },

  // 密码输入
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  // 切换密码显示/隐藏
  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  // 密码登录提交
  passwordLoginSubmit() {
    const { passwordPhone, password } = this.data
    
    if (!passwordPhone || passwordPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ passwordLogging: true })
    
    request({
      url: '/api/auth/password-login',
      method: 'POST',
      data: {
        loginType: 'password',
        phone: passwordPhone,
        password: password
      }
    }).then((response) => {
      this.handleLoginSuccess(response)
    }).catch((err) => {
      this.setData({
        passwordLogging: false,
        password: ''
      })
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
    })
  },

  // 处理登录成功
  handleLoginSuccess(response) {
    console.log('处理登录成功:', response)
    wx.hideLoading()
    
    // 检查是否是新用户（需要补充信息）
    const isNewUser = !response.data?.lastName || 
                      response.data?.lastName === '用户' || 
                      response.data?.lastName === ''
    
    if (isNewUser) {
      // 新用户：显示注册模态弹窗
      this.setData({
        showRegisterModal: true,
        tempUserInfo: response.data || response
      })
    } else {
      // 老用户：正常登录
      this.completeLogin(response)
    }
  },
  
  // 完成登录流程
  completeLogin(response) {
    const userInfo = response.data || response
    
    // 保存token和用户信息到本地存储
    wx.setStorageSync('token', response.token || '')
    wx.setStorageSync('userInfo', {
      userId: userInfo.userId,
      username: userInfo.username,
      lastName: userInfo.lastName,
      phone: userInfo.phone,
      gender: userInfo.gender,
      memberNo: userInfo.memberNo,
      amount: userInfo.amount,
      discount: userInfo.discount,
      permissionLevel: userInfo.permissionLevel,
      permissionName: userInfo.permissionName,
      avatarUrl: userInfo.avatarUrl,
      openId: userInfo.openId,
      unionId: userInfo.unionId
    })
    
    wx.showToast({ title: '登录成功', icon: 'success' })

    // 清理表单状态
    this.setData({
      showSmsForm: false,
      showPasswordForm: false,
      showRegisterModal: false,
      showWxAuthModal: false,
      tempUserInfo: null,
      registerForm: { lastName: '', gender: 1 },
      wxCode: '',
      wxUserInfo: null,
      gettingWxInfo: false,

      smsPhone: '',
      smsCode: '',
      smsCodeSending: false,
      smsCodeText: '获取验证码',
      smsLogging: false,

      passwordPhone: '',
      password: '',
      showPassword: false,
      passwordLogging: false
    })

    // 更新页面状态
    this.checkLoginStatus()
  },

  // 查看帮助
  viewHelp() {
    wx.showToast({
      title: '帮助功能开发中',
      icon: 'none'
    })
  },

  // 查看关于
  viewAbout() {
    wx.showModal({
      title: '关于',
      content: '会员管理系统 v1.0\n\n基于 Spring Boot + 微信小程序开发\n提供完整的用户认证和会员管理功能',
      showCancel: false
    })
  },

  // 跳转设置密码
  goToSetPassword() {
    wx.navigateTo({
      url: '/pages/set-password/set-password'
    })
  },

  // ===== 新用户注册模态弹窗相关方法 =====
  
  // 输入姓氏
  onRegisterLastNameInput(e) {
    this.setData({
      'registerForm.lastName': e.detail.value
    })
  },

  // 选择性别
  onGenderChange(e) {
    this.setData({
      'registerForm.gender': parseInt(e.detail.value)
    })
  },

  // 取消注册
  cancelRegister() {
    this.setData({
      showRegisterModal: false,
      tempUserInfo: null,
      registerForm: { lastName: '', gender: 1 }
    })
  },

  // 提交注册信息
  submitRegister() {
    const { registerForm, tempUserInfo } = this.data
    
    if (!registerForm.lastName.trim()) {
      wx.showToast({
        title: '请输入姓氏',
        icon: 'none'
      })
      return
    }

    if (registerForm.lastName.trim().length > 10) {
      wx.showToast({
        title: '姓氏不能超过10个字符',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '提交中...',
      mask: true
    })

    const updateData = {
      lastName: registerForm.lastName.trim(),
      gender: registerForm.gender
    }

    request({
      url: '/api/users/' + tempUserInfo.userId,
      method: 'PUT',
      data: updateData
    }).then(response => {
      wx.hideLoading()
      
      const updatedUserInfo = {
        ...tempUserInfo,
        ...updateData
      }

      this.completeLogin({ data: updatedUserInfo, token: tempUserInfo.token })
      
      wx.showToast({
        title: '注册成功',
        icon: 'success'
      })
    }).catch(err => {
      wx.hideLoading()
      console.error('更新用户信息失败:', err)
      
      wx.showToast({
        title: '注册失败，请重试',
        icon: 'none'
      })
    })
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          
          this.setData({
            isLoggedIn: false,
            userInfo: null,
            showSmsForm: false,
            showPasswordForm: false,
            smsPhone: '',
            smsCode: '',
            smsCodeSending: false,
            smsCodeText: '获取验证码',
            smsLogging: false,
            passwordPhone: '',
            password: '',
            showPassword: false,
            passwordLogging: false
          })
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        }
      }
    })
  },
})