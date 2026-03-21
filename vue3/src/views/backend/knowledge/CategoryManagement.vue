<template>
  <div class="category-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">知识分类管理</h1>
        </div>
        <div class="header-actions">
          <el-button 
            type="primary" 
            @click="handleCreate"
            size="default"
          >
            新建分类
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
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.categoryName"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="选择状态"
            clearable
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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
        @sort-change="handleSortChange"
        stripe
        style="width: 100%"
        height="600"
      >
        <el-table-column 
          prop="id" 
          label="ID" 
          width="80" 
          sortable="custom"
        />
        
        <el-table-column 
          prop="categoryName" 
          label="分类名称" 
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="category-name-cell">
              <i 
                class="fas fa-folder" 
                style="color: #409EFF; margin-right: 8px;"
              ></i>
              <span class="category-name">{{ row.categoryName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="description" 
          label="描述" 
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span v-if="row.description">{{ row.description }}</span>
            <span v-else class="text-muted">暂无描述</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="sortOrder" 
          label="排序" 
          width="80"
          sortable="custom"
        />
        
        <el-table-column 
          prop="status" 
          label="状态" 
          width="100"
        >
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : 'danger'" 
              size="small"
            >
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          label="文章数量" 
          width="100"
        >
          <template #default="{ row }">
            <el-tooltip content="该分类下的文章数量" placement="top">
              <span class="stat-item">
                <i class="fas fa-file-alt"></i>
                {{ row.articleCount || 0 }}
              </span>
            </el-tooltip>
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
                size="small" 
                type="primary" 
                link
                @click="handleEdit(row)"
              >
                <i class="fas fa-edit"></i>
                编辑
              </el-button>
              <el-button 
                size="small" 
                :type="row.status === 1 ? 'warning' : 'success'" 
                link
                @click="handleToggleStatus(row)"
              >
                <i :class="row.status === 1 ? 'fas fa-ban' : 'fas fa-check'"></i>
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                link
                @click="handleDelete(row)"
                :disabled="row.articleCount > 0"
              >
                <i class="fas fa-trash"></i>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          :current-page="searchForm.currentPage"
          :page-size="searchForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 分类表单对话框 -->
    <CategoryFormDialog
      v-model="formDialogVisible"
      :category="currentCategory"
      :categories="categoryOptions"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CategoryFormDialog from './CategoryFormDialog.vue'
import { 
  getCategoryPage, 
  deleteCategory, 
  updateCategory,
  getCategoryTree 
} from '@/api/knowledgeCategory'
import { formatDateTime } from '@/utils/dateUtils'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const categoryOptions = ref([])
const total = ref(0)

// 对话框控制
const formDialogVisible = ref(false)
const currentCategory = ref(null)

// 搜索表单
const searchForm = reactive({
  categoryName: '',
  status: null,
  currentPage: 1,
  size: 20
})

// 方法
const fetchData = () => {
  loading.value = true
  getCategoryPage(searchForm, {
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
      console.error('获取分类列表失败:', error)
      ElMessage.error('获取分类列表失败')
      tableData.value = []
      total.value = 0
      loading.value = false
    }
  })
}

const fetchCategoryOptions = () => {
  getCategoryTree({
    onSuccess: (response) => {
      categoryOptions.value = response || []
    },
    onError: (error) => {
      console.error('获取分类选项失败:', error)
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
    categoryName: '',
    status: null,
    currentPage: 1,
    size: 20
  })
  fetchData()
}

const handleRefresh = () => {
  fetchData()
  fetchCategoryOptions()
}

const handleSortChange = ({ prop, order }) => {
  // 如果需要排序功能，可以在这里实现
  console.log('排序变更:', prop, order)
}

const handleCreate = () => {
  currentCategory.value = null
  formDialogVisible.value = true
}


const handleEdit = (category) => {
  currentCategory.value = { ...category }
  formDialogVisible.value = true
}

const handleToggleStatus = (category) => {
  const newStatus = category.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}分类"${category.categoryName}"吗？`,
    '状态变更确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    updateCategory(category.id, { status: newStatus }, {
      onSuccess: () => {
        ElMessage.success(`${statusText}成功`)
        fetchData()
      },
      onError: (error) => {
        console.error(`${statusText}失败:`, error)
        ElMessage.error(`${statusText}失败`)
      }
    })
  }).catch(() => {
    // 用户取消操作
  })
}

const handleDelete = (category) => {
  if (category.articleCount > 0) {
    ElMessage.warning('该分类下存在文章，无法删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除分类"${category.categoryName}"吗？此操作不可恢复！`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    deleteCategory(category.id, {
      onSuccess: () => {
        ElMessage.success('删除成功')
        fetchData()
        fetchCategoryOptions()
      },
      onError: (error) => {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    })
  }).catch(() => {
    // 用户取消操作
  })
}

const handleFormSuccess = () => {
  formDialogVisible.value = false
  fetchData()
  fetchCategoryOptions()
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
  fetchCategoryOptions()
})
</script>

<style scoped>
.category-management {
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

.category-name-cell {
  display: flex;
  align-items: center;
}

.category-name {
  font-weight: 500;
  color: #2d3748;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
  color: #718096;
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

.table-area :deep(.el-tag--danger) {
  background: #fed7d7;
  color: #742a2a;
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

/* 响应式设计 */
@media (max-width: 1024px) {
  .category-management {
    padding: 1.5rem;
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
  .category-management {
    padding: 1rem;
  }
  
  .page-header {
    padding: 0 0 1.5rem 0;
    margin-bottom: 1.5rem;
  }
  
  .page-title {
    font-size: 1.75rem;
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
