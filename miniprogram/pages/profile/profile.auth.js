// 导入网络请求工具
const { request } = require('../../utils/request')

// 认证相关模块
module.exports = {
  // 短信登录相关数据
  authData: {
    showSmsForm: false,         // 短信登录表单显示状态
    showPasswordForm: false,     // 密码登录表单显示状态
    showRegisterModal: false,    // 新用户注册模态弹窗显示状态
    
    // 短信登录相关数据
    smsPhone: '',               // 短信登录手机号
    smsCode: '',                // 短信验证码
    smsCodeSending: false,       // 短信验证码发送状态
    smsCodeText: '获取验证码',  // 短信验证码按钮文本
    smsLogging: false,          // 短信登录状态
    
    // 密码登录相关数据
    passwordPhone: '',          // 密码登录手机号
    password: '',              // 密码登录密码
    showPassword: false,       // 密码显示/隐藏状态
    passwordLogging: false,     // 密码登录状态
    
    // 新用户注册相关数据
    registerForm: {
      lastName: '',            // 姓氏
      gender: 1               // 性别（默认男性）
    },
    tempUserInfo: null,        // 临时用户信息（登录成功但需要补充信息）
  },

  // 显示短信登录表单
  // 切换到短信登录界面
  showSmsLogin() {
    this.setData({ showSmsForm: true })  // 显示短信登录表单
    this.hidePasswordLogin()
  },

  // 隐藏短信登录表单
  // 关闭短信登录界面并清空表单数据
  hideSmsLogin() {
    this.setData({ 
      showSmsForm: false,  // 隐藏短信登录表单
      smsPhone: '',        // 清空手机号
      smsCode: ''         // 清空验证码
    })
  },

  // 短信手机号输入
  // 处理短信登录手机号输入事件
  onSmsPhoneInput(e) {
    this.setData({ smsPhone: e.detail.value })  // 更新手机号
  },

  // 短信验证码输入
  // 处理短信验证码输入事件
  onSmsCodeInput(e) {
    this.setData({ smsCode: e.detail.value })  // 更新验证码
  },

  // 发送短信验证码
  // 调用后端接口发送短信验证码
  sendSmsCode() {
    const phone = this.data.smsPhone  // 获取输入的手机号
    
    // 验证手机号格式
    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    // 设置发送状态，防止重复点击
    this.setData({ smsCodeSending: true })
    
    // 调用后端发送短信接口
    request({
      url: '/api/auth/send-sms',
      method: 'POST',
      data: { phone }  // 发送手机号
    }).then(() => {
      // 发送成功
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      this.startCountdown()  // 开始倒计时
    }).catch((err) => {
      // 发送失败
      this.setData({ smsCodeSending: false })  // 重置发送状态
      wx.showToast({
        title: err.message || '发送失败',
        icon: 'none'
      })
    })
  },

  // 开始倒计时
  // 短信验证码发送后的60秒倒计时
  startCountdown() {
    let count = 60  // 倒计时60秒
    const timer = setInterval(() => {
      count--
      if (count <= 0) {
        // 倒计时结束
        clearInterval(timer)  // 清除定时器
        this.setData({ 
          smsCodeText: '获取验证码',  // 恢复按钮文本
          smsCodeSending: false        // 重置发送状态
        })
      } else {
        // 更新倒计时显示
        this.setData({ smsCodeText: `${count}秒后重发` })
      }
    }, 1000)  // 每秒执行一次
  },

  // 短信登录提交
  // 提交短信验证码进行登录
  smsLoginSubmit() {
    const { smsPhone, smsCode } = this.data  // 获取表单数据
    
    // 验证手机号格式
    if (!smsPhone || smsPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    
    // 验证验证码格式
    if (!smsCode || smsCode.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }

    // 设置登录状态，防止重复提交
    this.setData({ smsLogging: true })
    
    // 调用后端短信登录接口
    request({
      url: '/api/auth/sms-login',
      method: 'POST',
      data: {
        loginType: 'sms',  // 登录类型
        phone: smsPhone,    // 手机号
        code: smsCode       // 验证码
      }
    }).then((response) => {
      // 登录成功
      this.handleLoginSuccess(response)  // 统一处理登录成功
    }).catch((err) => {
      // 登录失败
      this.setData({ smsLogging: false })  // 重置登录状态
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
    })
  },

  // 显示密码登录表单
  // 切换到密码登录界面
  showPasswordLogin() {
    this.setData({ showPasswordForm: true })  // 显示密码登录表单
    this.hideSmsLogin()
  },

  // 隐藏密码登录表单
  // 关闭密码登录界面并清空表单数据
  hidePasswordLogin() {
    this.setData({ 
      showPasswordForm: false,  // 隐藏密码登录表单
      passwordPhone: '',       // 清空手机号
      password: ''            // 清空密码
    })
  },

  // 密码登录手机号输入
  // 处理密码登录手机号输入事件
  onPasswordPhoneInput(e) {
    this.setData({ passwordPhone: e.detail.value })  // 更新手机号
  },

  // 密码输入
  // 处理密码输入事件
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })  // 更新密码
  },

  // 切换密码显示/隐藏
  // 切换密码输入框的显示状态
  togglePassword() {
    console.log('togglePassword 被调用')
    console.log('当前 showPassword 状态:', this.data.showPassword)
    const newState = !this.data.showPassword
    console.log('新的 showPassword 状态:', newState)
    this.setData({ showPassword: newState })
  },

  // 密码登录提交
  // 提交手机号和密码进行登录
  passwordLoginSubmit() {
    const { passwordPhone, password } = this.data  // 获取表单数据
    
    // 验证手机号格式
    if (!passwordPhone || passwordPhone.length !== 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    
    // 验证密码不为空
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    // 设置登录状态，防止重复提交
    this.setData({ passwordLogging: true })
    
    // 调用后端密码登录接口
    request({
      url: '/api/auth/password-login',
      method: 'POST',
      data: {
        loginType: 'password',  // 登录类型
        phone: passwordPhone,    // 手机号
        password: password      // 密码
      }
    }).then((response) => {
      // 登录成功
      this.handleLoginSuccess(response)  // 统一处理登录成功
    }).catch((err) => {
      // 登录失败
      this.setData({
        passwordLogging: false,  // 重置登录状态
        password: ''             // 清空密码
      })
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
    })
  },

  // 处理登录成功
  // 统一处理登录成功后的逻辑
  handleLoginSuccess(response) {
    console.log('处理登录成功:', response)  // 调试日志
    wx.hideLoading()  // 隐藏加载提示
    
    // 检查是否是新用户（lastName为空或为默认值"用户"）
    const isNewUser = !response.lastName || response.lastName === '用户'
    
    if (isNewUser) {
      // 新用户：显示注册模态弹窗，保存临时信息
      this.setData({
        showRegisterModal: true,
        tempUserInfo: response
      })
    } else {
      // 老用户：正常登录
      this.completeLogin(response)
    }
  },
  
  // 完成登录流程
  // 保存用户信息并更新页面状态
  completeLogin(response) {
    // 保存token和用户信息到本地存储
    wx.setStorageSync('token', response.token)
    wx.setStorageSync('userInfo', {
      userId: response.userId,                    // 用户ID
      username: response.username,                // 用户名
      lastName: response.lastName,                // 姓氏
      phone: response.phone,                      // 用户手机号
      gender: response.gender,                    // 性别
      memberNo: response.memberNo,                // 会员号
      amount: response.amount,                    // 账户金额
      discount: response.discount,                 // 折扣率
      permissionLevel: response.permissionLevel,     // 权限等级
      permissionName: response.permissionName,       // 权限名称
      avatarUrl: response.avatarUrl               // 用户头像
    })
    
    wx.showToast({ title: '登录成功', icon: 'success' })  // 显示成功提示

    // 清理表单状态与 loading，避免按钮 loading 一直存在
    this.setData({
      showSmsForm: false,
      showPasswordForm: false,
      showRegisterModal: false,
      tempUserInfo: null,
      registerForm: {
        lastName: '',
        gender: 1
      },

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

    // 更新页面状态，刷新显示
    this.checkLoginStatus()
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
      registerForm: {
        lastName: '',
        gender: 1
      }
    })
  },

  // 提交注册信息
  submitRegister() {
    const { registerForm, tempUserInfo } = this.data
    
    // 表单验证
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

    // 显示加载提示
    wx.showLoading({
      title: '提交中...',
      mask: true
    })

    // 构造更新数据
    const updateData = {
      lastName: registerForm.lastName.trim(),
      gender: registerForm.gender
    }

    // 发送请求更新用户信息
    request({
      url: '/api/users/' + tempUserInfo.userId,
      method: 'PUT',
      data: updateData
    }).then(response => {
      wx.hideLoading()
      
      // 更新临时用户信息
      const updatedUserInfo = {
        ...tempUserInfo,
        ...updateData
      }

      // 完成登录流程
      this.completeLogin(updatedUserInfo)
      
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
  // 处理用户退出登录操作
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 用户确认退出
          // 清除本地存储
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          
          // 立即更新页面状态
          this.setData({
            isLoggedIn: false,        // 重置登录状态
            userInfo: null,           // 清空用户信息
            showSmsForm: false,      // 隐藏短信表单
            showPasswordForm: false,   // 隐藏密码表单
            smsPhone: '',            // 清空短信手机号
            smsCode: '',             // 清空短信验证码
            smsCodeSending: false,    // 清空发送状态
            smsCodeText: '获取验证码', // 恢复按钮文本
            smsLogging: false,        // 清空短信登录状态
            passwordPhone: '',        // 清空密码手机号
            password: '',            // 清空密码
            showPassword: false,      // 重置密码显示状态
            passwordLogging: false    // 清空密码登录状态
          })
          
          // 显示退出成功提示
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        }
      }
    })
  }
}
