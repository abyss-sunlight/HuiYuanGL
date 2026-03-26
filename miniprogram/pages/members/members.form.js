/**
 * 成员列表页面表单处理模块
 * 
 * 负责处理页面中的各种表单逻辑，包括：
 * - 睫毛记录表单输入处理
 * - 消费记录表单输入处理
 * - 表单验证
 * - 表单提交
 * - 表单重置
 */

const { request } = require('../../utils/request')

const formMixin = {
  /**
   * 睫毛记录表单输入处理方法
   */
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

  /**
   * 消费记录表单输入处理方法
   */
  onConsumeAmountInput(e) {
    this.setData({ 'consumeForm.consumeAmount': e.detail.value })
  },

  onConsumeItemSelect(e) {
    const item = e.currentTarget.dataset.item
    
    this.setData({ 
      'consumeForm.consumeItem': item,
      consumeItemIndex: this.data.consumeItems.indexOf(item) // 同步更新索引以保持兼容性
    })
  },

  onConsumeItemChange(e) {
    const index = e.detail.value
    this.setData({ 
      consumeItemIndex: index,
      'consumeForm.consumeItem': this.data.consumeItems[index]
    })
  },

  onConsumeTypeSelect(e) {
    const type = e.currentTarget.dataset.type
    
    this.setData({ 
      'consumeForm.consumeType': type,
      consumeTypeIndex: this.data.consumeTypes.indexOf(type) // 同步更新索引以保持兼容性
    })
  },

  onConsumeTypeChange(e) {
    const index = e.detail.value
    this.setData({ 
      consumeTypeIndex: index,
      'consumeForm.consumeType': this.data.consumeTypes[index]
    })
  },

  /**
   * 提交睫毛记录表单
   */
  async submitEyelashForm() {
    const { eyelashForm, isFromMember } = this.data

    // 表单验证
    if (!this.validateEyelashForm(eyelashForm)) {
      return
    }

    try {
      const data = {
        phone: eyelashForm.phone,
        lastName: eyelashForm.lastName,
        gender: 1, // 默认性别
        style: eyelashForm.style,
        modelNumber: eyelashForm.modelNumber,
        length: parseFloat(eyelashForm.length),
        curl: eyelashForm.curl,
        recordDate: eyelashForm.recordDate
      }

      await request({
        url: '/api/eyelash-records',
        method: 'POST',
        data
      })
      
      wx.showToast({
        title: '添加成功',
        icon: 'success'
      })
      
      this.hideEyelashModal()
      
      // 如果是从成员列表打开的，询问是否跳转到记录页面
      if (isFromMember) {
        this.showJumpToRecordsModal('睫毛记录添加成功')
      }
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: '添加失败',
        icon: 'none'
      })
    }
  },

  /**
   * 提交消费记录表单
   */
  async submitConsumeForm() {
    const { consumeForm, consumeType } = this.data

    // 表单验证
    if (!this.validateConsumeForm(consumeForm, consumeType)) {
      return
    }

    try {
      const data = {
        phone: consumeForm.phone,
        lastName: consumeForm.lastName,
        gender: 1,
        balance: parseFloat(consumeForm.balance),
        consumeAmount: parseFloat(consumeForm.consumeAmount),
        consumeItem: consumeForm.consumeItem,
        consumeType: consumeForm.consumeType,
        consumeDate: consumeForm.consumeDate
      }
      
      await request({
        url: '/api/consume-records',
        method: 'POST',
        data
      })
      
      wx.showToast({
        title: consumeType === 'recharge' ? '充值成功' : '消费记录添加成功',
        icon: 'success'
      })
      
      this.hideConsumeModal()
      
      // 询问是否跳转到消费记录页面
      this.showJumpToRecordsModal(
        consumeType === 'recharge' ? '充值' : '消费记录添加',
        consumeType === 'recharge' ? '充值成功' : '消费记录添加成功'
      )
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: consumeType === 'recharge' ? '充值失败' : '消费记录添加失败',
        icon: 'none'
      })
    }
  },

  /**
   * 验证睫毛记录表单
   * 
   * @param {Object} form - 表单数据
   * @returns {boolean} 验证结果
   */
  validateEyelashForm(form) {
    if (!form.phone || !form.lastName || !form.style || 
        !form.modelNumber || !form.length || !form.curl) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      })
      return false
    }

    // 手机号格式验证
    if (!/^1[3-9]\d{9}$/.test(form.phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return false
    }

    return true
  },

  /**
   * 验证消费记录表单
   * 
   * @param {Object} form - 表单数据
   * @param {string} consumeType - 消费类型
   * @returns {boolean} 验证结果
   */
  validateConsumeForm(form, consumeType) {
    if (!form.consumeAmount) {
      wx.showToast({ title: '请输入金额', icon: 'none' })
      return false
    }
    
    if (consumeType === 'consume' && !form.consumeItem) {
      wx.showToast({ title: '请选择消费项目', icon: 'none' })
      return false
    }
    
    if (consumeType === 'consume' && !form.consumeType) {
      wx.showToast({ title: '请选择消费类型', icon: 'none' })
      return false
    }

    return true
  },

  /**
   * 显示跳转到记录页面的确认弹窗
   * 
   * @param {string} action - 操作类型
   * @param {string} successMessage - 成功消息
   */
  showJumpToRecordsModal(action, successMessage = `${action}成功`) {
    wx.showModal({
      title: '操作成功',
      content: `${successMessage}，是否跳转到记录页面查看？`,
      confirmText: '去查看',
      cancelText: '留在此页',
      success: (res) => {
        if (res.confirm) {
          wx.switchTab({
            url: '/pages/records/records'
          })
        }
      }
    })
  },

  /**
   * 重置所有表单
   */
  resetAllForms() {
    this.setData({
      eyelashForm: {
        phone: '',
        lastName: '',
        style: '',
        modelNumber: '',
        length: '',
        curl: '',
        recordDate: this.getCurrentDate()
      },
      consumeForm: {
        phone: '',
        lastName: '',
        balance: 0,
        consumeAmount: '',
        consumeItem: '',
        consumeType: '',
        consumeDate: this.getCurrentDate()
      }
    })
  }
}

module.exports = formMixin
