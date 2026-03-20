# AI分析API测试指南

## 🔧 **修复内容**

### 问题分析
前端发送JSON数据，但后端使用`@RequestParam`接收参数，导致参数无法正确传递。

### 修复方案
将`@RequestParam`改为`@RequestBody`接收JSON数据，并添加参数验证。

## 📋 **修复前后对比**

### 修复前（错误）
```java
@PostMapping("/analyze")
public ApiResponse<YuanbaoAIResponse> analyze(
        @RequestParam String question,
        @RequestParam String timeRange,
        @RequestParam(defaultValue = "false") boolean needCompare) {
    
    // 直接使用参数，但前端发送的是JSON
}
```

### 修复后（正确）
```java
@PostMapping("/analyze")
public ApiResponse<YuanbaoAIResponse> analyze(@RequestBody Map<String, Object> requestBody) {
    
    // 提取参数
    String question = (String) requestBody.get("question");
    String timeRange = (String) requestBody.get("timeRange");
    Boolean needCompare = (Boolean) requestBody.getOrDefault("needCompare", false);
    
    // 参数验证
    if (question == null || question.trim().isEmpty()) {
        return ApiResponse.fail(40001, "问题不能为空");
    }
    if (timeRange == null || timeRange.trim().isEmpty()) {
        return ApiResponse.fail(40002, "时间范围不能为空");
    }
}
```

## 🧪 **测试用例**

### 1. **正常请求测试**

#### 请求格式
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "question": "今天的营收情况怎么样？",
       "timeRange": "today",
       "needCompare": true
     }'
```

#### 预期响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "success",
    "content": "AI分析结果内容...",
    "requestId": "xxx",
    "usageTokens": 123
  }
}
```

### 2. **参数验证测试**

#### 缺少问题参数
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "timeRange": "today",
       "needCompare": true
     }'
```

#### 预期响应
```json
{
  "code": 40001,
  "message": "问题不能为空",
  "data": null
}
```

#### 缺少时间范围参数
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "question": "今天的营收情况怎么样？",
       "needCompare": true
     }'
```

#### 预期响应
```json
{
  "code": 40002,
  "message": "时间范围不能为空",
  "data": null
}
```

### 3. **空参数测试**

#### 空问题
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "question": "",
       "timeRange": "today",
       "needCompare": true
     }'
```

#### 预期响应
```json
{
  "code": 40001,
  "message": "问题不能为空",
  "data": null
}
```

### 4. **AI服务未配置测试**

#### 请求（当yuanqi配置为空时）
```bash
curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "question": "今天的营收情况怎么样？",
       "timeRange": "today",
       "needCompare": true
     }'
```

#### 预期响应
```json
{
  "code": 50001,
  "message": "AI服务暂未配置，请联系管理员",
  "data": null
}
```

## 📱 **前端调用测试**

### JavaScript调用示例
```javascript
// 前端调用AI分析API
async function performAnalysis(question, timeRange, needCompare = false) {
  try {
    const response = await request({
      url: '/api/ai/analyze',
      method: 'POST',
      data: {
        question,
        timeRange,
        needCompare
      }
    })
    
    console.log('AI分析结果:', response.content)
    return response
    
  } catch (error) {
    console.error('AI分析失败:', error.message)
    throw error
  }
}

// 使用示例
performAnalysis('今天的营收情况怎么样？', 'today', true)
  .then(result => {
    // 处理分析结果
  })
  .catch(error => {
    // 处理错误
  })
```

## 🔍 **调试步骤**

### 1. **检查配置**
```yaml
# application.yml
yuanqi:
  api-url: https://open.hunyuan.tencent.com/openapi/v1/agent/chat/completions
  app-id: 2033772830773034880
  app-key: LHYQtxsNKRi7Jw9x0cLZ0GVtm613VR7Y
```

### 2. **检查服务状态**
```bash
curl -X GET "http://localhost:8080/api/ai/status"
```

### 3. **查看日志**
```bash
# 查看应用日志
tail -f logs/application.log

# 查找AI相关日志
grep "AI" logs/application.log
```

### 4. **测试数据流**
1. 前端发送JSON请求
2. 后端接收JSON数据
3. 参数验证通过
4. 构建AI请求
5. 调用腾讯元器API
6. 解析AI响应
7. 返回分析结果

## 🚨 **常见问题**

### 1. **参数传递失败**
**症状**: 前端发送请求，但后端收到的参数为null
**原因**: 使用了错误的参数接收方式
**解决**: 使用`@RequestBody`接收JSON数据

### 2. **Content-Type错误**
**症状**: 415 Unsupported Media Type
**原因**: 请求头缺少Content-Type或类型不正确
**解决**: 设置`Content-Type: application/json`

### 3. **认证失败**
**症状**: 401 Unauthorized
**原因**: 缺少Authorization头或token无效
**解决**: 添加`Authorization: Bearer <token>`

### 4. **AI服务配置错误**
**症状**: AI服务不可用
**原因**: yuanqi配置缺失或错误
**解决**: 检查application.yml中的配置

## 📊 **性能测试**

### 并发测试
```bash
# 使用ab工具进行并发测试
ab -n 100 -c 10 -H "Content-Type: application/json" \
   -H "Authorization: Bearer <token>" \
   -p test.json -T application/json \
   http://localhost:8080/api/ai/analyze
```

### 响应时间测试
```bash
# 测试单次请求响应时间
time curl -X POST "http://localhost:8080/api/ai/analyze" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{"question":"test","timeRange":"today"}'
```

## ✅ **验证清单**

- [ ] API接口可以正常接收JSON数据
- [ ] 参数验证功能正常
- [ ] AI服务状态检查正常
- [ ] 腾讯元器API调用成功
- [ ] 响应解析正确
- [ ] 错误处理完善
- [ ] 前端调用正常
- [ ] 权限控制有效

现在AI分析API已经修复完成，可以正常处理前端的JSON请求了！
