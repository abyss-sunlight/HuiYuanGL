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
    
    // 微信登录相关数据
    showWxRegisterModal: false,  // 微信新用户注册弹窗显示状态
    tempWxInfo: null,          // 临时微信用户信息
    wxRegisterForm: {
      lastName: '',              // 姓氏
      gender: 1                 // 性别（默认男性）
    },
    
    // 个人信息编辑相关数据
    showPersonalInfoModal: false,  // 个人信息编辑弹窗显示状态
    showAvatarOptions: false,     // 头像选择器显示状态
    avatarOptions: [              // 头像选项
      '/images/toux1.jpeg',
      '/images/toux2.jpeg',
      '/images/toux3.jpeg',
      '/images/toux4.jpeg',
      '/images/toux5.jpeg'
    ],
    personalInfoForm: {
      avatarUrl: '',            // 头像URL
      username: '',             // 用户名
      lastName: '',             // 姓氏
      gender: 1                // 性别（默认男性）
    }
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

  // 显示短信登录表单
  // 切换到短信登录界面
  showSmsLogin() {
    this.setData({ showSmsForm: true })  // 显示短信登录表单
    this.hidePasswordLogin()
  },

  // 微信登录
  // 使用微信授权进行登录
  wxLogin() {
    wx.showModal({
      title: '微信登录授权',
      content: '为了给您提供更好的服务，需要获取您的微信昵称、头像和手机号',
      confirmText: '同意授权',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.performWxAuthorization()
        }
      }
    })
  },

  // 执行微信授权流程
  performWxAuthorization() {
    wx.showLoading({
      title: '获取授权中...',
      mask: true
    })

    // 先获取微信用户信息
    wx.getUserProfile({
      desc: '用于完善会员资料',
      success: (profileRes) => {
        console.log('获取微信用户信息成功:', profileRes)
        
        // 再获取手机号授权
        this.getPhoneNumber(profileRes)
      },
      fail: (err) => {
        console.error('获取微信用户信息失败:', err)
        wx.hideLoading()
        wx.showToast({
          title: '获取用户信息失败，请重试',
          icon: 'none'
        })
      }
    })
  },

  // 获取手机号
  getPhoneNumber(profileRes) {
    // 检查是否支持getPhoneNumber接口
    if (wx.getPhoneNumber) {
      wx.getPhoneNumber({
        success: (phoneRes) => {
          console.log('获取手机号成功:', phoneRes)
          this.handleWxLoginSuccess(profileRes, phoneRes)
        },
        fail: (err) => {
          console.error('获取手机号失败:', err)
          // 手机号获取失败，但继续登录流程
          this.handleWxLoginSuccess(profileRes, null)
        }
      })
    } else {
      // 如果不支持getPhoneNumber，直接使用用户信息登录
      this.handleWxLoginSuccess(profileRes, null)
    }
  },

  // 处理微信登录成功
  handleWxLoginSuccess(profileRes, phoneRes) {
    const userInfo = profileRes.userInfo
    let phoneNumber = null
    
    // 解析手机号
    if (phoneRes && phoneRes.code) {
      // 这里需要后端接口解析手机号，暂时保存code
      phoneNumber = phoneRes.code
    }

    // 保存微信信息到页面数据
    this.setData({
      tempWxInfo: {
        nickName: userInfo.nickName,
        avatarUrl: userInfo.avatarUrl,
        gender: userInfo.gender,
        city: userInfo.city,
        province: userInfo.province,
        country: userInfo.country,
        phoneNumber: phoneNumber
      }
    })

    wx.hideLoading()
    
    // 尝试登录
    this.attemptWxLogin()
  },

  // 尝试微信登录
  attemptWxLogin() {
    const { tempWxInfo } = this.data
    
    wx.showLoading({
      title: '登录中...',
      mask: true
    })

    // 获取微信登录凭证
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('微信登录凭证获取成功:', loginRes.code)
          
          // 发送登录请求到后端
          request({
            url: '/api/auth/wx-login',
            method: 'POST',
            data: {
              code: loginRes.code,
              userInfo: {
                nickName: tempWxInfo.nickName,
                avatarUrl: tempWxInfo.avatarUrl,
                gender: tempWxInfo.gender,
                city: tempWxInfo.city,
                province: tempWxInfo.province,
                country: tempWxInfo.country,
                phoneNumber: tempWxInfo.phoneNumber
              }
            }
          }).then(response => {
            this.handleWxLoginResponse(response, tempWxInfo)
          }).catch(error => {
            wx.hideLoading()
            console.error('微信登录请求失败:', error)
            wx.showToast({
              title: '网络错误，请重试',
              icon: 'none'
            })
          })
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

  // 处理微信登录响应
  handleWxLoginResponse(response, tempWxInfo) {
    wx.hideLoading()
    console.log('微信登录响应:', response)
    
    if (response.code === 200) {
      // 登录成功
      const userInfo = response.data
      wx.setStorageSync('userInfo', userInfo)
      wx.setStorageSync('token', response.token || '')
      
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      })
      
      // 刷新页面状态
      this.checkLoginStatus()
    } else if (response.code === 201) {
      // 新用户，需要补充信息
      this.setData({
        showWxRegisterModal: true,
        tempWxInfo: tempWxInfo
      })
    } else {
      wx.showToast({
        title: response.message || '登录失败',
        icon: 'none'
      })
    }
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

  // 微信新用户注册相关方法
  // 显示微信新用户注册弹窗
  showWxRegisterModal() {
    this.setData({ showWxRegisterModal: true })
  },

  // 隐藏微信新用户注册弹窗
  hideWxRegisterModal() {
    this.setData({ 
      showWxRegisterModal: false,
      wxRegisterForm: {
        lastName: '',
        gender: 1
      }
    })
  },

  // 微信注册表单姓氏输入
  onWxLastNameInput(e) {
    this.setData({
      'wxRegisterForm.lastName': e.detail.value
    })
  },

  // 微信注册表单性别选择
  onWxGenderChange(e) {
    this.setData({
      'wxRegisterForm.gender': parseInt(e.detail.value)
    })
  },

  // 提交微信新用户注册
  submitWxRegister() {
    const { wxRegisterForm, tempWxInfo } = this.data
    
    // 验证表单
    if (!wxRegisterForm.lastName.trim()) {
      wx.showToast({
        title: '请输入姓氏',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '注册中...',
      mask: true
    })

    // 获取微信登录凭证
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          // 发送注册请求到后端
          request({
            url: '/api/auth/wx-login',
            method: 'POST',
            data: {
              code: loginRes.code,
              userInfo: {
                nickName: tempWxInfo.nickName,
                avatarUrl: tempWxInfo.avatarUrl,
                gender: wxRegisterForm.gender,
                city: tempWxInfo.city,
                province: tempWxInfo.province,
                country: tempWxInfo.country,
                phoneNumber: tempWxInfo.phoneNumber,
                lastName: wxRegisterForm.lastName  // 添加用户输入的姓氏
              }
            }
          }).then(response => {
            wx.hideLoading()
            console.log('微信注册响应:', response)
            console.log('响应类型:', typeof response)
            
            // 检查响应是否包含token（直接返回LoginResponse的情况）
            if (response && response.token) {
              // 注册成功，直接使用响应作为用户信息
              const userInfo = response
              wx.setStorageSync('userInfo', userInfo)
              wx.setStorageSync('token', response.token || '')
              
              console.log('保存的用户信息:', userInfo)
              console.log('保存的token:', response.token)
              
              // 关闭弹窗
              this.hideWxRegisterModal()
              
              wx.showToast({
                title: '注册成功',
                icon: 'success'
              })
              
              // 刷新页面状态
              this.checkLoginStatus()
            } else if (response && response.code === 201) {
              // 新用户需要补充信息的情况
              console.log('新用户需要补充信息')
              wx.showToast({
                title: '需要补充信息',
                icon: 'none'
              })
            } else {
              console.log('注册失败，响应:', response)
              wx.showToast({
                title: '注册失败',
                icon: 'none'
              })
            }
          }).catch(error => {
            wx.hideLoading()
            console.error('微信注册请求失败:', error)
            wx.showToast({
              title: '网络错误，请重试',
              icon: 'none'
            })
          })
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

  // 查看关于
  // 显示应用关于信息
  viewAbout() {
    wx.showModal({
      title: '关于',
      content: '会员管理系统 v1.0\n\n基于 Spring Boot + 微信小程序开发\n提供完整的用户认证和会员管理功能',
      showCancel: false  // 只显示确定按钮
    })
  },

  // 跳转设置密码
  // 进入设置/重置密码页面（通过短信验证码）
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
  },

  // 个人信息编辑相关方法
  
  // 显示个人信息编辑弹窗
  showPersonalInfo() {
    const { userInfo } = this.data
    
    // 初始化表单数据为当前用户信息
    this.setData({
      showPersonalInfoModal: true,
      personalInfoForm: {
        avatarUrl: userInfo.avatarUrl || '',
        username: userInfo.username || '',
        lastName: userInfo.lastName || '',
        gender: userInfo.gender || 1
      }
    })
  },

  // 隐藏个人信息编辑弹窗
  hidePersonalInfoModal() {
    this.setData({
      showPersonalInfoModal: false,
      showAvatarOptions: false  // 关闭弹窗时重置头像选择器状态
    })
  },

  // 切换头像选择器显示状态
  toggleAvatarSelector() {
    this.setData({
      showAvatarOptions: !this.data.showAvatarOptions
    })
  },

  // 选择头像
  selectAvatar(e) {
    const index = e.currentTarget.dataset.index
    const { avatarOptions } = this.data
    const selectedAvatar = avatarOptions[index]
    
    this.setData({
      'personalInfoForm.avatarUrl': selectedAvatar,
      showAvatarOptions: false  // 选择后自动关闭选择器
    })
  },

  // 用户名输入
  onUsernameInput(e) {
    this.setData({
      'personalInfoForm.username': e.detail.value
    })
  },

  // 姓氏输入
  onLastNameInput(e) {
    this.setData({
      'personalInfoForm.lastName': e.detail.value
    })
  },

  // 性别选择
  onGenderChange(e) {
    this.setData({
      'personalInfoForm.gender': parseInt(e.detail.value)
    })
  },

  // 提交个人信息更新
  submitPersonalInfo() {
    const { personalInfoForm } = this.data
    
    // 表单验证
    if (!personalInfoForm.username.trim()) {
      wx.showToast({
        title: '请输入用户名',
        icon: 'none'
      })
      return
    }
    
    if (!personalInfoForm.lastName.trim()) {
      wx.showToast({
        title: '请输入姓氏',
        icon: 'none'
      })
      return
    }
    
    wx.showLoading({
      title: '保存中...',
      mask: true
    })
    
    // 调用后端接口更新个人信息
    request({
      url: '/api/users/update-profile',
      method: 'PUT',
      data: personalInfoForm
    }).then(response => {
      wx.hideLoading()
      
      // 更新本地存储的用户信息
      const updatedUserInfo = {
        ...this.data.userInfo,
        ...personalInfoForm
      }
      wx.setStorageSync('userInfo', updatedUserInfo)
      
      // 更新页面状态
      this.setData({
        userInfo: updatedUserInfo,
        showPersonalInfoModal: false
      })
      
      wx.showToast({
        title: '保存成功',
        icon: 'success'
      })
    }).catch(error => {
      wx.hideLoading()
      console.error('更新个人信息失败:', error)
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
    })
  }
})
