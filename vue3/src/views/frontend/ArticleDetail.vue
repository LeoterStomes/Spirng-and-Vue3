<template>
  <div class="article-detail-page">
    <!-- 页面头部 -->
    <section class="page-header">
      <div class="header-container">
        <div class="header-content">
          <div class="header-left">
            <div class="breathing-animation">
              <i class="fas fa-book-open"></i>
            </div>
            <div class="header-text">
              <h2>知识文章详情</h2>
              <p v-if="article">{{ article.categoryName }} - 深度解读心理健康知识</p>
              <p v-else>加载中...</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button @click="goBack" class="back-btn">
              <i class="fas fa-arrow-left"></i>
              返回知识库
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 详情内容 -->
    <div v-else-if="article" class="detail-content">
      <div class="content-container">
        <div class="content-layout">
        <!-- 左侧：文章详情 -->
        <div class="article-detail-section">
          <!-- 文章信息和封面组合卡片 -->
          <div class="detail-card article-info-card">
            <h3 class="card-title">
              <i class="fas fa-newspaper"></i>
              文章信息
            </h3>
            
            <div class="article-info-layout">
              <!-- 左侧：文章信息 -->
              <div class="article-info-content">
                <div class="article-meta">
                  <el-tag 
                    :type="getCategoryTagType(article.categoryName)"
                    size="large"
                    class="category-tag"
                  >
                    {{ article.categoryName }}
                  </el-tag>
                  <span class="publish-date">
                    <i class="fas fa-calendar"></i>
                    {{ formatDate(article.publishedAt) }}
                  </span>
                  <span class="read-time">
                    <i class="fas fa-clock"></i>
                    约 {{ getReadTime(article.content) }} 分钟阅读
                  </span>
                </div>
                
                <h1 class="article-title">{{ article.title }}</h1>
                
                <div class="article-summary" v-if="article.summary">
                  <div class="summary-content">
                    <i class="fas fa-quote-left"></i>
                    <p>{{ article.summary }}</p>
                  </div>
                </div>
                
                <div class="article-stats">
                  <div class="stats-left">
                    <span class="author">
                      <i class="fas fa-user-md"></i>
                      {{ article.authorName || '心理健康助手' }}
                    </span>
                    <span class="read-count">
                      <i class="fas fa-eye"></i>
                      {{ formatReadCount(article.readCount) }} 次阅读
                    </span>
                  </div>
                  
                  <div class="stats-right">
                    <el-button
                      :type="article.isFavorited ? 'danger' : 'primary'"
                      @click="toggleFavorite"
                      class="action-btn"
                    >
                      <i :class="article.isFavorited ? 'fas fa-heart' : 'far fa-heart'"></i>
                      {{ article.isFavorited ? '已收藏' : '收藏' }}
                    </el-button>
                    
                    <el-button
                      @click="shareArticle"
                      type="info"
                      class="action-btn"
                    >
                      <i class="fas fa-share-alt"></i>
                      分享
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 右侧：封面图片 -->
              <div class="article-cover-section" v-if="article.coverImage">
                <img 
                  :src="article.coverImage" 
                  :alt="article.title"
                  @error="handleImageError"
                  class="cover-image"
                >
              </div>
            </div>
          </div>

          <!-- 文章内容卡片 -->
          <div class="detail-card content-card">
            <h3 class="card-title">
              <i class="fas fa-file-alt"></i>
              正文内容
            </h3>
            <div class="content-wrapper" v-html="formatContent(article.content)"></div>
            
            <!-- 标签内容直接在正文底部 -->
            <div class="tags-content" v-if="article.tags">
              <h4 class="tags-title">
                <i class="fas fa-tags"></i>
                相关标签
              </h4>
              <div class="tags-list">
                <el-tag
                  v-for="tag in getTagArray(article.tags)"
                  :key="tag"
                  type="info"
                  effect="light"
                  class="tag-item"
                >
                  <i class="fas fa-tag"></i>
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>


        </div>

        <!-- 右侧：推荐内容 -->
        <div class="sidebar-section">
          <!-- 相关文章推荐 -->
          <div class="detail-card related-articles-card" v-if="relatedArticles.length > 0">
            <h3 class="card-title">
              <i class="fas fa-lightbulb"></i>
              相关文章推荐
            </h3>
            <div class="related-list">
              <div 
                v-for="relatedArticle in relatedArticles"
                :key="relatedArticle.id"
                class="related-item"
                @click="goToArticle(relatedArticle.id)"
              >
                <div class="related-image">
                  <img 
                    :src="relatedArticle.coverImage || defaultCover" 
                    :alt="relatedArticle.title"
                    @error="handleImageError"
                  >
                </div>
                <div class="related-content">
                  <h4>{{ relatedArticle.title }}</h4>
                  <p>{{ getAutoSummary(relatedArticle.content) }}</p>
                  <div class="related-meta">
                    <span><i class="fas fa-eye"></i>{{ formatReadCount(relatedArticle.readCount) }}</span>
                    <span><i class="fas fa-calendar"></i>{{ formatDate(relatedArticle.publishedAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 温馨提示卡片 -->
          <div class="detail-card tips-card">
            <h3 class="card-title">
              <i class="fas fa-info-circle"></i>
              温馨提示
            </h3>
            <div class="tips-content">
              <div class="tip-item">
                <i class="fas fa-book-reader"></i>
                定期阅读心理健康知识有助于提升心理素养
              </div>
              <div class="tip-item">
                <i class="fas fa-heart"></i>
                收藏喜欢的文章，方便日后查阅
              </div>
              <div class="tip-item">
                <i class="fas fa-share-alt"></i>
                分享有价值的内容，帮助更多人受益
              </div>
              <div class="tip-item">
                <i class="fas fa-user-friends"></i>
                如需专业帮助，请及时寻求心理咨询
              </div>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <div class="error-container">
        <div class="error-content">
          <i class="fas fa-exclamation-circle"></i>
          <h3>文章不存在</h3>
          <p>抱歉，您访问的文章不存在或已被删除</p>
          <el-button type="primary" @click="goBack">返回知识库</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticleById } from '@/api/knowledgeArticle'
import { favoriteArticle, unfavoriteArticle, checkFavoriteStatus } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const article = ref(null)
const relatedArticles = ref([])
const userRating = ref(null)

// 默认封面图片
const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

// 计算属性
const isLoggedIn = computed(() => {
  return localStorage.getItem('token')
})

// 方法
const fetchArticle = async () => {
  const articleId = route.params.id
  if (!articleId) {
    ElMessage.error('文章ID不存在')
    return
  }

  loading.value = true
  try {
    const response = await getArticleById(articleId)
    if (response) {
      article.value = response
      
      // 检查收藏状态
      if (isLoggedIn.value) {
        try {
          const favoriteStatus = await checkFavoriteStatus(articleId)
          article.value.isFavorited = favoriteStatus
        } catch (error) {
          article.value.isFavorited = false
        }
      }
      
      // 获取相关文章
      await fetchRelatedArticles()
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
  } finally {
    loading.value = false
  }
}

const fetchRelatedArticles = async () => {
  if (!article.value) return
  
  try {
    // 这里可以根据分类或标签获取相关文章
    // 暂时模拟相关文章数据
    relatedArticles.value = []
  } catch (error) {
    console.error('获取相关文章失败:', error)
  }
}

const toggleFavorite = async () => {
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

  if (!article.value) return

  try {
    if (article.value.isFavorited) {
      await unfavoriteArticle(article.value.id)
      article.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteArticle(article.value.id)
      article.value.isFavorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const shareArticle = async () => {
  if (navigator.share) {
    try {
      await navigator.share({
        title: article.value.title,
        text: article.value.summary,
        url: window.location.href
      })
    } catch (error) {
      if (error.name !== 'AbortError') {
        fallbackShare()
      }
    }
  } else {
    fallbackShare()
  }
}

const fallbackShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('文章链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('请手动复制当前页面链接进行分享')
  })
}

const rateArticle = (rating) => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后再评价')
    return
  }
  
  userRating.value = rating
  ElMessage.success(rating === 'helpful' ? '感谢您的反馈！' : '我们会持续改进内容质量')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/knowledge')
  }
}

const goToArticle = (articleId) => {
  router.push(`/knowledge/article/${articleId}`)
}

// 工具方法
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
  const wordCount = content.length
  return Math.max(1, Math.ceil(wordCount / 300))
}

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

const getTagArray = (tags) => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
}

const getAutoSummary = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
}

const formatContent = (content) => {
  if (!content) return ''
  
  // 基本的HTML清理和格式化
  let formatted = content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  return formatted
}

const handleImageError = (event) => {
  event.target.src = defaultCover
}

// 生命周期
onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: #f9fafb;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  color: white;
  padding: 2rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.breathing-animation {
  font-size: 2.5rem;
  animation: breathing 4s ease-in-out infinite;
  color: #7ED321;
}

@keyframes breathing {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.header-text h2 {
  font-size: 1.8rem;
  margin: 0 0 0.25rem 0;
  font-weight: 600;
  letter-spacing: -0.25px;
}

.header-text p {
  font-size: 0.95rem;
  margin: 0;
  opacity: 0.85;
  font-weight: 400;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

/* 头部按钮样式 */
.header-actions .el-button {
  border-radius: 0.5rem;
  font-weight: 500;
  padding: 0.6rem 1.2rem;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.header-actions .back-btn {
  background: #374151;
  border-color: #4B5563;
  color: white;
}

.header-actions .back-btn:hover {
  background: #1F2937;
  border-color: #374151;
}

/* 加载和错误状态 */
.loading-container, .error-state {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.error-container {
  background: white;
  border-radius: 2rem;
  padding: 4rem 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.error-content i {
  font-size: 4rem;
  color: #ef4444;
  margin-bottom: 1.5rem;
}

.error-content h3 {
  font-size: 1.5rem;
  color: #111827;
  margin-bottom: 1rem;
}

.error-content p {
  color: #6b7280;
  margin-bottom: 2rem;
}

/* 内容区域样式 */
.detail-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.content-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.article-detail-section, .sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 卡片样式 */
.detail-card {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 文章信息卡片 */
.article-info-card {
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  border: 1px solid #E2E8F0;
}

/* 文章信息和封面水平布局 */
.article-info-layout {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.article-info-content {
  flex: 1;
}

.article-cover-section {
  width: 300px;
  flex-shrink: 0;
}

.article-cover-section .cover-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 1rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 内容卡片 */
.content-card {
  background: linear-gradient(135deg, #FEFEFE 0%, #F9FAFB 100%);
  border: 1px solid #E5E7EB;
}



/* 相关文章卡片 */
.related-articles-card {
  background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
  border: 1px solid #BBF7D0;
}

/* 温馨提示卡片 */
.tips-card {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border: 1px solid #FDE68A;
  position: relative;
}

.tips-card::before {
  content: '💡';
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 1.5rem;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
}

.card-title i {
  color: #7ED321;
  font-size: 1.25rem;
}

/* 文章元数据 */
.article-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.category-tag {
  font-weight: 500;
}

.publish-date,
.read-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.article-title {
  font-size: 2rem;
  font-weight: bold;
  color: #111827;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.article-summary {
  margin-bottom: 1.5rem;
}

.summary-content {
  background: rgba(126, 211, 33, 0.1);
  border-left: 4px solid #7ED321;
  padding: 1rem 1.5rem;
  border-radius: 0 8px 8px 0;
  position: relative;
}

.summary-content i {
  color: #7ED321;
  font-size: 1.5rem;
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  opacity: 0.3;
}

.summary-content p {
  color: #374151;
  font-size: 1.125rem;
  line-height: 1.6;
  margin: 0;
  padding-left: 2rem;
}

.article-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.stats-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.author,
.read-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.stats-right {
  display: flex;
  gap: 0.75rem;
}

/* 内容样式 */
.content-wrapper {
  font-size: 1.125rem;
  line-height: 1.3;
  color: #374151;
}

.content-wrapper :deep(p) {
  margin-bottom: 1rem;
}

.content-wrapper :deep(h1),
.content-wrapper :deep(h2),
.content-wrapper :deep(h3),
.content-wrapper :deep(h4),
.content-wrapper :deep(h5),
.content-wrapper :deep(h6) {
  margin: 1.5rem 0 0.75rem;
  color: #111827;
  font-weight: 600;
}

.content-wrapper :deep(h2) {
  font-size: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.content-wrapper :deep(h3) {
  font-size: 1.3rem;
}

.content-wrapper :deep(ul),
.content-wrapper :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}

.content-wrapper :deep(li) {
  margin-bottom: 0.25rem;
}

.content-wrapper :deep(strong) {
  font-weight: 600;
  color: #111827;
}

.content-wrapper :deep(em) {
  font-style: italic;
  color: #6b7280;
}

.content-wrapper :deep(blockquote) {
  border-left: 4px solid #7ED321;
  background: #F0FDF4;
  padding: 1rem 1.5rem;
  margin: 1rem 0;
  border-radius: 0 8px 8px 0;
}

/* 标签内容 - 在正文底部 */
.tags-content {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.tags-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.tags-title i {
  color: #7ED321;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 0.5rem;
  font-weight: 500;
}

.tag-item i {
  color: #3B82F6;
}

/* 按钮样式 */
.action-btn, .action-btn-large, .rating-btn {
  border-radius: 0.5rem;
  font-weight: 500;
  border: 1px solid transparent;
}



/* 相关文章 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.related-item {
  display: flex;
  gap: 1rem;
  cursor: pointer;
  padding: 1rem;
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.related-item:hover {
  background: rgba(255, 255, 255, 0.8);
}

.related-image {
  width: 100px;
  height: 70px;
  flex-shrink: 0;
  border-radius: 0.5rem;
  overflow: hidden;
}

.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-content {
  flex: 1;
}

.related-content h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.related-content p {
  color: #6b7280;
  font-size: 0.8rem;
  line-height: 1.4;
  margin-bottom: 0.5rem;
}

.related-meta {
  display: flex;
  gap: 1rem;
  color: #9ca3af;
  font-size: 0.7rem;
}

.related-meta span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* 温馨提示 */
.tips-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #92400E;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 0.5rem;
}

.tip-item:hover {
  background: rgba(255, 255, 255, 0.8);
}

.tip-item i {
  color: #F59E0B;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 1.5rem 0;
  }
  
  .header-container {
    padding: 0 1rem;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .header-left {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .breathing-animation {
    font-size: 2rem;
  }
  
  .header-text h2 {
    font-size: 1.5rem;
  }
  
  .header-text p {
    font-size: 0.85rem;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem;
  }
  
  .header-actions .el-button {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }
  
  .content-container {
    padding: 0 1rem;
  }
  
  .content-layout {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .detail-card {
    padding: 1.5rem;
    border-radius: 1rem;
  }
  
  .card-title {
    font-size: 1.1rem;
  }
  
  .article-title {
    font-size: 1.5rem;
  }
  
  .content-wrapper {
    font-size: 1rem;
  }
  
  
  .article-stats {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .article-info-layout {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .article-cover-section {
    width: 100%;
  }
  
  .article-cover-section .cover-image {
    height: 200px;
  }
  
  .related-item {
    flex-direction: column;
  }
  
  .related-image {
    width: 100%;
    height: 120px;
  }
}
</style>