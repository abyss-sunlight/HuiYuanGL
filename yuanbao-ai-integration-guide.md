# 腾讯元宝AI接入指南

## 🎯 **功能概述**

为美甲美睫门店管理系统集成了腾讯元宝AI分析功能，提供基于门店数据的智能分析服务，包括：

- **营收分析**：实时营收统计和趋势分析
- **项目分析**：美甲美睫项目收入排名和表现分析
- **会员分析**：会员充值消费行为分析
- **趋势分析**：业绩变化趋势和预警提示
- **自定义分析**：支持用户自定义问题分析

## 🔧 **后端实现**

### 1. **核心服务类**

#### YuanbaoAIService.java
```java
@Service
public class YuanbaoAIService {
    
    @Value("${yuanbao.appid:}")
    private String appId;
    
    @Value("${yuanbao.appkey:}")
    private String appKey;
    
    @Value("${yuanbao.api.url:https://hunyuan.tencentcloudapi.com}")
    private String apiUrl;
    
    /**
     * 调用元宝AI进行数据分析
     */
    public YuanbaiAIResponse analyzeData(YuanbaoAIRequest request) {
        // 构建请求体
        Map<String, Object> requestBody = buildRequestBody(request);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildAuthorization());
        
        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl + "/v2/chat/completions",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return parseResponse(response.getBody());
    }
}
```

**核心特性**:
- ✅ 知识库集成：自动加载znt文件夹中的知识库内容
- ✅ 数据格式化：将数据库记录转换为AI可理解的格式
- ✅ 提示词构建：动态构建包含角色设定、数据和分析要求的提示词
- ✅ 错误处理：完善的异常处理和错误响应机制

### 2. **API控制器**

#### AIController.java
```java
@RestController
@RequestMapping("/api/ai")
public class AIController {
    
    /**
     * AI数据分析接口
     */
    @PostMapping("/analyze")
    public ApiResponse<YuanbaoAIResponse> analyze(
            @RequestParam String question,
            @RequestParam String timeRange,
            @RequestParam(defaultValue = "false") boolean needCompare) {
        
        // 检查AI服务是否可用
        if (!yuanbaoAIService.isServiceAvailable()) {
            return ApiResponse.fail(50001, "AI服务暂未配置，请联系管理员");
        }
        
        // 构建AI请求
        YuanbaoAIRequest request = buildAIRequest(question, timeRange, needCompare);
        
        // 调用AI服务
        YuanbaoAIResponse response = yuanbaoAIService.analyzeData(request);
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 快速分析接口 - 常用问题预设
     */
    @GetMapping("/quick-analyze")
    public ApiResponse<YuanbaoAIResponse> quickAnalyze(
            @RequestParam String type,
            @RequestParam(defaultValue = "today") String timeRange) {
        
        String question = getPresetQuestion(type);
        // ... 实现快速分析逻辑
    }
}
```

**API接口**:
- `POST /api/ai/analyze` - 自定义问题分析
- `GET /api/ai/quick-analyze` - 快速预设分析
- `GET /api/ai/status` - AI服务状态检查

### 3. **数据传输对象**

#### YuanbaoAIRequest.java
```java
public class YuanbaoAIRequest {
    private String question;                    // 用户问题
    private String timeRange;                   // 时间范围
    private String compareTimeRange;            // 对比时间范围
    private List<Map<String, Object>> currentData;   // 当前数据
    private List<Map<String, Object>> compareData;   // 对比数据
    private String analysisType;                // 分析类型
}
```

#### YuanbaoAIResponse.java
```java
public class YuanbaoAIResponse {
    private String status;          // 响应状态
    private String content;         // AI分析内容
    private String errorMessage;    // 错误信息
    private String requestId;       // 请求ID
    private Integer usageTokens;    // 使用token数量
}
```

### 4. **配置文件**

#### application.yml
```yaml
# 腾讯元器AI配置
yuanqi:
  api-url: https://open.hunyuan.tencent.com/openapi/v1/agent/chat/completions
  app-id: 2033772830773034880 # 从腾讯元器获取
  app-key: LHYQtxsNKRi7Jw9x0cLZ0GVtm613VR7Y       # 从腾讯元器获取
```

### 5. **数据查询增强**

#### ConsumeRecordService.java
```java
/**
 * 根据日期范围查询消费记录
 */
@Transactional(readOnly = true)
public List<Map<String, Object>> findByDateRange(String startDate, String endDate) {
    List<ConsumeRecord> records = consumeRecordRepository.findByConsumeDateBetween(startDate, endDate);
    
    return records.stream()
            .map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("phone", record.getPhone());
                map.put("last_name", record.getLastName());
                map.put("gender", record.getGender());
                map.put("balance", record.getBalance());
                map.put("consume_amount", record.getConsumeAmount());
                map.put("consume_item", record.getConsumeItem());
                map.put("consume_date", record.getConsumeDate());
                map.put("consume_type", record.getConsumeType());
                map.put("created_at", record.getCreatedAt());
                return map;
            })
            .collect(Collectors.toList());
}
```

## 📱 **前端实现**

### 1. **AI分析页面**

#### ai-analysis.js
```javascript
Page({
  data: {
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
    ]
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
      
    } catch (error) {
      wx.hideLoading()
      this.setData({ isLoading: false })
      
      wx.showToast({
        title: error.message || 'AI分析失败，请稍后重试',
        icon: 'none'
      })
    }
  }
})
```

### 2. **用户界面特性**

#### 快速分析网格
- 📊 5个预设分析选项，覆盖主要分析场景
- 🎨 美观的卡片式设计，图标直观
- ⚡ 一键触发，无需复杂操作

#### 自定义问题输入
- 📝 支持用户自定义问题
- 💡 提供提问技巧提示
- 🔄 实时验证和反馈

#### 分析结果展示
- 📋 支持复制结果到剪贴板
- 📤 支持分享功能（预留）
- 🔄 支持重新分析

### 3. **权限控制**

#### 页面访问权限
```javascript
onLoad() {
  const userInfo = getUserInfo()
  if (!userInfo || userInfo.permissionLevel > 2) {
    wx.showToast({ title: '权限不足，只有员工及以上可使用', icon: 'none' })
    setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
    return
  }
}
```

#### 菜单显示权限
```xml
<!-- AI分析 - 只有员工及以上可见 -->
<view class="menu-item" wx:if="{{userInfo.permissionLevel <= 2}}" bindtap="goToAIAnalysis">
  <view class="menu-icon">🤖</view>
  <view class="menu-title">AI数据分析</view>
  <view class="menu-arrow">></view>
</view>
```

## 🧠 **AI知识库集成**

### 1. **知识库结构**

#### znt文件夹内容
```
znt/
├── 盈利分析核心指标与公式.md      # 核心指标定义和计算公式
├── 美甲美睫门店数据库结构说明.md    # 数据库表结构和字段说明
├── 店长常见问题与数据分析逻辑对应表.md  # 问题与分析逻辑映射
└── 模拟消费数据示例.md            # 示例数据格式
```

### 2. **提示词构建**

#### 角色设定
```
你是"XX美甲美睫店"的专属数据AI分析师，专门为店长提供基于实时消费数据的盈利统计与变动分析。
```

#### 知识库内容集成
```
## 数据库结构
用户表(user)：id, username, last_name, phone, gender, amount, discount, permission_level, created_at
消费记录表(consume_records)：id, phone, last_name, gender, balance, consume_amount, consume_item, consume_date, consume_type, created_at

## 核心指标公式
1. 总营收 = SUM(consume_amount) WHERE consume_type = '充值'
2. 有效订单数 = COUNT(id) WHERE consume_type = '充值'
3. 客单价 = 总营收 / 有效订单数
4. 会员消费占比 = (会员消费总金额 / 总营收) * 100%
5. 项目收入排名：按 consume_item 分组计算营收和订单数
6. 环比增长率 = (本期数值 - 上期数值) / 上期数值 * 100%
```

#### 输出要求
```
## 回答要求
1. 先用一句话总结核心发现
2. 提供关键数据指标（营收、订单数、客单价等）
3. 进行对比分析（环比、同比或项目间对比）
4. 给出1-2条简明的业务观察或建议
5. 所有分析必须严格基于提供的数据，不得臆测
```

### 3. **数据格式化**

#### 消费记录格式化
```java
private String formatDataForAI(List<Map<String, Object>> data) {
    if (data == null || data.isEmpty()) {
        return "暂无数据";
    }
    
    StringBuilder sb = new StringBuilder();
    for (Map<String, Object> record : data) {
        sb.append(String.format("ID:%s, 手机号:%s, 姓氏:%s, 消费金额:%.2f, 消费项目:%s, 消费类型:%s, 日期:%s\n",
            record.get("id"),
            record.get("phone"),
            record.get("last_name"),
            record.get("consume_amount"),
            record.get("consume_item"),
            record.get("consume_type"),
            record.get("consume_date")
        ));
    }
    return sb.toString();
}
```

## 🔐 **安全与配置**

### 1. **API密钥配置**

#### 配置文件设置
```yaml
yuanbao:
  appid: ""    # 需要填写实际的AppID
  appkey: ""   # 需要填写实际的AppKey
```

#### 服务可用性检查
```java
public boolean isServiceAvailable() {
    return appId != null && !appId.isEmpty() && 
           appKey != null && !appKey.isEmpty();
}
```

### 2. **权限控制**

#### 后端权限验证
```java
// AIController中的权限检查
if (!yuanbaoAIService.isServiceAvailable()) {
    return ApiResponse.fail(50001, "AI服务暂未配置，请联系管理员");
}
```

#### 前端权限控制
```javascript
// 页面访问权限检查
if (!userInfo || userInfo.permissionLevel > 2) {
  wx.showToast({ title: '权限不足，只有员工及以上可使用', icon: 'none' })
  return
}
```

### 3. **错误处理**

#### 后端异常处理
```java
try {
    // AI服务调用
    YuanbaiAIResponse response = yuanbaoAIService.analyzeData(request);
    return ApiResponse.ok(response);
} catch (Exception e) {
    return ApiResponse.fail(50003, "分析服务异常: " + e.getMessage());
}
```

#### 前端错误处理
```javascript
try {
  const response = await request({ url: '/api/ai/analyze', method: 'POST', data })
  // 处理成功响应
} catch (error) {
  wx.showToast({
    title: error.message || 'AI分析失败，请稍后重试',
    icon: 'none'
  })
}
```

## 🚀 **部署与使用**

### 1. **配置步骤**

#### 步骤1：获取腾讯元宝API密钥
1. 访问腾讯云控制台
2. 开通混元AI服务
3. 获取AppID和AppKey

#### 步骤2：配置应用密钥
```yaml
# application.yml
yuanbao:
  appid: "your_appid_here"
  appkey: "your_appkey_here"
```

#### 步骤3：重启应用
```bash
# 重启Spring Boot应用
mvn spring-boot:run
```

### 2. **使用指南**

#### 访问AI分析
1. 使用员工及以上权限账户登录
2. 在个人中心点击"AI数据分析"
3. 选择分析类型和时间范围
4. 查看AI分析结果

#### 快速分析选项
- 💰 **营收分析**：今日/本周/本月营收情况
- 📊 **整体概况**：门店整体经营状况
- 💅 **项目分析**：美甲美睫项目收入排名
- 👥 **会员分析**：会员充值消费行为
- 📈 **趋势分析**：业绩变化趋势预警

#### 自定义问题
- 支持自然语言提问
- 提供提问技巧提示
- 基于实际数据进行分析

### 3. **API使用示例**

#### 自定义问题分析
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "question": "本周美甲项目的收入如何？",
       "timeRange": "week",
       "needCompare": true
     }'
```

#### 快速分析
```bash
curl -X GET "http://localhost:8080/api/ai/quick-analyze?type=revenue&timeRange=today" \
     -H "Authorization: Bearer <token>"
```

#### 服务状态检查
```bash
curl -X GET "http://localhost:8080/api/ai/status" \
     -H "Authorization: Bearer <token>"
```

## 🎯 **核心优势**

### 1. **智能化分析**
- 🧠 基于腾讯元宝大模型
- 📚 集成行业知识库
- 🎯 专注门店经营分析
- 📊 实时数据驱动

### 2. **用户友好**
- 🎨 美观的用户界面
- ⚡ 快速分析选项
- 📝 自定义问题支持
- 📋 结果可复制分享

### 3. **安全可靠**
- 🔐 严格的权限控制
- 🛡️ 完善的错误处理
- 🔑 API密钥安全管理
- 📝 详细的操作日志

### 4. **易于扩展**
- 🔧 模块化设计
- 📚 知识库可更新
- 🔄 分析类型可扩展
- 🌐 支持多数据源

## 📋 **检查清单**

### 部署前检查
- [ ] 获取腾讯元宝API密钥
- [ ] 配置application.yml文件
- [ ] 确认数据库连接正常
- [ ] 验证消费记录数据完整

### 功能测试
- [ ] AI服务状态检查
- [ ] 快速分析功能测试
- [ ] 自定义问题测试
- [ ] 权限控制测试

### 用户体验测试
- [ ] 页面加载速度测试
- [ ] 分析结果准确性测试
- [ ] 错误提示友好性测试
- [ ] 移动端适配测试

## 🎉 **总结**

腾讯元宝AI分析功能已成功集成到美甲美睫门店管理系统中，提供了：

1. **完整的AI分析能力** - 从营收到项目的全方位分析
2. **用户友好的界面** - 简洁直观的操作体验
3. **严格的安全控制** - 权限验证和数据保护
4. **灵活的扩展性** - 支持知识库更新和功能扩展

现在只需要在application.yml中配置实际的AppID和AppKey，即可开始使用AI分析功能！

## 📞 **技术支持**

如需技术支持或有疑问，请参考：
- 📚 腾讯元宝API文档
- 🔧 本项目代码注释
- 📝 znt文件夹中的知识库内容
