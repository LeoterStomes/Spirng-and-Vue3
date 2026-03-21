<template>
  <div class="emotion-diary-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">情绪日记管理</h2>
        </div>
        <div class="header-actions">
          <el-button type="info" @click="handleRefresh">
            <i class="fas fa-sync-alt"></i>刷新
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
        <el-form-item label="用户搜索">
          <el-input 
            v-model="searchForm.username" 
            placeholder="输入用户名或昵称"
            clearable
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="用户ID">
          <el-input 
            v-model.number="searchForm.userId" 
            placeholder="输入用户ID"
            clearable
          />
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleDateRangeChange"
          />
        </el-form-item>

        <el-form-item label="情绪评分">
          <el-select v-model="searchForm.moodScoreRange" placeholder="选择评分范围" clearable>
            <el-option label="全部" value="" />
            <el-option label="低分 (1-3)" value="1-3" />
            <el-option label="中分 (4-6)" value="4-6" />
            <el-option label="高分 (7-10)" value="7-10" />
          </el-select>
        </el-form-item>

        <el-form-item label="主要情绪">
          <el-select v-model="searchForm.dominantEmotion" placeholder="选择情绪" clearable>
            <el-option label="全部" value="" />
            <el-option label="快乐" value="快乐" />
            <el-option label="愤怒" value="愤怒" />
            <el-option label="悲伤" value="悲伤" />
            <el-option label="焦虑" value="焦虑" />
            <el-option label="平静" value="平静" />
            <el-option label="兴奋" value="兴奋" />
          </el-select>
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
              <i class="fas fa-list"></i>
              情绪日记列表 ({{ total }} 条记录)
            </span>
            <div class="table-actions">
              <el-button 
                type="success" 
                size="small"
                :disabled="selectedRows.length === 0"
                @click="handleBatchAiAnalysis"
              >
                <i class="fas fa-brain"></i>批量AI分析 ({{ selectedRows.length }})
              </el-button>
              <el-button 
                type="danger" 
                size="small"
                :disabled="selectedRows.length === 0"
                @click="handleBatchDelete"
              >
                <i class="fas fa-trash"></i>批量删除 ({{ selectedRows.length }})
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="tableData" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
          height="500"
          stripe
        >
          <el-table-column type="selection" width="50" fixed="left" />
          
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column label="用户信息" width="150">
            <template #default="{ row }">
              <div class="user-info">
                <p class="username">{{ row.username }}</p>
                <p class="nickname">{{ row.nickname || '-' }}</p>
                <el-tag size="small">ID: {{ row.userId }}</el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="diaryDate" label="记录日期" width="120" />

          <el-table-column label="情绪评分" width="100">
            <template #default="{ row }">
              <div class="mood-score">
                <el-rate 
                  :model-value="row.moodScore" 
                  :max="10" 
                  disabled 
                  show-score
                  score-template="{value}"
                />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="dominantEmotion" label="主要情绪" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getEmotionTagType(row.dominantEmotion)"
                size="small"
              >
                {{ row.dominantEmotion || '-' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="生活指标" width="120">
            <template #default="{ row }">
              <div class="life-indicators">
                <p v-if="row.sleepQuality">
                  <i class="fas fa-bed"></i> 睡眠: {{ row.sleepQuality }}/5
                </p>
                <p v-if="row.stressLevel">
                  <i class="fas fa-brain"></i> 压力: {{ row.stressLevel }}/5
                </p>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="emotionTriggers" label="情绪触发因素" min-width="200" show-overflow-tooltip />

          <el-table-column label="AI分析状态" width="150">
            <template #default="{ row }">
              <div class="ai-analysis-status">
                <el-tag 
                  v-if="row.aiEmotionAnalysis" 
                  :type="getAiAnalysisTagType(row.aiEmotionAnalysis)" 
                  size="small"
                  class="ai-status-tag"
                >
                  <i class="fas fa-brain"></i>
                  {{ getAiAnalysisStatus(row.aiEmotionAnalysis) }}
                </el-tag>
                <el-tag v-else type="info" size="small" class="ai-status-tag">
                  <i class="fas fa-clock"></i>
                  未分析
                </el-tag>
                <div v-if="row.aiEmotionAnalysis" class="ai-analysis-preview">
                  {{ getAiAnalysisPreview(row.aiEmotionAnalysis) }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="diaryContent" label="日记内容" min-width="250" show-overflow-tooltip />

          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleViewDetail(row)"
              >
                <i class="fas fa-eye"></i>详情
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="handleTriggerAiAnalysis(row)"
                :title="row.aiEmotionAnalysis ? '重新分析' : '开始分析'"
              >
                <i class="fas fa-brain"></i>{{ row.aiEmotionAnalysis ? '重新分析' : 'AI分析' }}
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDelete(row)"
              >
                <i class="fas fa-trash"></i>删除
              </el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="情绪日记详情" 
      width="800px"
      destroy-on-close
    >
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-section">
          <h4><i class="fas fa-user"></i> 用户信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ currentDetail.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ currentDetail.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ currentDetail.userId }}</el-descriptions-item>
            <el-descriptions-item label="记录日期">{{ currentDetail.diaryDate }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4><i class="fas fa-heart"></i> 情绪状态</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="情绪评分">
              <el-rate :model-value="currentDetail.moodScore" :max="10" disabled show-score />
            </el-descriptions-item>
            <el-descriptions-item label="主要情绪">
              <el-tag :type="getEmotionTagType(currentDetail.dominantEmotion)">
                {{ currentDetail.dominantEmotion || '-' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="睡眠质量">{{ currentDetail.sleepQuality || '-' }}/5</el-descriptions-item>
            <el-descriptions-item label="压力水平">{{ currentDetail.stressLevel || '-' }}/5</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4><i class="fas fa-sticky-note"></i> 日记内容</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="情绪触发因素">
              {{ currentDetail.emotionTriggers || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="日记内容">
              {{ currentDetail.diaryContent || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.aiEmotionAnalysis" class="detail-section">
          <h4><i class="fas fa-brain"></i> AI情绪分析结果</h4>
          <div class="ai-analysis-result">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="主要情绪">
                <el-tag :type="getAiEmotionTagType(aiAnalysisData.primaryEmotion)">
                  <i :class="aiAnalysisData.icon"></i>
                  {{ aiAnalysisData.primaryEmotion }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="情绪强度">
                <el-progress 
                  :percentage="aiAnalysisData.emotionScore" 
                  :color="getEmotionScoreColor(aiAnalysisData.emotionScore)"
                  :stroke-width="8"
                />
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskLevelTagType(aiAnalysisData.riskLevel)">
                  {{ getRiskLevelText(aiAnalysisData.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="情绪性质">
                <el-tag :type="aiAnalysisData.isNegative ? 'danger' : 'success'">
                  <i :class="aiAnalysisData.isNegative ? 'fas fa-frown' : 'fas fa-smile'"></i>
                  {{ aiAnalysisData.isNegative ? '负面情绪' : '正面情绪' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="aiAnalysisData.keywords && aiAnalysisData.keywords.length > 0" class="ai-keywords-section">
              <h5><i class="fas fa-tags"></i> 情绪关键词</h5>
              <div class="keywords-container">
                <el-tag 
                  v-for="keyword in aiAnalysisData.keywords" 
                  :key="keyword"
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
            </div>

            <div v-if="aiAnalysisData.suggestion" class="ai-suggestion-section">
              <h5><i class="fas fa-lightbulb"></i> 专业建议</h5>
              <div class="suggestion-content">
                {{ aiAnalysisData.suggestion }}
              </div>
            </div>

            <div v-if="aiAnalysisData.riskDescription" class="ai-risk-section">
              <h5><i class="fas fa-exclamation-triangle"></i> 风险描述</h5>
              <div class="risk-content">
                {{ aiAnalysisData.riskDescription }}
              </div>
            </div>

            <div v-if="aiAnalysisData.improvementSuggestions && aiAnalysisData.improvementSuggestions.length > 0" class="ai-improvements-section">
              <h5><i class="fas fa-arrow-up"></i> 改善建议</h5>
              <ul class="improvement-list">
                <li v-for="suggestion in aiAnalysisData.improvementSuggestions" :key="suggestion">
                  {{ suggestion }}
                </li>
              </ul>
            </div>

            <div class="ai-analysis-meta">
              <p class="analysis-time">
                <i class="fas fa-clock"></i>
                分析时间: {{ formatTimestamp(aiAnalysisData.timestamp) }}
              </p>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4><i class="fas fa-clock"></i> 时间信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentDetail.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(currentDetail.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh } from '@element-plus/icons-vue'
import {
  getAdminEmotionDiariesPage,
  getAdminEmotionOverview,
  adminDeleteEmotionDiary,
  getAiEmotionAnalysis,
  adminTriggerAiEmotionAnalysis,
  adminBatchTriggerAiEmotionAnalysis
} from '@/api/emotionDiary'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedRows = ref([])
const detailDialogVisible = ref(false)
const currentDetail = ref(null)
const dateRange = ref([])
const aiAnalysisData = ref({})

// 搜索表单
const searchForm = reactive({
  username: '',
  userId: null,
  startDate: '',
  endDate: '',
  moodScoreRange: '',
  dominantEmotion: ''
})

// 概览数据
const overviewData = reactive({
  recordDays: 0,
  targetDays: 0,
  positiveDays: 0,
  averageMoodScore: 0
})

// 获取概览数据
const fetchOverviewData = async () => {
  try {
    await getAdminEmotionOverview({
      onSuccess: (res) => {
        Object.assign(overviewData, res)
      }
    })
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取情绪日记列表
const fetchEmotionDiaries = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      username: searchForm.username || undefined,
      userId: searchForm.userId || undefined,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined,
      dominantEmotion: searchForm.dominantEmotion || undefined
    }

    // 处理情绪评分范围
    if (searchForm.moodScoreRange) {
      const [min, max] = searchForm.moodScoreRange.split('-').map(Number)
      params.minMoodScore = min
      params.maxMoodScore = max
    }

    await getAdminEmotionDiariesPage(params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取情绪日记列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理日期范围变化
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchEmotionDiaries()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    userId: null,
    startDate: '',
    endDate: '',
    moodScoreRange: '',
    dominantEmotion: ''
  })
  dateRange.value = []
  currentPage.value = 1
  fetchEmotionDiaries()
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  fetchEmotionDiaries()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchEmotionDiaries()
}

// 表格选择处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 查看详情
const handleViewDetail = (row) => {
  currentDetail.value = row
  
  // 解析AI分析数据
  if (row.aiEmotionAnalysis) {
    try {
      aiAnalysisData.value = JSON.parse(row.aiEmotionAnalysis)
    } catch (error) {
      console.error('解析AI分析数据失败:', error)
      aiAnalysisData.value = {}
    }
  } else {
    aiAnalysisData.value = {}
  }
  
  detailDialogVisible.value = true
}

// 删除单条记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${row.username} 在 ${row.diaryDate} 的情绪日记吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )

    await adminDeleteEmotionDiary(row.id, {
      onSuccess: () => {
        fetchEmotionDiaries()
        fetchOverviewData()
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条情绪日记记录吗？`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )

    // 逐个删除选中的记录
    for (const row of selectedRows.value) {
      await adminDeleteEmotionDiary(row.id)
    }

    ElMessage.success(`成功删除 ${selectedRows.value.length} 条记录`)
    selectedRows.value = []
    fetchEmotionDiaries()
    fetchOverviewData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 导出数据
const handleExportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

// 刷新
const handleRefresh = () => {
  fetchEmotionDiaries()
  fetchOverviewData()
}

// 手动触发AI分析
const handleTriggerAiAnalysis = async (row) => {
  const isReAnalysis = !!row.aiEmotionAnalysis
  const actionText = isReAnalysis ? '重新分析' : '分析'
  const confirmText = isReAnalysis 
    ? `确定要重新分析用户 ${row.username} 在 ${row.diaryDate} 的情绪日记吗？\n注意：这将覆盖现有的分析结果。`
    : `确定要为用户 ${row.username} 在 ${row.diaryDate} 的情绪日记触发AI分析吗？`

  try {
    await ElMessageBox.confirm(
      confirmText,
      `AI情绪${actionText}确认`,
      {
        type: isReAnalysis ? 'warning' : 'info',
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true
      }
    )

    await adminTriggerAiEmotionAnalysis(row.id, {
      onSuccess: () => {
        ElMessage.success(`AI${actionText}已加入队列，请稍后刷新查看结果`)
        // 延迟刷新数据
        setTimeout(() => {
          fetchEmotionDiaries()
        }, 2000)
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('触发AI分析失败:', error)
    }
  }
}

// 批量AI分析
const handleBatchAiAnalysis = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要分析的记录')
    return
  }

  const hasAnalyzedCount = selectedRows.value.filter(row => row.aiEmotionAnalysis).length
  const newAnalysisCount = selectedRows.value.length - hasAnalyzedCount
  
  let confirmMessage = `确定要为选中的 ${selectedRows.value.length} 条情绪日记记录触发AI分析吗？`
  if (hasAnalyzedCount > 0) {
    confirmMessage += `\n\n其中 ${hasAnalyzedCount} 条已有分析结果，${newAnalysisCount} 条未分析。`
    confirmMessage += `\n已分析的记录将被重新分析并覆盖现有结果。`
  }

  try {
    await ElMessageBox.confirm(
      confirmMessage,
      '批量AI分析确认',
      {
        type: 'warning',
        confirmButtonText: '确定分析',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true
      }
    )

    const diaryIds = selectedRows.value.map(row => row.id)
    
    await adminBatchTriggerAiEmotionAnalysis(diaryIds, {
      onSuccess: (result) => {
        const { totalCount, successCount, failCount, failReasons } = result
        
        let message = `批量AI分析任务已提交完成！\n总计: ${totalCount} 条，成功: ${successCount} 条`
        
        if (failCount > 0) {
          message += `，失败: ${failCount} 条`
          if (failReasons && failReasons.length > 0) {
            message += `\n\n失败原因：\n${failReasons.slice(0, 5).join('\n')}`
            if (failReasons.length > 5) {
              message += `\n...等${failReasons.length - 5}个其他错误`
            }
          }
        }

        ElMessageBox.alert(message, '批量分析结果', {
          type: successCount === totalCount ? 'success' : 'warning',
          dangerouslyUseHTMLString: true
        })

        // 清空选择并刷新数据
        selectedRows.value = []
        setTimeout(() => {
          fetchEmotionDiaries()
          fetchOverviewData()
        }, 2000)
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量AI分析失败:', error)
    }
  }
}

// 辅助函数
const getGrowthPercent = (value) => {
  return Math.floor(Math.random() * 20) + 5 // 模拟增长率
}

const getMoodScoreClass = (score) => {
  if (score >= 7) return 'stat-growth-positive'
  if (score >= 4) return 'stat-growth-neutral'
  return 'stat-growth-negative'
}

const getMoodScoreIcon = (score) => {
  if (score >= 7) return 'fas fa-smile'
  if (score >= 4) return 'fas fa-meh'
  return 'fas fa-frown'
}

const getMoodScoreText = (score) => {
  if (score >= 7) return '良好'
  if (score >= 4) return '一般'
  return '偏低'
}

const getEmotionTagType = (emotion) => {
  const emotionTypes = {
    '快乐': 'success',
    '平静': 'info',
    '兴奋': 'warning',
    '愤怒': 'danger',
    '悲伤': 'info',
    '焦虑': 'warning'
  }
  return emotionTypes[emotion] || 'info'
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return new Date(dateTimeStr).toLocaleString('zh-CN')
}

// AI分析相关辅助函数
const getAiAnalysisStatus = (aiAnalysisJson) => {
  try {
    const analysis = JSON.parse(aiAnalysisJson)
    return analysis.primaryEmotion || '已分析'
  } catch {
    return '数据异常'
  }
}

const getAiAnalysisTagType = (aiAnalysisJson) => {
  try {
    const analysis = JSON.parse(aiAnalysisJson)
    if (analysis.riskLevel >= 3) return 'danger'
    if (analysis.riskLevel >= 2) return 'warning'
    if (analysis.isNegative) return 'info'
    return 'success'
  } catch {
    return 'danger'
  }
}

const getAiAnalysisPreview = (aiAnalysisJson) => {
  try {
    const analysis = JSON.parse(aiAnalysisJson)
    const riskText = getRiskLevelText(analysis.riskLevel)
    return `${riskText} | 强度: ${analysis.emotionScore}%`
  } catch {
    return '解析失败'
  }
}

const getAiEmotionTagType = (emotion) => {
  const emotionTagMap = {
    '快乐': 'success',
    '平静': 'success',
    '兴奋': 'warning',
    '满足': 'success',
    '愤怒': 'danger',
    '悲伤': 'info',
    '焦虑': 'warning',
    '恐惧': 'danger',
    '沮丧': 'info',
    '压力': 'warning'
  }
  return emotionTagMap[emotion] || 'info'
}

const getRiskLevelTagType = (riskLevel) => {
  const riskTagMap = {
    0: 'success',
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return riskTagMap[riskLevel] || 'info'
}

const getRiskLevelText = (riskLevel) => {
  const riskTextMap = {
    0: '正常',
    1: '关注',
    2: '预警',
    3: '危机'
  }
  return riskTextMap[riskLevel] || '未知'
}

const getEmotionScoreColor = (score) => {
  if (score >= 80) return '#f56c6c'
  if (score >= 60) return '#e6a23c'
  if (score >= 40) return '#909399'
  return '#67c23a'
}

const formatTimestamp = (timestamp) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchOverviewData()
  fetchEmotionDiaries()
})
</script>

<style lang="scss" scoped>
.emotion-diary-management {
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
    gap: 12px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
    &.stat-value-green { color: #67c23a; }
    &.stat-value-orange { color: #e6a23c; }
    &.stat-value-purple { color: #9c27b0; }
  }
  
  .stat-growth {
    margin: 0;
    font-size: 12px;
    
    &.stat-growth-positive { color: #67c23a; }
    &.stat-growth-neutral { color: #e6a23c; }
    &.stat-growth-negative { color: #f56c6c; }
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
  &.stat-icon-green { background-color: #67c23a; }
  &.stat-icon-orange { background-color: #e6a23c; }
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
  width: 240px;
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
  
  .user-info {
    .username {
      margin: 0 0 4px 0;
      font-weight: 600;
      color: #303133;
    }
    
    .nickname {
      margin: 0 0 4px 0;
      color: #909399;
      font-size: 12px;
    }
  }
  
  .mood-score {
    .el-rate {
      height: auto;
    }
  }
  
  .life-indicators {
    p {
      margin: 0 0 4px 0;
      font-size: 12px;
      color: #606266;
      
      i {
        margin-right: 4px;
        color: #909399;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }
}

.detail-content {
  .detail-section {
    margin-bottom: 24px;
    
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
      
      i {
        margin-right: 8px;
        color: #409eff;
      }
    }
  }
}

// AI分析相关样式
.ai-analysis-status {
  .ai-status-tag {
    margin-bottom: 4px;
    
    i {
      margin-right: 4px;
    }
  }
  
  .ai-analysis-preview {
    font-size: 11px;
    color: #909399;
    margin-top: 2px;
  }
}

.ai-analysis-result {
  .ai-keywords-section,
  .ai-suggestion-section,
  .ai-risk-section,
  .ai-improvements-section {
    margin-top: 16px;
    padding: 12px;
    background-color: #f8f9fa;
    border-radius: 4px;
    
    h5 {
      margin: 0 0 8px 0;
      color: #606266;
      font-size: 14px;
      font-weight: 600;
      
      i {
        margin-right: 6px;
        color: #909399;
      }
    }
  }
  
  .keywords-container {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    
    .keyword-tag {
      background-color: #e1f3d8;
      color: #67c23a;
      border-color: #b3d8a4;
    }
  }
  
  .suggestion-content,
  .risk-content {
    line-height: 1.6;
    color: #606266;
    background-color: white;
    padding: 8px;
    border-radius: 4px;
    border: 1px solid #ebeef5;
  }
  
  .improvement-list {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 4px;
      color: #606266;
      line-height: 1.5;
    }
  }
  
  .ai-analysis-meta {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    
    .analysis-time {
      margin: 0;
      font-size: 12px;
      color: #909399;
      
      i {
        margin-right: 4px;
      }
    }
  }
  
  .el-progress {
    .el-progress__text {
      font-size: 12px !important;
    }
  }
}
</style>

