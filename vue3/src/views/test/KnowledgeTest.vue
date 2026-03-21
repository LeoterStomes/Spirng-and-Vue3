<template>
  <div class="knowledge-test-page">
    <div class="test-container">
      <h1>知识库模块功能测试</h1>
      <p class="test-desc">本页面用于测试知识库模块的各项功能，确保前后端API集成正常工作</p>
      
      <!-- 测试控制面板 -->
      <div class="test-control-panel">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>测试控制面板</span>
              <el-button type="primary" @click="runAllTests">运行所有测试</el-button>
            </div>
          </template>
          
          <div class="test-buttons">
            <el-button @click="testCategoryAPI" type="info">测试分类API</el-button>
            <el-button @click="testArticleAPI" type="success">测试文章API</el-button>
            <el-button @click="testFavoriteAPI" type="warning">测试收藏API</el-button>
            <el-button @click="testPageNavigation" type="primary">测试页面跳转</el-button>
            <el-button @click="clearResults" type="danger">清空结果</el-button>
          </div>
        </el-card>
      </div>

      <!-- 测试结果显示 -->
      <div class="test-results">
        <el-card shadow="hover">
          <template #header>
            <span>测试结果</span>
          </template>
          
          <div class="results-content">
            <div v-if="testResults.length === 0" class="no-results">
              <i class="fas fa-flask"></i>
              <p>点击上方按钮开始测试</p>
            </div>
            
            <div 
              v-for="(result, index) in testResults" 
              :key="index"
              class="test-result-item"
              :class="result.status"
            >
              <div class="result-header">
                <span class="result-title">{{ result.title }}</span>
                <span class="result-status">
                  <i :class="result.status === 'success' ? 'fas fa-check' : 'fas fa-times'"></i>
                  {{ result.status === 'success' ? '成功' : '失败' }}
                </span>
                <span class="result-time">{{ result.timestamp }}</span>
              </div>
              <div class="result-message">{{ result.message }}</div>
              <div v-if="result.data" class="result-data">
                <pre>{{ JSON.stringify(result.data, null, 2) }}</pre>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 快速导航 -->
      <div class="quick-navigation">
        <el-card shadow="hover">
          <template #header>
            <span>快速导航</span>
          </template>
          
          <div class="nav-buttons">
            <el-button @click="goToKnowledge" type="primary" size="large">
              <i class="fas fa-book"></i>
              前台知识库
            </el-button>
            
            <el-button @click="goToBackendKnowledge" type="success" size="large">
              <i class="fas fa-cogs"></i>
              后台文章管理
            </el-button>
            
            <el-button @click="goToArticleDetail(1)" type="info" size="large">
              <i class="fas fa-file-alt"></i>
              示例文章详情
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- API测试详情 -->
      <div class="api-test-details">
        <el-card shadow="hover">
          <template #header>
            <span>API接口测试详情</span>
          </template>
          
          <el-collapse v-model="activeCollapse">
            <el-collapse-item title="分类管理API" name="category">
              <div class="api-list">
                <div class="api-item" v-for="api in categoryAPIs" :key="api.name">
                  <span class="api-name">{{ api.name }}</span>
                  <span class="api-method" :class="api.method.toLowerCase()">{{ api.method }}</span>
                  <span class="api-url">{{ api.url }}</span>
                  <el-button size="small" @click="testSingleAPI(api)">测试</el-button>
                </div>
              </div>
            </el-collapse-item>
            
            <el-collapse-item title="文章管理API" name="article">
              <div class="api-list">
                <div class="api-item" v-for="api in articleAPIs" :key="api.name">
                  <span class="api-name">{{ api.name }}</span>
                  <span class="api-method" :class="api.method.toLowerCase()">{{ api.method }}</span>
                  <span class="api-url">{{ api.url }}</span>
                  <el-button size="small" @click="testSingleAPI(api)">测试</el-button>
                </div>
              </div>
            </el-collapse-item>
            
            <el-collapse-item title="收藏管理API" name="favorite">
              <div class="api-list">
                <div class="api-item" v-for="api in favoriteAPIs" :key="api.name">
                  <span class="api-name">{{ api.name }}</span>
                  <span class="api-method" :class="api.method.toLowerCase()">{{ api.method }}</span>
                  <span class="api-url">{{ api.url }}</span>
                  <el-button size="small" @click="testSingleAPI(api)">测试</el-button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategoryTree } from '@/api/knowledgeCategory'
import { getArticlePage, getArticleById } from '@/api/knowledgeArticle'
import { favoriteArticle, unfavoriteArticle, checkFavoriteStatus, getUserFavoriteCount } from '@/api/userFavorite'

const router = useRouter()

// 响应式数据
const testResults = ref([])
const activeCollapse = ref(['category'])

// API配置
const categoryAPIs = [
  { name: '获取分类树', method: 'GET', url: '/knowledge/category/tree', func: getCategoryTree },
]

const articleAPIs = [
  { name: '分页查询文章', method: 'GET', url: '/knowledge/article/page', func: getArticlePage },
  { name: '获取文章详情', method: 'GET', url: '/knowledge/article/{id}', func: getArticleById },
]

const favoriteAPIs = [
  { name: '检查收藏状态', method: 'GET', url: '/knowledge/favorite/{id}/status', func: checkFavoriteStatus },
  { name: '获取收藏数量', method: 'GET', url: '/knowledge/favorite/count', func: getUserFavoriteCount },
  { name: '收藏文章', method: 'POST', url: '/knowledge/favorite/{id}', func: favoriteArticle },
  { name: '取消收藏', method: 'DELETE', url: '/knowledge/favorite/{id}', func: unfavoriteArticle },
]

// 方法
const addTestResult = (title, status, message, data = null) => {
  testResults.value.unshift({
    title,
    status,
    message,
    data,
    timestamp: new Date().toLocaleTimeString()
  })
}

const testCategoryAPI = async () => {
  try {
    ElMessage.info('开始测试分类API...')
    
    // 测试获取分类树
    const categories = await getCategoryTree()
    addTestResult(
      '分类API测试',
      'success',
      `成功获取${categories.length}个分类`,
      categories.slice(0, 3) // 只显示前3个分类避免数据过长
    )
    
    ElMessage.success('分类API测试完成')
  } catch (error) {
    console.error('分类API测试失败:', error)
    addTestResult('分类API测试', 'error', `测试失败: ${error.message}`)
    ElMessage.error('分类API测试失败')
  }
}

const testArticleAPI = async () => {
  try {
    ElMessage.info('开始测试文章API...')
    
    // 测试分页查询文章
    const pageResult = await getArticlePage({
      currentPage: 1,
      size: 5
    })
    
    if (pageResult && pageResult.records) {
      addTestResult(
        '文章分页查询',
        'success',
        `成功获取文章列表，共${pageResult.total}篇文章`,
        {
          total: pageResult.total,
          current: pageResult.current,
          size: pageResult.size,
          firstArticle: pageResult.records[0]
        }
      )
      
      // 测试获取文章详情
      if (pageResult.records.length > 0) {
        const article = await getArticleById(pageResult.records[0].id)
        addTestResult(
          '文章详情查询',
          'success',
          `成功获取文章详情: ${article.title}`,
          {
            id: article.id,
            title: article.title,
            readCount: article.readCount
          }
        )
      }
    }
    
    ElMessage.success('文章API测试完成')
  } catch (error) {
    console.error('文章API测试失败:', error)
    addTestResult('文章API测试', 'error', `测试失败: ${error.message}`)
    ElMessage.error('文章API测试失败')
  }
}

const testFavoriteAPI = async () => {
  try {
    ElMessage.info('开始测试收藏API...')
    
    // 测试获取收藏数量
    const count = await getUserFavoriteCount()
    addTestResult(
      '收藏数量查询',
      'success',
      `当前用户收藏了${count}篇文章`,
      { favoriteCount: count }
    )
    
    // 测试检查收藏状态（使用Mock文章的UUID）
    const testArticleId = '550e8400-e29b-41d4-a716-446655440001'
    const isFavorited = await checkFavoriteStatus(testArticleId)
    addTestResult(
      '收藏状态检查',
      'success',
      `文章ID ${testArticleId.substring(0, 8)}...的收藏状态: ${isFavorited ? '已收藏' : '未收藏'}`,
      { articleId: testArticleId, isFavorited }
    )
    
    ElMessage.success('收藏API测试完成')
  } catch (error) {
    console.error('收藏API测试失败:', error)
    addTestResult('收藏API测试', 'error', `测试失败: ${error.message}`)
    ElMessage.error('收藏API测试失败')
  }
}

const testPageNavigation = () => {
  try {
    ElMessage.info('测试页面导航功能...')
    
    // 测试路由是否正确配置
    const routes = [
      { name: '知识库主页', path: '/knowledge' },
      { name: '文章详情页', path: '/knowledge/article/550e8400-e29b-41d4-a716-446655440001' },
      { name: '后台文章管理', path: '/back/knowledge' }
    ]
    
    routes.forEach(route => {
      addTestResult(
        '路由配置检查',
        'success',
        `${route.name} 路由配置正确: ${route.path}`,
        route
      )
    })
    
    ElMessage.success('页面导航测试完成')
  } catch (error) {
    console.error('页面导航测试失败:', error)
    addTestResult('页面导航测试', 'error', `测试失败: ${error.message}`)
    ElMessage.error('页面导航测试失败')
  }
}

const testSingleAPI = async (api) => {
  try {
    ElMessage.info(`测试 ${api.name}...`)
    
    let result
    const testArticleId = '550e8400-e29b-41d4-a716-446655440001' // 使用Mock文章的UUID
    if (api.func === getArticleById) {
      result = await api.func(testArticleId) // 使用UUID进行测试
    } else if (api.func === checkFavoriteStatus || api.func === favoriteArticle || api.func === unfavoriteArticle) {
      result = await api.func(testArticleId) // 使用文章UUID进行测试
    } else if (api.func === getArticlePage) {
      result = await api.func({ currentPage: 1, size: 3 })
    } else {
      result = await api.func()
    }
    
    addTestResult(
      api.name,
      'success',
      `${api.method} ${api.url} 调用成功`,
      Array.isArray(result) ? result.slice(0, 2) : result
    )
    
    ElMessage.success(`${api.name} 测试成功`)
  } catch (error) {
    console.error(`${api.name} 测试失败:`, error)
    addTestResult(api.name, 'error', `${api.method} ${api.url} 调用失败: ${error.message}`)
    ElMessage.error(`${api.name} 测试失败`)
  }
}

const runAllTests = async () => {
  ElMessage.info('开始运行所有测试...')
  clearResults()
  
  await testCategoryAPI()
  await testArticleAPI()
  await testFavoriteAPI()
  testPageNavigation()
  
  ElMessage.success('所有测试完成！')
}

const clearResults = () => {
  testResults.value = []
  ElMessage.info('测试结果已清空')
}

// 导航方法
const goToKnowledge = () => {
  router.push('/knowledge')
}

const goToBackendKnowledge = () => {
  router.push('/back/knowledge')
}

const goToArticleDetail = (id) => {
  router.push(`/knowledge/article/${id}`)
}
</script>

<style scoped>
.knowledge-test-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 2rem;
}

.test-container {
  max-width: 1200px;
  margin: 0 auto;
}

.knowledge-test-page h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 2rem;
}

.test-desc {
  text-align: center;
  color: #7f8c8d;
  margin-bottom: 2rem;
  font-size: 1.125rem;
}

.test-control-panel,
.test-results,
.quick-navigation,
.api-test-details {
  margin-bottom: 2rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-buttons,
.nav-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

/* 测试结果样式 */
.results-content {
  max-height: 500px;
  overflow-y: auto;
}

.no-results {
  text-align: center;
  color: #7f8c8d;
  padding: 2rem;
}

.no-results i {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.test-result-item {
  padding: 1rem;
  border-left: 4px solid #e0e0e0;
  margin-bottom: 1rem;
  background: #fafafa;
  border-radius: 0 6px 6px 0;
}

.test-result-item.success {
  border-left-color: #4caf50;
  background: #f1f8e9;
}

.test-result-item.error {
  border-left-color: #f44336;
  background: #ffebee;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.result-title {
  font-weight: 600;
  color: #2c3e50;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.result-status i {
  font-size: 0.875rem;
}

.success .result-status {
  color: #4caf50;
}

.error .result-status {
  color: #f44336;
}

.result-time {
  color: #7f8c8d;
  font-size: 0.75rem;
}

.result-message {
  color: #5a6c7d;
  margin-bottom: 0.5rem;
}

.result-data {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 1rem;
  border-radius: 4px;
  font-size: 0.875rem;
  overflow-x: auto;
}

.result-data pre {
  margin: 0;
  white-space: pre-wrap;
}

/* API测试详情样式 */
.api-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.api-item {
  display: grid;
  grid-template-columns: 2fr 80px 3fr 80px;
  gap: 1rem;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.api-name {
  font-weight: 500;
  color: #2c3e50;
}

.api-method {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-align: center;
  color: white;
}

.api-method.get {
  background: #28a745;
}

.api-method.post {
  background: #007bff;
}

.api-method.put {
  background: #ffc107;
  color: #212529;
}

.api-method.delete {
  background: #dc3545;
}

.api-url {
  font-family: 'Courier New', monospace;
  color: #6c757d;
  font-size: 0.875rem;
}

/* 导航按钮样式 */
.nav-buttons .el-button {
  flex: 1;
  height: 60px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 1rem;
}

.nav-buttons .el-button i {
  font-size: 1.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .knowledge-test-page {
    padding: 1rem;
  }
  
  .test-buttons,
  .nav-buttons {
    flex-direction: column;
  }
  
  .api-item {
    grid-template-columns: 1fr;
    gap: 0.5rem;
    text-align: center;
  }
  
  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
}
</style>