// 表单管理模块
module.exports = {
  // 表单相关数据
  formData: {
    showModal: false,
    isEdit: false,
    currentEditId: null,
    
    // 表单数据
    form: {
      rechargeAmount: '',
      discountPercentage: '',
      effectiveDate: '',
      isActive: true
    }
  },

  /**
   * 显示添加折扣弹窗
   */
  showAddDiscountModal() {
    this.setData({
      showModal: true,
      isEdit: false,
      currentEditId: null,
      form: {
        rechargeAmount: '',
        discountPercentage: '',
        effectiveDate: this.getCurrentDate(),
        isActive: true
      }
    })
  },

  /**
   * 隐藏弹窗
   */
  hideModal() {
    this.setData({ showModal: false })
  },

  /**
   * 阻止事件冒泡
   */
  stopPropagation() {
    // 阻止点击弹窗内容时关闭弹窗
  },

  /**
   * 编辑折扣规则
   */
  editDiscount(e) {
    const item = e.currentTarget.dataset.item
    
    this.setData({
      showModal: true,
      isEdit: true,
      currentEditId: item.id,
      form: {
        rechargeAmount: item.rechargeAmount.toString(),
        discountPercentage: item.discountPercentage.toString(),
        effectiveDate: this.getCurrentDate(), // 编辑时默认为当天
        isActive: item.isActive
      }
    })
  },

  /**
   * 删除折扣规则
   */
  deleteDiscount(e) {
    const item = e.currentTarget.dataset.item
    
    wx.showModal({
      title: '确认删除',
      content: `确定要删除充值金额 ¥${item.rechargeAmount} 的折扣规则吗？`,
      success: async (res) => {
        if (res.confirm) {
          await this.deleteDiscountItem(item)
        }
      }
    })
  },

  /**
   * 表单输入事件
   */
  onAmountInput(e) {
    this.setData({ 'form.rechargeAmount': e.detail.value })
  },

  onDiscountInput(e) {
    this.setData({ 'form.discountPercentage': e.detail.value })
  },

  onStatusChange(e) {
    this.setData({ 'form.isActive': e.detail.value })
  },

  /**
   * 提交表单
   */
  async submitForm() {
    if (!this.validateForm()) {
      return
    }

    await this.submitFormData()
  },

  /**
   * 表单验证
   */
  validateForm() {
    const { form, isEdit } = this.data
    
    if (!form.rechargeAmount || parseFloat(form.rechargeAmount) <= 0) {
      wx.showToast({
        title: '请输入有效的充值金额',
        icon: 'none'
      })
      return false
    }
    
    if (!form.discountPercentage || parseFloat(form.discountPercentage) <= 0 || parseFloat(form.discountPercentage) > 100) {
      wx.showToast({
        title: '折扣百分比必须在0-100之间',
        icon: 'none'
      })
      return false
    }
    
    // 编辑模式下生效日期自动设置为当天，不需要验证
    if (!isEdit && !form.effectiveDate) {
      wx.showToast({
        title: '请选择生效日期',
        icon: 'none'
      })
      return false
    }
    
    return true
  },

  /**
   * 获取当前日期
   */
  getCurrentDate() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  /**
   * 切换折扣状态
   */
  async toggleStatus(e) {
    const item = e.currentTarget.dataset.item
    const newStatus = e.detail.value // switch组件的值
    
    await this.toggleDiscountStatus(item, newStatus)
  }
}
