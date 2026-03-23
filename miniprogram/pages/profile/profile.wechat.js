// 导入网络请求工具
const { request } = require('../../utils/request')

// 微信登录模块
module.exports = {
  // 微信登录相关数据
  wechatData: {
    // 微信登录相关数据
    showWxRegisterModal: false,  // 微信新用户注册弹窗显示状态
    tempWxInfo: null,          // 临时微信用户信息
    wxRegisterForm: {
      lastName: '',              // 姓氏
      gender: 1                 // 性别（默认男性）
    },
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
  }
}
