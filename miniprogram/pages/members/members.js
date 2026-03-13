const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: {},
    filters: {
      phone: '',
      lastName: '',
      permissionLevel: null
    },
    permissionOptions: [
      { label: '全部', value: null },
      { label: '店长', value: 1 },
      { label: '员工', value: 2 },
      { label: '会员', value: 3 },
      { label: '游客', value: 4 }
    ],
    permissionIndex: 0,
    list: [],
    statusText: '',
    
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
  },

  onLoad() {
    if (!isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }

    const userInfo = getUserInfo()
    if (!userInfo || userInfo.permissionLevel > 2) {
      wx.showToast({ title: '权限不足，只有员工及以上可查看', icon: 'none' })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
      return
    }

    // 设置用户信息到 data 中，用于权限控制
    this.setData({ userInfo })

    // 检查是否有预设的搜索参数
    const app = getApp()
    const searchParams = app.globalData.memberSearchParams
    
    if (searchParams) {
      // 应用预设的搜索参数
      const filters = {
        phone: searchParams.phone || '',
        lastName: searchParams.lastName || '',
        permissionLevel: null
      }
      
      // 设置权限选择器的索引
      let permissionIndex = 0
      if (searchParams.permissionLevel !== undefined) {
        permissionIndex = this.data.permissionOptions.findIndex(opt => opt.value === searchParams.permissionLevel)
        if (permissionIndex === -1) permissionIndex = 0
      }
      
      this.setData({
        filters,
        permissionIndex
      })
      
      // 清除全局搜索参数
      app.globalData.memberSearchParams = null
      
      // 执行搜索
      this.search()
    } else {
      // 没有预设参数，正常加载
      this.search()
    }
  },

  refreshData() {
    this.search()
  },

  onPhoneInput(e) {
    this.setData({ 'filters.phone': e.detail.value })
  },

  onLastNameInput(e) {
    this.setData({ 'filters.lastName': e.detail.value })
  },

  onPermissionChange(e) {
    const idx = Number(e.detail.value || 0)
    const opt = this.data.permissionOptions[idx]
    this.setData({
      permissionIndex: idx,
      'filters.permissionLevel': opt ? opt.value : null
    })
  },

  resetFilters() {
    this.setData({
      filters: { phone: '', lastName: '', permissionLevel: null },
      permissionIndex: 0
    })
    this.search()
  },

  // 添加美睫记录
  addEyelashRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 直接在成员列表页面显示添加睫毛记录弹窗
    this.showEyelashRecordModal(user)
  },

  // 添加会员消费记录
  addConsumeRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 直接在成员列表页面显示消费记录弹窗
    this.showConsumeRecordModal(user, 'consume')
  },

  // 添加充值记录
  addRechargeRecord(e) {
    const user = e.currentTarget.dataset.user
    
    // 直接在成员列表页面显示充值记录弹窗
    this.showConsumeRecordModal(user, 'recharge')
  },

  // 编辑成员
  editMember(e) {
    const user = e.currentTarget.dataset.user
    wx.showToast({ title: '编辑功能开发中', icon: 'none' })
  },

  // 删除成员
  deleteMember(e) {
    const user = e.currentTarget.dataset.user
    wx.showModal({
      title: '确认删除',
      content: `确定要删除成员 ${user.username} 吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showToast({ title: '删除功能开发中', icon: 'none' })
        }
      }
    })
  },

  search() {
    this.setData({ statusText: '加载中...' })

    // 搜索用户
    this.searchUsers()
  },

  // 睫毛记录模态弹窗相关方法
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

  hideEyelashModal() {
    this.setData({
      showEyelashModal: false,
      isFromMember: false
    })
  },

  // 获取当前日期
  getCurrentDate() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  // 消费记录模态弹窗相关方法
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

  hideConsumeModal() {
    this.setData({
      showConsumeModal: false
    })
  },

  // 消费金额输入
  onConsumeAmountInput(e) {
    this.setData({ 'consumeForm.consumeAmount': e.detail.value })
  },

  // 消费项目选择
  onConsumeItemChange(e) {
    const index = e.detail.value
    this.setData({ 
      consumeItemIndex: index,
      'consumeForm.consumeItem': this.data.consumeItems[index]
    })
  },

  // 消费类型选择
  onConsumeTypeChange(e) {
    const index = e.detail.value
    this.setData({ 
      consumeTypeIndex: index,
      'consumeForm.consumeType': this.data.consumeTypes[index]
    })
  },

  // 提交消费记录表单
  async submitConsumeForm() {
    const { consumeForm, consumeType } = this.data

    // 表单验证
    if (!consumeForm.consumeAmount) {
      wx.showToast({ title: '请输入金额', icon: 'none' })
      return
    }
    
    if (consumeType === 'consume' && !consumeForm.consumeItem) {
      wx.showToast({ title: '请选择消费项目', icon: 'none' })
      return
    }
    
    if (consumeType === 'consume' && !consumeForm.consumeType) {
      wx.showToast({ title: '请选择消费类型', icon: 'none' })
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
      wx.showModal({
        title: '操作成功',
        content: `${consumeType === 'recharge' ? '充值' : '消费记录添加'}成功，是否跳转到消费记录页面查看？`,
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
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: consumeType === 'recharge' ? '充值失败' : '消费记录添加失败',
        icon: 'none'
      })
    }
  },

  // 睫毛记录表单输入处理
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
    const { eyelashForm, isFromMember } = this.data

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
        wx.showModal({
          title: '添加成功',
          content: '睫毛记录添加成功，是否跳转到记录页面查看？',
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
      }
    } catch (error) {
      console.error('提交失败:', error)
      wx.showToast({
        title: '添加失败',
        icon: 'none'
      })
    }
  },

  searchUsers() {
    const { phone, lastName, permissionLevel } = this.data.filters
    const params = []

    if (phone) params.push(`phone=${encodeURIComponent(phone)}`)
    if (lastName) params.push(`lastName=${encodeURIComponent(lastName)}`)
    if (permissionLevel !== null && permissionLevel !== undefined) {
      params.push(`permissionLevel=${permissionLevel}`)
    }

    const url = params.length ? `/api/users?${params.join('&')}` : '/api/users'

    return request({ url })
      .then((data) => {
        const list = Array.isArray(data) ? data : []
        this.setData({ list, statusText: '' })
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '请求失败'
        this.setData({ statusText: '失败：' + msg })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  }
})
