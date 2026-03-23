// 弹窗管理模块
module.exports = {
  // 弹窗相关数据已合并到其他模块中，这里主要提供弹窗控制方法

  // 查看关于
  // 显示应用关于信息
  viewAbout() {
    wx.showModal({
      title: '关于',
      content: '会员管理系统 v1.0\n\n基于 Spring Boot + 微信小程序开发\n提供完整的用户认证和会员管理功能',
      showCancel: false  // 只显示确定按钮
    })
  },

  // 设置密码
  // 显示密码设置弹窗（通过短信验证码）
  showSetPasswordModal() {
    // 调用密码设置模块的显示方法
    if (this.showSetPassword) {
      this.showSetPassword()
    }
  },

  // 通用弹窗显示方法
  showModal(options) {
    const defaultOptions = {
      title: '提示',
      showCancel: true,
      confirmText: '确定',
      cancelText: '取消'
    }
    
    wx.showModal({
      ...defaultOptions,
      ...options
    })
  },

  // 通用Toast提示方法
  showToast(options) {
    const defaultOptions = {
      title: '操作成功',
      icon: 'success',
      duration: 2000
    }
    
    wx.showToast({
      ...defaultOptions,
      ...options
    })
  },

  // 显示加载提示
  showLoading(options) {
    const defaultOptions = {
      title: '加载中...',
      mask: true
    }
    
    wx.showLoading({
      ...defaultOptions,
      ...options
    })
  },

  // 隐藏加载提示
  hideLoading() {
    wx.hideLoading()
  },

  // 显示操作菜单
  showActionSheet(options) {
    const defaultOptions = {
      itemList: [],
      itemColor: '#000000'
    }
    
    wx.showActionSheet({
      ...defaultOptions,
      ...options
    })
  }
}
