/**
 * 充值折扣管理页面
 * 仅店长权限可访问
 */

const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: null,
    loading: false,
    showModal: false,
    isEdit: false,
    currentEditId: null,
    
    // 折扣列表
    discountList: [],
    
    // 表单数据
    form: {
      rechargeAmount: '',
      discountPercentage: '',
      effectiveDate: '',
      isActive: true
    }
  },

  onLoad() {
    console.log('充值折扣管理页面加载')
    this.checkLoginStatus()
    this.loadDiscountList()
  },

  onShow() {
    // 每次显示页面时检查登录状态
    this.checkLoginStatus()
  },

  /**
   * 检查登录状态和权限
   */
  checkLoginStatus() {
    const loggedIn = isLoggedIn()
    if (!loggedIn) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          } else {
            wx.navigateBack()
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    this.setData({ userInfo })
    
    // 检查权限 - 只有店长可以访问
    if (userInfo.permissionLevel !== 1) {
      wx.showModal({
        title: '权限不足',
        content: '只有店长权限可以管理充值折扣',
        showCancel: false,
        success: () => {
          wx.navigateBack()
        }
      })
      return
    }
  },

  /**
   * 加载折扣列表
   */
  async loadDiscountList() {
    this.setData({ loading: true })
    
    try {
      console.log('开始加载折扣列表...')
      const response = await request({
        url: '/api/recharge-discount',
        method: 'GET'
      })
      
      console.log('折扣列表响应:', response)
      console.log('数据类型:', typeof response)
      console.log('是否为数组:', Array.isArray(response))
      console.log('数组长度:', response ? response.length : 'N/A')
      
      if (response && response.length > 0) {
        console.log('第一条数据:', response[0])
        console.log('第一条数据的字段:', Object.keys(response[0]))
      } else {
        console.log('响应为空或不是数组')
      }
      
      // 按充值金额从低到高排序（前端排序作为备份）
      const sortedList = response && Array.isArray(response) ? response.sort((a, b) => {
        return parseFloat(a.rechargeAmount) - parseFloat(b.rechargeAmount)
      }) : []
      
      this.setData({ 
        discountList: sortedList,
        loading: false 
      })
      
      console.log('设置后的discountList:', this.data.discountList)
      console.log('设置后的discountList长度:', this.data.discountList.length)
    } catch (error) {
      console.error('加载折扣列表失败:', error)
      console.error('错误详情:', error.message || error)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
      this.setData({ loading: false })
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
          try {
            await request({
              url: `/api/recharge-discount/${item.id}`,
              method: 'DELETE'
            })
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            })
            
            // 重新加载列表数据，确保数据同步
            this.loadDiscountList()
          } catch (error) {
            console.error('删除失败:', error)
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            })
          }
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

    const { form, isEdit, currentEditId } = this.data
    
    // 计算折扣率
    const discountRate = (parseFloat(form.discountPercentage) / 100).toFixed(4)
    
    const data = {
      rechargeAmount: parseFloat(form.rechargeAmount),
      discountRate: parseFloat(discountRate),
      discountPercentage: parseFloat(form.discountPercentage),
      effectiveDate: form.effectiveDate,
      isActive: form.isActive,
      createdBy: this.data.userInfo.lastName || 'admin'
    }

    try {
      if (isEdit) {
        // 更新
        await request({
          url: `/api/recharge-discount/${currentEditId}`,
          method: 'PUT',
          data
        })
        
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        })
      } else {
        // 添加
        await request({
          url: '/api/recharge-discount',
          method: 'POST',
          data
        })
        
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        })
      }
      
      this.hideModal()
      // 重新加载列表数据，确保数据同步
      this.loadDiscountList()
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: isEdit ? '更新失败' : '添加失败',
        icon: 'none'
      })
    }
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
    
    try {
      await request({
        url: `/api/recharge-discount/${item.id}/status`,
        method: 'PUT',
        data: {
          isActive: newStatus
        }
      })
      
      wx.showToast({
        title: newStatus ? '启用成功' : '禁用成功',
        icon: 'success'
      })
      
      // 重新加载列表数据，确保数据同步
      this.loadDiscountList()
    } catch (error) {
      console.error('状态切换失败:', error)
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      })
      
      // 恢复switch状态
      const discountList = this.data.discountList.map(discount => {
        if (discount.id === item.id) {
          return { ...discount, isActive: !newStatus }
        }
        return discount
      })
      
      this.setData({ discountList })
    }
  }
})
