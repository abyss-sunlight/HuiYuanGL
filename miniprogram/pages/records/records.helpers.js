function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

function validatePhoneOrToast(phone) {
  if (!phone) {
    wx.showToast({ title: '请输入手机号', icon: 'none' })
    return false
  }
  if (!isValidPhone(phone)) {
    wx.showToast({ title: '手机号格式不正确', icon: 'none' })
    return false
  }
  return true
}

// 用于模糊搜索的手机号验证，只检查是否为空
function validatePhoneForFuzzySearch(phone) {
  if (!phone) {
    wx.showToast({ title: '请输入手机号', icon: 'none' })
    return false
  }
  return true
}

function validateDateRangeOrToast(startDate, endDate) {
  if (!startDate || !endDate) {
    wx.showToast({ title: '请选择开始和结束日期', icon: 'none' })
    return false
  }
  if (new Date(startDate) > new Date(endDate)) {
    wx.showToast({ title: '开始日期不能晚于结束日期', icon: 'none' })
    return false
  }
  return true
}

function sortByDateDesc(list, fieldName) {
  const arr = Array.isArray(list) ? list : []
  arr.sort((a, b) => new Date(b[fieldName]) - new Date(a[fieldName]))
  return arr
}

function filterByDateRange(list, fieldName, startDate, endDate) {
  if (!startDate || !endDate) return list
  return (Array.isArray(list) ? list : []).filter((item) => {
    const d = item[fieldName]
    return d >= startDate && d <= endDate
  })
}

module.exports = {
  isValidPhone,
  validatePhoneOrToast,
  validatePhoneForFuzzySearch,
  validateDateRangeOrToast,
  sortByDateDesc,
  filterByDateRange
}
