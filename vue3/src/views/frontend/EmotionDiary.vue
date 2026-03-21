<template>
  <div class="emotion-diary-page">
    <!-- 页面头部 -->
    <div class="header-section">
      <div class="header-content">
        <div class="header-left">
          <div class="breathing-animation">
            <i class="fas fa-heart"></i>
          </div>
          <div class="header-text">
            <h2>情绪日记</h2>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showHistoryDialog = true">
            <i class="fas fa-calendar"></i>
            查看历史记录
          </el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="content-layout">
        <!-- 左侧：今日情绪记录 -->
        <div class="diary-section">
          <!-- 情绪评分卡片 -->
          <div class="diary-card">
            <h3 class="card-title">
              <i class="fas fa-smile"></i>今日情绪评分
            </h3>
            
            <div class="mood-score-section">
              <p class="mood-label">您今天的整体情绪状态如何？(1-10分)</p>
              
              <div class="mood-selectors">
                <span class="mood-range-label">很糟糕</span>
                <div class="mood-buttons">
                  <div
                    v-for="score in 10"
                    :key="score"
                    class="mood-selector"
                    :class="{ selected: diaryForm.moodScore === score }"
                    @click="selectMoodScore(score)"
                  >
                    {{ score }}
                  </div>
                </div>
                <span class="mood-range-label">非常好</span>
              </div>
              
              <div class="selected-mood" v-if="diaryForm.moodScore">
                <span class="current-selection">
                  当前选择：{{ diaryForm.moodScore }}分 - {{ getMoodDescription(diaryForm.moodScore) }}
                </span>
              </div>
            </div>
          </div>

          <!-- 主要情绪选择 -->
          <div class="diary-card">
            <h3 class="card-title">
              <i class="fas fa-palette"></i>主要情绪
            </h3>
            
            <div class="emotion-grid">
              <div
                v-for="emotion in emotionOptions"
                :key="emotion.name"
                class="emotion-card"
                :class="{ selected: diaryForm.dominantEmotion === emotion.name }"
                @click="selectEmotion(emotion.name)"
              >
                <i :class="emotion.icon" :style="{ color: emotion.color }"></i>
                <div class="emotion-name">{{ emotion.name }}</div>
              </div>
            </div>
          </div>

          <!-- 详细记录 -->
          <div class="diary-card">
            <h3 class="card-title">
              <i class="fas fa-edit"></i>详细记录
            </h3>
            
            <div class="detail-form">
              <!-- 情绪触发因素 -->
              <div class="form-group">
                <label class="form-label">情绪触发因素</label>
                <el-input
                  v-model="diaryForm.emotionTriggers"
                  type="textarea"
                  :rows="3"
                  placeholder="今天什么事情影响了您的情绪？"
                  maxlength="1000"
                  show-word-limit
                />
              </div>

              <!-- 日记内容 -->
              <div class="form-group">
                <label class="form-label">今日感想</label>
                <el-input
                  v-model="diaryForm.diaryContent"
                  type="textarea"
                  :rows="5"
                  placeholder="写下您今天的想法、感受或发生的有趣事情..."
                  maxlength="2000"
                  show-word-limit
                />
              </div>

              <!-- 生活指标 -->
              <div class="life-indicators">
                <div class="indicator-group">
                  <label class="form-label">睡眠质量</label>
                  <el-select v-model="diaryForm.sleepQuality" placeholder="请选择">
                    <el-option label="很差" :value="1" />
                    <el-option label="较差" :value="2" />
                    <el-option label="一般" :value="3" />
                    <el-option label="良好" :value="4" />
                    <el-option label="优秀" :value="5" />
                  </el-select>
                </div>
                
                <div class="indicator-group">
                  <label class="form-label">压力水平</label>
                  <el-select v-model="diaryForm.stressLevel" placeholder="请选择">
                    <el-option label="很低" :value="1" />
                    <el-option label="较低" :value="2" />
                    <el-option label="中等" :value="3" />
                    <el-option label="较高" :value="4" />
                    <el-option label="很高" :value="5" />
                  </el-select>
                </div>
              </div>

              <!-- 保存按钮 -->
              <div class="action-buttons">
                <el-button @click="resetForm">
                  重置
                </el-button>
                <el-button 
                  type="primary" 
                  @click="saveDiary"
                  :loading="saving"
                >
                  <i class="fas fa-save"></i>
                  提交记录
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：统计与分析 -->
        <div class="statistics-section">
          <!-- 情绪趋势 -->
          <div class="stat-card">
            <h4 class="stat-title">
              <i class="fas fa-chart-line"></i>7天情绪趋势
            </h4>
            <div class="trend-chart" ref="trendChartContainer">
              <canvas ref="trendChart" width="300" height="200"></canvas>
            </div>
          </div>

          <!-- 情绪统计 -->
          <div class="stat-card">
            <h4 class="stat-title">
              <i class="fas fa-chart-pie"></i>本周情绪分布
            </h4>
            <div class="emotion-stats">
              <template v-if="emotionStats.length > 0">
                <div 
                  v-for="stat in emotionStats" 
                  :key="stat.emotion"
                  class="stat-item"
                >
                  <div class="stat-color" :style="{ backgroundColor: stat.color }"></div>
                  <span class="stat-label">{{ stat.emotion }}</span>
                  <span class="stat-value">{{ stat.percentage }}%</span>
                </div>
              </template>
              <div v-else class="no-emotion-data">
                <i class="fas fa-chart-pie"></i>
                <p>暂无情绪数据</p>
                <p class="tip">请先记录情绪日记以查看统计数据</p>
              </div>
            </div>
          </div>

          <!-- AI分析建议 -->
          <div class="stat-card analysis-card">
            <h4 class="stat-title">
              <i class="fas fa-lightbulb"></i>AI智能分析
            </h4>
            <div class="analysis-content">
              <div v-if="statistics.suggestions && statistics.suggestions.length > 0">
                <div 
                  v-for="(suggestion, index) in statistics.suggestions" 
                  :key="index"
                  class="suggestion-item"
                >
                  <i class="fas fa-circle"></i>
                  {{ suggestion }}
                </div>
              </div>
              <div v-else class="no-analysis">
                暂无分析数据，请继续记录情绪日记
              </div>
            </div>
          </div>

          <!-- 今日AI情绪分析 -->
          <div class="stat-card ai-emotion-card" v-if="currentDiaryId">
            <h4 class="stat-title">
              <i class="fas fa-brain"></i>今日AI情绪分析
              <el-button 
                type="text" 
                size="small" 
                @click="refreshTodayAnalysisStatus"
                :loading="aiAnalysisLoading"
              >
                <i class="fas fa-sync-alt"></i>
              </el-button>
            </h4>
            <div class="ai-analysis-content">
              <div v-if="aiAnalysis" class="analysis-result">
                <div class="emotion-header">
                  <span class="emotion-icon">{{ aiAnalysis.icon }}</span>
                  <span class="emotion-label">{{ aiAnalysis.label }}</span>
                  <span class="emotion-score">{{ aiAnalysis.emotionScore }}分</span>
                </div>
                
                <div class="risk-level" :class="getRiskLevelClass(aiAnalysis.riskLevel)">
                  风险等级：{{ getRiskLevelText(aiAnalysis.riskLevel) }}
                </div>
                
                <div class="ai-suggestion" v-if="aiAnalysis.suggestion">
                  <h5>AI建议：</h5>
                  <p>{{ aiAnalysis.suggestion }}</p>
                </div>
                
                <div class="improvement-suggestions" v-if="aiAnalysis.improvementSuggestions && aiAnalysis.improvementSuggestions.length > 0">
                  <h5>改善建议：</h5>
                  <ul>
                    <li v-for="suggestion in aiAnalysis.improvementSuggestions" :key="suggestion">
                      {{ suggestion }}
                    </li>
                  </ul>
                </div>
              </div>
              <div v-else-if="aiAnalysisLoading" class="loading-analysis">
                <i class="fas fa-spinner fa-spin"></i>
                正在分析中...
              </div>
              <div v-else class="no-ai-analysis">
                <p>AI分析尚未完成</p>
                <el-button size="small" @click="triggerAnalysis">
                  <i class="fas fa-play"></i>开始分析
                </el-button>
              </div>
            </div>
          </div>

          <!-- 快速操作 -->
          <div class="stat-card">
            <h4 class="stat-title">快速操作</h4>
            <div class="quick-actions">
              <el-button class="action-btn" @click="$router.push('/consultation')">
                <i class="fas fa-comments"></i>
                AI情绪咨询
              </el-button>
              <el-button class="action-btn" @click="showHistoryDialog = true">
                <i class="fas fa-calendar"></i>
                查看历史记录
              </el-button>
            </div>
          </div>

          <!-- 每日提醒 -->
          <div class="stat-card tip-card">
            <h4 class="stat-title">💡 每日小贴士</h4>
            <p class="tip-content">
              坚持记录情绪日记有助于提高情绪觉察能力，建议每天花5-10分钟进行记录。
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录对话框 -->
    <el-dialog v-model="showHistoryDialog" title="情绪日记历史记录" width="80%" top="5vh">
      <div class="history-content">
        <!-- 查询条件 -->
        <div class="history-filters">
          <el-date-picker
            v-model="historyDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadHistoryData"
          />
          <el-select v-model="historyEmotionFilter" placeholder="情绪筛选" clearable @change="loadHistoryData">
            <el-option 
              v-for="emotion in emotionOptions" 
              :key="emotion.name"
              :label="emotion.name" 
              :value="emotion.name" 
            />
          </el-select>
        </div>

        <!-- 历史记录列表 -->
        <div class="history-list">
          <div 
            v-for="diary in historyData" 
            :key="diary.id"
            class="history-item"
          >
            <div class="history-date">{{ diary.diaryDate }}</div>
            <div class="history-mood">
              <span class="mood-score">{{ diary.moodScore }}分</span>
              <span class="mood-emotion">{{ diary.dominantEmotion || '未记录' }}</span>
            </div>
            <div class="history-content-preview">
              {{ diary.diaryContentPreview || '无内容' }}
            </div>
            <!-- AI分析状态 -->
            <div class="ai-analysis-status">
              <el-tag 
                v-if="diary.aiAnalysisStatus === 'PENDING'" 
                type="warning" 
                size="small"
                effect="plain"
              >
                <i class="fas fa-clock"></i>
                分析中
              </el-tag>
              <el-tag 
                v-else-if="diary.aiAnalysisStatus === 'COMPLETED'" 
                type="success" 
                size="small"
                effect="plain"
              >
                <i class="fas fa-check-circle"></i>
                已完成
              </el-tag>
              <el-tag 
                v-else-if="diary.aiAnalysisStatus === 'FAILED'" 
                type="danger" 
                size="small"
                effect="plain"
              >
                <i class="fas fa-exclamation-circle"></i>
                分析失败
              </el-tag>
              <el-tag 
                v-else 
                type="info" 
                size="small"
                effect="plain"
              >
                <i class="fas fa-minus-circle"></i>
                未分析
              </el-tag>
            </div>
            <div class="history-actions">
              <el-button size="small" @click="viewDiaryDetail(diary.id)">查看详情</el-button>
              <el-button size="small" @click="editDiary(diary)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteDiary(diary.id)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          :current-page="historyPage.current"
          :page-size="historyPage.size"
          :total="historyPage.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="(page) => { historyPage.current = page; loadHistoryData(); }"
          @size-change="(size) => { historyPage.size = size; loadHistoryData(); }"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
import {
  createOrUpdateEmotionDiary,
  getTodayEmotionDiary,
  getEmotionDiaryPage,
  getEmotionDiaryStatistics,
  deleteEmotionDiary,
  updateEmotionDiary,
  getAiEmotionAnalysis,
  triggerAiEmotionAnalysis
} from '@/api/emotionDiary'

// 工具函数 - 格式化本地日期，避免时区问题
const formatLocalDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

// 响应式数据
const diaryForm = reactive({
  diaryDate: formatLocalDate(new Date()),
  moodScore: null,
  dominantEmotion: '',
  emotionTriggers: '',
  diaryContent: '',
  sleepQuality: null,
  stressLevel: null
})

const saving = ref(false)
const showHistoryDialog = ref(false)
const trendChart = ref(null)
const trendChartContainer = ref(null)
const currentDiaryId = ref(null)
const aiAnalysis = ref(null)
const aiAnalysisLoading = ref(false)

// 统计数据
const statistics = reactive({
  totalDays: 0,
  recordedDays: 0,
  averageMoodScore: 0,
  suggestions: []
})

const emotionStats = ref([])

// 历史记录相关
const historyData = ref([])
const historyDateRange = ref([])
const historyEmotionFilter = ref('')
const historyPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 情绪选项
const emotionOptions = [
  { name: '开心', icon: 'fas fa-laugh', color: '#FBBF24' },
  { name: '平静', icon: 'fas fa-smile', color: '#60A5FA' },
  { name: '焦虑', icon: 'fas fa-frown', color: '#EF4444' },
  { name: '悲伤', icon: 'fas fa-sad-tear', color: '#6B7280' },
  { name: '兴奋', icon: 'fas fa-grin-beam', color: '#10B981' },
  { name: '疲惫', icon: 'fas fa-tired', color: '#8B5CF6' },
  { name: '惊讶', icon: 'fas fa-surprise', color: '#F59E0B' },
  { name: '困惑', icon: 'fas fa-meh', color: '#64748B' }
]

// 方法
const selectMoodScore = (score) => {
  diaryForm.moodScore = score
}

const selectEmotion = (emotion) => {
  diaryForm.dominantEmotion = emotion
}

const getMoodDescription = (score) => {
  const descriptions = {
    1: '非常糟糕', 2: '糟糕', 3: '不好', 4: '略差', 5: '一般',
    6: '还可以', 7: '不错', 8: '很好', 9: '非常好', 10: '极好'
  }
  return descriptions[score] || '未知'
}

const saveDiary = async () => {
  if (!diaryForm.moodScore) {
    ElMessage.warning('请选择情绪评分')
    return
  }

  saving.value = true
  
  // 如果是编辑模式，使用update API
  if (currentDiaryId.value) {
    await updateEmotionDiary(currentDiaryId.value, diaryForm, {
      onSuccess: (data) => {
        ElMessage.success('情绪日记更新成功！')
        currentDiaryId.value = data.id
        
        // 更新后AI分析状态已重置，清空当前分析数据
        aiAnalysis.value = null
        
        loadStatistics()
        
        // AI分析已自动加入队列，启动轮询检查结果
        ElMessage.info('情绪日记已更新，AI分析正在后台进行中...')
        startAiAnalysisPolling()
      },
      onError: (error) => {
        ElMessage.error('更新失败：' + error.message)
      }
    })
  } else {
    // 新建模式，使用create API
    await createOrUpdateEmotionDiary(diaryForm, {
      onSuccess: (data) => {
        ElMessage.success('情绪日记保存成功！')
        currentDiaryId.value = data.id // 保存当前日记ID
        loadStatistics()
        // AI分析已自动加入队列，启动轮询检查结果
        ElMessage.info('AI情绪分析正在后台进行中...')
        startAiAnalysisPolling()
      },
      onError: (error) => {
        console.log('Error details:', error)
        // 检查是否是重复发布错误
        if (error.code === 'DUPLICATE_DIARY_EXISTS' && error.data) {
          const existingDiaryId = error.data.id || error.data
          handleDuplicateDiary(existingDiaryId)
        } else {
          ElMessage.error('保存失败：' + error.message)
        }
      }
    })
  }

  saving.value = false
}

const resetForm = () => {
  Object.assign(diaryForm, {
    diaryDate: formatLocalDate(new Date()),
    moodScore: null,
    dominantEmotion: '',
    emotionTriggers: '',
    diaryContent: '',
    sleepQuality: null,
    stressLevel: null
  })
}

const loadTodayDiary = async () => {

  await getTodayEmotionDiary({
    showDefaultMsg: false,
    onSuccess: (data) => {
      Object.assign(diaryForm, {
        diaryDate: data.diaryDate,
        moodScore: data.moodScore,
        dominantEmotion: data.dominantEmotion || '',
        emotionTriggers: data.emotionTriggers || '',
        diaryContent: data.diaryContent || '',
        sleepQuality: data.sleepQuality,
        stressLevel: data.stressLevel
      })
      currentDiaryId.value = data.id // 保存当前日记ID
      // 加载AI分析结果
      loadAiAnalysis()
    },
    onError: (error) => {
      if (error.message !== '今日还没有记录情绪日记') {
        console.log('加载今日日记失败:', error)
      }
    }
  })
}

const loadStatistics = async () => {
  await getEmotionDiaryStatistics({ days: 7 }, {
    onSuccess: (data) => {
      Object.assign(statistics, data)
      
      // 更新情绪分布数据
      if (data.emotionDistribution && Object.keys(data.emotionDistribution).length > 0) {
        const total = Object.values(data.emotionDistribution).reduce((sum, count) => sum + count, 0)
        if (total > 0) {
          emotionStats.value = Object.entries(data.emotionDistribution).map(([emotion, count]) => ({
            emotion,
            percentage: Math.round((count / total) * 100),
            color: getEmotionColor(emotion)
          }))
        } else {
          // 如果总数为0，显示空状态
          emotionStats.value = []
        }
      } else {
        // 如果没有情绪分布数据，显示空状态
        emotionStats.value = []
      }

      // 渲染趋势图
      renderTrendChart(data.moodTrend || [])
    },
    onError: (error) => {
      console.error('加载统计数据失败:', error)
      // 错误时也显示空状态
      emotionStats.value = []
    }
  })
}

const loadHistoryData = async () => {
  const params = {
    current: historyPage.current,
    size: historyPage.size
  }
  
  if (historyDateRange.value && historyDateRange.value.length === 2) {
    params.startDate = formatLocalDate(historyDateRange.value[0])
    params.endDate = formatLocalDate(historyDateRange.value[1])
  }
  
  if (historyEmotionFilter.value) {
    params.dominantEmotion = historyEmotionFilter.value
  }

  await getEmotionDiaryPage(params, {
    onSuccess: (data) => {
      historyData.value = data.records || []
      historyPage.total = data.total || 0
    },
    onError: (error) => {
      console.error('加载历史数据失败:', error)
    }
  })
}

const editDiary = (diary) => {
  // 填充表单数据
  Object.assign(diaryForm, {
    diaryDate: diary.diaryDate,
    moodScore: diary.moodScore,
    dominantEmotion: diary.dominantEmotion || '',
    emotionTriggers: diary.emotionTriggers || '',
    diaryContent: diary.diaryContent || '',
    sleepQuality: diary.sleepQuality || null,
    stressLevel: diary.stressLevel || null
  })
  
  currentDiaryId.value = diary.id
  showHistoryDialog.value = false
  ElMessage.success('已切换到编辑模式，请修改后保存')
}

const deleteDiary = async (diaryId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条日记记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteEmotionDiary(diaryId, {
      onSuccess: () => {
        ElMessage.success('删除成功')
        loadHistoryData()
        loadStatistics()
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 处理重复发布情况
const handleDuplicateDiary = async (existingDiaryId) => {
  try {
    const action = await ElMessageBox.confirm(
      '当天已存在情绪日记记录，是否要编辑现有记录？', 
      '重复发布提醒', 
      {
        confirmButtonText: '编辑现有记录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (action === 'confirm') {
      // 加载现有记录到编辑表单
      await getEmotionDiaryById(existingDiaryId, {
        onSuccess: (diary) => {
          editDiary(diary)
        },
        onError: (error) => {
          ElMessage.error('加载记录失败：' + error.message)
        }
      })
    }
  } catch (error) {
    // 用户取消
    console.log('用户取消编辑')
  }
}

// 查看日记详情
const viewDiaryDetail = (diaryId) => {
  // 使用 Vue Router 导航到详情页面
  router.push({ name: 'EmotionDiaryDetail', params: { id: diaryId } })
}

const getEmotionColor = (emotion) => {
  const emotionOption = emotionOptions.find(opt => opt.name === emotion)
  return emotionOption ? emotionOption.color : '#9CA3AF'
}

const renderTrendChart = (trendData) => {
  nextTick(() => {
    if (!trendChart.value || !trendData.length) return

    const ctx = trendChart.value.getContext('2d')
    const canvas = trendChart.value
    const container = trendChartContainer.value
    
    // 设置canvas尺寸
    canvas.width = container.clientWidth
    canvas.height = 200

    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 绘制趋势线
    if (trendData.length > 1) {
      const padding = 40
      const width = canvas.width - padding * 2
      const height = canvas.height - padding * 2
      
      ctx.strokeStyle = '#7ED321'
      ctx.lineWidth = 3
      ctx.beginPath()
      
      trendData.forEach((point, index) => {
        const x = padding + (index / (trendData.length - 1)) * width
        const y = padding + height - (point.moodScore / 10) * height
        
        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })
      
      ctx.stroke()
      
      // 绘制数据点
      trendData.forEach((point, index) => {
        const x = padding + (index / (trendData.length - 1)) * width
        const y = padding + height - (point.moodScore / 10) * height
        
        ctx.fillStyle = '#7ED321'
        ctx.beginPath()
        ctx.arc(x, y, 6, 0, Math.PI * 2)
        ctx.fill()
      })
    }
  })
}

// 加载AI分析结果
const loadAiAnalysis = async () => {
  if (!currentDiaryId.value) {
    return
  }
  
  aiAnalysisLoading.value = true
  await getAiEmotionAnalysis(currentDiaryId.value, {
    onSuccess: (data) => {
      aiAnalysis.value = data
      aiAnalysisLoading.value = false
    },
    onError: (error) => {
      // 如果是尚未生成的错误，不显示错误消息
      if (!error.message.includes('尚未生成')) {
        console.error('加载AI分析结果失败:', error)
      }
      aiAnalysis.value = null
      aiAnalysisLoading.value = false
    }
  })
  
}

// 刷新今日AI分析状态
const refreshTodayAnalysisStatus = async () => {
  if (!currentDiaryId.value) {
    return
  }
  
  aiAnalysisLoading.value = true
  
  try {
    // 重新加载今日日记状态
    await getTodayEmotionDiary({
      onSuccess: (data) => {
        // 更新当前日记的部分信息（主要是AI分析状态相关字段）
        if (data.id === currentDiaryId.value) {
          // 这里我们只更新AI相关的状态，不影响表单数据
          // 可以考虑添加一个状态变量来跟踪AI分析状态
        }
      },
      onError: (error) => {
        console.error('刷新今日日记状态失败:', error)
      }
    })
    
    // 尝试加载AI分析结果
    await getAiEmotionAnalysis(currentDiaryId.value, {
      onSuccess: (data) => {
        aiAnalysis.value = data
        ElMessage.success('AI分析结果已更新')
      },
      onError: (error) => {
        if (!error.message.includes('尚未生成')) {
          console.error('加载AI分析结果失败:', error)
          ElMessage.info('AI分析尚未完成，请稍后再试')
        } else {
          ElMessage.info('AI分析仍在进行中，请稍后再试')
        }
        aiAnalysis.value = null
      }
    })
    
  } catch (error) {
    console.error('刷新AI分析状态失败:', error)
    ElMessage.error('刷新失败，请重试')
  }
  
  aiAnalysisLoading.value = false
}

// 触发AI分析
const triggerAnalysis = async () => {
  if (!currentDiaryId.value) {
    ElMessage.warning('请先保存日记')
    return
  }
  
  await triggerAiEmotionAnalysis(currentDiaryId.value, {
    onSuccess: () => {
      // 启动轮询检查分析结果
      startAiAnalysisPolling()
    }
  })
}

// AI分析轮询相关
let pollingTimer = null
const maxPollingAttempts = 20 // 最多轮询20次（10分钟）
let pollingAttempts = 0

// 启动AI分析结果轮询
const startAiAnalysisPolling = () => {
  if (!currentDiaryId.value) return
  
  pollingAttempts = 0
  clearPollingTimer()
  
  pollingTimer = setInterval(async () => {
    pollingAttempts++
    
    try {
      // 使用统一的刷新状态方法
      await refreshTodayAnalysisStatus()
      
      // 如果获取到分析结果，停止轮询
      if (aiAnalysis.value) {
        clearPollingTimer()
        ElMessage.success('AI情绪分析完成！')
        return
      }
      
      // 超过最大尝试次数，停止轮询
      if (pollingAttempts >= maxPollingAttempts) {
        clearPollingTimer()
        ElMessage.warning('AI分析耗时较长，请稍后手动刷新查看结果')
        return
      }
    } catch (error) {
      console.error('轮询AI分析结果失败:', error)
      
      // 如果连续失败多次，停止轮询
      if (pollingAttempts >= maxPollingAttempts) {
        clearPollingTimer()
      }
    }
  }, 30000) // 每30秒轮询一次
}

// 清除轮询定时器
const clearPollingTimer = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 获取风险等级样式类
const getRiskLevelClass = (riskLevel) => {
  const classes = {
    0: 'risk-low',
    1: 'risk-low',
    2: 'risk-medium',
    3: 'risk-high'
  }
  return classes[riskLevel] || 'risk-low'
}

// 获取风险等级文本
const getRiskLevelText = (riskLevel) => {
  const texts = {
    0: '正常',
    1: '关注',
    2: '预警',
    3: '危机'
  }
  return texts[riskLevel] || '未知'
}

// 监听历史记录对话框打开
watch(showHistoryDialog, (newValue) => {
  if (newValue) {
    // 对话框打开时加载历史数据
    loadHistoryData()
  }
})

// 组件卸载时清理轮询
onUnmounted(() => {
  clearPollingTimer()
})

// 组件挂载
onMounted(() => {
  loadTodayDiary()
  loadStatistics()
})
</script>

<style scoped>
.emotion-diary-page {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
}

.header-section {
  background: linear-gradient(135deg, #7ED321 0%, #F5A623 100%);
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
  margin: 0 0 0.5rem 0;
  font-weight: bold;
}

.header-text p {
  font-size: 1.25rem;
  margin: 0;
  opacity: 0.9;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.content-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.diary-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.diary-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.diary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(126, 211, 33, 0.15);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-title i {
  color: #7ED321;
}

.mood-score-section .mood-label {
  font-size: 1rem;
  color: #6B7280;
  margin-bottom: 1.5rem;
}

.mood-selectors {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.mood-range-label {
  font-size: 0.875rem;
  color: #9CA3AF;
  min-width: 4rem;
}

.mood-buttons {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  justify-content: center;
}

.mood-selector {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #EF4444, #F59E0B, #FBBF24, #84CC16, #22C55E);
  background-size: 1000% 100%;
}

.mood-selector:nth-child(1) { background-position: 0% 0%; }
.mood-selector:nth-child(2) { background-position: 11% 0%; }
.mood-selector:nth-child(3) { background-position: 22% 0%; }
.mood-selector:nth-child(4) { background-position: 33% 0%; }
.mood-selector:nth-child(5) { background-position: 44% 0%; }
.mood-selector:nth-child(6) { background-position: 55% 0%; }
.mood-selector:nth-child(7) { background-position: 66% 0%; }
.mood-selector:nth-child(8) { background-position: 77% 0%; }
.mood-selector:nth-child(9) { background-position: 88% 0%; }
.mood-selector:nth-child(10) { background-position: 100% 0%; }

.mood-selector.selected {
  transform: scale(1.2);
  box-shadow: 0 0 20px rgba(74, 144, 226, 0.5);
}

.selected-mood {
  text-align: center;
  margin-top: 1rem;
}

.current-selection {
  font-size: 1.125rem;
  font-weight: 600;
  color: #7ED321;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.emotion-card {
  padding: 1.5rem;
  border: 2px solid #E5E7EB;
  border-radius: 0.75rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #F9FAFB;
}

.emotion-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(126, 211, 33, 0.2);
}

.emotion-card.selected {
  border-color: #7ED321;
  background: #F0FDF4;
  transform: translateY(-3px);
}

.emotion-card i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.emotion-name {
  font-weight: 500;
  color: #374151;
}

.detail-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.life-indicators {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.indicator-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.statistics-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stat-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.stat-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-title i {
  color: #7ED321;
}

.trend-chart {
  width: 100%;
  height: 200px;
}

.emotion-stats {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-color {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
}

.stat-label {
  flex: 1;
  font-size: 0.875rem;
  color: #6B7280;
}

.stat-value {
  font-weight: 600;
  color: #111827;
}

.no-emotion-data {
  text-align: center;
  padding: 2rem 1rem;
  color: #9CA3AF;
}

.no-emotion-data i {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #D1D5DB;
}

.no-emotion-data p {
  margin: 0.5rem 0;
  font-size: 0.875rem;
}

.no-emotion-data .tip {
  font-size: 0.75rem;
  color: #6B7280;
}

.analysis-card {
  background: linear-gradient(135deg, #EBF8FF 0%, #F3E8FF 100%);
  border: 1px solid #BFDBFE;
}

.analysis-content {
  color: #3B82F6;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5;
}

.suggestion-item i {
  color: #FBBF24;
  font-size: 0.5rem;
  margin-top: 0.5rem;
}

.no-analysis {
  font-style: italic;
  color: #6B7280;
}

.quick-actions {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 0.35rem;
}

.action-btn {
  justify-content: flex-start;
  border: none;
  background: #F9FAFB;
  color: #374151;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #F3F4F6;
  color: #7ED321;
}

.tip-card {
  background: #FFFBEB;
  border: 1px solid #FDE68A;
}

.tip-content {
  font-size: 0.875rem;
  color: #92400E;
  line-height: 1.6;
  margin: 0;
}

/* AI情绪分析卡片样式 */
.ai-emotion-card {
  background: linear-gradient(135deg, #F0F9FF 0%, #E0E7FF 100%);
  border: 1px solid #C7D2FE;
}

.ai-analysis-content {
  margin-top: 1rem;
}

.analysis-result {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.emotion-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 0.5rem;
}

.emotion-icon {
  font-size: 1.5rem;
}

.emotion-label {
  font-weight: 600;
  color: #374151;
}

.emotion-score {
  margin-left: auto;
  font-weight: bold;
  color: #7C3AED;
}

.risk-level {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-align: center;
}

.risk-low {
  background-color: #D1FAE5;
  color: #065F46;
}

.risk-medium {
  background-color: #FEF3C7;
  color: #92400E;
}

.risk-high {
  background-color: #FEE2E2;
  color: #991B1B;
}

.ai-suggestion h5,
.improvement-suggestions h5 {
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
}

.ai-suggestion p {
  margin: 0;
  font-size: 0.75rem;
  color: #6B7280;
  line-height: 1.4;
}

.improvement-suggestions ul {
  margin: 0;
  padding-left: 1rem;
}

.improvement-suggestions li {
  font-size: 0.75rem;
  color: #6B7280;
  line-height: 1.4;
  margin-bottom: 0.25rem;
}

.loading-analysis {
  text-align: center;
  color: #6B7280;
  font-size: 0.875rem;
}

.loading-analysis i {
  margin-right: 0.5rem;
  color: #7C3AED;
}

.no-ai-analysis {
  text-align: center;
  color: #9CA3AF;
}

.no-ai-analysis p {
  margin: 0 0 0.75rem 0;
  font-size: 0.875rem;
}

.history-content {
  max-height: 60vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.history-filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #E5E7EB;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 1rem;
}

.history-item {
  display: grid;
  grid-template-columns: auto auto 1fr auto auto;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  border: 1px solid #E5E7EB;
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.history-item:hover {
  background: #F9FAFB;
  border-color: #7ED321;
}

.history-date {
  font-weight: 600;
  color: #374151;
}

.history-mood {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.mood-score {
  font-weight: bold;
  color: #7ED321;
}

.mood-emotion {
  font-size: 0.75rem;
  color: #6B7280;
}

.history-content-preview {
  font-size: 0.875rem;
  color: #6B7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-analysis-status {
  display: flex;
  justify-content: center;
  align-items: center;
}

.ai-analysis-status .el-tag {
  font-size: 0.75rem;
}

.ai-analysis-status .el-tag i {
  margin-right: 0.25rem;
}

.history-actions {
  display: flex;
  gap: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .emotion-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .mood-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .life-indicators {
    grid-template-columns: 1fr;
  }
}
</style>
