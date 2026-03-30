const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: null,
    carouselContent: {
      title: '',
      videoUrl: '',
      images: [],
      contents: []
    },
    currentImageIndex: 0,
    videoMuted: true,  // 视频默认静音
    stats: {
      totalMembers: 0,
      todayNew: 0,
      activeUsers: 0
    },
    rechargeDiscounts: []  // 充值折扣规则列表
  },

  onLoad() {
    this.setData({ userInfo: getUserInfo() })
    this.loadCarouselContent()
    this.loadStats()
    this.loadRechargeDiscounts()
  },

  onShow() {
    // 每次显示页面时重新加载数据
    this.setData({ userInfo: getUserInfo() })
    this.loadCarouselContent()
    this.loadStats()
    this.loadRechargeDiscounts()
  },

  loadCarouselContent() {
    // 检查权限：只有未登录、游客、会员可以看到轮播内容
    const userInfo = getUserInfo()
    if (userInfo && userInfo.permissionLevel < 3) {
      // 员工及以上权限不显示轮播内容
      this.setData({
        carouselContent: {
          title: '',
          videoUrl: '',
          images: [],
          contents: []
        }
      })
      return
    }

    // 调用轮播内容API
    request({
      url: '/api/carousel/latest',
      method: 'GET'
    }).then(response => {
      const images = [
        response.image1Url || '',
        response.image2Url || '',
        response.image3Url || '',
        response.image4Url || '',
        response.image5Url || ''
      ].filter(url => url.trim() !== '')

      const contents = [
        response.content1 || '',
        response.content2 || '',
        response.content3 || '',
        response.content4 || '',
        response.content5 || ''
      ].filter(content => content.trim() !== '')

      this.setData({
        carouselContent: {
          title: response.title || '欢迎光临',
          videoUrl: response.videoUrl || '',
          images: images,
          contents: contents
        }
      })
    }).catch(err => {
      console.error('加载轮播内容失败:', err)
      
      // 加载失败时设置默认值
      this.setData({
        carouselContent: {
          title: '欢迎光临',
          videoUrl: '',
          images: [],
          contents: []
        }
      })
    })
  },

  onImageChange(e) {
    this.setData({
      currentImageIndex: e.detail.current
    })
  },

  toggleMute() {
    this.setData({
      videoMuted: !this.data.videoMuted
    })
    
    // 显示提示
    wx.showToast({
      title: this.data.videoMuted ? '已静音' : '已开启声音',
      icon: 'none',
      duration: 1000
    })
  },

  loadStats() {
    // 检查登录状态和权限
    if (!isLoggedIn()) {
      // 未登录时不显示统计数据
      this.setData({
        stats: {
          totalMembers: 0,
          todayNew: 0,
          activeUsers: 0
        }
      })
      return
    }

    const userInfo = getUserInfo()
    if (userInfo && userInfo.permissionLevel > 2) {
      // 只有员工及以上权限可以查看统计数据
      this.setData({
        stats: {
          totalMembers: 0,
          todayNew: 0,
          activeUsers: 0
        }
      })
      return
    }

    // 调用统计API
    wx.showLoading({
      title: '加载统计数据...',
      mask: true
    })

    request({
      url: '/api/stats',
      method: 'GET'
    }).then(response => {
      wx.hideLoading()
      this.setData({
        stats: {
          totalMembers: response.totalMembers || 0,
          todayNew: response.todayNew || 0,
          activeUsers: response.activeUsers || 0
        }
      })
    }).catch(err => {
      wx.hideLoading()
      console.error('加载统计数据失败:', err)
      
      // 加载失败时显示默认值
      this.setData({
        stats: {
          totalMembers: 0,
          todayNew: 0,
          activeUsers: 0
        }
      })
      
      wx.showToast({
        title: '统计数据加载失败',
        icon: 'none'
      })
    })
  },

  goToRecords() {
    // 检查登录状态和权限
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看会员记录',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    if (userInfo.permissionLevel > 2) {
      wx.showToast({
        title: '权限不足，只有员工及以上权限可查看',
        icon: 'none'
      })
      return
    }

    wx.switchTab({ url: '/pages/records/records' })
  },

  goToProfile() {
    wx.switchTab({ url: '/pages/profile/profile' })
  },

  goToMembers() {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看成员列表',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    if (!userInfo || userInfo.permissionLevel > 2) {
      wx.showToast({
        title: '权限不足，只有员工及以上权限可查看',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({ url: '/pages/members/members' })
  },

  goToDiscountManagement() {
    console.log('点击充值折扣管理')
    
    if (!isLoggedIn()) {
      console.log('用户未登录')
      wx.showModal({
        title: '提示',
        content: '请先登录后管理充值折扣',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    const userInfo = getUserInfo()
    console.log('用户信息:', userInfo)
    
    if (!userInfo) {
      console.log('用户信息为空')
      wx.showToast({
        title: '用户信息获取失败',
        icon: 'none'
      })
      return
    }
    
    if (userInfo.permissionLevel !== 1) {
      console.log('权限不足:', userInfo.permissionLevel)
      wx.showToast({
        title: '权限不足，只有店长权限可管理',
        icon: 'none'
      })
      return
    }

    console.log('准备跳转到充值折扣管理页面')
    wx.navigateTo({ 
      url: '/pages/discount-management/discount-management',
      success: () => {
        console.log('跳转成功')
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        })
      }
    })
  },

  viewReports() {
    if (!isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看报表',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' })
          }
        }
      })
      return
    }

    // 跳转到AI分析页面
    wx.navigateTo({
      url: '/pages/ai-analysis/ai-analysis'
    })
  },

  loadRechargeDiscounts() {
    // 调用后端API获取启用的充值折扣规则
    request({
      url: '/api/recharge-discount/active',
      method: 'GET'
    }).then(discounts => {
      // request.js已经返回了data字段，所以discounts就是数组
      this.setData({
        rechargeDiscounts: discounts || []
      })
    }).catch(err => {
      console.error('加载充值折扣规则失败:', err)
      // 加载失败时设置为空数组
      this.setData({
        rechargeDiscounts: []
      })
    })
  },

  /**
   * 处理AI客服消息
   */
  handleAIMessage(e) {
    const { type, data } = e.detail
    
    switch(type) {
      case 'navigate':
        // 处理页面跳转请求
        this.handleAINavigation(data)
        break
      case 'contact':
        // 处理联系店长请求
        this.handleContactManager(data)
        break
      case 'ai-response':
        // 处理AI回复（可以在这里添加特殊逻辑）
        console.log('AI回复:', data)
        break
      default:
        console.log('AI消息:', data)
    }
  },

  /**
   * AI导航处理
   */
  handleAINavigation(page) {
    if (!page || !page.url) {
      return
    }

    wx.showModal({
      title: '页面跳转',
      content: `AI助手建议您跳转到${page.name || '相关页面'}，是否前往？`,
      success: (res) => {
        if (res.confirm) {
          // 判断是否为tabBar页面
          if (page.url.includes('/pages/profile/profile') || 
              page.url.includes('/pages/records/records')) {
            wx.switchTab({ url: page.url })
          } else {
            wx.navigateTo({ url: page.url })
          }
        }
      }
    })
  },

  /**
   * 联系店长处理
   */
  handleContactManager(data) {
    const contact = data?.contact || '18726685085'
    
    // 直接显示联系方式，不拨打电话
    wx.showToast({
      title: `店长电话：${contact}`,
      icon: 'none',
      duration: 3000
    })
  }
})
