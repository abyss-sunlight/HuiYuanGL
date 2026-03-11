/**
 * 认证相关工具函数
 */

// 检查是否已登录
function isLoggedIn() {
  const token = wx.getStorageSync('token')
  return !!token
}

// 获取用户信息
function getUserInfo() {
  return wx.getStorageSync('userInfo') || null
}

// 获取token
function getToken() {
  return wx.getStorageSync('token') || ''
}

// 获取权限等级
function getPermissionLevel() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.permissionLevel : 4 // 默认游客权限
}

// 检查是否有足够权限
function hasPermission(requiredLevel) {
  const userLevel = getPermissionLevel()
  // 数字越小权限越高
  return userLevel <= requiredLevel
}

// 登出 - 仅清除本地存储，不跳转
function logoutLocal() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
}

// 登出并跳转到登录页
function logout() {
  logoutLocal()
  wx.reLaunch({ url: '/pages/profile/profile' })
}

// 检查登录状态并跳转
function checkLoginAndRedirect() {
  if (!isLoggedIn()) {
    wx.reLaunch({ url: '/pages/profile/profile' })
    return false
  }
  return true
}

// 获取权限名称
function getPermissionName(level) {
  const names = {
    1: '店长',
    2: '员工', 
    3: '会员',
    4: '游客'
  }
  return names[level] || '未知'
}

module.exports = {
  isLoggedIn,
  getUserInfo,
  getToken,
  getPermissionLevel,
  hasPermission,
  logout,
  logoutLocal,
  checkLoginAndRedirect,
  getPermissionName
}
