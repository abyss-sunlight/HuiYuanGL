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
      statusText: '',
      // 添加成员相关数据
      showAddMemberModal: false,
      showAvatarOptions: false,
      createPermissionOptions: ['员工', '会员', '游客'], // 不允许创建店长
      createPermissionIndex: 0,
      genderOptions: ['男', '女'],
      genderIndex: 0,
      avatarOptions: [
        '/images/toux1.jpeg',
        '/images/toux2.jpeg',
        '/images/toux3.jpeg',
        '/images/toux4.jpeg',
        '/images/toux5.jpeg'
      ],
      addMemberForm: {
        permissionLevel: 2, // 默认员工
        phone: '',
        lastName: '',
        username: '',
        avatarUrl: '',
        gender: 1, // 默认男性
        amount: '',
        discount: ''
      },
      // 编辑成员相关数据
      showEditMemberModal: false,
      editMemberForm: {
        userId: null,
        username: '',
        permissionName: '',
        phone: '',
        avatarUrl: '',
        createdAt: '',
        updatedAt: '',
        status: 0 // 0-正常，1-禁用
      }
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
      this.showEditMemberModal(user)
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
    },

    // ========== 添加成员相关方法 ==========

    /**
     * 显示添加成员模态弹窗
     */
    showAddMemberModal() {
      this.resetAddMemberForm()
      this.setData({ showAddMemberModal: true })
    },

    /**
     * 隐藏添加成员模态弹窗
     */
    hideAddMemberModal() {
      this.setData({ showAddMemberModal: false })
    },

    /**
     * 重置添加成员表单
     */
    resetAddMemberForm() {
      this.setData({
        createPermissionIndex: 0,
        genderIndex: 0,
        showAvatarOptions: false,
        addMemberForm: {
          permissionLevel: 2,
          phone: '',
          lastName: '',
          username: '',
          avatarUrl: '',
          gender: 1,
          amount: '',
          discount: ''
        }
      })
    },

    /**
     * 权限选择处理
     */
    onPermissionSelect(e) {
      const permissionLevel = parseInt(e.currentTarget.dataset.permission)
      
      this.setData({
        'addMemberForm.permissionLevel': permissionLevel,
        createPermissionIndex: permissionLevel - 2 // 同步更新索引以保持兼容性
      })
    },

    /**
     * 权限选择变化处理（保留以保持兼容性）
     */
    onCreatePermissionChange(e) {
      const index = parseInt(e.detail.value)
      const permissionLevel = index + 2 // 员工=2, 会员=3, 游客=4
      
      this.setData({
        createPermissionIndex: index,
        'addMemberForm.permissionLevel': permissionLevel
      })
    },

    /**
     * 性别选择处理
     */
    onGenderSelect(e) {
      const gender = parseInt(e.currentTarget.dataset.gender)
      
      this.setData({
        'addMemberForm.gender': gender,
        genderIndex: gender - 1 // 同步更新索引以保持兼容性
      })
    },

    /**
     * 切换头像选择器显示状态
     */
    toggleAvatarSelector() {
      this.setData({
        showAvatarOptions: !this.data.showAvatarOptions
      })
    },

    /**
     * 选择头像
     */
    selectAvatar(e) {
      const index = e.currentTarget.dataset.index
      const { avatarOptions } = this.data
      const selectedAvatar = avatarOptions[index]
      
      this.setData({
        'addMemberForm.avatarUrl': selectedAvatar,
        showAvatarOptions: false  // 选择后自动关闭选择器
      })
    },

    /**
     * 添加成员表单手机号输入处理
     */
    onAddMemberPhoneInput(e) {
      this.setData({
        'addMemberForm.phone': e.detail.value
      })
    },

    /**
     * 添加成员表单姓氏输入处理
     */
    onAddMemberLastNameInput(e) {
      this.setData({
        'addMemberForm.lastName': e.detail.value
      })
    },

    /**
     * 用户名输入处理
     */
    onUsernameInput(e) {
      this.setData({
        'addMemberForm.username': e.detail.value
      })
    },

    /**
     * 初始余额输入处理
     */
    onAmountInput(e) {
      this.setData({
        'addMemberForm.amount': e.detail.value
      })
    },

    /**
     * 折扣率输入处理
     */
    onDiscountInput(e) {
      this.setData({
        'addMemberForm.discount': e.detail.value
      })
    },

    /**
     * 提交添加成员表单
     */
    async submitAddMemberForm() {
      if (!this.validateAddMemberForm()) {
        return
      }

      try {
        const formData = this.prepareAddMemberData()
        
        await this.callApiWithLoading(
          () => this.createUser(formData, this.data.userInfo),
          '创建中...'
        )
        
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        })
        
        this.hideAddMemberModal()
        this.search() // 刷新列表
      } catch (error) {
        this.showApiError(error, '添加失败')
      }
    },

    /**
     * 验证添加成员表单
     */
    validateAddMemberForm() {
      const { addMemberForm } = this.data
      
      // 验证必填字段
      if (!addMemberForm.phone.trim()) {
        wx.showToast({ title: '请输入手机号', icon: 'none' })
        return false
      }
      
      if (!/^1[3-9]\d{9}$/.test(addMemberForm.phone)) {
        wx.showToast({ title: '手机号格式不正确', icon: 'none' })
        return false
      }
      
      if (!addMemberForm.lastName.trim()) {
        wx.showToast({ title: '请输入姓氏', icon: 'none' })
        return false
      }
      
      // 会员专属字段验证
      if (addMemberForm.permissionLevel === 3) {
        if (addMemberForm.amount && !/^\d+(\.\d{1,2})?$/.test(addMemberForm.amount)) {
          wx.showToast({ title: '余额格式不正确', icon: 'none' })
          return false
        }
        
        if (addMemberForm.discount && !/^\d+(\.\d{1,2})?$/.test(addMemberForm.discount)) {
          wx.showToast({ title: '折扣率格式不正确', icon: 'none' })
          return false
        }
        
        const discountValue = parseFloat(addMemberForm.discount)
        if (addMemberForm.discount && (discountValue < 0 || discountValue > 1)) {
          wx.showToast({ title: '折扣率必须在0-1之间', icon: 'none' })
          return false
        }
      }
      
      return true
    },

    /**
     * 准备添加成员数据
     */
    prepareAddMemberData() {
      const { addMemberForm } = this.data
      const data = {
        permissionLevel: addMemberForm.permissionLevel,
        phone: addMemberForm.phone.trim(),
        lastName: addMemberForm.lastName.trim(),
        gender: addMemberForm.gender
      }
      
      // 可选字段
      if (addMemberForm.username.trim()) {
        data.username = addMemberForm.username.trim()
      }
      
      if (addMemberForm.avatarUrl.trim()) {
        data.avatarUrl = addMemberForm.avatarUrl.trim()
      }
      
      // 会员专属字段
      if (addMemberForm.permissionLevel === 3) {
        if (addMemberForm.amount) {
          data.amount = parseFloat(addMemberForm.amount)
        }
        
        if (addMemberForm.discount) {
          data.discount = parseFloat(addMemberForm.discount)
        }
      }
      
      return data
    },

    // ========== 编辑成员相关方法 ==========

    /**
     * 显示编辑成员模态弹窗
     * 
     * @param {Object} user - 要编辑的用户信息
     */
    showEditMemberModal(user) {
      // 格式化时间
      const createdAt = this.formatDateTime(user.createdAt)
      const updatedAt = this.formatDateTime(user.updatedAt)
      
      // 获取权限名称
      const permissionName = this.getPermissionName(user.permissionLevel)
      
      this.setData({
        showEditMemberModal: true,
        editMemberForm: {
          userId: user.userId,
          username: user.username || user.lastName,
          permissionName: permissionName,
          phone: user.phone,
          avatarUrl: user.avatarUrl || '',
          createdAt: createdAt,
          updatedAt: updatedAt,
          status: user.status || 0
        }
      })
    },

    /**
     * 隐藏编辑成员模态弹窗
     */
    hideEditMemberModal() {
      this.setData({ showEditMemberModal: false })
    },

    /**
     * 状态切换处理
     * 
     * @param {Object} e - 事件对象
     */
    onStatusChange(e) {
      const newStatus = e.detail.value ? 0 : 1 // true=启用(0), false=禁用(1)
      const statusText = newStatus === 0 ? '启用' : '禁用'
      
      wx.showModal({
        title: '确认操作',
        content: `确定要${statusText}该用户吗？`,
        confirmText: `确认${statusText}`,
        confirmColor: newStatus === 0 ? '#07c160' : '#ff4757',
        success: (res) => {
          if (res.confirm) {
            this.updateMemberStatus(newStatus)
          } else {
            // 用户取消，恢复开关状态
            this.setData({
              'editMemberForm.status': newStatus === 0 ? 1 : 0
            })
          }
        }
      })
    },

    /**
     * 更新用户状态
     * 
     * @param {Number} newStatus - 新状态 (0-启用, 1-禁用)
     */
    async updateMemberStatus(newStatus) {
      const { editMemberForm, userInfo } = this.data
      
      try {
        await this.callApiWithLoading(
          () => this.updateUserStatus(editMemberForm.userId, newStatus, userInfo),
          newStatus === 0 ? '启用中...' : '禁用中...'
        )
        
        // 更新本地状态
        this.setData({
          'editMemberForm.status': newStatus
        })
        
        // 更新列表中的用户状态
        this.updateUserStatusInList(editMemberForm.userId, newStatus)
        
        wx.showToast({
          title: newStatus === 0 ? '启用成功' : '禁用成功',
          icon: 'success'
        })
      } catch (error) {
        // 恢复原状态
        this.setData({
          'editMemberForm.status': newStatus === 0 ? 1 : 0
        })
        this.showApiError(error, newStatus === 0 ? '启用失败' : '禁用失败')
      }
    },

    /**
     * 保存成员更改
     */
    saveMemberChanges() {
      // 目前只有状态可以更改，直接关闭弹窗
      this.hideEditMemberModal()
      wx.showToast({
        title: '保存成功',
        icon: 'success'
      })
    },

    /**
     * 格式化日期时间
     * 
     * @param {String} dateTime - 日期时间字符串
     * @returns {String} 格式化后的日期时间
     */
    formatDateTime(dateTime) {
      if (!dateTime) return '未知'
      
      try {
        const date = new Date(dateTime)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        
        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        return dateTime
      }
    },

    /**
     * 获取权限名称
     * 
     * @param {Number} permissionLevel - 权限等级
     * @returns {String} 权限名称
     */
    getPermissionName(permissionLevel) {
      const permissionMap = {
        1: '店长',
        2: '员工', 
        3: '会员',
        4: '游客'
      }
      return permissionMap[permissionLevel] || '未知'
    },

    /**
     * 更新列表中的用户状态
     * 
     * @param {Number} userId - 用户ID
     * @param {Number} newStatus - 新状态
     */
    updateUserStatusInList(userId, newStatus) {
      const { list } = this.data
      const updatedList = list.map(item => {
        if (item.userId === userId) {
          return { ...item, status: newStatus }
        }
        return item
      })
      
      this.setData({ list: updatedList })
    },

    /**
     * 阻止事件冒泡
     */
    stopPropagation() {
      // 空方法，用于阻止模态弹窗点击事件冒泡
    }
  },
  modalMixin,
  formMixin,
  apiMixin
))
