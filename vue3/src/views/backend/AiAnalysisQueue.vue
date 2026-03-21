<template>
  <div class="ai-analysis-queue">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">AI分析队列管理</h2>
        </div>
        <div class="header-actions">
          <div class="auto-refresh-controls">
            <span class="refresh-label">
              <i class="fas fa-sync-alt"></i>自动刷新间隔:
            </span>
            <el-select
              v-model="refreshInterval"
              placeholder="刷新间隔"
              style="width: 110px"
              size="small"
              @change="updateRefreshInterval"
            >
              <el-option label="5秒" :value="5000" />
              <el-option label="10秒" :value="10000" />
              <el-option label="30秒" :value="30000" />
              <el-option label="1分钟" :value="60000" />
              <el-option label="2分钟" :value="120000" />
            </el-select>
            <span v-if="countdown > 0" class="countdown-text">
              <i class="fas fa-clock"></i>{{ Math.ceil(countdown / 1000) }}s后刷新
            </span>
          </div>
          <el-button type="success" @click="handleRefresh" :loading="loading">
            <i class="fas fa-sync-alt"></i>立即刷新
          </el-button>
        </div>
      </div>
    </div>


    <!-- 筛选和搜索 -->
    <div class="search-area">
      <el-form 
        :model="searchForm" 
        ref="searchFormRef" 
        inline 
        label-width="80px"
        @submit.prevent="handleSearch"
        class="search-form"
      >
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>

        <el-form-item label="任务类型">
          <el-select v-model="searchForm.taskType" placeholder="选择类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="自动触发" value="AUTO" />
            <el-option label="手动触发" value="MANUAL" />
            <el-option label="管理员触发" value="ADMIN" />
            <el-option label="批量触发" value="BATCH" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="选择优先级" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="低" :value="1" />
            <el-option label="正常" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="用户搜索">
          <el-input 
            v-model="searchForm.username" 
            placeholder="输入用户名"
            clearable
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
            @change="handleDateRangeChange"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="searchForm.failedOnly">只显示失败</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="searchForm.retryableOnly">只显示可重试</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="table-header">
            <span class="table-title">
              <i class="fas fa-tasks"></i>
              任务队列 ({{ total }} 个任务)
            </span>
            <div class="table-actions">
              <el-button 
                type="success" 
                size="small"
                :disabled="selectedRows.length === 0"
                @click="handleBatchRetry"
              >
                <i class="fas fa-redo"></i>批量重试 ({{ selectedRows.length }})
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="tableData" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
          height="600"
          stripe
        >
          <el-table-column type="selection" width="50" fixed="left" />
          
          <el-table-column prop="id" label="任务ID" width="80" />

          <el-table-column label="日记信息" width="180">
            <template #default="{ row }">
              <div class="diary-info">
                <p class="diary-id">日记ID: {{ row.diaryId }}</p>
                <p class="diary-date">{{ row.diaryDate || '-' }}</p>
                <p class="user-info">
                  <i class="fas fa-user"></i>
                  {{ row.username || 'Unknown' }}
                  <span v-if="row.nickname">({{ row.nickname }})</span>
                </p>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="任务状态" width="120">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.status)" 
                size="small"
                :class="{ 'processing-tag': row.status === 'PROCESSING' }"
              >
                <i :class="getStatusIcon(row.status)"></i>
                {{ row.statusDescription }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="任务类型" width="120">
            <template #default="{ row }">
              <el-tag 
                :type="getTaskTypeTagType(row.taskType)" 
                size="small"
              >
                <i :class="getTaskTypeIcon(row.taskType)"></i>
                {{ row.taskTypeDescription }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="优先级" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getPriorityTagType(row.priority)" 
                size="small"
              >
                <i :class="getPriorityIcon(row.priority)"></i>
                {{ row.priorityDescription }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="重试信息" width="120">
            <template #default="{ row }">
              <div class="retry-info">
                <p class="retry-count">{{ row.retryCount }}/{{ row.maxRetryCount }}</p>
                <el-tag 
                  v-if="row.canRetry" 
                  type="warning" 
                  size="small"
                >
                  可重试
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />

          <el-table-column label="时间信息" width="160">
            <template #default="{ row }">
              <div class="time-info">
                <p class="created-time">创建: {{ formatDateTime(row.createdAt) }}</p>
                <p v-if="row.startedAt" class="started-time">开始: {{ formatDateTime(row.startedAt) }}</p>
                <p v-if="row.completedAt" class="completed-time">完成: {{ formatDateTime(row.completedAt) }}</p>
                <p v-if="row.processingTimeMs" class="processing-time">
                  耗时: {{ formatDuration(row.processingTimeMs) }}
                </p>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="row.canRetry"
                type="warning" 
                size="small" 
                @click="handleRetryTask(row)"
              >
                <i class="fas fa-redo"></i>重试
              </el-button>
              <el-tag v-else type="info" size="small">
                {{ row.status === 'COMPLETED' ? '已完成' : '不可重试' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  getAiAnalysisTaskPage,
  getAiAnalysisTaskStatistics,
  retryAiAnalysisTask,
  batchRetryAiAnalysisTasks
} from '@/api/aiAnalysisTask'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedRows = ref([])
const dateRange = ref([])

// 自动刷新相关
const REFRESH_INTERVAL_KEY = 'ai_analysis_queue_refresh_interval'
const refreshInterval = ref(getStoredRefreshInterval()) // 从缓存获取间隔
const countdown = ref(0)
let refreshTimer = null
let countdownTimer = null

// 从localStorage获取存储的刷新间隔
function getStoredRefreshInterval() {
  const stored = localStorage.getItem(REFRESH_INTERVAL_KEY)
  return stored ? parseInt(stored) : 10000 // 默认10秒
}

// 存储刷新间隔到localStorage
function storeRefreshInterval(interval) {
  localStorage.setItem(REFRESH_INTERVAL_KEY, interval.toString())
}

// 搜索表单
const searchForm = reactive({
  status: '',
  taskType: '',
  priority: null, // 改为null，避免类型转换问题
  username: '',
  startTime: '',
  endTime: '',
  failedOnly: false,
  retryableOnly: false
})

// 队列统计数据
const queueStats = reactive({
  totalTasks: 0,
  pendingTasks: 0,
  processingTasks: 0,
  completedTasks: 0,
  failedTasks: 0,
  retryableTasks: 0,
  taskTypeStats: {}
})

// 获取队列统计信息
const fetchQueueStatistics = async () => {
  try {
    await getAiAnalysisTaskStatistics({
      onSuccess: (res) => {
        Object.assign(queueStats, res)
      }
    })
  } catch (error) {
    console.error('获取队列统计失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    // 构建参数对象，只包含有效值
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    // 临时测试：先只传递基本参数，逐步添加其他参数来定位问题
    // 只有当值存在且有效时才添加到参数中
    if (searchForm.status && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (searchForm.taskType && searchForm.taskType !== '') {
      params.taskType = searchForm.taskType
    }
    // 特别处理priority参数
    if (searchForm.priority !== null && searchForm.priority !== '' && Number.isInteger(searchForm.priority)) {
      params.priority = Number(searchForm.priority) // 确保是数字类型
    }
    if (searchForm.username && searchForm.username.trim() !== '') {
      params.username = searchForm.username.trim()
    }
    if (searchForm.startTime && searchForm.startTime !== '') {
      params.startTime = searchForm.startTime
    }
    if (searchForm.endTime && searchForm.endTime !== '') {
      params.endTime = searchForm.endTime
    }
    if (searchForm.failedOnly === true) {
      params.failedOnly = true
    }
    if (searchForm.retryableOnly === true) {
      params.retryableOnly = true
    }

    // 调试日志（临时）
    console.log('🔍 发送请求参数:', params)

    await getAiAnalysisTaskPage(params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理日期范围变化
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    searchForm.startTime = value[0]
    searchForm.endTime = value[1]
  } else {
    searchForm.startTime = ''
    searchForm.endTime = ''
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchTasks()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    status: '',
    taskType: '',
    priority: null, // 改为null，保持一致性
    username: '',
    startTime: '',
    endTime: '',
    failedOnly: false,
    retryableOnly: false
  })
  dateRange.value = []
  currentPage.value = 1
  fetchTasks()
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  fetchTasks()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTasks()
}

// 表格选择处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 重试单个任务
const handleRetryTask = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重试任务 ${row.id} 吗？`,
      '重试确认',
      {
        type: 'warning',
        confirmButtonText: '确定重试',
        cancelButtonText: '取消'
      }
    )

    await retryAiAnalysisTask(row.id, {
      onSuccess: () => {
        fetchTasks()
        fetchQueueStatistics()
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重试任务失败:', error)
    }
  }
}

// 批量重试
const handleBatchRetry = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要重试的任务')
    return
  }

  const retryableTasks = selectedRows.value.filter(row => row.canRetry)
  if (retryableTasks.length === 0) {
    ElMessage.warning('所选任务中没有可重试的任务')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要重试选中的 ${retryableTasks.length} 个可重试任务吗？`,
      '批量重试确认',
      {
        type: 'warning',
        confirmButtonText: '确定重试',
        cancelButtonText: '取消'
      }
    )

    const taskIds = retryableTasks.map(row => row.id)
    
    await batchRetryAiAnalysisTasks(taskIds, {
      onSuccess: (result) => {
        const { totalCount, successCount, failCount, failReasons } = result
        
        let message = `批量重试完成！\n总计: ${totalCount} 个，成功: ${successCount} 个`
        
        if (failCount > 0) {
          message += `，失败: ${failCount} 个`
          if (failReasons && failReasons.length > 0) {
            message += `\n\n失败原因：\n${failReasons.slice(0, 3).join('\n')}`
            if (failReasons.length > 3) {
              message += `\n...等${failReasons.length - 3}个其他错误`
            }
          }
        }

        ElMessageBox.alert(message, '批量重试结果', {
          type: successCount === totalCount ? 'success' : 'warning',
          dangerouslyUseHTMLString: true
        })

        // 清空选择并刷新数据
        selectedRows.value = []
        fetchTasks()
        fetchQueueStatistics()
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量重试失败:', error)
    }
  }
}

// 手动刷新
const handleRefresh = () => {
  fetchTasks()
  fetchQueueStatistics()
  resetCountdown() // 重置倒计时
}

// 启动自动刷新
const startAutoRefresh = () => {
  // 清除现有定时器
  stopAutoRefresh()
  
  // 设置刷新定时器
  refreshTimer = setInterval(() => {
    fetchTasks()
    fetchQueueStatistics()
    resetCountdown()
  }, refreshInterval.value)
  
  // 启动倒计时
  resetCountdown()
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

// 重置倒计时
const resetCountdown = () => {
  countdown.value = refreshInterval.value
  
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  
  countdownTimer = setInterval(() => {
    countdown.value -= 100
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 100)
}

// 更新刷新间隔
const updateRefreshInterval = () => {
  // 存储到缓存
  storeRefreshInterval(refreshInterval.value)
  
  // 重启自动刷新以应用新间隔
  startAutoRefresh()
  
  ElMessage.success(`刷新间隔已更新为 ${refreshInterval.value / 1000} 秒`)
}

// 辅助函数
const getStatusTagType = (status) => {
  const statusTypes = {
    'PENDING': 'warning',
    'PROCESSING': 'success',
    'COMPLETED': 'success',
    'FAILED': 'danger'
  }
  return statusTypes[status] || 'info'
}

const getStatusIcon = (status) => {
  const statusIcons = {
    'PENDING': 'fas fa-clock',
    'PROCESSING': 'fas fa-cog fa-spin',
    'COMPLETED': 'fas fa-check-circle',
    'FAILED': 'fas fa-times-circle'
  }
  return statusIcons[status] || 'fas fa-question-circle'
}

const getTaskTypeTagType = (taskType) => {
  const typeTypes = {
    'AUTO': 'info',
    'MANUAL': 'primary',
    'ADMIN': 'warning',
    'BATCH': 'success'
  }
  return typeTypes[taskType] || 'info'
}

const getTaskTypeIcon = (taskType) => {
  const typeIcons = {
    'AUTO': 'fas fa-robot',
    'MANUAL': 'fas fa-hand-pointer',
    'ADMIN': 'fas fa-user-shield',
    'BATCH': 'fas fa-layer-group'
  }
  return typeIcons[taskType] || 'fas fa-question'
}

const getPriorityTagType = (priority) => {
  const priorityTypes = {
    1: 'info',
    2: 'primary',
    3: 'warning',
    4: 'danger'
  }
  return priorityTypes[priority] || 'info'
}

const getPriorityIcon = (priority) => {
  const priorityIcons = {
    1: 'fas fa-arrow-down',
    2: 'fas fa-minus',
    3: 'fas fa-arrow-up',
    4: 'fas fa-exclamation'
  }
  return priorityIcons[priority] || 'fas fa-question'
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return new Date(dateTimeStr).toLocaleString('zh-CN')
}

const formatDuration = (ms) => {
  if (!ms) return '-'
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`
  return `${(ms / 60000).toFixed(1)}min`
}

// 组件挂载时初始化数据并启动自动刷新
onMounted(() => {
  fetchQueueStatistics()
  fetchTasks()
  // 启动自动刷新
  startAutoRefresh()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.ai-analysis-queue {
  padding: 2rem;
  background: #fefefe;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-header {
  margin-bottom: 2rem;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    padding: 2rem;
    border-radius: 16px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
  
  .header-left {
    flex: 1;
    
    .page-title {
      font-size: 2rem;
      font-weight: 300;
      color: #2d3748;
      margin: 0;
      letter-spacing: -0.02em;
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .auto-refresh-controls {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 12px;
      background: #f0f9ff;
      border-radius: 6px;
      border: 1px solid #d1e7ff;
      
      .refresh-label {
        font-size: 13px;
        color: #606266;
        font-weight: 500;
        white-space: nowrap;
        
        i {
          margin-right: 4px;
          color: #409eff;
        }
      }
      
      .countdown-text {
        font-size: 12px;
        color: #67c23a;
        font-weight: 600;
        margin-left: 4px;
        min-width: 80px;
        white-space: nowrap;
        
        i {
          margin-right: 4px;
        }
      }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  .stat-label {
    margin: 0 0 8px 0;
    color: #909399;
    font-size: 14px;
  }
  
  .stat-value {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 600;
    
    &.stat-value-blue { color: #409eff; }
    &.stat-value-orange { color: #e6a23c; }
    &.stat-value-green { color: #67c23a; }
    &.stat-value-red { color: #f56c6c; }
    &.stat-value-purple { color: #9c27b0; }
  }
  
  .stat-growth {
    margin: 0;
    font-size: 12px;
    
    &.stat-growth-positive { color: #67c23a; }
    &.stat-growth-warning { color: #e6a23c; }
    &.stat-growth-negative { color: #f56c6c; }
    &.stat-growth-neutral { color: #909399; }
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  
  &.stat-icon-blue { background-color: #409eff; }
  &.stat-icon-orange { background-color: #e6a23c; }
  &.stat-icon-green { background-color: #67c23a; }
  &.stat-icon-red { background-color: #f56c6c; }
  &.stat-icon-purple { background-color: #9c27b0; }
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

.search-area :deep(.el-date-editor) {
  width: 300px;
}

.search-area :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-date-editor .el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-checkbox) {
  margin-right: 0;
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

.table-section {
  :deep(.el-card) {
    border-radius: 16px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
  
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .table-title {
      font-weight: 600;
      color: #303133;
      
      i {
        margin-right: 8px;
        color: #409eff;
      }
    }
  }
  
  .diary-info {
    .diary-id {
      margin: 0 0 4px 0;
      font-weight: 600;
      color: #303133;
      font-size: 12px;
    }
    
    .diary-date {
      margin: 0 0 4px 0;
      color: #606266;
      font-size: 12px;
    }
    
    .user-info {
      margin: 0;
      color: #909399;
      font-size: 11px;
      
      i {
        margin-right: 4px;
      }
    }
  }
  
  .retry-info {
    .retry-count {
      margin: 0 0 4px 0;
      font-size: 12px;
      color: #606266;
    }
  }
  
  .time-info {
    p {
      margin: 0 0 2px 0;
      font-size: 11px;
      color: #909399;
    }
    
    .processing-time {
      color: #67c23a;
      font-weight: 600;
    }
  }
  
  .processing-tag {
    animation: pulse 1.5s infinite;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}
</style>

