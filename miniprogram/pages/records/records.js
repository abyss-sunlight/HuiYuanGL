/**
 * 睫毛记录页面
 * 
 * 功能说明：
 * 1. 显示用户的睫毛记录列表
 * 2. 支持按手机号搜索记录
 * 3. 登录状态检查和权限验证
 * 4. 用户信息展示
 * 
 * 权限要求：游客及以上权限（等级4）可以访问
 */

const { data } = require('./records.data')
const lifecycle = require('./records.lifecycle')
const eyelash = require('./records.eyelash')
const consume = require('./records.consume')
const ui = require('./records.ui')

Page(Object.assign(
  { data },
  lifecycle,
  eyelash,
  consume,
  ui,
  {
    clearSearchConditions() {
      this.setData({
        searchPhone: '',
        searchLastName: '',
        startDate: '',
        endDate: ''
      })

      if (this.data.currentTab === 'eyelash') {
        this.loadRecords()
      } else if (this.data.currentTab === 'consume') {
        this.loadConsumeRecords()
      }
    },

    // 姓氏搜索输入处理
    onSearchLastNameInput(e) {
      this.setData({
        searchLastName: e.detail.value
      })
    },

    // 跳转到成员列表并自动搜索
    jumpToMember(e) {
      const { lastName, phone } = e.currentTarget.dataset
      
      if (!lastName && !phone) {
        wx.showToast({ title: '缺少成员信息', icon: 'none' })
        return
      }

      // 构建搜索参数
      const searchParams = {}
      if (lastName) searchParams.lastName = lastName
      if (phone) searchParams.phone = phone

      // 将搜索参数存储到全局数据中，供成员列表页面使用
      const app = getApp()
      app.globalData.memberSearchParams = searchParams

      // 跳转到成员列表页面
      wx.navigateTo({
        url: '/pages/members/members'
      })
    }
  }
))
