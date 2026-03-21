<template>
  <el-dialog
    v-model="dialogVisible"
    title="文章预览"
    width="800px"
    :close-on-click-modal="true"
    class="article-preview-dialog"
  >
    <div v-if="article" class="preview-container">
      <!-- 文章头部 -->
      <div class="article-header">
        <div class="article-meta">
          <el-tag 
            :type="getCategoryTagType(article.categoryName)"
            size="large"
          >
            {{ article.categoryName }}
          </el-tag>
          <span class="article-status">
            <el-tag 
              :type="getStatusTagType(article.status)" 
              size="default"
            >
              {{ getStatusText(article.status) }}
            </el-tag>
          </span>
          <span class="publish-date" v-if="article.publishedAt">
            <i class="fas fa-calendar"></i>
            {{ formatDateTime(article.publishedAt) }}
          </span>
        </div>
        
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-info">
          <span class="author">
            <i class="fas fa-user-md"></i>
            {{ article.authorName || '心理健康助手' }}
          </span>
          <span class="read-count">
            <i class="fas fa-eye"></i>
            {{ formatReadCount(article.readCount) }} 次阅读
          </span>
          <span class="read-time">
            <i class="fas fa-clock"></i>
            约 {{ getReadTime(article.content) }} 分钟阅读
          </span>
        </div>
        
        <div class="article-summary" v-if="article.summary">
          <p>{{ article.summary }}</p>
        </div>
      </div>

      <!-- 封面图片 -->
      <div class="article-cover" v-if="article.coverImage">
        <img 
          :src="article.coverImage" 
          :alt="article.title"
          @error="handleImageError"
        >
      </div>

      <!-- 文章内容 -->
      <div class="article-content">
        <div 
          class="content-body" 
          v-html="formatContent(article.content)"
        ></div>
      </div>

      <!-- 标签 -->
      <div class="article-tags" v-if="article.tags">
        <span class="tags-label">相关标签：</span>
        <el-tag
          v-for="tag in getTagArray(article.tags)"
          :key="tag"
          type="info"
          effect="plain"
          size="small"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
      </div>

      <!-- 文章统计 -->
      <div class="article-stats">
        <div class="stats-grid">
          <div class="stat-item">
            <i class="fas fa-eye"></i>
            <span class="stat-label">阅读量</span>
            <span class="stat-value">{{ formatReadCount(article.readCount) }}</span>
          </div>
          
          <div class="stat-item">
            <i class="fas fa-heart"></i>
            <span class="stat-label">收藏数</span>
            <span class="stat-value">{{ article.favoriteCount || 0 }}</span>
          </div>
          
          <div class="stat-item">
            <i class="fas fa-calendar-plus"></i>
            <span class="stat-label">创建时间</span>
            <span class="stat-value">{{ formatDateTime(article.createdAt) }}</span>
          </div>
          
          <div class="stat-item">
            <i class="fas fa-edit"></i>
            <span class="stat-label">更新时间</span>
            <span class="stat-value">{{ formatDateTime(article.updatedAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, watch } from 'vue'
import { formatDateTime } from '@/utils/dateUtils'

// Props & Emits
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  article: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听 article 属性变化，添加调试信息
watch(() => props.article, (newArticle) => {
  if (newArticle) {
    console.log('🔍 预览对话框接收到文章数据:')
    console.log('   - 文章ID:', newArticle.id)
    console.log('   - 标题:', newArticle.title)
    console.log('   - 内容长度:', newArticle.content ? newArticle.content.length : '无内容')
    console.log('   - 内容预览:', newArticle.content ? newArticle.content.substring(0, 100) + '...' : '无内容')
  }
}, { immediate: true })

// 默认封面图片
const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'

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

const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '已发布', 
    2: '已下线'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || ''
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

const formatContent = (content) => {
  console.log('🎨 formatContent 被调用:')
  console.log('   - 原始内容类型:', typeof content)
  console.log('   - 原始内容长度:', content ? content.length : '无内容')
  console.log('   - 原始内容预览:', content ? content.substring(0, 100) + '...' : '无内容')
  
  if (!content) {
    console.log('   - 返回空字符串')
    return ''
  }
  
  // 如果已经是HTML格式（来自富文本编辑器），直接返回
  if (content.includes('<') && content.includes('>')) {
    console.log('   - 检测到HTML格式，直接返回')
    return content
  }
  
  // 基本的文本格式化（用于向后兼容）
  let formatted = content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  console.log('   - 格式化后长度:', formatted.length)
  console.log('   - 格式化后预览:', formatted.substring(0, 100) + '...')
  
  return formatted
}

const handleImageError = (event) => {
  event.target.src = defaultCover
}
</script>

<style scoped>
.article-preview-dialog :deep(.el-dialog) {
  max-width: 95vw;
}

.article-preview-dialog :deep(.el-dialog__body) {
  max-height: 80vh;
  overflow-y: auto;
  padding: 0;
}

.preview-container {
  padding: 2rem;
}

/* 文章头部 */
.article-header {
  margin-bottom: 2rem;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.article-status {
  margin-left: auto;
}

.publish-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.article-title {
  font-size: 1.875rem;
  font-weight: bold;
  color: #111827;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.article-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.author,
.read-count,
.read-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.article-summary {
  background: #f9fafb;
  border-left: 4px solid #f59e0b;
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 0 8px 8px 0;
}

.article-summary p {
  color: #374151;
  font-size: 1.125rem;
  line-height: 1.6;
  margin: 0;
}

/* 封面图片 */
.article-cover {
  width: 100%;
  max-height: 400px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 文章内容 */
.article-content {
  margin-bottom: 2rem;
}

.content-body {
  font-size: 1.125rem;
  line-height: 1.8;
  color: #374151;
}

.content-body :deep(p) {
  margin-bottom: 1.5rem;
}

.content-body :deep(h1),
.content-body :deep(h2),
.content-body :deep(h3),
.content-body :deep(h4),
.content-body :deep(h5),
.content-body :deep(h6) {
  margin: 2rem 0 1rem;
  color: #111827;
  font-weight: 600;
}

.content-body :deep(h2) {
  font-size: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.content-body :deep(h3) {
  font-size: 1.3rem;
}

.content-body :deep(ul),
.content-body :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1.5rem;
}

.content-body :deep(li) {
  margin-bottom: 0.5rem;
}

.content-body :deep(strong) {
  font-weight: 600;
  color: #111827;
}

.content-body :deep(em) {
  font-style: italic;
  color: #6b7280;
}

.content-body :deep(blockquote) {
  border-left: 4px solid #f59e0b;
  background: #fffbeb;
  padding: 1rem 1.5rem;
  margin: 1.5rem 0;
  border-radius: 0 8px 8px 0;
}

/* 标签 */
.article-tags {
  padding: 1.5rem 0;
  border-top: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 2rem;
}

.tags-label {
  color: #6b7280;
  font-weight: 500;
}

.tag-item {
  margin-right: 0.5rem;
}

/* 文章统计 */
.article-stats {
  background: #f9fafb;
  border-radius: 8px;
  padding: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.stat-item i {
  font-size: 1.5rem;
  color: #3b82f6;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1.125rem;
  font-weight: 600;
  color: #111827;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .preview-container {
    padding: 1rem;
  }
  
  .article-title {
    font-size: 1.5rem;
  }
  
  .article-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .article-status {
    margin-left: 0;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
  }
}
</style>