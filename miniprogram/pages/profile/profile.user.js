// 导入网络请求工具
const { request } = require('../../utils/request')

// 用户信息管理模块
module.exports = {
  // 个人信息编辑相关数据
  userData: {
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
}
