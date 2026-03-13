const { request } = require('../../utils/request')

module.exports = {
  showAddRecordModal() {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    // 显示添加睫毛记录模态弹窗
    this.setData({
      showEyelashModal: true,
      isEdit: false,
      currentEditId: null,
      isFromMember: false,
      eyelashForm: {
        phone: '',
        lastName: '',
        style: '',
        modelNumber: '',
        length: '',
        curl: '',
        recordDate: this.getCurrentDate()
      }
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
  },

  // 睫毛记录模态弹窗相关方法
  hideEyelashModal() {
    this.setData({
      showEyelashModal: false,
      isFromMember: false
    })
  },

  // 从成员列表添加睫毛记录
  showAddEyelashFromMember(memberInfo) {
    if (this.data.userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足', icon: 'none' })
      return
    }

    // 预填会员信息
    this.setData({
      showEyelashModal: true,
      isEdit: false,
      currentEditId: null,
      isFromMember: true,
      eyelashForm: {
        phone: memberInfo.phone || '',
        lastName: memberInfo.lastName || '',
        style: '',
        modelNumber: '',
        length: '',
        curl: '',
        recordDate: this.getCurrentDate()
      }
    })
  },

  // 睫毛记录表单输入事件
  onEyelashPhoneInput(e) {
    this.setData({ 'eyelashForm.phone': e.detail.value })
  },

  onEyelashLastNameInput(e) {
    this.setData({ 'eyelashForm.lastName': e.detail.value })
  },

  onEyelashStyleInput(e) {
    this.setData({ 'eyelashForm.style': e.detail.value })
  },

  onEyelashModelNumberInput(e) {
    this.setData({ 'eyelashForm.modelNumber': e.detail.value })
  },

  onEyelashLengthInput(e) {
    this.setData({ 'eyelashForm.length': e.detail.value })
  },

  onEyelashCurlInput(e) {
    this.setData({ 'eyelashForm.curl': e.detail.value })
  },

  // 提交睫毛记录表单
  async submitEyelashForm() {
    const { eyelashForm, isEdit, currentEditId } = this.data

    // 表单验证
    if (!eyelashForm.phone || !eyelashForm.lastName || !eyelashForm.style || 
        !eyelashForm.modelNumber || !eyelashForm.length || !eyelashForm.curl) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      })
      return
    }

    // 手机号格式验证
    if (!/^1[3-9]\d{9}$/.test(eyelashForm.phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    try {
      const data = {
        phone: eyelashForm.phone,
        lastName: eyelashForm.lastName,
        gender: 1, // 默认性别，可以根据需要修改
        style: eyelashForm.style,
        modelNumber: eyelashForm.modelNumber,
        length: parseFloat(eyelashForm.length),
        curl: eyelashForm.curl,
        recordDate: eyelashForm.recordDate
      }

      if (isEdit) {
        // 更新记录
        await request({
          url: `/api/eyelash-records/${currentEditId}`,
          method: 'PUT',
          data
        })
        
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        })
      } else {
        // 添加记录
        await request({
          url: '/api/eyelash-records',
          method: 'POST',
          data
        })
        
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        })
      }
      
      this.hideEyelashModal()
      // 重新加载睫毛记录
      this.loadRecords()
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: isEdit ? '更新失败' : '添加失败',
        icon: 'none'
      })
    }
  },

  // 获取当前日期
  getCurrentDate() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
}
