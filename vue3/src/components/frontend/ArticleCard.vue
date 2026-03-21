<template>
  <div 
    class="article-card"
    @click="goToArticle"
  >
    <div class="article-image">
      <img 
        :src="article.coverImage || defaultCover" 
        :alt="article.title"
        @error="handleImageError"
      >
      <div class="image-overlay">
        <i class="fas fa-eye"></i>
      </div>
    </div>
    
    <div class="article-content">
      <div class="article-meta">
        <el-tag 
          :type="getCategoryTagType(article.categoryName)"
          size="small"
          effect="plain"
        >
          {{ article.categoryName }}
        </el-tag>
        <span class="read-time">{{ getReadTime(article.content) }}分钟阅读</span>
      </div>
      
      <h3 class="article-title">{{ article.title }}</h3>
      
      <p class="article-summary">
        {{ article.summary || getAutoSummary(article.content) }}
      </p>
      
      <div class="article-footer">
        <div class="author-info">
          <i class="fas fa-user-md"></i>
          <span>{{ article.authorName || '心理健康助手' }}</span>
          <span class="separator">·</span>
          <i class="fas fa-calendar"></i>
          <span>{{ formatDate(article.publishedAt) }}</span>
        </div>
        
        <div class="article-stats">
          <span class="stat-item">
            <i class="fas fa-eye"></i>
            {{ formatReadCount(article.readCount) }}
          </span>
          <span 
            class="favorite-btn stat-item"
            :class="{ favorited: article.isFavorited }"
            @click.stop="toggleFavorite"
          >
            <i :class="article.isFavorited ? 'fas fa-heart' : 'far fa-heart'"></i>
            {{ article.favoriteCount || 0 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteArticle, unfavoriteArticle } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

// Props
const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['favorite-changed'])

const router = useRouter()

// 默认封面图片
const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

// 计算属性
const isLoggedIn = computed(() => {
  return localStorage.getItem('token')
})

// 方法
const goToArticle = () => {
  router.push(`/knowledge/article/${props.article.id}`)
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

  try {
    if (props.article.isFavorited) {
      await unfavoriteArticle(props.article.id)
      props.article.isFavorited = false
      props.article.favoriteCount = Math.max(0, (props.article.favoriteCount || 1) - 1)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteArticle(props.article.id)
      props.article.isFavorited = true
      props.article.favoriteCount = (props.article.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
    
    // 通知父组件收藏状态变化
    emit('favorite-changed', props.article)
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
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

const getAutoSummary = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 120 ? plainText.substring(0, 120) + '...' : plainText
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
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s ease;
  display: flex;
  height: 180px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(245, 158, 11, 0.15);
}

.article-card:hover .image-overlay {
  opacity: 1;
}

.article-image {
  width: 240px;
  height: 100%;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .article-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-overlay i {
  color: white;
  font-size: 1.5rem;
}

.article-content {
  flex: 1;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.read-time {
  font-size: 0.75rem;
  color: #6b7280;
}

.article-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.75rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.article-card:hover .article-title {
  color: #f59e0b;
}

.article-summary {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.5;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 0.75rem;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
}

.separator {
  margin: 0 0.25rem;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
  transition: color 0.3s ease;
}

.favorite-btn {
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.favorite-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.favorite-btn.favorited {
  color: #ef4444;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
    height: auto;
  }
  
  .article-image {
    width: 100%;
    height: 150px;
  }
  
  .article-content {
    padding: 1rem;
  }
  
  .article-footer {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
  
  .article-stats {
    align-self: flex-end;
  }
}
</style>