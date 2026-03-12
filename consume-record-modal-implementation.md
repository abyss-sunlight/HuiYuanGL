# 会员消费记录模态弹窗功能实现

## ✅ 功能实现

### 1. 需求描述
实现添加会员消费记录功能，采用模态弹窗的方式，入口在成员列表中：
- **游客卡片的充值按钮**: 显示充值弹窗
- **会员卡片的会员消费/充值按钮**: 显示消费/充值弹窗

### 2. 功能特性

#### 游客充值弹窗
- **手机号**: 卡片用户的手机号，只读文本
- **姓氏**: 卡片用户的姓氏，只读文本
- **余额**: 0，只读文本
- **充值金额**: 输入框
- **消费项目**: "会员充值"，只读文本
- **消费类型**: "充值"，只读文本
- **日期**: 当天，只读文本

#### 会员消费/充值弹窗
- **手机号**: 卡片用户的手机号，只读文本
- **姓氏**: 卡片用户的姓氏，只读文本
- **余额**: 数据库中的余额，只读文本
- **消费/充值金额**: 输入框
- **消费项目**: 选择框（会员充值，美睫项目，美甲项目）
- **消费类型**: 选择框（充值，支出）
- **日期**: 当天，只读文本

## 🔧 技术实现

### 1. 数据结构设计

#### records.data.js - 消费记录数据
```javascript
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
```

### 2. UI界面设计

#### records.wxml - 消费记录模态弹窗
```xml
<!-- 消费记录模态弹窗 -->
<view class="modal-overlay" wx:if="{{showConsumeModal}}" bindtap="hideConsumeModal">
  <view class="modal-content" catchtap="stopPropagation">
    <view class="modal-header">
      <text class="modal-title">{{consumeType === 'recharge' ? '会员充值' : '会员消费'}}</text>
      <text class="modal-close" bindtap="hideConsumeModal">×</text>
    </view>
    
    <view class="modal-body">
      <!-- 手机号（只读） -->
      <view class="form-group">
        <text class="form-label">手机号</text>
        <view class="form-input readonly">{{consumeForm.phone}}</view>
      </view>
      
      <!-- 姓氏（只读） -->
      <view class="form-group">
        <text class="form-label">姓氏</text>
        <view class="form-input readonly">{{consumeForm.lastName}}</view>
      </view>
      
      <!-- 余额（只读） -->
      <view class="form-group">
        <text class="form-label">余额</text>
        <view class="form-input readonly">¥{{consumeForm.balance}}</view>
      </view>
      
      <!-- 金额输入 -->
      <view class="form-group">
        <text class="form-label">{{consumeType === 'recharge' ? '充值金额' : '消费金额'}} *</text>
        <input class="form-input" type="digit" placeholder="请输入金额" 
               value="{{consumeForm.consumeAmount}}" bindinput="onConsumeAmountInput" />
      </view>
      
      <!-- 消费项目选择（仅消费模式） -->
      <view class="form-group" wx:if="{{consumeType === 'consume'}}">
        <text class="form-label">消费项目 *</text>
        <picker class="form-input" mode="selector" range="{{consumeItems}}" 
                value="{{consumeItemIndex}}" bindchange="onConsumeItemChange">
          <view class="picker-text">{{consumeItems[consumeItemIndex] || '请选择消费项目'}}</view>
        </picker>
      </view>
      
      <!-- 消费项目（仅充值模式，固定为会员充值） -->
      <view class="form-group" wx:else>
        <text class="form-label">消费项目</text>
        <view class="form-input readonly">会员充值</view>
      </view>
      
      <!-- 消费类型选择（仅消费模式） -->
      <view class="form-group" wx:if="{{consumeType === 'consume'}}">
        <text class="form-label">消费类型 *</text>
        <picker class="form-input" mode="selector" range="{{consumeTypes}}" 
                value="{{consumeTypeIndex}}" bindchange="onConsumeTypeChange">
          <view class="picker-text">{{consumeTypes[consumeTypeIndex] || '请选择消费类型'}}</view>
        </picker>
      </view>
      
      <!-- 消费类型（仅充值模式，固定为充值） -->
      <view class="form-group" wx:else>
        <text class="form-label">消费类型</text>
        <view class="form-input readonly">充值</view>
      </view>
      
      <!-- 日期（只读） -->
      <view class="form-group">
        <text class="form-label">日期</text>
        <view class="form-input readonly">{{consumeForm.consumeDate}}</view>
      </view>
    </view>
    
    <view class="modal-footer">
      <button class="modal-btn cancel-btn" bindtap="hideConsumeModal">取消</button>
      <button class="modal-btn confirm-btn" bindtap="submitConsumeForm">确认{{consumeType === 'recharge' ? '充值' : '消费'}}</button>
    </view>
  </view>
</view>
```

### 3. 业务逻辑实现

#### records.ui.js - 消费记录方法
```javascript
// 显示游客充值弹窗
showRechargeModal(memberInfo) {
  this.setData({
    showConsumeModal: true,
    consumeType: 'recharge',
    consumeItemIndex: 0,
    consumeForm: {
      phone: memberInfo.phone || '',
      lastName: memberInfo.lastName || '',
      balance: 0,
      consumeAmount: '',
      consumeItem: '会员充值',
      consumeType: '充值',
      consumeDate: this.getCurrentDate()
    }
  })
},

// 显示会员消费/充值弹窗
showConsumeModal(memberInfo) {
  this.setData({
    showConsumeModal: true,
    consumeType: 'consume',
    consumeItemIndex: 0,
    consumeTypeIndex: 1, // 默认选择"支出"
    consumeForm: {
      phone: memberInfo.phone || '',
      lastName: memberInfo.lastName || '',
      balance: memberInfo.balance || 0,
      consumeAmount: '',
      consumeItem: '',
      consumeType: '支出',
      consumeDate: this.getCurrentDate()
    }
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
    this.loadConsumeRecords()
  } catch (error) {
    console.error('提交失败:', error)
    wx.showToast({
      title: consumeType === 'recharge' ? '充值失败' : '消费记录添加失败',
      icon: 'none'
    })
  }
}
```

### 4. 成员列表集成

#### members.js - 按钮事件处理
```javascript
// 游客充值
addRechargeRecord(e) {
  const user = e.currentTarget.dataset.user
  
  // 存储用户信息到全局数据
  const app = getApp()
  app.globalData.memberForRecharge = {
    phone: user.phone,
    lastName: user.lastName,
    balance: 0,
    userId: user.userId,
    type: 'recharge'
  }
  
  // 跳转到records页面
  wx.switchTab({
    url: '/pages/records/records',
    success: () => {
      setTimeout(() => {
        const pages = getCurrentPages()
        const recordsPage = pages.find(page => page.route === 'pages/records/records')
        if (recordsPage) {
          recordsPage.showRechargeModal({
            phone: user.phone,
            lastName: user.lastName
          })
        }
      }, 100)
    }
  })
},

// 会员消费/充值
addConsumeRecord(e) {
  const user = e.currentTarget.dataset.user
  
  // 存储用户信息到全局数据
  const app = getApp()
  app.globalData.memberForConsume = {
    phone: user.phone,
    lastName: user.lastName,
    balance: user.balance || 0,
    userId: user.userId,
    type: 'consume'
  }
  
  // 跳转到records页面
  wx.switchTab({
    url: '/pages/records/records',
    success: () => {
      setTimeout(() => {
        const pages = getCurrentPages()
        const recordsPage = pages.find(page => page.route === 'pages/records/records')
        if (recordsPage) {
          recordsPage.showConsumeModal({
            phone: user.phone,
            lastName: user.lastName,
            balance: user.balance || 0
          })
        }
      }, 100)
    }
  })
}
```

#### records.lifecycle.js - 全局数据处理
```javascript
onShow() {
  this.checkLoginStatus()
  
  const app = getApp()
  
  // 检查充值记录
  if (app.globalData && app.globalData.memberForRecharge) {
    const memberInfo = app.globalData.memberForRecharge
    if (this.showRechargeModal) {
      this.showRechargeModal({
        phone: memberInfo.phone,
        lastName: memberInfo.lastName
      })
    }
    app.globalData.memberForRecharge = null
  }
  
  // 检查消费记录
  if (app.globalData && app.globalData.memberForConsume) {
    const memberInfo = app.globalData.memberForConsume
    if (this.showConsumeModal) {
      this.showConsumeModal({
        phone: memberInfo.phone,
        lastName: memberInfo.lastName,
        balance: memberInfo.balance
      })
    }
    app.globalData.memberForConsume = null
  }
}
```

### 5. 样式设计

#### records-modal.wxss - 选择器样式
```css
/* 选择器样式 */
.picker-text {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  background: #fff;
  box-sizing: border-box;
}
```

## 📱 用户体验

### 1. 游客充值流程
1. 点击游客卡片的"充值"按钮
2. 自动跳转到记录页面
3. 显示充值弹窗，手机号和姓氏已预填
4. 余额显示为0
5. 输入充值金额
6. 消费项目和消费类型固定为"会员充值"和"充值"
7. 点击"确认充值"
8. 提交成功后自动关闭弹窗

### 2. 会员消费/充值流程
1. 点击会员卡片的"会员消费/充值"按钮
2. 自动跳转到记录页面
3. 显示消费弹窗，手机号、姓氏、余额已预填
4. 输入消费/充值金额
5. 选择消费项目（会员充值，美睫项目，美甲项目）
6. 选择消费类型（充值，支出）
7. 点击"确认消费"或"确认充值"
8. 提交成功后自动关闭弹窗

## 🎯 界面效果

### 游客充值弹窗
```
┌─────────────────────────────────┐
│ 会员充值                  ×    │
├─────────────────────────────────┤
│ 手机号                       │
│ [13800138001         🔒]     │  ← 只读
│ 姓氏                         │
│ [张                 🔒]     │  ← 只读
│ 余额                         │
│ [¥0                 🔒]     │  ← 只读
│ 充值金额 *                   │
│ [请输入金额               ]     │  ← 可编辑
│ 消费项目                     │
│ [会员充值            🔒]     │  ← 只读
│ 消费类型                     │
│ [充值               🔒]     │  ← 只读
│ 日期                         │
│ [ 2024-03-11        🔒]     │  ← 只读
├─────────────────────────────────┤
│ [取消]           [确认充值]    │
└─────────────────────────────────┘
```

### 会员消费/充值弹窗
```
┌─────────────────────────────────┐
│ 会员消费                  ×    │
├─────────────────────────────────┤
│ 手机号                       │
│ [13800138001         🔒]     │  ← 只读
│ 姓氏                         │
│ [张                 🔒]     │  ← 只读
│ 余额                         │
│ [¥150.00            🔒]     │  ← 只读
│ 消费金额 *                   │
│ [请输入金额               ]     │  ← 可编辑
│ 消费项目 *                   │
│ [会员充值 ▼]                 │  ← 选择器
│ 消费类型 *                   │
│ [支出 ▼]                     │  ← 选择器
│ 日期                         │
│ [ 2024-03-11        🔒]     │  ← 只读
├─────────────────────────────────┤
│ [取消]           [确认消费]    │
└─────────────────────────────────┘
```

## 🧪 测试要点

### 1. 基本功能测试
- [ ] 游客充值按钮正常工作
- [ ] 会员消费/充值按钮正常工作
- [ ] 表单验证功能正常
- [ ] 数据提交成功
- [ ] 列表自动刷新

### 2. 数据预填测试
- [ ] 游客充值时手机号和姓氏正确预填
- [ ] 会员消费时手机号、姓氏、余额正确预填
- [ ] 日期字段自动设置为当天
- [ ] 余额显示正确

### 3. 选择器功能测试
- [ ] 消费项目选择器正常工作
- [ ] 消费类型选择器正常工作
- [ ] 选择结果正确显示
- [ ] 充值模式下选择器正确隐藏

### 4. 交互体验测试
- [ ] 模态弹窗显示/隐藏正常
- [ ] 表单输入响应及时
- [ ] 验证提示友好
- [ ] 成功反馈及时

## 🔄 后续优化

1. **余额计算**: 充值/消费后自动更新余额
2. **历史记录**: 显示用户消费历史
3. **批量操作**: 支持批量充值/消费
4. **折扣计算**: 集成充值折扣功能
5. **报表统计**: 消费统计和报表功能

现在会员消费记录的模态弹窗功能已完整实现！
