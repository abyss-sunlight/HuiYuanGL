/**
 * 测试验证工具
 * 用于验证AI客服功能的正确性
 */

const { searchKnowledgeBase, getRecommendedQuestions, getUserRole } = require('./knowledge-base')

/**
 * 验证知识库搜索功能
 */
function validateKnowledgeBase() {
  console.log('=== 验证知识库搜索功能 ===')
  
  const testCases = [
    {
      question: '如何成为会员？',
      userPermission: 4,
      expectFound: true
    },
    {
      question: '我的余额在哪里看？',
      userPermission: 3,
      expectFound: true
    },
    {
      question: '营业时间是什么时候？',
      userPermission: 4,
      expectFound: true
    },
    {
      question: '如何管理会员信息？',
      userPermission: 2,
      expectFound: true
    }
  ]
  
  let passedTests = 0
  
  testCases.forEach((testCase, index) => {
    const userInfo = { permissionLevel: testCase.userPermission }
    const result = searchKnowledgeBase(testCase.question, userInfo)
    
    const passed = result.found === testCase.expectFound
    console.log(`测试 ${index + 1}: "${testCase.question}" - ${passed ? '✅' : '❌'}`)
    
    if (passed) {
      passedTests++
    }
  })
  
  console.log(`知识库验证: ${passedTests}/${testCases.length} 通过`)
  return passedTests === testCases.length
}

/**
 * 验证推荐问题功能
 */
function validateRecommendedQuestions() {
  console.log('=== 验证推荐问题功能 ===')
  
  const roles = [
    { permissionLevel: 4, expectedCount: 4 },
    { permissionLevel: 3, expectedCount: 4 },
    { permissionLevel: 2, expectedCount: 4 }
  ]
  
  let passedTests = 0
  
  roles.forEach((role, index) => {
    const userInfo = { permissionLevel: role.permissionLevel }
    const questions = getRecommendedQuestions(userInfo)
    
    const passed = Array.isArray(questions) && questions.length > 0
    console.log(`角色 ${index + 1} 推荐问题: ${questions.length} 个 - ${passed ? '✅' : '❌'}`)
    
    if (passed) {
      passedTests++
    }
  })
  
  console.log(`推荐问题验证: ${passedTests}/${roles.length} 通过`)
  return passedTests === roles.length
}

/**
 * 验证用户角色识别
 */
function validateUserRoleRecognition() {
  console.log('=== 验证用户角色识别 ===')
  
  const testCases = [
    { permissionLevel: 4, expectedRole: '游客' },
    { permissionLevel: 3, expectedRole: '会员' },
    { permissionLevel: 2, expectedRole: '员工' },
    { permissionLevel: 1, expectedRole: '店长' }
  ]
  
  let passedTests = 0
  
  testCases.forEach((testCase, index) => {
    const userInfo = { permissionLevel: testCase.permissionLevel }
    const role = getUserRole(userInfo)
    
    const passed = role.name === testCase.expectedRole
    console.log(`权限级别 ${testCase.permissionLevel} -> ${role.name} - ${passed ? '✅' : '❌'}`)
    
    if (passed) {
      passedTests++
    }
  })
  
  console.log(`角色识别验证: ${passedTests}/${testCases.length} 通过`)
  return passedTests === testCases.length
}

/**
 * 运行所有验证
 */
function runAllValidations() {
  console.log('🚀 开始AI客服功能验证')
  
  const results = {
    knowledgeBase: validateKnowledgeBase(),
    recommendedQuestions: validateRecommendedQuestions(),
    userRoleRecognition: validateUserRoleRecognition()
  }
  
  const passedCount = Object.values(results).filter(Boolean).length
  const totalCount = Object.keys(results).length
  
  console.log('\n📊 验证总结:')
  console.log(`通过项目: ${passedCount}/${totalCount}`)
  
  if (passedCount === totalCount) {
    console.log('🎉 所有验证通过!')
  } else {
    console.log('⚠️ 部分验证未通过，需要检查')
  }
  
  return results
}

module.exports = {
  validateKnowledgeBase,
  validateRecommendedQuestions,
  validateUserRoleRecognition,
  runAllValidations
}
