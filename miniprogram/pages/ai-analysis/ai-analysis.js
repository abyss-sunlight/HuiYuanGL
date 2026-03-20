const { request } = require('../../utils/request')
const { isLoggedIn, getUserInfo } = require('../../utils/auth')

Page({
  data: {
    userInfo: {},
    
    // 快速分析选项
    quickOptions: [
      { type: 'revenue', title: '营收分析', icon: '💰', question: '今天的营收情况怎么样？' },
      { type: 'overview', title: '整体概况', icon: '📊', question: '今天门店的整体经营情况如何？' },
      { type: 'projects', title: '项目分析', icon: '💅', question: '最近什么项目最赚钱？' },
      { type: 'members', title: '会员分析', icon: '👥', question: '会员充值和消费情况如何？' },
      { type: 'trend', title: '趋势分析', icon: '📈', question: '最近业绩有下滑吗？' }
    ],
    
    // 时间范围选项
    timeRanges: [
      { value: 'today', label: '今天' },
      { value: 'yesterday', label: '昨天' },
      { value: 'week', label: '本周' },
      { value: 'lastweek', label: '上周' },
      { value: 'month', label: '本月' },
      { value: 'lastmonth', label: '上月' }
    ],
    
    // 分析结果
    analysisResult: null,
    isLoading: false,
    
    // 自定义问题输入
    customQuestion: '',
    selectedTimeRange: 'today',
    showCustomInput: false
  },

  onLoad() {
    if (!isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }

    const userInfo = getUserInfo()
    if (!userInfo || userInfo.permissionLevel > 1) {
      wx.showToast({ title: '权限不足，只有店长及以上可使用', icon: 'none' })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
      return
    }

    this.setData({ userInfo })
    
    // 检查AI服务状态
    this.checkAIServiceStatus()
  },

  // 检查AI服务状态
  async checkAIServiceStatus() {
    try {
      const response = await request({
        url: '/api/ai/status',
        method: 'GET'
      })
      
      if (!response.available) {
        wx.showModal({
          title: 'AI服务提示',
          content: 'AI分析服务暂未配置，请联系管理员设置腾讯元宝API密钥',
          showCancel: false
        })
      }
    } catch (error) {
      console.error('检查AI服务状态失败:', error)
    }
  },

  // 快速分析点击
  onQuickAnalysis(e) {
    const { type, question } = e.currentTarget.dataset
    this.performAnalysis(question, this.data.selectedTimeRange)
  },

  // 时间范围选择
  onTimeRangeChange(e) {
    const index = e.detail.value
    const timeRange = this.data.timeRanges[index].value
    this.setData({ selectedTimeRange: timeRange })
  },

  // 显示自定义输入
  showCustomInput() {
    this.setData({ showCustomInput: true })
  },

  // 隐藏自定义输入
  hideCustomInput() {
    this.setData({ showCustomInput: false, customQuestion: '' })
  },

  // 自定义问题输入
  onCustomQuestionInput(e) {
    this.setData({ customQuestion: e.detail.value })
  },

  // 提交自定义问题
  submitCustomQuestion() {
    const question = this.data.customQuestion.trim()
    if (!question) {
      wx.showToast({ title: '请输入问题', icon: 'none' })
      return
    }
    
    this.hideCustomInput()
    this.performAnalysis(question, this.data.selectedTimeRange)
  },

  // 执行AI分析
  async performAnalysis(question, timeRange) {
    this.setData({ isLoading: true, analysisResult: null })
    
    try {
      wx.showLoading({ title: 'AI分析中...' })
      
      const response = await request({
        url: '/api/ai/analyze',
        method: 'POST',
        data: {
          question,
          timeRange,
          needCompare: true
        }
      })
      
      wx.hideLoading()
      this.setData({ 
        isLoading: false,
        analysisResult: response.content
      })
      
      // 滚动到结果区域
      this.scrollToResult()
      
    } catch (error) {
      wx.hideLoading()
      this.setData({ isLoading: false })
      
      console.error('AI分析失败:', error)
      wx.showToast({
        title: error.message || 'AI分析失败，请稍后重试',
        icon: 'none',
        duration: 3000
      })
    }
  },

  // 滚动到结果区域
  scrollToResult() {
    wx.createSelectorQuery()
      .select('.analysis-result')
      .boundingClientRect()
      .exec((res) => {
        if (res[0]) {
          wx.pageScrollTo({
            scrollTop: res[0].top - 20,
            duration: 300
          })
        }
      })
  },

  // 复制分析结果
  copyResult() {
    if (!this.data.analysisResult) return
    
    wx.setClipboardData({
      data: this.data.analysisResult,
      success: () => {
        wx.showToast({ title: '已复制到剪贴板', icon: 'success' })
      }
    })
  },

  // 分享分析结果
  shareResult() {
    if (!this.data.analysisResult) return
    
    // 这里可以实现分享功能
    wx.showToast({ title: '分享功能开发中', icon: 'none' })
  },

  // 重新分析
  reanalyze() {
    if (this.data.lastQuestion) {
      this.performAnalysis(this.data.lastQuestion, this.data.selectedTimeRange)
    }
  },

  // 格式化时间范围显示
  formatTimeRange(timeRange) {
    const range = this.data.timeRanges.find(r => r.value === timeRange)
    return range ? range.label : timeRange
  }
})
