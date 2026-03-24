/**
 * 消息存储工具类
 * 用于管理AI客服的消息历史记录
 */

const STORAGE_KEY = 'ai_customer_service_messages'
const MAX_MESSAGES = 50  // 最大保存消息数量

/**
 * 保存消息到本地存储
 * @param {Array} messages - 消息列表
 */
function saveMessages(messages) {
  try {
    // 只保存最近的消息，避免存储空间过大
    const messagesToSave = messages.slice(-MAX_MESSAGES)
    wx.setStorageSync(STORAGE_KEY, messagesToSave)
    return true
  } catch (error) {
    console.error('保存消息失败:', error)
    return false
  }
}

/**
 * 从本地存储加载消息
 * @returns {Array} 消息列表
 */
function loadMessages() {
  try {
    const messages = wx.getStorageSync(STORAGE_KEY)
    return Array.isArray(messages) ? messages : []
  } catch (error) {
    console.error('加载消息失败:', error)
    return []
  }
}

/**
 * 清空消息历史
 */
function clearMessages() {
  try {
    wx.removeStorageSync(STORAGE_KEY)
    return true
  } catch (error) {
    console.error('清空消息失败:', error)
    return false
  }
}

/**
 * 添加新消息
 * @param {Array} messages - 当前消息列表
 * @param {Object} newMessage - 新消息
 * @returns {Array} 更新后的消息列表
 */
function addMessage(messages, newMessage) {
  const updatedMessages = [...messages, newMessage]
  saveMessages(updatedMessages)
  return updatedMessages
}

/**
 * 获取消息统计信息
 * @returns {Object} 统计信息
 */
function getMessageStats() {
  try {
    const messages = loadMessages()
    const stats = {
      total: messages.length,
      user: 0,
      ai: 0,
      system: 0,
      today: 0,
      thisWeek: 0
    }

    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const weekStart = new Date(todayStart)
    weekStart.setDate(weekStart.getDate() - weekStart.getDay())

    messages.forEach(message => {
      // 按类型统计
      switch (message.type) {
        case 'user':
          stats.user++
          break
        case 'ai':
          stats.ai++
          break
        case 'system':
          stats.system++
          break
      }

      // 按时间统计
      if (message.timestamp) {
        const messageDate = new Date(message.timestamp)
        if (messageDate >= todayStart) {
          stats.today++
        }
        if (messageDate >= weekStart) {
          stats.thisWeek++
        }
      }
    })

    return stats
  } catch (error) {
    console.error('获取消息统计失败:', error)
    return {
      total: 0,
      user: 0,
      ai: 0,
      system: 0,
      today: 0,
      thisWeek: 0
    }
  }
}

/**
 * 搜索消息
 * @param {string} keyword - 搜索关键词
 * @returns {Array} 匹配的消息列表
 */
function searchMessages(keyword) {
  try {
    const messages = loadMessages()
    if (!keyword.trim()) {
      return messages
    }

    const keywordLower = keyword.toLowerCase()
    return messages.filter(message => {
      return message.content && 
             message.content.toLowerCase().includes(keywordLower)
    })
  } catch (error) {
    console.error('搜索消息失败:', error)
    return []
  }
}

/**
 * 导出消息数据
 * @returns {string} JSON格式的消息数据
 */
function exportMessages() {
  try {
    const messages = loadMessages()
    const exportData = {
      exportTime: new Date().toISOString(),
      messageCount: messages.length,
      messages: messages
    }
    return JSON.stringify(exportData, null, 2)
  } catch (error) {
    console.error('导出消息失败:', error)
    return null
  }
}

/**
 * 导入消息数据
 * @param {string} jsonData - JSON格式的消息数据
 * @returns {boolean} 导入是否成功
 */
function importMessages(jsonData) {
  try {
    const importData = JSON.parse(jsonData)
    
    if (!importData.messages || !Array.isArray(importData.messages)) {
      throw new Error('无效的消息数据格式')
    }

    // 验证消息格式
    const validMessages = importData.messages.filter(message => {
      return message && 
             typeof message.id === 'number' && 
             typeof message.type === 'string' && 
             typeof message.content === 'string'
    })

    if (validMessages.length === 0) {
      throw new Error('没有有效的消息数据')
    }

    saveMessages(validMessages)
    return true
  } catch (error) {
    console.error('导入消息失败:', error)
    return false
  }
}

module.exports = {
  saveMessages,
  loadMessages,
  clearMessages,
  addMessage,
  getMessageStats,
  searchMessages,
  exportMessages,
  importMessages,
  MAX_MESSAGES
}
