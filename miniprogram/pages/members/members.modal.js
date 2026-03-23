/**
 * 成员列表页面模态弹窗模块
 * 
 * 负责处理页面中的各种模态弹窗逻辑，包括：
 * - 睫毛记录添加弹窗
 * - 消费记录添加弹窗
 * - 充值记录添加弹窗
 * - 弹窗显示/隐藏控制
 * - 弹窗数据初始化
 */

const modalMixin = {
  /**
   * 显示睫毛记录模态弹窗
   * 
   * @param {Object} user - 用户信息对象
   */
  showEyelashRecordModal(user) {
    this.setData({
      showEyelashModal: true,
      isFromMember: true,
      eyelashForm: {
        phone: user.phone || '',
        lastName: user.lastName || '',
        style: '',
        modelNumber: '',
        length: '',
        curl: '',
        recordDate: this.getCurrentDate()
      }
    })
  },

  /**
   * 隐藏睫毛记录模态弹窗
   */
  hideEyelashModal() {
    this.setData({
      showEyelashModal: false,
      isFromMember: false
    })
  },

  /**
   * 显示消费记录模态弹窗
   * 
   * @param {Object} user - 用户信息对象
   * @param {string} type - 消费类型 ('recharge' 或 'consume')
   */
  showConsumeRecordModal(user, type) {
    this.setData({
      showConsumeModal: true,
      consumeType: type,
      consumeItemIndex: 0,
      consumeTypeIndex: type === 'recharge' ? 0 : 1, // 充值默认选择充值，消费默认选择支出
      consumeForm: {
        phone: user.phone || '',
        lastName: user.lastName || '',
        balance: user.amount || 0, // 使用数据库中的amount字段
        consumeAmount: '',
        consumeItem: type === 'recharge' ? '会员充值' : '',
        consumeType: type === 'recharge' ? '充值' : '支出',
        consumeDate: this.getCurrentDate()
      }
    })
  },

  /**
   * 隐藏消费记录模态弹窗
   */
  hideConsumeModal() {
    this.setData({
      showConsumeModal: false
    })
  },

  /**
   * 获取当前日期
   * 
   * @returns {string} 格式化的当前日期 (YYYY-MM-DD)
   */
  getCurrentDate() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  /**
   * 初始化模态弹窗数据
   */
  initModalData() {
    this.setData({
      // 睫毛记录模态弹窗相关数据
      showEyelashModal: false,
      isFromMember: false,
      eyelashForm: {
        phone: '',
        lastName: '',
        style: '',
        modelNumber: '',
        length: '',
        curl: '',
        recordDate: ''
      },
      
      // 消费记录模态弹窗相关数据
      showConsumeModal: false,
      consumeType: '', // 'recharge' 或 'consume'
      consumeItemIndex: 0,
      consumeTypeIndex: 0,
      consumeItems: ['会员充值', '美睫项目', '美甲项目'],
      consumeTypes: ['充值', '支出'],
      consumeForm: {
        phone: '',
        lastName: '',
        balance: 0,
        consumeAmount: '',
        consumeItem: '',
        consumeType: '',
        consumeDate: ''
      }
    })
  }
}

module.exports = modalMixin
