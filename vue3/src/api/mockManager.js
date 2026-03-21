/**
 * Mock数据管理器
 * 功能描述：统一管理所有API接口的Mock数据实现
 */

// Mock数据存储
const mockData = new Map()

/**
 * 创建成功响应
 * @param {*} data 响应数据
 * @param {string} msg 响应消息
 * @returns {Object} 标准响应格式
 */
export function createSuccessResponse(data = null, msg = '操作成功') {
  return {
    code: '200',
    data,
    msg
  }
}

/**
 * 创建错误响应
 * @param {string} msg 错误消息
 * @param {string} code 错误码
 * @returns {Object} 标准错误响应格式
 */
export function createErrorResponse(msg = '操作失败', code = '500') {
  return {
    code,
    data: null,
    msg
  }
}

/**
 * 注册Mock API
 * @param {string} method HTTP方法
 * @param {string} url API路径
 * @param {Function} handler 处理函数
 */
export function registerMockApi(method, url, handler) {
  const key = `${method.toUpperCase()}_${url}`
  mockData.set(key, handler)
  console.log('🎀 注册Mock API:', key)
}

/**
 * 获取Mock处理函数
 * @param {string} method HTTP方法
 * @param {string} url API路径
 * @returns {Function|null} 处理函数
 */
export function getMockHandler(method, url) {
  const key = `${method.toUpperCase()}_${url}`
  const handler = mockData.get(key)
  console.log('🔍 查找Mock处理器:', key, handler ? '✅找到' : '❌未找到')
  console.log('📋 已注册的Mock API:', Array.from(mockData.keys()))
  return handler || null
}

// ==================== 用户认证相关Mock数据 ====================

// Mock用户数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    name: '管理员',
    email: 'admin@example.com',
    phone: '13800138000',
    sex: '男',
    avatar: '/img/avatar/admin.jpg',
    roleCode: 'ADMIN',
    token: 'mock_admin_token_123456',
    menuList: [
      { id: 1, name: '用户管理', path: '/back/user', icon: 'user' },
      { id: 2, name: '系统设置', path: '/back/system', icon: 'setting' }
    ]
  },
  {
    id: 2,
    username: 'user001',
    name: '普通用户',
    email: 'user001@example.com',
    phone: '13800138001',
    sex: '女',
    avatar: '/img/avatar/user001.jpg',
    roleCode: 'USER',
    token: 'mock_user_token_123456'
  }
]

// Mock测试账号说明
console.log('🎭 Mock测试账号:')
console.log('管理员: admin / 123456')
console.log('普通用户: user / 123456')

// 注册用户登录Mock
registerMockApi('POST', '/user/login', (params) => {
  const { username, password } = params
  
  if (!username || !password) {
    return createErrorResponse('用户名和密码不能为空', '400')
  }
  
  const user = mockUsers.find(u => u.username === username)
  if (!user) {
    return createErrorResponse('用户名不存在。测试账号: admin/123456 或 user/123456', '400')
  }

  if (password !== '123456') { // Mock密码验证
    return createErrorResponse('密码错误。测试密码: 123456', '400')
  }
  
  return createSuccessResponse(user, '登录成功')
})

// 注册用户注册Mock
registerMockApi('POST', '/user/add', (params) => {
  const { username, password, confirmPassword, email, phone, nickname, gender, birthday, userType } = params
  
  if (!username || !password || !email) {
    return createErrorResponse('用户名、密码和邮箱不能为空', '400')
  }
  
  // 密码确认校验（按照用户偏好，进行trim处理）
  const trimmedPassword = password.trim()
  const trimmedConfirmPassword = confirmPassword ? confirmPassword.trim() : ''
  if (confirmPassword && trimmedPassword !== trimmedConfirmPassword) {
    return createErrorResponse('两次输入的密码不一致', '400')
  }
  
  // 检查用户名是否已存在
  const existingUser = mockUsers.find(u => u.username === username)
  if (existingUser) {
    return createErrorResponse('用户名已存在', '400')
  }
  
  // 检查邮箱是否已存在
  const existingEmail = mockUsers.find(u => u.email === email)
  if (existingEmail) {
    return createErrorResponse('邮箱已被注册', '400')
  }
  
  // 创建新用户（按照新的数据结构）
  const newUser = {
    id: Date.now(), // 使用时间戳作为ID避免冲突
    username,
    nickname: nickname || username,
    email,
    phone: phone || null,
    gender: gender !== null && gender !== undefined ? gender : null,
    genderDisplayName: gender === 1 ? '男' : gender === 0 ? '女' : '未知',
    birthday: birthday || null,
    userType: userType || 1,
    userTypeDisplayName: (userType || 1) === 2 ? '管理员' : '普通用户',
    status: 1, // 新用户默认正常状态
    statusDisplayName: '正常',
    avatar: '/img/avatar/default.jpg',
    displayName: nickname || username,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  
  mockUsers.push(newUser)
  
  // 返回新用户信息（不包含密码）
  const { password: _, ...userResponse } = newUser
  return createSuccessResponse(userResponse, '注册成功')
})

// 注册获取当前用户信息Mock
registerMockApi('GET', '/user/current', () => {
  // 模拟从token获取用户信息
  const currentUser = mockUsers[0] // 默认返回第一个用户
  return createSuccessResponse(currentUser, '获取用户信息成功')
})

// 注册根据ID获取用户信息Mock
registerMockApi('GET', '/user/:id', (params, pathParams) => {
  const userId = parseInt(pathParams.id)
  const user = mockUsers.find(u => u.id === userId)
  
  if (!user) {
    return createErrorResponse('用户不存在', '404')
  }
  
  return createSuccessResponse(user, '获取用户信息成功')
})

// 注册更新用户信息Mock
registerMockApi('PUT', '/user/:id', (params, pathParams) => {
  const userId = parseInt(pathParams.id)
  const userIndex = mockUsers.findIndex(u => u.id === userId)
  
  if (userIndex === -1) {
    return createErrorResponse('用户不存在', '404')
  }
  
  // 更新用户信息
  mockUsers[userIndex] = { ...mockUsers[userIndex], ...params }
  
  return createSuccessResponse(mockUsers[userIndex], '用户信息更新成功')
})

// 注册修改密码Mock
registerMockApi('PUT', '/user/password/:id', (params, pathParams) => {
  const { oldPassword, newPassword } = params
  const userId = parseInt(pathParams.id)
  
  if (!oldPassword || !newPassword) {
    return createErrorResponse('旧密码和新密码不能为空', '400')
  }
  
  const user = mockUsers.find(u => u.id === userId)
  if (!user) {
    return createErrorResponse('用户不存在', '404')
  }
  
  // Mock密码验证（实际项目中应该验证旧密码）
  if (oldPassword !== '123456') {
    return createErrorResponse('旧密码错误', '400')
  }
  
  return createSuccessResponse(null, '密码修改成功')
})

// 注册忘记密码Mock
registerMockApi('GET', '/user/forget', (params) => {
  const { email, newPassword } = params
  if (!email || !newPassword) {
    return createErrorResponse('邮箱和新密码不能为空', '400')
  }
  
  const user = mockUsers.find(u => u.email === email)
  if (!user) {
    return createErrorResponse('邮箱不存在', '404')
  }
  
  return createSuccessResponse(null, '密码重置成功')
})

// 注册退出登录Mock
registerMockApi('POST', '/user/logout', () => {
  return createSuccessResponse(null, '退出登录成功')
})

// 注册用户列表分页查询Mock
registerMockApi('GET', '/user/page', (params) => {
  const { username, email, nickname, phone, userType, status, currentPage = 1, size = 10 } = params
  
  // 模拟更多用户数据
  const allUsers = [
    {
      id: 1,
      username: 'admin',
      nickname: '系统管理员',
      email: 'admin@example.com',
      phone: '13800138000',
      gender: 1,
      genderDisplayName: '男',
      birthday: '1985-01-01',
      userType: 2,
      userTypeDisplayName: '管理员',
      status: 1,
      statusDisplayName: '正常',
      avatar: '/img/avatar/admin.jpg',
      displayName: '系统管理员',
      createdAt: '2024-01-01T00:00:00',
      updatedAt: '2024-08-30T10:00:00'
    },
    {
      id: 2,
      username: 'user001',
      nickname: '小明',
      email: 'user001@example.com',
      phone: '13800138001',
      gender: 1,
      genderDisplayName: '男',
      birthday: '1990-05-15',
      userType: 1,
      userTypeDisplayName: '普通用户',
      status: 1,
      statusDisplayName: '正常',
      avatar: '/img/avatar/user001.jpg',
      displayName: '小明',
      createdAt: '2024-02-01T00:00:00',
      updatedAt: '2024-08-30T09:00:00'
    },
    {
      id: 3,
      username: 'user002',
      nickname: '小红',
      email: 'user002@example.com',
      phone: '13800138002',
      gender: 0,
      genderDisplayName: '女',
      birthday: '1992-08-20',
      userType: 1,
      userTypeDisplayName: '普通用户',
      status: 1,
      statusDisplayName: '正常',
      avatar: '/img/avatar/user002.jpg',
      displayName: '小红',
      createdAt: '2024-03-01T00:00:00',
      updatedAt: '2024-08-30T08:00:00'
    },
    {
      id: 4,
      username: 'user003',
      nickname: '小杨',
      email: 'user003@example.com',
      phone: '13800138003',
      gender: 1,
      genderDisplayName: '男',
      birthday: '1988-12-10',
      userType: 1,
      userTypeDisplayName: '普通用户',
      status: 0,
      statusDisplayName: '禁用',
      avatar: '/img/avatar/user003.jpg',
      displayName: '小杨',
      createdAt: '2024-04-01T00:00:00',
      updatedAt: '2024-08-30T07:00:00'
    }
  ]
  
  // 过滤数据
  let filteredUsers = allUsers
  
  if (username) {
    filteredUsers = filteredUsers.filter(user => 
      user.username.toLowerCase().includes(username.toLowerCase())
    )
  }
  
  if (email) {
    filteredUsers = filteredUsers.filter(user => 
      user.email.toLowerCase().includes(email.toLowerCase())
    )
  }
  
  if (nickname) {
    filteredUsers = filteredUsers.filter(user => 
      user.nickname && user.nickname.includes(nickname)
    )
  }
  
  if (phone) {
    filteredUsers = filteredUsers.filter(user => 
      user.phone && user.phone.includes(phone)
    )
  }
  
  if (userType !== undefined && userType !== null) {
    filteredUsers = filteredUsers.filter(user => user.userType === parseInt(userType))
  }
  
  if (status !== undefined && status !== null) {
    filteredUsers = filteredUsers.filter(user => user.status === parseInt(status))
  }
  
  // 分页处理
  const total = filteredUsers.length
  const start = (currentPage - 1) * size
  const end = start + size
  const records = filteredUsers.slice(start, end)
  
  return createSuccessResponse({
    records,
    total,
    current: currentPage,
    size
  }, '查询成功')
})

// 注册用户统计数据Mock
registerMockApi('GET', '/user/statistics', () => {
  return createSuccessResponse({
    totalUsers: 1247,
    activeUsers: 965,
    newUsers: 56,
    riskUsers: 23,
    totalGrowth: 5.2,
    activeGrowth: 3.8,
    newGrowth: 12.3
  }, '获取统计数据成功')
})

// 注册更新用户状态Mock
registerMockApi('PUT', '/user/:id/status', (params, pathParams) => {
  const userId = parseInt(pathParams.id)
  const { status } = params
  
  // 模拟参数校验
  if (status === undefined || status === null) {
    return createErrorResponse('用户状态不能为空', '400')
  }
  
  if (![0, 1].includes(parseInt(status))) {
    return createErrorResponse('无效的用户状态', '400')
  }
  
  // 模拟管理员防护
  if (userId === 1 && parseInt(status) === 0) {
    return createErrorResponse('不能禁用管理员账号', '400')
  }
  
  return createSuccessResponse(null, '用户状态更新成功')
})

// 注册删除用户Mock
registerMockApi('DELETE', '/user/:id', (params, pathParams) => {
  const userId = parseInt(pathParams.id)
  
  // 模拟管理员防护
  if (userId === 1) {
    return createErrorResponse('不能删除管理员账号', '400')
  }
  
  // 模拟用户不存在的情况
  if (userId > 100) {
    return createErrorResponse('用户不存在', '404')
  }
  
  return createSuccessResponse(null, '用户删除成功')
})

// 导出Mock管理器
export default {
  registerMockApi,
  getMockHandler,
  createSuccessResponse,
  createErrorResponse
}

// ==================== 知识库相关Mock数据 ====================

// Mock分类数据
const mockCategories = [
  { 
    id: 1, 
    parentId: null, 
    categoryName: '情绪管理', 
    categoryCode: 'emotion', 
    description: '情绪识别、调节与管理技巧',
    icon: 'fas fa-heart',
    sortOrder: 1,
    status: 1,
    articleCount: 15
  },
  { 
    id: 2, 
    parentId: null, 
    categoryName: '焦虑抑郁', 
    categoryCode: 'anxiety', 
    description: '焦虑症和抑郁症的认识与应对',
    icon: 'fas fa-brain',
    sortOrder: 2,
    status: 1,
    articleCount: 12
  },
  { 
    id: 3, 
    parentId: null, 
    categoryName: '工作压力', 
    categoryCode: 'work_stress', 
    description: '职场压力管理与工作生活平衡',
    icon: 'fas fa-briefcase',
    sortOrder: 3,
    status: 1,
    articleCount: 8
  },
  { 
    id: 4, 
    parentId: null, 
    categoryName: '人际关系', 
    categoryCode: 'relationships', 
    description: '人际沟通与关系维护技巧',
    icon: 'fas fa-users',
    sortOrder: 4,
    status: 1,
    articleCount: 10
  },
  { 
    id: 5, 
    parentId: null, 
    categoryName: '睡眠健康', 
    categoryCode: 'sleep', 
    description: '睡眠质量改善与睡眠障碍应对',
    icon: 'fas fa-bed',
    sortOrder: 5,
    status: 1,
    articleCount: 6
  },
  { 
    id: 6, 
    parentId: null, 
    categoryName: '放松技巧', 
    categoryCode: 'relaxation', 
    description: '冥想、呼吸法等放松训练技巧',
    icon: 'fas fa-leaf',
    sortOrder: 6,
    status: 1,
    articleCount: 9
  }
]

// Mock文章数据
const mockArticles = [
  {
    id: '550e8400-e29b-41d4-a716-446655440001',
    categoryId: 1,
    categoryName: '情绪管理',
    title: '如何有效管理负面情绪：科学方法与实用技巧',
    summary: '负面情绪是人类心理体验的重要组成部分，学会科学地识别、理解和管理这些情绪，对维护心理健康具有重要意义。本文将介绍几种被科学验证的情绪管理方法。',
    content: `<h2>什么是负面情绪？</h2>
<p>负面情绪包括愤怒、恐惧、焦虑、悲伤等，它们是正常的心理反应，具有重要的适应性功能。</p>
<h2>情绪管理的科学方法</h2>
<h3>1. 认知重构技术</h3>
<p>通过改变对事件的认知和解释来调节情绪反应。当遇到困难时，尝试从多个角度看问题。</p>
<h3>2. 正念冥想</h3>
<p>通过专注当下的感受和体验，减少对负面情绪的反刍和放大。</p>
<h3>3. 行为激活</h3>
<p>通过参与积极的活动来改善情绪状态，如运动、社交、兴趣爱好等。</p>
<h2>实用技巧</h2>
<ul>
<li>深呼吸练习：4-7-8呼吸法</li>
<li>情绪日记：记录情绪变化和触发因素</li>
<li>渐进式肌肉放松</li>
<li>积极自我对话</li>
</ul>`,
    coverImage: 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '情绪管理,心理健康,压力应对',
    authorId: 1,
    authorName: '张心理医师',
    readCount: 2580,
    favoriteCount: 128,
    status: 1,
    publishedAt: '2024-01-15T10:00:00',
    createdAt: '2024-01-15T10:00:00',
    updatedAt: '2024-01-15T10:00:00'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440002',
    categoryId: 6,
    categoryName: '放松技巧',
    title: '正念冥想入门指南：从零开始的内心平静之旅',
    summary: '正念冥想是一种古老而有效的心理训练方法，现代科学研究证实了它在减压、提高专注力、改善情绪调节等方面的显著效果。本指南将带您了解正念冥想的基本原理和实践方法。',
    content: `<h2>什么是正念冥想？</h2>
<p>正念冥想是一种专注于当下时刻的冥想练习，不带评判地观察自己的思想、感受和身体感觉。</p>
<h2>正念冥想的益处</h2>
<ul>
<li>减少压力和焦虑</li>
<li>提高注意力和专注力</li>
<li>改善情绪调节能力</li>
<li>增强自我觉察</li>
<li>改善睡眠质量</li>
</ul>
<h2>基础练习方法</h2>
<h3>1. 呼吸觉察练习</h3>
<p>找一个安静的地方坐下，闭上眼睛，专注于自然的呼吸。当思维游走时，轻柔地将注意力带回呼吸。</p>
<h3>2. 身体扫描</h3>
<p>从头到脚系统地觉察身体各个部位的感觉，不试图改变什么，只是觉察。</p>
<h3>3. 行走冥想</h3>
<p>缓慢而有意识地行走，专注于脚步和身体的感觉。</p>`,
    coverImage: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '正念,冥想,放松,压力管理',
    authorId: 2,
    authorName: '李正念导师',
    readCount: 3210,
    favoriteCount: 205,
    status: 1,
    publishedAt: '2024-01-12T14:30:00',
    createdAt: '2024-01-12T14:30:00',
    updatedAt: '2024-01-12T14:30:00'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440003',
    categoryId: 5,
    categoryName: '睡眠健康',
    title: '改善睡眠质量的10个科学方法',
    summary: '良好的睡眠对心理健康至关重要。失眠和睡眠障碍会显著影响情绪调节、认知功能和整体心理状态。本文将分享10个经过科学验证的改善睡眠质量的方法。',
    content: `<h2>睡眠与心理健康的关系</h2>
<p>睡眠不足会导致情绪波动、注意力不集中、决策能力下降等问题。</p>
<h2>10个改善睡眠的科学方法</h2>
<h3>1. 建立规律的睡眠时间表</h3>
<p>每天在相同时间睡觉和起床，包括周末。</p>
<h3>2. 创造理想的睡眠环境</h3>
<p>保持卧室凉爽、黑暗和安静。</p>
<h3>3. 限制蓝光暴露</h3>
<p>睡前1-2小时避免使用电子设备。</p>
<h3>4. 避免刺激性物质</h3>
<p>下午避免咖啡因，睡前不饮酒。</p>
<h3>5. 规律运动</h3>
<p>但避免睡前4小时内剧烈运动。</p>`,
    coverImage: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '睡眠健康,失眠,睡眠卫生',
    authorId: 3,
    authorName: '王睡眠专家',
    readCount: 1820,
    favoriteCount: 95,
    status: 1,
    publishedAt: '2024-01-10T09:00:00',
    createdAt: '2024-01-10T09:00:00',
    updatedAt: '2024-01-10T09:00:00'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440004',
    categoryId: 3,
    categoryName: '工作压力',
    title: '职场压力管理：维护工作与生活平衡的智慧',
    summary: '现代职场环境中，工作压力已成为影响心理健康的重要因素。学会有效管理职场压力，建立健康的工作与生活边界，对维护心理健康和提升生活质量具有重要意义。',
    content: `<h2>职场压力的来源</h2>
<p>工作负荷过重、人际关系复杂、职业发展不明确等都可能成为压力源。</p>
<h2>压力管理策略</h2>
<h3>1. 时间管理</h3>
<p>使用优先级矩阵，合理安排工作任务。</p>
<h3>2. 边界设定</h3>
<p>明确工作和生活的边界，避免过度加班。</p>
<h3>3. 沟通技巧</h3>
<p>学会有效沟通，减少误解和冲突。</p>
<h3>4. 放松练习</h3>
<p>在工作间隙进行简短的放松练习。</p>`,
    coverImage: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '工作压力,职场健康,工作生活平衡',
    authorId: 4,
    authorName: '刘职场心理师',
    readCount: 4120,
    favoriteCount: 312,
    status: 1,
    publishedAt: '2024-01-08T16:45:00',
    createdAt: '2024-01-08T16:45:00',
    updatedAt: '2024-01-08T16:45:00'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440005',
    categoryId: 2,
    categoryName: '焦虑抑郁',
    title: '识别和应对焦虑症状的实用指南',
    summary: '焦虑是一种常见的心理状态，适度的焦虑有助于我们应对挑战，但过度焦虑可能影响日常生活。了解焦虑的表现和应对方法，有助于更好地管理这种情绪。',
    content: `<h2>焦虑症状的识别</h2>
<p>包括身体症状（心跳加速、出汗、肌肉紧张）和心理症状（担忧、恐惧、注意力难以集中）。</p>
<h2>应对焦虑的方法</h2>
<h3>1. 深呼吸技巧</h3>
<p>4-7-8呼吸法：吸气4秒，屏息7秒，呼气8秒。</p>
<h3>2. 渐进式肌肉放松</h3>
<p>系统地紧张和放松身体各个肌肉群。</p>
<h3>3. 认知重构</h3>
<p>识别和挑战不合理的担忧想法。</p>
<h3>4. 分级暴露</h3>
<p>逐步面对引起焦虑的情境。</p>`,
    coverImage: 'https://images.unsplash.com/photo-1544027993-37dbfe43562a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '焦虑症,心理健康,情绪调节',
    authorId: 1,
    authorName: '张心理医师',
    readCount: 2890,
    favoriteCount: 187,
    status: 1,
    publishedAt: '2024-01-06T11:20:00',
    createdAt: '2024-01-06T11:20:00',
    updatedAt: '2024-01-06T11:20:00'
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440006',
    categoryId: 4,
    categoryName: '人际关系',
    title: '建立健康人际关系的心理学原理',
    summary: '良好的人际关系是心理健康的重要支柱。了解人际交往的心理学原理，掌握有效的沟通技巧，有助于建立和维护健康、积极的人际关系。',
    content: `<h2>人际关系的重要性</h2>
<p>良好的人际关系能提供情感支持，减少孤独感，增强自尊心和归属感。</p>
<h2>建立健康关系的原则</h2>
<h3>1. 真诚与诚实</h3>
<p>真实表达自己，建立信任基础。</p>
<h3>2. 倾听与理解</h3>
<p>积极倾听，理解他人的感受和需求。</p>
<h3>3. 尊重与包容</h3>
<p>尊重个体差异，包容不同观点。</p>
<h3>4. 边界与界限</h3>
<p>设定健康的人际边界，保护自己的权益。</p>`,
    coverImage: 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    tags: '人际关系,沟通技巧,社交心理',
    authorId: 5,
    authorName: '陈人际专家',
    readCount: 1654,
    favoriteCount: 98,
    status: 1,
    publishedAt: '2024-01-04T13:15:00',
    createdAt: '2024-01-04T13:15:00',
    updatedAt: '2024-01-04T13:15:00'
  }
]

// Mock用户收藏数据
const mockUserFavorites = [
  { userId: 1, articleId: 1 },
  { userId: 1, articleId: 2 },
  { userId: 2, articleId: 1 },
  { userId: 2, articleId: 3 }
]

// 注册分类相关Mock API
registerMockApi('GET', '/knowledge/category/tree', () => {
  return createSuccessResponse(mockCategories, '获取分类树成功')
})

registerMockApi('GET', '/knowledge/category/page', (params) => {
  const { parentId, categoryName, status, currentPage = 1, size = 10 } = params
  
  let filteredCategories = mockCategories
  
  if (parentId !== undefined && parentId !== null) {
    filteredCategories = filteredCategories.filter(cat => cat.parentId === parentId)
  }
  
  if (categoryName) {
    filteredCategories = filteredCategories.filter(cat => 
      cat.categoryName.includes(categoryName)
    )
  }
  
  if (status !== undefined && status !== null) {
    filteredCategories = filteredCategories.filter(cat => cat.status === parseInt(status))
  }
  
  const total = filteredCategories.length
  const start = (currentPage - 1) * size
  const end = start + size
  const records = filteredCategories.slice(start, end)
  
  return createSuccessResponse({
    records,
    total,
    current: currentPage,
    size
  }, '查询成功')
})

registerMockApi('GET', '/knowledge/category/:id', (params, pathParams) => {
  const categoryId = parseInt(pathParams.id)
  const category = mockCategories.find(c => c.id === categoryId)
  
  if (!category) {
    return createErrorResponse('分类不存在', '404')
  }
  
  return createSuccessResponse(category, '获取分类详情成功')
})

// 注册文章相关Mock API
registerMockApi('GET', '/knowledge/article/page', (params) => {
  const { 
    keyword, 
    title, 
    categoryId, 
    status = 1, 
    sortField = 'publishedAt', 
    sortDirection = 'desc',
    currentPage = 1, 
    size = 12 
  } = params
  
  let filteredArticles = mockArticles.filter(article => article.status === parseInt(status))
  
  if (keyword) {
    filteredArticles = filteredArticles.filter(article => 
      article.title.includes(keyword) || 
      article.summary.includes(keyword) ||
      article.content.includes(keyword) ||
      (article.tags && article.tags.includes(keyword))
    )
  }
  
  if (title) {
    filteredArticles = filteredArticles.filter(article => 
      article.title.includes(title)
    )
  }
  
  if (categoryId) {
    filteredArticles = filteredArticles.filter(article => 
      article.categoryId === parseInt(categoryId)
    )
  }
  
  // 排序
  filteredArticles.sort((a, b) => {
    let aValue = a[sortField]
    let bValue = b[sortField]
    
    if (sortField === 'publishedAt' || sortField === 'createdAt') {
      aValue = new Date(aValue).getTime()
      bValue = new Date(bValue).getTime()
    }
    
    if (sortDirection === 'desc') {
      return bValue - aValue
    } else {
      return aValue - bValue
    }
  })
  
  const total = filteredArticles.length
  const start = (currentPage - 1) * size
  const end = start + size
  const records = filteredArticles.slice(start, end)
  
  return createSuccessResponse({
    records,
    total,
    current: currentPage,
    size
  }, '查询成功')
})

registerMockApi('GET', '/knowledge/article/:id', (params, pathParams) => {
  const articleId = pathParams.id
  const article = mockArticles.find(a => a.id === articleId)
  
  if (!article) {
    return createErrorResponse('文章不存在', '404')
  }
  
  // 模拟增加阅读次数
  article.readCount += 1
  
  return createSuccessResponse(article, '获取文章详情成功')
})

// 注册收藏相关Mock API
registerMockApi('POST', '/knowledge/favorite/:articleId', (params, pathParams) => {
  const articleId = pathParams.articleId
  const userId = 1 // 模拟当前登录用户ID
  
  // 检查文章是否存在
  const article = mockArticles.find(a => a.id === articleId)
  if (!article) {
    return createErrorResponse('文章不存在', '404')
  }
  
  // 检查是否已收藏
  const existingFavorite = mockUserFavorites.find(f => 
    f.userId === userId && f.articleId === articleId
  )
  
  if (existingFavorite) {
    return createErrorResponse('已收藏该文章', '400')
  }
  
  // 添加收藏
  mockUserFavorites.push({ userId, articleId })
  article.favoriteCount = (article.favoriteCount || 0) + 1
  
  return createSuccessResponse(null, '收藏成功')
})

registerMockApi('DELETE', '/knowledge/favorite/:articleId', (params, pathParams) => {
  const articleId = pathParams.articleId
  const userId = 1 // 模拟当前登录用户ID
  
  const favoriteIndex = mockUserFavorites.findIndex(f => 
    f.userId === userId && f.articleId === articleId
  )
  
  if (favoriteIndex === -1) {
    return createErrorResponse('未收藏该文章', '400')
  }
  
  // 移除收藏
  mockUserFavorites.splice(favoriteIndex, 1)
  
  // 更新文章收藏数
  const article = mockArticles.find(a => a.id === articleId)
  if (article) {
    article.favoriteCount = Math.max(0, (article.favoriteCount || 0) - 1)
  }
  
  return createSuccessResponse(null, '取消收藏成功')
})

registerMockApi('GET', '/knowledge/favorite/:articleId/status', (params, pathParams) => {
  const articleId = pathParams.articleId
  const userId = 1 // 模拟当前登录用户ID
  
  const isFavorited = mockUserFavorites.some(f => 
    f.userId === userId && f.articleId === articleId
  )
  
  return createSuccessResponse(isFavorited, '获取收藏状态成功')
})

registerMockApi('GET', '/knowledge/favorite/page', (params) => {
  const { title, categoryId, currentPage = 1, size = 10 } = params
  const userId = 1 // 模拟当前登录用户ID
  
  // 获取用户收藏的文章ID列表
  const favoriteArticleIds = mockUserFavorites
    .filter(f => f.userId === userId)
    .map(f => f.articleId)
  
  // 获取收藏的文章详情
  let favoriteArticles = mockArticles.filter(article => 
    favoriteArticleIds.includes(article.id)
  )
  
  // 过滤
  if (title) {
    favoriteArticles = favoriteArticles.filter(article => 
      article.title.includes(title)
    )
  }
  
  if (categoryId) {
    favoriteArticles = favoriteArticles.filter(article => 
      article.categoryId === parseInt(categoryId)
    )
  }
  
  const total = favoriteArticles.length
  const start = (currentPage - 1) * size
  const end = start + size
  const records = favoriteArticles.slice(start, end)
  
  return createSuccessResponse({
    records,
    total,
    current: currentPage,
    size
  }, '查询成功')
})

registerMockApi('GET', '/knowledge/favorite/count', () => {
  const userId = 1 // 模拟当前登录用户ID
  const count = mockUserFavorites.filter(f => f.userId === userId).length
  
  return createSuccessResponse(count, '获取收藏数量成功')
})

// 注册后台管理相关Mock API
registerMockApi('PUT', '/knowledge/article/:id/status', (params, pathParams) => {
  const articleId = pathParams.id
  const { status } = params
  
  const article = mockArticles.find(a => a.id === articleId)
  if (!article) {
    return createErrorResponse('文章不存在', '404')
  }
  
  // 更新文章状态
  article.status = status
  if (status === 1 && !article.publishedAt) {
    article.publishedAt = new Date().toISOString()
  }
  
  return createSuccessResponse(article, '文章状态更新成功')
})

registerMockApi('DELETE', '/knowledge/article/batch', (params) => {
  const { ids } = params
  
  if (!ids || !Array.isArray(ids)) {
    return createErrorResponse('请提供要删除的文章ID列表', '400')
  }
  
  // 模拟批量删除
  let deletedCount = 0
  ids.forEach(id => {
    const index = mockArticles.findIndex(a => a.id === parseInt(id))
    if (index !== -1) {
      mockArticles.splice(index, 1)
      deletedCount++
    }
  })
  
  return createSuccessResponse({ deletedCount }, `成功删除${deletedCount}篇文章`)
})

registerMockApi('GET', '/knowledge/article/statistics', () => {
  const totalArticles = mockArticles.length
  const publishedArticles = mockArticles.filter(a => a.status === 1).length
  const draftArticles = mockArticles.filter(a => a.status === 0).length
  const offlineArticles = mockArticles.filter(a => a.status === 2).length
  const totalViews = mockArticles.reduce((sum, a) => sum + (a.readCount || 0), 0)
  
  return createSuccessResponse({
    totalArticles,
    publishedArticles,
    draftArticles,
    offlineArticles,
    totalViews,
    totalFavorites: mockUserFavorites.length
  }, '获取统计信息成功')
})
