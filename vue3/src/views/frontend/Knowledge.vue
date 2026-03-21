<template>
  <div class="knowledge-page">
    <!-- 页面头部 -->
    <div class="header-section">
      <div class="header-content">
        <div class="header-left">
          <div class="breathing-animation">
            <i class="fas fa-book-open"></i>
          </div>
          <div class="header-text">
            <h2>心理健康知识库</h2>
          </div>
        </div>
        <div class="header-right">
          <!-- 搜索框 -->
          <div class="search-container">
            <div class="search-wrapper">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索心理健康知识..."
                size="large"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
                @focus="showSearchHistory = true"
                @blur="setTimeout(() => showSearchHistory = false, 200)"
                class="search-input"
              >
                <template #suffix>
                  <el-button 
                    type="primary" 
                    @click="handleSearch"
                    :icon="Search"
                    class="search-btn"
                  />
                </template>
              </el-input>
              
              <!-- 搜索历史下拉 -->
              <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
                <div class="history-header">
                  <span>搜索历史</span>
                  <el-button type="text" size="small" @click="clearSearchHistory">清除</el-button>
                </div>
                <div class="history-list">
                  <div 
                    v-for="item in searchHistory" 
                    :key="item"
                    class="history-item"
                    @click="selectHistoryItem(item)"
                  >
                    <i class="fas fa-history"></i>
                    <span class="history-text">{{ item }}</span>
                    <i class="fas fa-times remove-btn" @click.stop="removeHistoryItem(item)"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="knowledge-content">
      <div class="content-container">
        <div class="content-wrapper">
          <!-- 左侧分类导航 -->
          <aside class="sidebar">
            <!-- 知识分类 -->
            <div class="category-section">
              <h3 class="section-title">知识分类</h3>
              <div class="category-list">
                <div 
                  v-for="category in categories"
                  :key="category.id"
                  class="category-item"
                  :class="{ active: selectedCategoryId === category.id }"
                  @click="selectCategory(category.id)"
                >
                  <i :class="category.icon"></i>
                  <span>{{ category.categoryName }}</span>
                  <span class="count">({{ category.articleCount || 0 }})</span>
                </div>
              </div>
            </div>

            <!-- 推荐文章 -->
            <div class="recommend-section">
              <h3 class="section-title">
                <i class="fas fa-star"></i>
                推荐阅读
              </h3>
              <div class="recommend-list">
                <div 
                  v-for="article in recommendArticles"
                  :key="article.id"
                  class="recommend-item"
                  @click="goToArticle(article.id)"
                >
                  <h4>{{ article.title }}</h4>
                  <p class="read-count">
                    <i class="fas fa-eye"></i>
                    阅读量：{{ formatReadCount(article.readCount) }}
                  </p>
                </div>
              </div>
            </div>

            <!-- 每日提示 -->
            <div class="daily-tip">
              <h3 class="section-title">
                <i class="fas fa-lightbulb"></i>
                每日心理小贴士
              </h3>
              <p class="tip-content">
                {{ dailyTip }}
              </p>
            </div>
          </aside>

          <!-- 主内容区域 -->
          <main class="main-content">
            <!-- 排序和筛选 -->
            <div class="filter-bar">
              <div class="filter-left">
                <span class="filter-label">排序方式：</span>
                <el-select 
                  v-model="searchForm.sortField" 
                  @change="handleSearch"
                  placeholder="选择排序方式"
                >
                  <el-option label="最新发布" value="publishedAt" />
                  <el-option label="最多阅读" value="readCount" />
                  <el-option label="相关度" value="relevance" />
                </el-select>
              </div>
              <div class="filter-right">
                <span class="article-count">
                  共找到 <strong>{{ total }}</strong> 篇文章
                </span>
              </div>
            </div>

            <!-- 文章列表 -->
            <div class="article-list" v-loading="loading">
              <ArticleCard
                v-for="article in articles"
                :key="article.id"
                :article="article"
                @favorite-changed="handleFavoriteChanged"
              />

              <!-- 空状态 -->
              <div v-if="!loading && articles.length === 0" class="empty-state">
                <i class="fas fa-search"></i>
                <p>暂无相关文章</p>
                <p class="empty-tip">尝试调整搜索条件或浏览其他分类</p>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrapper" v-if="total > 0">
              <el-pagination
                :current-page="searchForm.currentPage"
                :page-size="searchForm.size"
                :page-sizes="[6, 12, 18, 24]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                background
              />
            </div>
          </main>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import ArticleCard from '@/components/frontend/ArticleCard.vue'
import { getArticlePage } from '@/api/knowledgeArticle'
import { getCategoryTree } from '@/api/knowledgeCategory'
import { favoriteArticle, unfavoriteArticle, checkFavoriteStatus } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const articles = ref([])
const categories = ref([])
const recommendArticles = ref([])
const total = ref(0)
const selectedCategoryId = ref(null)
const searchHistory = ref([])
const showSearchHistory = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  sortField: 'publishedAt',
  sortDirection: 'desc',
  currentPage: 1,
  size: 12
})

// 默认封面图片
const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

// 每日提示
const dailyTip = ref('深呼吸是最简单有效的放松技巧。当感到压力时，尝试4-7-8呼吸法：吸气4秒，屏息7秒，呼气8秒。')

// 计算属性
const isLoggedIn = computed(() => {
  // 这里需要根据实际的用户状态管理来判断
  return localStorage.getItem('token')
})

// 方法
const fetchArticles = () => {
  loading.value = true
  const params = {
    ...searchForm,
    categoryId: selectedCategoryId.value
  }
  
  getArticlePage(params, {
    onSuccess: (response) => {
      if (response && response.records) {
        // 处理文章列表并检查收藏状态
        articles.value = response.records.map(article => {
          article.isFavorited = false // 默认未收藏
          return article
        })
        total.value = response.total || 0
        
        // 如果用户已登录，批量检查收藏状态
        if (isLoggedIn.value && articles.value.length > 0) {
          checkArticlesFavoriteStatus()
        }
      } else {
        articles.value = []
        total.value = 0
      }
      loading.value = false
    },
    onError: (error) => {
      console.error('获取文章列表失败:', error)
      ElMessage.error('获取文章列表失败')
      articles.value = []
      total.value = 0
      loading.value = false
    }
  })
}

// 批量检查文章收藏状态
const checkArticlesFavoriteStatus = () => {
  articles.value.forEach(article => {
    checkFavoriteStatus(article.id, {
      onSuccess: (favoriteStatus) => {
        article.isFavorited = favoriteStatus
      },
      onError: () => {
        article.isFavorited = false
      }
    })
  })
}

const fetchCategories = () => {
  getCategoryTree( {
    onSuccess: (response) => {
      if (response && Array.isArray(response)) {
        // 计算所有分类的文章总数
        const totalArticleCount = response.reduce((sum, category) => sum + (category.articleCount || 0), 0)
        
        // 添加全部分类选项
        categories.value = [
          { 
            id: null, 
            categoryName: '全部分类', 
            icon: 'fas fa-th-large',
            articleCount: totalArticleCount
          },
          ...response.map(category => ({
            ...category,
            icon: getCategoryIcon(category.categoryName)
          }))
        ]
      }
    },
    onError: (error) => {
      console.error('获取分类列表失败:', error)
      ElMessage.error('获取分类列表失败')
    }
  })
}

const fetchRecommendArticles = () => {
  getArticlePage({
    sortField: 'readCount',
    sortDirection: 'desc',
    currentPage: 1,
    size: 3
  }, {
    onSuccess: (response) => {
      if (response && response.records) {
        recommendArticles.value = response.records
      }
    },
    onError: (error) => {
      console.error('获取推荐文章失败:', error)
    }
  })
}

const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  searchForm.currentPage = 1
  handleSearch()
}

const handleSearch = () => {
  searchForm.categoryId = selectedCategoryId.value
  
  // 添加搜索历史
  if (searchForm.keyword && searchForm.keyword.trim()) {
    addSearchHistory(searchForm.keyword.trim())
  }
  
  // 隐藏搜索历史
  showSearchHistory.value = false
  
  fetchArticles()
}

// 搜索历史管理
const addSearchHistory = (keyword) => {
  // 移除重复项
  const filteredHistory = searchHistory.value.filter(item => item !== keyword)
  // 添加到开头
  searchHistory.value = [keyword, ...filteredHistory].slice(0, 10) // 最多保存10条
  // 保存到localStorage
  localStorage.setItem('knowledge_search_history', JSON.stringify(searchHistory.value))
}

const loadSearchHistory = () => {
  try {
    const saved = localStorage.getItem('knowledge_search_history')
    if (saved) {
      searchHistory.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
    searchHistory.value = []
  }
}

const clearSearchHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('knowledge_search_history')
  showSearchHistory.value = false
}

const selectHistoryItem = (keyword) => {
  searchForm.keyword = keyword
  handleSearch()
}

const removeHistoryItem = (keyword) => {
  searchHistory.value = searchHistory.value.filter(item => item !== keyword)
  localStorage.setItem('knowledge_search_history', JSON.stringify(searchHistory.value))
}

const goToArticle = (articleId) => {
  router.push(`/knowledge/article/${articleId}`)
}

const handleFavoriteChanged = (article) => {
  // 文章收藏状态变化时的处理逻辑
  console.log('文章收藏状态变化:', article.id, article.isFavorited)
}

const toggleFavorite = (article) => {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('需要登录才能收藏文章，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/auth/login')
    }).catch(() => {})
    return
  }

  if (article.isFavorited) {
    // 取消收藏
    unfavoriteArticle(article.id, {
      successMsg: '已取消收藏',
      onSuccess: () => {
        article.isFavorited = false
        article.favoriteCount = Math.max(0, (article.favoriteCount || 1) - 1)
      },
      onError: (error) => {
        console.error('取消收藏失败:', error)
        ElMessage.error('取消收藏失败')
      }
    })
  } else {
    // 添加收藏
    favoriteArticle(article.id, {
      successMsg: '收藏成功',
      onSuccess: () => {
        article.isFavorited = true
        article.favoriteCount = (article.favoriteCount || 0) + 1
      },
      onError: (error) => {
        console.error('收藏失败:', error)
        ElMessage.error('收藏失败')
      }
    })
  }
}

// 工具方法
const getCategoryIcon = (categoryName) => {
  const iconMap = {
    '情绪管理': 'fas fa-heart',
    '焦虑抑郁': 'fas fa-brain',
    '工作压力': 'fas fa-briefcase',
    '人际关系': 'fas fa-users',
    '睡眠健康': 'fas fa-bed',
    '儿童心理': 'fas fa-child',
    '创伤康复': 'fas fa-heart-broken',
    '放松技巧': 'fas fa-leaf'
  }
  return iconMap[categoryName] || 'fas fa-bookmark'
}

const getCategoryTagType = (categoryName) => {
  const typeMap = {
    '情绪管理': 'warning',
    '焦虑抑郁': 'info',
    '工作压力': 'danger',
    '人际关系': 'success',
    '睡眠健康': 'primary',
    '儿童心理': '',
    '创伤康复': 'warning',
    '放松技巧': 'success'
  }
  return typeMap[categoryName] || ''
}

const getReadTime = (content) => {
  if (!content) return 5
  // 估算阅读时间：假设每分钟300个字符
  const wordCount = content.length
  return Math.max(1, Math.ceil(wordCount / 300))
}

const getAutoSummary = (content) => {
  if (!content) return ''
  // 移除HTML标签，取前150个字符作为摘要
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 150 ? plainText.substring(0, 150) + '...' : plainText
}

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

const handleImageError = (event) => {
  event.target.src = defaultCover
}

const handleSizeChange = (size) => {
  searchForm.size = size
  searchForm.currentPage = 1
  fetchArticles()
}

const handleCurrentChange = (page) => {
  searchForm.currentPage = page
  fetchArticles()
}

// 生命周期
onMounted(() => {
  loadSearchHistory()
  fetchCategories()
  fetchArticles()
  fetchRecommendArticles()
})
</script>

<style scoped>
.knowledge-page {
  min-height: 100vh;
  background: #f8fafc;
}

/* 页面头部样式 */
.header-section {
  background: linear-gradient(135deg, #f59e0b 0%, #8b5cf6 100%);
  color: white;
  padding: 3rem 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.breathing-animation {
  font-size: 4rem;
  animation: breathing 4s ease-in-out infinite;
}

@keyframes breathing {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.header-text h2 {
  font-size: 2.5rem;
  margin: 0;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-container {
  min-width: 400px;
}

.search-wrapper {
  position: relative;
}

.search-input {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  transition: all 0.3s ease;
}

.search-input:hover {
  background: rgba(255, 255, 255, 0.15);
}

.search-input :deep(.el-input__wrapper) {
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 1rem;
  padding: 0.5rem 1rem;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper):hover,
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(255, 255, 255, 0.6);
}

.search-input :deep(.el-input__inner) {
  color: white;
  font-size: 1rem;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.7);
}

.search-btn {
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 0.75rem;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

/* 搜索历史样式 */
.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  margin-top: 0.5rem;
  overflow: hidden;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.875rem;
  font-weight: 500;
  color: #64748b;
}

.history-list {
  max-height: 200px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  gap: 0.75rem;
}

.history-item:hover {
  background: #f1f5f9;
}

.history-item i.fa-history {
  color: #94a3b8;
  font-size: 0.875rem;
  width: 14px;
}

.history-text {
  flex: 1;
  color: #374151;
  font-size: 0.875rem;
}

.remove-btn {
  color: #94a3b8;
  font-size: 0.75rem;
  opacity: 0;
  transition: opacity 0.2s ease;
  padding: 0.25rem;
}

.history-item:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  color: #ef4444;
}

/* 内容区域样式 */
.knowledge-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
}

/* 侧边栏样式 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.category-section,
.recommend-section,
.daily-tip {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6b7280;
  font-weight: 500;
}

.category-item:hover {
  background: #f3f4f6;
  color: #374151;
}

.category-item.active {
  background: linear-gradient(135deg, #f59e0b, #8b5cf6);
  color: white;
  transform: translateY(-2px);
}

.category-item i {
  width: 20px;
  margin-right: 0.5rem;
}

.category-item .count {
  margin-left: auto;
  font-size: 0.875rem;
  opacity: 0.7;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.recommend-item {
  border-left: 4px solid #f59e0b;
  padding-left: 1rem;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.recommend-item:hover {
  transform: translateX(4px);
}

.recommend-item h4 {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
  line-height: 1.4;
  margin-bottom: 0.25rem;
}

.recommend-item .read-count {
  font-size: 0.75rem;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.daily-tip .tip-content {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.5;
}

/* 主内容区域样式 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-bar {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: nowrap;
  min-width: 0;
  flex: 1;
}

.filter-label {
  color: #374151;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-left .el-select {
  min-width: 140px;
}

.filter-right {
  flex-shrink: 0;
  min-width: fit-content;
}

.filter-right .article-count {
  color: #6b7280;
  white-space: nowrap;
}

.filter-right strong {
  color: #f59e0b;
}

/* 文章列表样式 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: #6b7280;
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
}

.empty-tip {
  font-size: 0.875rem;
  opacity: 0.8;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .sidebar {
    order: -1;
  }
  
  .category-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 0.5rem;
  }
}

@media (max-width: 900px) {
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .filter-left {
    width: 100%;
    justify-content: flex-start;
  }
  
  .filter-right {
    width: 100%;
    text-align: left;
  }
}

@media (max-width: 768px) {
  .header-section {
    padding: 2rem 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
  }
  
  .header-left {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-text h2 {
    font-size: 2rem;
  }
  
  .search-container {
    min-width: 300px;
    width: 100%;
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .filter-left {
    width: 100%;
    justify-content: flex-start;
  }
  
  .filter-right {
    width: 100%;
  }
  
  .content-container {
    padding: 0 1rem;
  }
}
</style>