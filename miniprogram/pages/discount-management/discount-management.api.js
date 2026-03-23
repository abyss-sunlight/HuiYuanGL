// 导入网络请求工具
const { request } = require('../../utils/request')

// API调用模块
module.exports = {
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
   * 提交表单数据（添加或更新）
   */
  async submitFormData() {
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
   * 删除折扣规则
   */
  async deleteDiscountItem(item) {
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
  },

  /**
   * 切换折扣状态
   */
  async toggleDiscountStatus(item, newStatus) {
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
}
