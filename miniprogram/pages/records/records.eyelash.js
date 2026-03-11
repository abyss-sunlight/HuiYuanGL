const { request } = require('../../utils/request')
const {
  validatePhoneForFuzzySearch,
  validateDateRangeOrToast,
  sortByDateDesc,
  filterByDateRange
} = require('./records.helpers')

module.exports = {
  onLoadRecords() {
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

    this.setData({ currentTab: 'eyelash' })
    this.loadRecords()
  },

  refreshEyelashRecords() {
    this.setData({ currentTab: 'eyelash' })
    
    // 如果有搜索条件，执行搜索；否则加载所有记录
    const { searchPhone, searchLastName, startDate, endDate } = this.data
    if (searchPhone || searchLastName || (startDate && endDate)) {
      this.searchRecords()
    } else {
      this.loadRecords()
    }
  },

  onSearchPhoneInput(e) {
    this.setData({ searchPhone: e.detail.value })
  },

  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value })
  },

  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value })
  },

  loadRecords() {
    if (!this.data.isLoggedIn) {
      this.showLoginModal()
      return
    }

    this.setData({ statusText: '加载中...' })

    request({ url: '/api/eyelash-records' })
      .then((data) => {
        const records = sortByDateDesc(data, 'recordDate')
        this.setData({ records, statusText: '已加载' })
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '请求失败'
        this.setData({ statusText: '失败：' + msg })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  },

  searchByPhone() {
    if (!validatePhoneForFuzzySearch(this.data.searchPhone)) return

    this.setData({ statusText: '搜索中...' })

    request({ url: `/api/eyelash-records/phone/${this.data.searchPhone}` })
      .then((data) => {
        const records = sortByDateDesc(data, 'recordDate')
        this.setData({ records, statusText: '搜索完成' })

        if (records.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ statusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  },

  searchByDateRange() {
    if (!validateDateRangeOrToast(this.data.startDate, this.data.endDate)) return

    this.setData({ statusText: '搜索中...' })

    const url = `/api/eyelash-records/date-range?startDate=${this.data.startDate}&endDate=${this.data.endDate}`
    request({ url })
      .then((data) => {
        const records = sortByDateDesc(data, 'recordDate')
        this.setData({ records, statusText: '搜索完成' })

        if (records.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ statusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  },

  searchRecords() {
    const { searchPhone, searchLastName, startDate, endDate, userInfo } = this.data
    let url = '/api/eyelash-records'

    const hasPhone = !!searchPhone && userInfo.permissionLevel <= 2
    const hasLastName = !!searchLastName && userInfo.permissionLevel <= 2
    const hasDate = !!startDate && !!endDate && userInfo.permissionLevel <= 4

    if (hasPhone && !validatePhoneForFuzzySearch(searchPhone)) return
    if (hasDate && !validateDateRangeOrToast(startDate, endDate)) return

    // 根据搜索条件组合选择合适的API
    if (hasPhone && hasLastName && hasDate) {
      // 手机号 + 姓氏 + 日期：使用手机号搜索，然后在前端过滤姓氏和日期
      url = `/api/eyelash-records/phone/${searchPhone}`
    } else if (hasPhone && hasLastName) {
      // 手机号 + 姓氏：使用手机号搜索，然后在前端过滤姓氏
      url = `/api/eyelash-records/phone/${searchPhone}`
    } else if (hasPhone && hasDate) {
      // 手机号 + 日期：使用手机号搜索，然后在前端过滤日期
      url = `/api/eyelash-records/phone/${searchPhone}`
    } else if (hasLastName && hasDate) {
      // 姓氏 + 日期：使用后端支持的组合搜索
      url = `/api/eyelash-records/last-name/${searchLastName}/date-range?startDate=${startDate}&endDate=${endDate}`
    } else if (hasPhone) {
      // 仅手机号
      url = `/api/eyelash-records/phone/${searchPhone}`
    } else if (hasLastName) {
      // 仅姓氏
      url = `/api/eyelash-records/last-name/${searchLastName}`
    } else if (hasDate) {
      // 仅日期
      url = `/api/eyelash-records/date-range?startDate=${startDate}&endDate=${endDate}`
    }

    this.setData({ statusText: '搜索中...' })

    request({ url })
      .then((data) => {
        let records = Array.isArray(data) ? data : []

        // 前端过滤：根据搜索条件进行二次过滤
        if (hasPhone && hasLastName && hasDate) {
          // 手机号 + 姓氏 + 日期：过滤姓氏和日期
          records = records.filter(record => 
            record.lastName && record.lastName.includes(searchLastName) &&
            record.recordDate >= startDate && record.recordDate <= endDate
          )
        } else if (hasPhone && hasLastName) {
          // 手机号 + 姓氏：过滤姓氏
          records = records.filter(record => 
            record.lastName && record.lastName.includes(searchLastName)
          )
        } else if (hasPhone && hasDate) {
          // 手机号 + 日期：过滤日期
          records = filterByDateRange(records, 'recordDate', startDate, endDate)
        }

        records = sortByDateDesc(records, 'recordDate')

        this.setData({ records, statusText: '搜索完成' })

        if (records.length === 0) {
          wx.showToast({ title: '未找到相关记录', icon: 'none' })
        }
      })
      .catch((err) => {
        const msg = err && err.message ? err.message : '搜索失败'
        this.setData({ statusText: '搜索失败：' + msg })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  }
}
