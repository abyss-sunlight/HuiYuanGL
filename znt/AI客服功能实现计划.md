# 美甲美睫门店小程序 - AI客服功能实现计划

## 一、项目概述

### 1.1 功能定位
在主页添加AI客服功能，为用户提供智能问答服务，基于现有的知识库内容解答关于会员管理、充值、服务记录等问题。

### 1.2 技术配置
application.yml配置文件中已经有了一个AI配置，需要另外添加配置
- **API URL**: https://yuanqi.tencent.com/openapi/v1/agent/chat/completions
- **App ID**: 2036301370614053632
- **App Key**: AUaRrLgsYXAQZUh45Xww7eSTmRAmgD63
- **知识库位置**: `znt/美甲美睫门店小程序 - AI客服助手知识库.md`

### 1.3 交互设计
- **入口**: 主页右下角圆形悬浮窗
- **交互**: 点击后弹出对话框，包含聊天界面和输入框
- **形式**: 弹窗形式，非跳转新页面
- **动画**: 平滑的弹出和收起动画效果

## 二、功能需求分析

### 2.1 用户身份识别
AI客服需要根据用户身份提供差异化服务：
- **游客**（权限等级4）：查看美睫记录，引导充值
- **会员**（权限等级3）：查看消费记录、余额、美睫记录
- **员工/店长**：引导使用专门的店长AI助手

### 2.2 核心功能
1. **智能问答**: 基于知识库回答用户问题
2. **操作引导**: 指导用户使用小程序各项功能
3. **页面跳转**: 在用户同意后跳转到相关页面
4. **人工转接**: 提供店长联系方式处理复杂问题

### 2.3 边界限制
- 不处理闲聊和非业务相关问题
- 不进行资金操作和数据修改
- 不承诺固定折扣规则
- 不在用户未同意时进行页面跳转

## 三、技术实现方案

### 3.1 组件结构
```
miniprogram/
├── components/
│   └── ai-customer-service/
│       ├── ai-customer-service.js      # 组件逻辑
│       ├── ai-customer-service.json    # 组件配置
│       ├── ai-customer-service.wxml    # 组件模板
│       └── ai-customer-service.wxss    # 组件样式
└── pages/
    └── index/
        ├── index.js    # 添加AI客服相关方法
        ├── index.wxml  # 添加悬浮窗组件
        └── index.wxss  # 添加悬浮窗样式
```

### 3.2 核心组件设计

#### 3.2.1 悬浮窗组件 (ai-customer-service)
**功能特性**:
- 圆形悬浮按钮，固定在右下角
- 点击展开聊天界面
- 支持拖拽移动位置
- 未读消息提示

**主要状态**:
```javascript
data: {
  isVisible: false,        // 聊天界面是否显示
  messages: [],           // 消息列表
  inputValue: '',         // 输入框内容
  isLoading: false,       // 是否正在加载
  unreadCount: 0,         // 未读消息数
  position: {             // 悬浮窗位置
    right: '20px',
    bottom: '20px'
  }
}
```

#### 3.2.2 聊天界面设计
**界面元素**:
- 聊天消息区域（可滚动）
- 输入框和发送按钮
- 关闭按钮
- 快捷问题建议

**消息类型**:
- 用户消息（右对齐，蓝色气泡）
- AI回复（左对齐，白色气泡）
- 系统提示（居中，灰色文字）

### 3.3 AI集成方案

#### 3.3.1 API集成
使用提供的App ID和App Key集成AI服务：

```javascript
// utils/ai-service.js
const AI_CONFIG = {
  appId: '2036301370614053632',
  appKey: 'AUaRrLgsYXAQZUh45Xww7eSTmRAmgD63',
  baseUrl: 'https://api.example.com/ai'  // 实际API地址需要确认
}

// 发送消息到AI服务
function sendMessageToAI(message, userInfo) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: AI_CONFIG.baseUrl + '/chat',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'App-Id': AI_CONFIG.appId,
        'App-Key': AI_CONFIG.appKey
      },
      data: {
        message: message,
        userInfo: userInfo,
        knowledgeBase: '美甲美睫门店客服知识库'
      },
      success: resolve,
      fail: reject
    })
  })
}
```

#### 3.3.2 知识库处理
将知识库内容结构化处理：

```javascript
// utils/knowledge-base.js
const KNOWLEDGE_BASE = {
  // 用户身份相关
  userRoles: {
    guest: {
      permissions: ['查看美睫记录'],
      restrictions: ['查看消费记录', '查看余额']
    },
    member: {
      permissions: ['查看美睫记录', '查看消费记录', '查看余额'],
      features: ['储值消费', '会员折扣']
    }
  },
  
  // 常见问题
  faq: {
    becomeMember: '如何成为会员？充值有优惠吗？',
    checkBalance: '我的余额在哪里看？',
    lastService: '我上次做的睫毛是什么款式？'
  },
  
  // 页面跳转映射
  pageMapping: {
    balance: 'pages/profile/profile',
    records: 'pages/records/records',
    eyelashRecords: 'pages/records/records?tab=eyelash'
  }
}
```

### 3.4 页面集成

#### 3.4.1 主页修改
在 `pages/index/index.wxml` 中添加悬浮窗：

```xml
<!-- AI客服悬浮窗 -->
<ai-customer-service id="aiService" bind:message="handleAIMessage"></ai-customer-service>
```

在 `pages/index/index.js` 中添加处理方法：

```javascript
// 处理AI消息
handleAIMessage(e) {
  const { type, data } = e.detail
  
  switch(type) {
    case 'navigate':
      // 处理页面跳转请求
      this.handleAINavigation(data)
      break
    case 'contact':
      // 处理联系店长请求
      this.handleContactManager()
      break
    default:
      console.log('AI消息:', data)
  }
}

// AI导航处理
handleAINavigation(page) {
  wx.showModal({
    title: '页面跳转',
    content: `AI助手建议您跳转到${page.name}，是否前往？`,
    success: (res) => {
      if (res.confirm) {
        wx.navigateTo({ url: page.url })
      }
    }
  })
}
```

## 四、实现步骤

### 4.1 第一阶段：基础组件开发
1. **创建AI客服组件**
   - 实现悬浮窗基础UI
   - 添加点击展开/收起动画
   - 实现基础聊天界面

2. **主页集成**
   - 在主页添加组件引用
   - 调整页面布局避免遮挡
   - 添加基础交互逻辑

### 4.2 第二阶段：AI服务集成
1. **API服务开发**
   - 创建AI服务工具类
   - 实现消息发送和接收
   - 添加错误处理机制

2. **知识库集成**
   - 解析现有知识库文档
   - 构建问答映射表
   - 实现智能匹配逻辑

### 4.3 第三阶段：功能完善
1. **用户体验优化**
   - 添加快捷问题建议
   - 实现消息历史记录
   - 添加加载状态提示

2. **权限控制**
   - 根据用户权限显示不同功能
   - 实现智能身份识别
   - 添加权限相关提示

### 4.4 第四阶段：测试与优化
1. **功能测试**
   - 测试各种问答场景
   - 验证页面跳转功能
   - 测试权限控制逻辑

2. **性能优化**
   - 优化API调用频率
   - 实现消息缓存机制
   - 优化动画性能

## 五、界面设计规范

### 5.1 悬浮窗设计
- **尺寸**: 60px × 60px 圆形按钮
- **位置**: 距右边缘20px，距底部80px（避免遮挡tabBar）
- **颜色**: 主色调蓝色 (#007aff)
- **图标**: images/AI.jpeg
- **阴影**: 轻微阴影效果提升层次感

### 5.2 聊天界面设计
- **尺寸**: 85%屏幕宽度，70%屏幕高度
- **位置**: 居中显示，带遮罩层
- **圆角**: 12px圆角设计
- **背景**: 白色背景，浅灰色遮罩

### 5.3 消息气泡设计
- **用户消息**: 蓝色背景，白色文字，右对齐
- **AI消息**: 白色背景，深灰色文字，左对齐
- **系统消息**: 灰色背景，居中显示
- **间距**: 10px消息间距，15px上下边距
- **宽度**: 不超出聊天界面

### 5.4 弹出动画设计
- **点开第一步**: 输入框和发送按钮向左边伸出延长
- **第二步**: 聊天界面向上延长
- **打开的定位**: 定位在最下面

### 5.5 打开后的快捷语言
- **问题**：如何成为会员、如何联系店长、现在的充值折扣是多少、帮我查看我最近一星期的睫毛记录。
- **显示和消失**：在打开后显示快捷信息、发现一个信息后消失、再次打开后会再次显示快捷信息
- **位置**：和聊天记录在一起，位于信息最下面

## 六、技术难点与解决方案

### 6.1 AI响应延迟
**问题**: AI服务响应可能较慢，影响用户体验
**解决方案**:
- 添加加载动画和提示
- 实现本地常用问题缓存
- 设置超时处理机制

### 6.2 知识库更新
**问题**: 知识库内容需要定期更新
**解决方案**:
- 设计可配置的知识库结构
- 提供后台管理界面
- 实现热更新机制

### 6.3 权限判断
**问题**: 准确识别用户权限等级
**解决方案**:
- 结合本地存储和API验证
- 实现权限缓存机制
- 添加权限异常处理

## 七、开发时间估算

### 7.1 开发周期
- **总工期**: 5-7个工作日
- **第一阶段**: 1-2天（基础组件）
- **第二阶段**: 2-3天（AI集成）
- **第三阶段**: 1-2天（功能完善）
- **第四阶段**: 1天（测试优化）

### 7.2 人员配置
- **前端开发**: 1人
- **AI集成**: 1人
- **测试**: 1人

## 八、风险评估与应对

### 8.1 技术风险
**风险**: AI服务不稳定或响应慢
**应对**: 
- 实现降级方案（本地FAQ）
- 添加重试机制
- 设置合理的超时时间

### 8.2 用户体验风险
**风险**: AI回答不准确或不符合业务逻辑
**应对**:
- 完善知识库内容
- 添加人工转接机制
- 收集用户反馈持续优化

### 8.3 安全风险
**风险**: API密钥泄露或滥用
**应对**:
- 服务端代理API调用
- 实现访问频率限制
- 定期更新密钥

## 九、后续优化方向

### 9.1 功能扩展
- 支持语音输入和语音回复
- 添加表情包和图片支持
- 实现多轮对话上下文理解

### 9.2 智能化提升
- 基于用户行为的个性化推荐
- 智能问题预测和主动服务
- 情感识别和个性化回复

### 9.3 数据分析
- 用户问题统计分析
- AI服务质量监控
- 知识库效果评估

---

**文档版本**: v1.0  
**创建日期**: 2026年3月24日  
**最后更新**: 2026年3月24日  
**负责人**: AI客服功能开发团队
