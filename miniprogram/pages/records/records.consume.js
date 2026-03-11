const { request } = require('../../utils/request')
const {
  validatePhoneForFuzzySearch,
  validateDateRangeOrToast,
  sortByDateDesc,
  filterByDateRange
} = require('./records.helpers')

module.exports = {
  onLoadConsumeRecords() {
    if (!this.data.isLoggedIn) {
      this.showLoginModal()
      return
    }

    this.setData({
      searchPhone: '',
      searchLastName: '',
      startDate: '',
      endDate: ''
    })

    this.setData({ currentTab: 'consume' })
    this.loadConsumeRecords()
  },

  refreshConsumeRecords() {
    this.setData({ currentTab: 'consume' })
    
    // 如果有搜索条件，执行搜索；否则加载所有记录
    const { searchPhone, searchLastName, startDate, endDate } = this.data
    if (searchPhone || searchLastName || (startDate && endDate)) {
      this.searchConsumeRecords()
    } else {
      this.loadConsumeRecords()
    }
  },

  loadConsumeRecords() {
    if (!this.data.isLoggedIn) {
      this.showLoginModal()
      return
    }

    this.setData({ consumeStatusText: '加载中...' })

    request({ url: '/api/consume-records' })
      .then((data) => {
        const consumeRecords = sortByDateDesc(data, 'consumeDate')
        this.setData({ consumeRecords, consumeStatusText: '已加载' })
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '请求失败'
        this.setData({ consumeStatusText: '失败：' + msg })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  },

  searchConsumeByDateRange() {
    if (!validateDateRangeOrToast(this.data.startDate, this.data.endDate)) return

    this.setData({ consumeStatusText: '搜索中...' })

    const url = `/api/consume-records/date-range?startDate=${this.data.startDate}&endDate=${this.data.endDate}`
    request({ url })
      .then((data) => {
        const consumeRecords = sortByDateDesc(data, 'consumeDate')
        this.setData({ consumeRecords, consumeStatusText: '搜索完成' })

        if (consumeRecords.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ consumeStatusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  },

  searchConsumeByPhone() {
    if (!validatePhoneForFuzzySearch(this.data.searchPhone)) return

    this.setData({ consumeStatusText: '搜索中...' })

    request({ url: `/api/consume-records/phone/${this.data.searchPhone}` })
      .then((data) => {
        const consumeRecords = sortByDateDesc(data, 'consumeDate')
        this.setData({ consumeRecords, consumeStatusText: '搜索完成' })

        if (consumeRecords.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ consumeStatusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  },

  searchConsumeRecords() {
    const { searchPhone, searchLastName, startDate, endDate, userInfo } = this.data
    let url = '/api/consume-records'

    const hasPhone = !!searchPhone && userInfo.permissionLevel <= 2
    const hasLastName = !!searchLastName && userInfo.permissionLevel <= 2
    const hasDate = !!startDate && !!endDate && userInfo.permissionLevel <= 4

    if (hasPhone && !validatePhoneForFuzzySearch(searchPhone)) return
    if (hasDate && !validateDateRangeOrToast(startDate, endDate)) return

    // 根据搜索条件组合选择合适的API
    if (hasPhone && hasLastName && hasDate) {
      // 手机号 + 姓氏 + 日期：使用手机号搜索，然后在前端过滤姓氏和日期
      url = `/api/consume-records/phone/${searchPhone}`
    } else if (hasPhone && hasLastName) {
      // 手机号 + 姓氏：使用手机号搜索，然后在前端过滤姓氏
      url = `/api/consume-records/phone/${searchPhone}`
    } else if (hasPhone && hasDate) {
      // 手机号 + 日期：使用手机号搜索，然后在前端过滤日期
      url = `/api/consume-records/phone/${searchPhone}`
    } else if (hasLastName && hasDate) {
      // 姓氏 + 日期：使用后端支持的组合搜索
      url = `/api/consume-records/last-name/${searchLastName}/date-range?startDate=${startDate}&endDate=${endDate}`
    } else if (hasPhone) {
      // 仅手机号
      url = `/api/consume-records/phone/${searchPhone}`
    } else if (hasLastName) {
      // 仅姓氏
      url = `/api/consume-records/last-name/${searchLastName}`
    } else if (hasDate) {
      // 仅日期
      url = `/api/consume-records/date-range?startDate=${startDate}&endDate=${endDate}`
    }

    this.setData({ consumeStatusText: '搜索中...' })

    request({ url })
      .then((data) => {
        let consumeRecords = Array.isArray(data) ? data : []

        // 前端过滤：根据搜索条件进行二次过滤
        if (hasPhone && hasLastName && hasDate) {
          // 手机号 + 姓氏 + 日期：过滤姓氏和日期
          consumeRecords = consumeRecords.filter(record => 
            record.lastName && record.lastName.includes(searchLastName) &&
            record.consumeDate >= startDate && record.consumeDate <= endDate
          )
        } else if (hasPhone && hasLastName) {
          // 手机号 + 姓氏：过滤姓氏
          consumeRecords = consumeRecords.filter(record => 
            record.lastName && record.lastName.includes(searchLastName)
          )
        } else if (hasPhone && hasDate) {
          // 手机号 + 日期：过滤日期
          consumeRecords = filterByDateRange(consumeRecords, 'consumeDate', startDate, endDate)
        }

        consumeRecords = sortByDateDesc(consumeRecords, 'consumeDate')

        this.setData({ consumeRecords, consumeStatusText: '搜索完成' })

        if (consumeRecords.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ consumeStatusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  }
}
