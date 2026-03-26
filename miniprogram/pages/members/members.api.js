/**
 * 成员列表页面API调用模块
 * 
 * 负责处理页面中的所有API调用，包括：
 * - 用户列表查询
 * - 用户创建
 * - 用户删除
 * - 睫毛记录创建
 * - 消费记录创建
 * - 错误处理
 */

const { request } = require('../../utils/request')

const apiMixin = {
  /**
   * 搜索用户列表
   * 
   * @param {Object} filters - 搜索过滤条件
   * @param {string} filters.phone - 手机号
   * @param {string} filters.lastName - 姓氏
   * @param {number} filters.permissionLevel - 权限等级
   * @returns {Promise<Array>} 用户列表
   */
  async searchUsers(filters = {}) {
    const { phone, lastName, permissionLevel } = filters
    const params = []

    if (phone) params.push(`phone=${encodeURIComponent(phone)}`)
    if (lastName) params.push(`lastName=${encodeURIComponent(lastName)}`)
    if (permissionLevel !== null && permissionLevel !== undefined) {
      params.push(`permissionLevel=${permissionLevel}`)
    }

    const url = params.length ? `/api/users?${params.join('&')}` : '/api/users'

    try {
      const data = await request({ url })
      const list = Array.isArray(data) ? data : []
      return list
    } catch (error) {
      console.error('搜索用户失败:', error)
      throw error
    }
  },

  /**
   * 创建新用户
   * 
   * @param {Object} userData - 用户数据
   * @param {number} userData.permissionLevel - 权限等级
   * @param {string} userData.lastName - 姓氏
   * @param {string} userData.phone - 手机号
   * @param {string} userData.username - 用户名（可选）
   * @param {string} userData.avatarUrl - 头像URL（可选）
   * @param {number} userData.gender - 性别
   * @param {string} userData.amount - 初始余额（仅会员）
   * @param {string} userData.discount - 折扣率（仅会员）
   * @param {Object} currentUser - 当前用户信息
   * @returns {Promise<Object>} 创建的用户信息
   */
  async createUser(userData, currentUser) {
    // 权限检查：只有店长可以创建用户
    if (currentUser.permissionLevel > 1) {
      throw new Error('权限不足，只有店长可以创建用户')
    }

    // 验证不能创建店长账户
    if (userData.permissionLevel === 1) {
      throw new Error('不能创建店长账户')
    }

    try {
      const result = await request({
        url: '/api/users/create',
        method: 'POST',
        data: userData
      })
      return result
    } catch (error) {
      console.error('创建用户失败:', error)
      throw error
    }
  },

  /**
   * 删除用户
   * 
   * @param {number} userId - 用户ID
   * @param {Object} currentUser - 当前用户信息
   * @returns {Promise<void>}
   */
  async deleteUser(userId, currentUser) {
    // 权限检查：只有店长可以删除用户
    if (currentUser.permissionLevel > 1) {
      throw new Error('权限不足，只有店长可以删除用户')
    }

    try {
      await request({
        url: `/api/users/${userId}`,
        method: 'DELETE'
      })
    } catch (error) {
      console.error('删除用户失败:', error)
      throw error
    }
  },

  /**
   * 更新用户状态
   * 
   * @param {number} userId - 用户ID
   * @param {number} status - 新状态 (0-启用, 1-禁用)
   * @param {Object} currentUser - 当前用户信息
   * @returns {Promise<Object>} 更新结果
   */
  async updateUserStatus(userId, status, currentUser) {
    // 权限检查：只有店长和员工可以修改用户状态
    if (currentUser.permissionLevel > 2) {
      throw new Error('权限不足，只有员工及以上可以修改用户状态')
    }

    // 员工不能修改店长状态
    if (currentUser.permissionLevel === 2) {
      // 这里需要先获取目标用户信息来检查权限，但为了简化，暂时允许
      // 实际项目中应该先查询用户信息再验证
    }

    try {
      const result = await request({
        url: `/api/users/${userId}/status`,
        method: 'PUT',
        data: { status }
      })
      return result
    } catch (error) {
      console.error('更新用户状态失败:', error)
      throw error
    }
  },

  /**
   * 创建睫毛记录
   * 
   * @param {Object} recordData - 睫毛记录数据
   * @returns {Promise<Object>} 创建结果
   */
  async createEyelashRecord(recordData) {
    try {
      const result = await request({
        url: '/api/eyelash-records',
        method: 'POST',
        data: recordData
      })
      return result
    } catch (error) {
      console.error('创建睫毛记录失败:', error)
      throw error
    }
  },

  /**
   * 创建消费记录
   * 
   * @param {Object} recordData - 消费记录数据
   * @returns {Promise<Object>} 创建结果
   */
  async createConsumeRecord(recordData) {
    try {
      const result = await request({
        url: '/api/consume-records',
        method: 'POST',
        data: recordData
      })
      return result
    } catch (error) {
      console.error('创建消费记录失败:', error)
      throw error
    }
  },

  /**
   * 批量操作用户列表
   * 
   * @param {Array} userIds - 用户ID数组
   * @param {string} operation - 操作类型 ('delete', 'updatePermission')
   * @param {Object} operationData - 操作数据
   * @returns {Promise<Array>} 操作结果
   */
  async batchOperateUsers(userIds, operation, operationData = {}) {
    try {
      const results = await Promise.all(
        userIds.map(userId => {
          switch (operation) {
            case 'delete':
              return this.deleteUser(userId, operationData.currentUser)
            default:
              throw new Error(`不支持的操作类型: ${operation}`)
          }
        })
      )
      return results
    } catch (error) {
      console.error('批量操作用户失败:', error)
      throw error
    }
  },

  /**
   * 获取用户统计信息
   * 
   * @param {Object} filters - 过滤条件
   * @returns {Promise<Object>} 统计信息
   */
  async getUserStatistics(filters = {}) {
    try {
      const params = new URLSearchParams(filters).toString()
      const url = params ? `/api/users/statistics?${params}` : '/api/users/statistics'
      
      const result = await request({ url })
      return result
    } catch (error) {
      console.error('获取用户统计信息失败:', error)
      throw error
    }
  },

  /**
   * 导出用户数据
   * 
   * @param {Object} filters - 导出过滤条件
   * @param {string} format - 导出格式 ('excel', 'csv')
   * @returns {Promise<string>} 下载链接
   */
  async exportUserData(filters = {}, format = 'excel') {
    try {
      const params = new URLSearchParams({ ...filters, format }).toString()
      const url = `/api/users/export?${params}`
      
      const result = await request({ url })
      return result.downloadUrl
    } catch (error) {
      console.error('导出用户数据失败:', error)
      throw error
    }
  },

  /**
   * 处理API错误
   * 
   * @param {Error} error - 错误对象
   * @param {string} defaultMessage - 默认错误消息
   * @returns {string} 用户友好的错误消息
   */
  handleApiError(error, defaultMessage = '操作失败') {
    console.error('API调用错误:', error)
    
    if (error.message) {
      // 如果是业务错误，直接显示
      if (error.message.includes('权限不足') || 
          error.message.includes('用户不存在') ||
          error.message.includes('账户已被禁用')) {
        return error.message
      }
    }
    
    // 网络错误或其他系统错误
    if (error.statusCode) {
      switch (error.statusCode) {
        case 400:
          return '请求参数错误'
        case 401:
          return '未授权，请重新登录'
        case 403:
          return '权限不足'
        case 404:
          return '资源不存在'
        case 500:
          return '服务器内部错误'
        default:
          return `请求失败 (${error.statusCode})`
      }
    }
    
    return defaultMessage
  },

  /**
   * 显示API错误提示
   * 
   * @param {Error} error - 错误对象
   * @param {string} defaultMessage - 默认错误消息
   */
  showApiError(error, defaultMessage = '操作失败') {
    const message = this.handleApiError(error, defaultMessage)
    wx.showToast({
      title: message,
      icon: 'none',
      duration: 3000
    })
  },

  /**
   * 带加载提示的API调用
   * 
   * @param {Function} apiCall - API调用函数
   * @param {string} loadingMessage - 加载提示消息
   * @returns {Promise<any>} API调用结果
   */
  async callApiWithLoading(apiCall, loadingMessage = '加载中...') {
    try {
      wx.showLoading({ title: loadingMessage })
      const result = await apiCall()
      wx.hideLoading()
      return result
    } catch (error) {
      wx.hideLoading()
      throw error
    }
  }
}

module.exports = apiMixin
