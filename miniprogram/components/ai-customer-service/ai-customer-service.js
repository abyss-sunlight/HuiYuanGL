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
      
      // 解析AI回复中的跳转信息
      let hasNavigation = false;
      let navigationUrl = '';
      let navigationText = '';
      let displayContent = content;
      
      if (role === 'assistant') {
        const navigationMatch = this.parseNavigationInfo(content);
        hasNavigation = navigationMatch.hasNavigation;
        navigationUrl = navigationMatch.url;
        navigationText = navigationMatch.text;
        
        // 如果是对象格式，提取content字段作为显示内容
        if (typeof content === 'object' && content.content) {
          displayContent = content.content;
        }
      }
      
      const message = {
        id: messageId,
        role: role,
        content: displayContent,
        time: this.formatTime(new Date()),
        hasNavigation: hasNavigation,
        navigationUrl: navigationUrl,
        navigationText: navigationText
      };

      const messages = [...this.data.messages, message];
      
      this.setData({
        messages: messages,
        messageId: messageId,
        scrollIntoView: `msg-${messageId}`
      });
    },

    /**
     * 解析AI回复中的跳转信息
     */
    parseNavigationInfo(content) {
      // 首先检查是否是包含actionType的对象格式回复
      if (typeof content === 'object' && content.actionType === 'navigate') {
        const actionParams = content.actionParams;
        if (actionParams && actionParams.page) {
          return {
            hasNavigation: true,
            url: '/' + actionParams.page, // 确保路径以/开头
            text: `前往${actionParams.name || '目标页面'}`
          };
        }
      }
      
      // 如果是字符串，使用原有的文本解析逻辑
      if (typeof content === 'string') {
        // 匹配跳转信息的正则表达式
        const patterns = [
          /跳转到[\"']?([^\"'\s]+)[\"']?页面/gi,
          /前往[\"']?([^\"'\s]+)[\"']?页面/gi,
          /打开[\"']?([^\"'\s]+)[\"']?页面/gi,
          /页面[\"']?([^\"'\s]+)[\"']?/gi,
          /跳转到[\"']?([^\"'\s]+)[\"']?/gi,
          /前往[\"']?([^\"'\s]+)[\"']?/gi
        ];
        
        // 页面路径映射
        const pageMapping = {
          '主页': '/pages/index/index',
          '记录': '/pages/records/records',
          '我的': '/pages/profile/profile',
          '会员管理': '/pages/members/members',
          '折扣管理': '/pages/discount-management/discount-management',
          'AI分析': '/pages/ai-analysis/ai-analysis',
          '美睫记录': '/pages/records/records'
        };
        
        for (const pattern of patterns) {
          const matches = content.match(pattern);
          if (matches) {
            for (const match of matches) {
              // 提取页面名称
              const pageNameMatch = match.match(/[\"']?([^\"'\s]+)[\"']?/);
              if (pageNameMatch) {
                const pageName = pageNameMatch[1];
                const url = pageMapping[pageName];
                
                if (url) {
                  return {
                    hasNavigation: true,
                    url: url,
                    text: `前往${pageName}`
                  };
                }
              }
            }
          }
        }
      }
      
      return {
        hasNavigation: false,
        url: '',
        text: ''
      };
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
          
          // 添加AI回复 - 传递完整对象以便解析跳转信息
          this.addMessage('assistant', aiResponse);
          
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
    },

    /**
     * 确认跳转
     */
    confirmNavigation(e) {
      const url = e.currentTarget.dataset.url;
      const messageId = e.currentTarget.dataset.messageId;
      
      if (url) {
        // 检查是否是tabBar页面（根据app.json中的实际配置）
        const tabBarPages = [
          '/pages/index/index',
          '/pages/records/records',
          '/pages/profile/profile'
        ];
        
        const isTabBarPage = tabBarPages.some(tabBarUrl => url.startsWith(tabBarUrl));
        
        const navigateMethod = isTabBarPage ? wx.switchTab : wx.navigateTo;
        
        // 执行页面跳转
        navigateMethod({
          url: url,
          success: () => {
            // 跳转成功后隐藏聊天框
            this.hideChat();
            
            // 添加确认消息
            this.addMessage('system', `已为您跳转到${this.getNavigationText(url)}`);
          },
          fail: (err) => {
            console.error('页面跳转失败:', err);
            wx.showToast({
              title: '页面跳转失败',
              icon: 'none'
            });
          }
        });
      }
      
      // 移除跳转按钮
      this.removeNavigationButtons(messageId);
    },

    /**
     * 取消跳转
     */
    cancelNavigation(e) {
      const messageId = e.currentTarget.dataset.messageId;
      
      // 移除跳转按钮
      this.removeNavigationButtons(messageId);
      
      // 添加取消消息
      this.addMessage('system', '已取消跳转');
    },

    /**
     * 移除跳转按钮
     */
    removeNavigationButtons(messageId) {
      const messages = this.data.messages.map(msg => {
        if (msg.id === messageId) {
          return {
            ...msg,
            hasNavigation: false,
            navigationUrl: '',
            navigationText: ''
          };
        }
        return msg;
      });
      
      this.setData({
        messages: messages
      });
    },

    /**
     * 获取页面导航文本
     */
    getNavigationText(url) {
      const pageTextMapping = {
        '/pages/index/index': '主页',
        '/pages/records/records': '记录',
        '/pages/profile/profile': '我的',
        '/pages/members/members': '会员管理',
        '/pages/discount-management/discount-management': '折扣管理',
        '/pages/ai-analysis/ai-analysis': 'AI分析'
      };
      
      return pageTextMapping[url] || '目标页面';
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
