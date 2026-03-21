<template>
  <div class="article-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">知识文章管理</h1>
        </div>
        <div class="header-actions">
          <el-button 
            type="primary" 
            @click="handleCreate"
            size="default"
          >
            新建文章
          </el-button>
          <el-button 
            type="danger" 
            :disabled="!selectedRows.length"
            @click="handleBatchDelete"
            size="default"
          >
            批量删除
          </el-button>
          <el-button 
            @click="handleRefresh"
            size="default"
          >
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-area">
      <el-form 
        :model="searchForm" 
        inline 
        label-width="80px"
        @submit.prevent="handleSearch"
      >
        <el-form-item label="文章标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入文章标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select 
            v-model="searchForm.categoryId" 
            placeholder="选择分类"
            clearable
            filterable
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="选择状态"
            clearable
          >
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下线" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="作者">
          <el-input
            v-model="searchForm.authorName"
            placeholder="请输入作者姓名"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-area">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        style="width: 100%"
        height="600"
      >
        <el-table-column type="selection" width="50" fixed="left" />
        
        <el-table-column 
          prop="id" 
          label="ID" 
          width="120" 
          sortable="custom"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="id-cell" :title="row.id">
              {{ row.id.substring(0, 8) }}...
            </span>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="title" 
          label="文章标题" 
          min-width="250"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title-text" @click="handlePreview(row)">
                {{ row.title }}
              </div>
              <div class="title-meta">
                <el-tag 
                  :type="getStatusTagType(row.status)" 
                  size="small"
                >
                  {{ getStatusText(row.status) }}
                </el-tag>
                <span class="read-count">
                  <i class="fas fa-eye"></i>
                  {{ row.readCount }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="categoryName" 
          label="分类" 
          width="120"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="authorName" 
          label="作者" 
          width="120"
          sortable="custom"
        />
        
        <el-table-column 
          prop="readCount" 
          label="阅读量" 
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            <span class="read-count-cell">
              {{ formatReadCount(row.readCount) }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="publishedAt" 
          label="发布时间" 
          width="180"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.publishedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="createdAt" 
          label="创建时间" 
          width="180"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column 
          label="操作" 
          width="200" 
          fixed="right"
        >
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                link
              >
                编辑
              </el-button>
              
              <el-button
                v-if="row.status === 0 || row.status === 2"
                type="success"
                size="small"
                @click="handlePublish(row)"
                link
              >
                {{ row.status === 0 ? '发布' : '重新发布' }}
              </el-button>
              
              <el-button
                v-if="row.status === 1"
                type="warning"
                size="small"
                @click="handleOffline(row)"
                link
              >
                下线
              </el-button>
              
              <el-button
                type="info"
                size="small"
                @click="handlePreview(row)"
                link
              >
                预览
              </el-button>
              
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                link
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-area">
      <el-pagination
        :current-page="searchForm.currentPage"
        :page-size="searchForm.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
      />
    </div>

    <!-- 文章编辑对话框 -->
    <ArticleEditDialog
      v-model="editDialogVisible"
      :article="currentArticle"
      :categories="categories"
      @success="handleEditSuccess"
    />

    <!-- 文章预览对话框 -->
    <ArticlePreviewDialog
      v-model="previewDialogVisible"
      :article="previewArticle"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Edit, Delete, Refresh, Top, Bottom, View
} from '@element-plus/icons-vue'
import ArticleEditDialog from './ArticleEditDialog.vue'
import ArticlePreviewDialog from './ArticlePreviewDialog.vue'
import { getArticlePage, deleteArticle, updateArticleStatus, batchDeleteArticles, getArticleById } from '@/api/knowledgeArticle'
import { getCategoryTree } from '@/api/knowledgeCategory'
import { formatDateTime } from '@/utils/dateUtils'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const selectedRows = ref([])
const total = ref(0)

// 对话框控制
const editDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const currentArticle = ref(null)
const previewArticle = ref(null)

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: null,
  status: null,
  authorName: '',
  sortField: 'createdAt',
  sortDirection: 'desc',
  currentPage: 1,
  size: 20
})

// 方法
const fetchData = () => {
  loading.value = true
  getArticlePage({
    ...searchForm,
    // 后台可以查看所有状态的文章
    status: searchForm.status
  }, {
    onSuccess: (response) => {
      if (response && response.records) {
        tableData.value = response.records
        total.value = response.total || 0
      } else {
        tableData.value = []
        total.value = 0
      }
      loading.value = false
    },
    onError: (error) => {
      console.error('获取文章列表失败:', error)
      ElMessage.error('获取文章列表失败')
      tableData.value = []
      total.value = 0
      loading.value = false
    }
  })
}

const fetchCategories = () => {
  getCategoryTree( {
    onSuccess: (response) => {
      console.log('获取分类列表成功:', response)
      // if (response && Array.isArray(response)) {
      //   categories.value = response
      // }
      categories.value = response||[]
    },
    onError: (error) => {
      console.error('获取分类列表失败:', error)
      ElMessage.error('获取分类列表失败')
    }
  })
}

const handleSearch = () => {
  searchForm.currentPage = 1
  fetchData()
}

const handleSizeChange = (size) => {
  searchForm.size = size
  searchForm.currentPage = 1
  fetchData()
}

const handleCurrentChange = (page) => {
  searchForm.currentPage = page
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    categoryId: null,
    status: null,
    authorName: '',
    sortField: 'createdAt',
    sortDirection: 'desc',
    currentPage: 1,
    size: 20
  })
  fetchData()
}

const handleRefresh = () => {
  fetchData()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleSortChange = ({ prop, order }) => {
  if (prop) {
    searchForm.sortField = prop
    searchForm.sortDirection = order === 'ascending' ? 'asc' : 'desc'
    handleSearch()
  }
}

const handleCreate = () => {
  currentArticle.value = null
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  // 获取完整的文章数据（包含内容）
  console.log('🔍 获取文章详情用于编辑:', row.id)
  getArticleById(row.id, {
    onSuccess: (articleDetail) => {
      console.log('📄 获取到的文章详情:', articleDetail)
      currentArticle.value = articleDetail
      editDialogVisible.value = true
    },
    onError: (error) => {
      console.error('获取文章详情失败:', error)
      ElMessage.error('获取文章详情失败: ' + error.message)
    }
  })
}


const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章"${row.title}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    deleteArticle(row.id, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchData()
      },
      onError: (error) => {
        console.error('删除文章失败:', error)
        ElMessage.error('删除失败')
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.log('用户取消删除操作')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的文章')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 篇文章吗？此操作不可恢复。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除逻辑
    const articleIds = selectedRows.value.map(row => row.id)
    const deleteCount = selectedRows.value.length
    
    batchDeleteArticles({ ids: articleIds }, {
      successMsg: `成功删除 ${deleteCount} 篇文章`,
      onSuccess: () => {
        fetchData()
      },
      onError: (error) => {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败')
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.log('用户取消批量删除操作')
    }
  }
}

const handlePublish = async (row) => {
  try {
    const isRepublish = row.status === 2  // 是否为重新发布
    const actionText = isRepublish ? '重新发布' : '发布'
    
    await ElMessageBox.confirm(
      `确定要${actionText}文章"${row.title}"吗？`,
      `${actionText}确认`,
      {
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    updateArticleStatus(row.id, { status: 1 }, {
      successMsg: `文章${actionText}成功`,
      onSuccess: () => {
        fetchData()
      },
      onError: (error) => {
        console.error('发布文章失败:', error)
        ElMessage.error('发布失败')
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.log('用户取消发布操作')
    }
  }
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下线文章"${row.title}"吗？`,
      '下线确认',
      {
        confirmButtonText: '确定下线',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    updateArticleStatus(row.id, { status: 2 }, {
      successMsg: '文章下线成功',
      onSuccess: () => {
        fetchData()
      },
      onError: (error) => {
        console.error('下线文章失败:', error)
        ElMessage.error('下线失败')
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.log('用户取消下线操作')
    }
  }
}

const handlePreview = (row) => {
  // 获取完整的文章数据（包含内容）
  console.log('🔍 获取文章详情用于预览:', row.id)
  getArticleById(row.id, {
    onSuccess: (articleDetail) => {
      console.log('📄 获取到的文章详情:', articleDetail)
      previewArticle.value = articleDetail
      previewDialogVisible.value = true
    },
    onError: (error) => {
      console.error('获取文章详情失败:', error)
      ElMessage.error('获取文章详情失败: ' + error.message)
    }
  })
}

const handleEditSuccess = () => {
  editDialogVisible.value = false
  fetchData()
}

// 工具方法
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

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

// 生命周期
onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.article-management {
  padding: 2rem;
  background: #fefefe;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 页面头部 */
.page-header {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}


.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 300;
  color: #2d3748;
  margin: 0;
  letter-spacing: -0.02em;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

/* 按钮样式优化 */
.header-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 400;
  letter-spacing: 0.02em;
  transition: all 0.3s ease;
}

.header-actions :deep(.el-button--primary) {
  background: #667eea;
  border-color: #667eea;
}

.header-actions :deep(.el-button--primary:hover) {
  background: #5a6fd8;
  border-color: #5a6fd8;
}

.header-actions :deep(.el-button--danger) {
  background: #f093fb;
  border-color: #f093fb;
}

.header-actions :deep(.el-button--danger:hover) {
  background: #e879f9;
  border-color: #e879f9;
}

/* 搜索区域 */
.search-area {
  background: #fff;
  padding: 1.5rem 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.search-area :deep(.el-form) {
  margin: 0;
}

.search-area :deep(.el-form-item) {
  margin-bottom: 1rem;
  margin-right: 2rem;
}

.search-area :deep(.el-form-item:last-child) {
  margin-right: 0;
}

.search-area :deep(.el-form-item__label) {
  color: #4a5568;
  font-weight: 500;
  font-size: 0.875rem;
  margin-bottom: 0;
}

.search-area :deep(.el-input) {
  width: 180px;
}

.search-area :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-select) {
  width: 140px;
}

.search-area :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-select .el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 16px;
}

.search-area :deep(.el-button--primary) {
  background: #667eea;
  border-color: #667eea;
}

.search-area :deep(.el-button--primary:hover) {
  background: #5a6fd8;
  border-color: #5a6fd8;
}

/* 表格区域 */
.table-area {
  background: #fff;
  padding: 2rem;
  border-radius: 16px;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}


.table-area :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.table-area :deep(.el-table__header) {
  background: #f8fafc;
}

.table-area :deep(.el-table th) {
  background: #f8fafc;
  color: #4a5568;
  font-weight: 500;
  padding: 16px 12px;
}

.table-area :deep(.el-table td) {
  padding: 16px 12px;
  color: #2d3748;
}

.table-area :deep(.el-table__row:hover) {
  background: #f7fafc;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.title-text {
  font-weight: 500;
  color: #2d3748;
  cursor: pointer;
  line-height: 1.5;
  transition: all 0.3s ease;
}

.title-text:hover {
  color: #667eea;
}

.title-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.read-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #718096;
  font-size: 0.875rem;
}

.read-count-cell {
  font-weight: 500;
  color: #38a169;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-buttons :deep(.el-button) {
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}


/* 标签样式优化 */
.table-area :deep(.el-tag) {
  border-radius: 20px;
  font-weight: 400;
  padding: 4px 12px;
}

.table-area :deep(.el-tag--success) {
  background: #c6f6d5;
  color: #22543d;
}

.table-area :deep(.el-tag--info) {
  background: #e2e8f0;
  color: #2d3748;
}

.table-area :deep(.el-tag--warning) {
  background: #fef5e7;
  color: #c05621;
}

/* 分页区域 */
.pagination-area {
  display: flex;
  justify-content: center;
  padding: 2rem;
  background: #fff;
  border-radius: 16px;
  transition: all 0.3s ease;
}


.pagination-area :deep(.el-pagination) {
  font-weight: 400;
}

.pagination-area :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
}


.pagination-area :deep(.btn-prev),
.pagination-area :deep(.btn-next) {
  border-radius: 8px;
  transition: all 0.3s ease;
}


/* ID显示优化 */
.id-cell {
  font-family: 'SF Mono', Monaco, Inconsolata, 'Roboto Mono', Consolas, 'Courier New', monospace;
  font-size: 0.875rem;
  color: #718096;
  background: #f7fafc;
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .article-management {
    padding: 1.5rem;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 1.5rem;
    align-items: stretch;
    padding: 1.5rem;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }
  
  .search-area {
    padding: 1.5rem;
  }
  
  .search-area :deep(.el-form) {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .search-area :deep(.el-form-item) {
    margin-bottom: 1rem;
  }
  
  .search-area :deep(.el-input),
  .search-area :deep(.el-select) {
    width: 100%;
  }
  
  .table-area {
    overflow-x: auto;
    padding: 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.5rem;
  }
}

@media (max-width: 768px) {
  .article-management {
    padding: 1rem;
  }
  
  .page-header {
    padding: 0 0 1.5rem 0;
    margin-bottom: 1.5rem;
  }
  
  .page-title {
    font-size: 1.75rem;
  }
  
  .toolbar {
    padding: 1.5rem 1rem;
  }
  
  .search-area {
    padding: 1.5rem 1rem;
  }
  
  .table-area {
    padding: 1.5rem 1rem;
  }
  
  .pagination-area {
    padding: 1.5rem 1rem;
  }
}

</style>