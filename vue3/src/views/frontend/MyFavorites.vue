<template>
  <div class="favorites-page">
    <!-- 页面头部 -->
    <div class="header-section">
      <div class="header-content">
        <div class="header-left">
          <div class="breathing-animation">
            <i class="fas fa-heart"></i>
          </div>
          <div class="header-text">
            <h2>我的收藏</h2>
            <p class="header-subtitle">{{ favoriteCount || 0 }} 篇收藏文章</p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="refreshList" class="refresh-btn">
            <i class="fas fa-sync-alt"></i>
            刷新列表
          </el-button>
          <el-button @click="goToKnowledge" class="knowledge-btn">
            <i class="fas fa-book-open"></i>
            浏览知识库
          </el-button>
        </div>
      </div>
    </div>

    <div class="favorites-content">
      <div class="content-container">
        <!-- 文章列表 -->
        <div class="article-list" v-loading="loading">
          <!-- 文章网格 -->
          <div v-if="articles.length > 0" class="articles-grid">
            <div
              v-for="article in articles"
              :key="article.id"
              class="article-card"
            >
              <div class="article-cover" @click="viewArticle(article.id)">
                <img
                  v-if="article.coverImage"
                  :src="article.coverImage"
                  :alt="article.title"
                  @error="handleImageError"
                />
                <div v-else class="default-cover">
                  <i class="fas fa-file-alt"></i>
                </div>
                <div class="cover-overlay">
                  <i class="fas fa-eye"></i>
                  <span>查看详情</span>
                </div>
              </div>
              
              <div class="article-content">
                <h3 class="article-title" @click="viewArticle(article.id)">
                  {{ article.title }}
                </h3>
                
                <p class="article-summary">
                  {{ article.summary || getAutoSummary(article.content) }}
                </p>
                
                <div class="article-meta">
                  <div class="meta-info">
                    <span class="meta-item">
                      <i class="fas fa-calendar"></i>
                      收藏于 {{ formatDateString(article.favoriteTime) }}
                    </span>
                    <span class="meta-item">
                      <i class="fas fa-eye"></i>
                      {{ formatReadCount(article.readCount) }} 次阅读
                    </span>
                  </div>
                  
                  <div class="article-actions">
                    <el-button
                      type="primary"
                      size="small"
                      @click="viewArticle(article.id)"
                    >
                      <i class="fas fa-eye"></i>
                      查看
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="confirmRemoveFavorite(article)"
                    >
                      <i class="fas fa-heart-broken"></i>
                      取消收藏
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && articles.length === 0" class="empty-state">
            <i class="fas fa-heart-broken"></i>
            <p>暂无收藏文章</p>
            <p class="empty-tip">您还没有收藏任何文章，快去<router-link to="/knowledge">知识库</router-link>收藏感兴趣的内容吧！</p>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            :current-page="searchParams.currentPage"
            :page-size="searchParams.size"
            :page-sizes="[6, 12, 18, 24]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ArticleCard from '@/components/frontend/ArticleCard.vue'
import { getUserFavoritePage, getUserFavoriteCount, unfavoriteArticle } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const articles = ref([])
const total = ref(0)
const favoriteCount = ref(null)

// 查询参数
const searchParams = reactive({
  currentPage: 1,
  size: 8
})

// 格式化日期
const formatDateString = (dateString) => {
  if (!dateString) return ''
  return formatDate(new Date(dateString))
}

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true
  
  try {
    await getUserFavoritePage(searchParams, {
      onSuccess: (res) => {
        articles.value = res.records || []
        total.value = res.total || 0
        searchParams.currentPage = res.current || 1
        searchParams.size = res.size || 8
      }
    })
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 获取收藏总数
const fetchFavoriteCount = async () => {
  try {
    await getUserFavoriteCount({
      onSuccess: (res) => {
        favoriteCount.value = res
      }
    })
  } catch (error) {
    console.error('获取收藏总数失败:', error)
  }
}

// 工具方法
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
  event.target.src = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
}

// 分页处理
const handlePageChange = (page) => {
  searchParams.currentPage = page
  fetchFavorites()
}

const handleSizeChange = (size) => {
  searchParams.size = size
  searchParams.currentPage = 1
  fetchFavorites()
}

// 查看文章详情
const viewArticle = (articleId) => {
  router.push(`/knowledge/article/${articleId}`)
}

// 确认取消收藏
const confirmRemoveFavorite = async (article) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏文章"${article.title}"吗？`,
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await removeFavorite(article.id)
  } catch (error) {
    // 用户取消操作
  }
}

// 取消收藏
const removeFavorite = async (articleId) => {
  try {
    await unfavoriteArticle(articleId, {
      onSuccess: () => {
        ElMessage.success('取消收藏成功')
        fetchFavorites()
        fetchFavoriteCount()
      }
    })
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  }
}

// 处理收藏状态变化
const handleFavoriteChanged = (article) => {
  // 重新获取列表以确保数据同步
  fetchFavorites()
  fetchFavoriteCount()
}

// 跳转到知识库
const goToKnowledge = () => {
  router.push('/knowledge')
}

// 刷新列表
const refreshList = () => {
  fetchFavorites()
  fetchFavoriteCount()
}

// 页面挂载时加载数据
onMounted(() => {
  fetchFavorites()
  fetchFavoriteCount()
})
</script>

<style scoped>
.favorites-page {
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

.header-subtitle {
  font-size: 1.125rem;
  margin: 0.5rem 0 0;
  opacity: 0.9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.knowledge-btn {
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.knowledge-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* 内容区域样式 */
.favorites-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* 文章列表样式 */
.article-list {
  margin-top: 2rem;
}

/* 文章网格 */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}

/* 文章封面 */
.article-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.default-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f59e0b, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 3rem;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-card:hover .cover-overlay {
  opacity: 1;
}

.cover-overlay i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

/* 文章内容 */
.article-content {
  padding: 1.5rem;
}

.article-title {
  margin: 0 0 0.75rem;
  color: #1f2937;
  font-size: 1.125rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s ease;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-title:hover {
  color: #f59e0b;
}

.article-summary {
  margin: 0 0 1rem;
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 文章元信息 */
.article-meta {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.75rem;
}

.meta-item i {
  width: 12px;
  color: #9ca3af;
}

/* 文章操作 */
.article-actions {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
}

.article-actions .el-button {
  flex: 1;
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

.empty-state a {
  color: #f59e0b;
  text-decoration: none;
}

.empty-state a:hover {
  text-decoration: underline;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* 响应式设计 */
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
  
  .header-right {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .content-container {
    padding: 0 1rem;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .article-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .article-cover {
    height: 150px;
  }
  
  .article-content {
    padding: 1rem;
  }
}
</style>
