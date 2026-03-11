const { request } = require('../../utils/request')

module.exports = {
  showAddRecordModal() {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    // 显示提示信息，添加功能已移除
    wx.showToast({
      title: '添加睫毛记录功能已移除',
      icon: 'none',
      duration: 2000
    })
  },

  showAddConsumeModal() {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    // 显示提示信息，添加功能已移除
    wx.showToast({
      title: '添加消费记录功能已移除',
      icon: 'none',
      duration: 2000
    })
  },

  stopPropagation() {
    // 阻止点击弹窗内容区域时关闭弹窗
  },

  showDeleteRecordModal(e) {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    const record = e.currentTarget.dataset.record
    const type = e.currentTarget.dataset.type
    const recordWithType = { ...record, type }

    this.setData({
      showDeleteModal: true,
      deleteRecord: recordWithType
    })
  },

  hideDeleteModal() {
    this.setData({
      showDeleteModal: false,
      deleteRecord: null
    })
  },

  confirmDelete() {
    const { deleteRecord } = this.data
    if (!deleteRecord) return

    wx.showLoading({ title: '删除中...' })

    const url = deleteRecord.type === 'eyelash'
      ? `/api/eyelash-records/${deleteRecord.id}`
      : `/api/consume-records/${deleteRecord.id}`

    request({
      url,
      method: 'DELETE'
    })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '删除成功', icon: 'success' })

        this.hideDeleteModal()

        if (deleteRecord.type === 'eyelash') {
          this.loadRecords()
        } else {
          this.loadConsumeRecords()
        }
      })
      .catch((err) => {
        wx.hideLoading()
        const msg = err && err.message ? err.message : '删除失败'
        wx.showToast({ title: msg, icon: 'none' })
      })
  },

  goToLogin() {
    wx.switchTab({ url: '/pages/profile/profile' })
  },

  showLoginModal() {
    wx.showModal({
      title: '请先登录',
      content: '只有登录后才能查看睫毛记录',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          this.goToLogin()
        }
      }
    })
  },

  showAddRecordForCustomer(e) {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    const record = e.currentTarget.dataset.record
    const type = e.currentTarget.dataset.type
    
    // 将客户信息存储到全局数据，供添加页面使用
    const customerData = {
      phone: record.phone,
      lastName: record.lastName
    }
    
    // 存储到全局或页面数据中
    getApp().globalData = getApp().globalData || {}
    getApp().globalData.customerData = customerData

    // 根据类型跳转到对应的添加页面
    const url = type === 'eyelash' 
      ? '/pages/add-eyelash-record/add-eyelash-record'
      : '/pages/add-consume-record/add-consume-record'
    
    wx.navigateTo({
      url
    })
  },

  /**
   * 页面滚动监听
   */
  onPageScroll(e) {
    // 当页面滚动超过 200px 时显示回到顶部按钮
    this.setData({
      showBackToTop: e.scrollTop > 200
    })
  },

  /**
   * 回到顶部
   */
  scrollToTop() {
    wx.pageScrollTo({
      scrollTop: 0,
      duration: 300
    })
  }
}
