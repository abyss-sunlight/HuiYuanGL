// AI客服悬浮窗组件
const { request } = require('../../utils/request.js');

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 用户信息
    userInfo: {
      type: Object,
      value: {}
    }
  },

  /**
   * 组件的初始数据
   */
  data: {
    isVisible: false,        // 聊天界面是否显示
    messages: [],           // 消息列表
    inputValue: '',         // 输入框内容
    isLoading: false,       // 是否正在加载
    unreadCount: 0,         // 未读消息数
    position: {             // 悬浮窗位置
      right: '40rpx',
      bottom: '160rpx'      // 避免遮挡tabBar
    },
    quickQuestions: [],      // 快捷问题
    scrollIntoView: '',      // 滚动到指定消息
    messageId: 0            // 消息ID计数器
  },

  /**
   * 组件的方法列表
   */
  methods: {
    /**
     * 切换聊天界面显示状态
     */
    toggleChat() {
      if (this.data.isVisible) {
        this.hideChat();
      } else {
        this.showChat();
      }
    },

    /**
     * 显示聊天界面
     */
    showChat() {
      this.setData({
        isVisible: true,
        unreadCount: 0
      });
      
      // 获取快捷问题
      this.getQuickQuestions();
    },

    /**
     * 隐藏聊天界面
     */
    hideChat() {
      this.setData({
        isVisible: false
      });
    },

    /**
     * 输入框内容变化
     */
    onInputChange(e) {
      this.setData({
        inputValue: e.detail.value
      });
    },

    /**
     * 发送消息
     */
    sendMessage() {
      const message = this.data.inputValue.trim();
      if (!message || this.data.isLoading) {
        return;
      }

      // 添加用户消息
      this.addMessage('user', message);
      
      // 清空输入框
      this.setData({
        inputValue: '',
        isLoading: true,
        quickQuestions: [] // 发送消息后隐藏快捷问题
      });

      // 发送到AI服务
      this.sendToAI(message);
    },

    /**
     * 发送快捷问题
     */
    sendQuickQuestion(e) {
      const question = e.currentTarget.dataset.question;
      this.setData({
        inputValue: question
      });
      this.sendMessage();
    },

    /**
     * 添加消息到列表
     */
    addMessage(role, content) {
      const messageId = this.data.messageId + 1;
      const message = {
        id: messageId,
        role: role,
        content: content,
        time: this.formatTime(new Date())
      };

      const messages = [...this.data.messages, message];
      
      this.setData({
        messages: messages,
        messageId: messageId,
        scrollIntoView: `msg-${messageId}`
      });
    },

    /**
     * 发送消息到AI服务
     */
    async sendToAI(message) {
      try {
        // 构建请求数据
        const requestData = {
          message: message,
          userInfo: this.properties.userInfo || {},
          chatHistory: this.data.messages.slice(-10).map(msg => ({
            role: msg.role,
            content: msg.content,
            timestamp: new Date(msg.time).getTime()
          })),
          sessionId: this.getSessionId()
        };

        // 调用AI客服接口
        const response = await request({
          url: '/api/customer-service/chat',
          method: 'POST',
          data: requestData
        });

        if (response) {
          const aiResponse = response;
          
          // 添加AI回复
          this.addMessage('assistant', aiResponse.content);
          
          // 处理操作建议
          if (aiResponse.actionType && aiResponse.actionParams) {
            this.handleAction(aiResponse.actionType, aiResponse.actionParams);
          }
          
          // 更新快捷问题
          if (aiResponse.quickQuestions && aiResponse.quickQuestions.length > 0) {
            this.setData({
              quickQuestions: aiResponse.quickQuestions
            });
          }

          // 触发外部事件
          this.triggerEvent('message', {
            type: 'ai-response',
            data: aiResponse
          });

        } else {
          this.addMessage('assistant', '抱歉，服务暂时不可用，请稍后再试。');
        }

      } catch (error) {
        console.error('AI客服请求失败:', error);
        this.addMessage('assistant', '网络连接异常，请检查网络后重试。如果问题持续，请联系店长。');
      } finally {
        this.setData({
          isLoading: false
        });
      }
    },

    /**
     * 处理AI建议的操作
     */
    handleAction(actionType, actionParams) {
      switch (actionType) {
        case 'navigate':
          // 页面跳转建议
          this.triggerEvent('message', {
            type: 'navigate',
            data: actionParams
          });
          break;
        case 'contact':
          // 联系店长
          this.triggerEvent('message', {
            type: 'contact',
            data: actionParams
          });
          break;
      }
    },

    /**
     * 获取快捷问题
     */
    async getQuickQuestions() {
      try {
        const userId = this.properties.userInfo?.userId;
        const response = await request({
          url: '/api/customer-service/quick-questions',
          method: 'GET',
          data: {
            userId: userId
          }
        });

        if (response && response.quickQuestions) {
          this.setData({
            quickQuestions: response.quickQuestions
          });
        } else {
          // 设置默认快捷问题
          this.setDefaultQuickQuestions();
        }

      } catch (error) {
        console.error('获取快捷问题失败:', error);
        this.setDefaultQuickQuestions();
      }
    },

    /**
     * 设置默认快捷问题
     */
    setDefaultQuickQuestions() {
      const isMember = this.properties.userInfo?.isMember;
      const questions = isMember ? [
        '我的余额是多少？',
        '查看我的消费记录',
        '我上次做的睫毛是什么款式？'
      ] : [
        '如何成为会员？',
        '充值有优惠吗？',
        '查看我的美睫记录'
      ];
      
      questions.push('如何联系店长？');
      
      this.setData({
        quickQuestions: questions
      });
    },

    /**
     * 获取会话ID
     */
    getSessionId() {
      let sessionId = wx.getStorageSync('ai_chat_session_id');
      if (!sessionId) {
        sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        wx.setStorageSync('ai_chat_session_id', sessionId);
      }
      return sessionId;
    },

    /**
     * 格式化时间
     */
    formatTime(date) {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      
      const timeStr = date.toTimeString().substr(0, 5); // HH:MM
      
      if (messageDate.getTime() === today.getTime()) {
        return timeStr;
      } else {
        // 显示日期+时间
        return `${date.getMonth() + 1}/${date.getDate()} ${timeStr}`;
      }
    },

    /**
     * 重置聊天状态
     */
    resetChat() {
      this.setData({
        messages: [],
        inputValue: '',
        isLoading: false,
        quickQuestions: [],
        scrollIntoView: '',
        messageId: 0
      });
    }
  },

  /**
   * 组件生命周期
   */
  lifetimes: {
    /**
     * 组件实例进入页面节点树时执行
     */
    attached() {
      // 初始化时获取快捷问题
      this.setDefaultQuickQuestions();
    },

    /**
     * 组件实例被从页面节点树移除时执行
     */
    detached() {
      // 清理工作
    }
  },

  /**
   * 监听属性变化
   */
  observers: {
    'userInfo': function(userInfo) {
      // 用户信息变化时更新快捷问题
      if (this.data.isVisible) {
        this.getQuickQuestions();
      }
    }
  }
});
