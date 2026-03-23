const { isLoggedIn, getUserInfo } = require('../../utils/auth')
const modalMixin = require('./members.modal')
const formMixin = require('./members.form')
const apiMixin = require('./members.api')

Page(Object.assign(
  {
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
      statusText: ''
    },

    onLoad() {
      this.checkLoginAndPermission()
      this.initModalData()
      this.handleSearchParams()
    },

    /**
     * 检查登录状态和权限
     */
    checkLoginAndPermission() {
      if (!isLoggedIn()) {
        wx.showToast({ title: '请先登录', icon: 'none' })
        wx.switchTab({ url: '/pages/profile/profile' })
        return false
      }

      const userInfo = getUserInfo()
      if (!userInfo || userInfo.permissionLevel > 2) {
        wx.showToast({ title: '权限不足，只有员工及以上可查看', icon: 'none' })
        setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
        return false
      }

      this.setData({ userInfo })
      return true
    },

    /**
     * 处理预设的搜索参数
     */
    handleSearchParams() {
      const app = getApp()
      const searchParams = app.globalData.memberSearchParams
      
      if (searchParams) {
        this.applySearchParams(searchParams)
        app.globalData.memberSearchParams = null
      }
      
      this.search()
    },

    /**
     * 应用搜索参数
     * 
     * @param {Object} searchParams - 搜索参数
     */
    applySearchParams(searchParams) {
      const filters = {
        phone: searchParams.phone || '',
        lastName: searchParams.lastName || '',
        permissionLevel: null
      }
      
      let permissionIndex = 0
      if (searchParams.permissionLevel !== undefined) {
        permissionIndex = this.data.permissionOptions.findIndex(opt => opt.value === searchParams.permissionLevel)
        if (permissionIndex === -1) permissionIndex = 0
      }
      
      this.setData({ filters, permissionIndex })
    },

    /**
     * 刷新数据
     */
    refreshData() {
      this.search()
    },

    /**
     * 搜索用户
     */
    async search() {
      this.setData({ statusText: '加载中...' })

      try {
        const list = await this.searchUsers(this.data.filters)
        this.setData({ list, statusText: '' })
      } catch (error) {
        const msg = error && error.message ? error.message : '请求失败'
        this.setData({ statusText: '失败：' + msg })
        this.showApiError(error, '加载失败')
      }
    },

    /**
     * 筛选条件输入处理
     */
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

    /**
     * 重置筛选条件
     */
    resetFilters() {
      this.setData({
        filters: { phone: '', lastName: '', permissionLevel: null },
        permissionIndex: 0
      })
      this.search()
    },

    /**
     * 用户操作方法
     */
    addEyelashRecord(e) {
      const user = e.currentTarget.dataset.user
      this.showEyelashRecordModal(user)
    },

    addConsumeRecord(e) {
      const user = e.currentTarget.dataset.user
      this.showConsumeRecordModal(user, 'consume')
    },

    addRechargeRecord(e) {
      const user = e.currentTarget.dataset.user
      this.showConsumeRecordModal(user, 'recharge')
    },

    editMember(e) {
      const user = e.currentTarget.dataset.user
      wx.showToast({ title: '编辑功能开发中', icon: 'none' })
    },

    async deleteMember(e) {
      const user = e.currentTarget.dataset.user
      const { userInfo } = this.data
      
      if (!this.checkDeletePermission(user, userInfo)) {
        return
      }
      
      this.showDeleteConfirmModal(user)
    },

    /**
     * 检查删除权限
     * 
     * @param {Object} user - 要删除的用户
     * @param {Object} currentUser - 当前用户
     * @returns {boolean} 是否有删除权限
     */
    checkDeletePermission(user, currentUser) {
      // 检查权限：只有店长可以删除用户
      if (currentUser.permissionLevel > 1) {
        wx.showToast({ 
          title: '权限不足，只有店长可以删除用户', 
          icon: 'none' 
        })
        return false
      }
      
      // 检查是否尝试删除店长
      if (user.permissionLevel === 1) {
        wx.showToast({ 
          title: '不能删除店长账户', 
          icon: 'none' 
        })
        return false
      }
      
      return true
    },

    /**
     * 显示删除确认弹窗
     * 
     * @param {Object} user - 要删除的用户
     */
    showDeleteConfirmModal(user) {
      wx.showModal({
        title: '确认删除',
        content: `确定要删除成员 ${user.username}(${user.phone}) 吗？\n删除后将无法恢复！`,
        confirmText: '确认删除',
        confirmColor: '#ff4757',
        success: async (res) => {
          if (res.confirm) {
            await this.performDeleteUser(user)
          }
        }
      })
    },

    /**
     * 执行删除用户操作
     * 
     * @param {Object} user - 要删除的用户
     */
    async performDeleteUser(user) {
      try {
        await this.callApiWithLoading(
          () => this.deleteUser(user.userId, this.data.userInfo),
          '删除中...'
        )
        
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        })
        
        this.search()
      } catch (error) {
        this.showApiError(error, '删除失败')
      }
    }
  },
  modalMixin,
  formMixin,
  apiMixin
))
