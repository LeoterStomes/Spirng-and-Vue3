<template>
  <div class="diary-detail-page">
    <!-- 页面头部 -->
    <section class="page-header">
      <div class="header-container">
        <div class="header-content">
          <div class="header-left">
            <div class="breathing-animation">
              <i class="fas fa-heart"></i>
            </div>
            <div class="header-text">
              <h2>情绪日记详情</h2>
              <p>{{ diaryData ? formatDateTime(diaryData.diaryDate) : '' }} - 深入了解您的情绪状态</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button @click="goBack" class="back-btn">
              <i class="fas fa-arrow-left"></i>
              返回列表
            </el-button>
            <el-button type="primary" @click="enterEditMode" v-if="!isEditMode && diaryData">
              <i class="fas fa-edit"></i>
              编辑
            </el-button>
            <el-button type="success" @click="saveDiary" v-if="isEditMode" :loading="saving">
              <i class="fas fa-save"></i>
              保存
            </el-button>
            <el-button @click="cancelEdit" v-if="isEditMode">
              <i class="fas fa-times"></i>
              取消
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
    <div v-else-if="diaryData" class="detail-content">
      <div class="content-container">
        <div class="content-layout">
          <!-- 左侧：日记详情 -->
          <div class="diary-detail-section">
          <!-- 情绪与生活指标综合卡片 -->
          <div class="detail-card emotion-summary-card">
            <h3 class="card-title">
              <i class="fas fa-chart-bar"></i>
              情绪与生活指标
            </h3>
            
            <!-- 情绪评分区域 -->
            <div class="emotion-section">
              <h4 class="section-title">今日情绪评分</h4>
              <div class="mood-score-display">
                <div class="score-circle" :class="getMoodClass(diaryData.moodScore)">
                  {{ diaryData.moodScore }}
                </div>
                <div class="score-info">
                  <div class="score-text">{{ getMoodDescription(diaryData.moodScore) }}</div>
                  <div class="score-range">评分范围：1-10分</div>
                </div>
              </div>
              
              <!-- 编辑模式下的评分选择器 -->
              <div v-if="isEditMode" class="mood-edit-section">
                <div class="mood-selectors">
                  <span class="mood-range-label">很糟糕</span>
                  <div class="mood-buttons">
                    <div
                      v-for="score in 10"
                      :key="score"
                      class="mood-selector"
                      :class="{ selected: editForm.moodScore === score }"
                      @click="editForm.moodScore = score"
                    >
                      {{ score }}
                    </div>
                  </div>
                  <span class="mood-range-label">非常好</span>
                </div>
              </div>
            </div>

            <!-- 主要情绪区域 -->
            <div class="emotion-section">
              <h4 class="section-title">主要情绪</h4>
              <div class="emotion-display" v-if="!isEditMode">
                <div class="emotion-tag" :style="{ color: getEmotionColor(diaryData.dominantEmotion) }">
                  <i :class="getEmotionIcon(diaryData.dominantEmotion)"></i>
                  {{ diaryData.dominantEmotion || '未记录' }}
                </div>
              </div>
              <div v-else class="emotion-edit">
                <div class="emotion-grid">
                  <div
                    v-for="emotion in emotionOptions"
                    :key="emotion.name"
                    class="emotion-card"
                    :class="{ selected: editForm.dominantEmotion === emotion.name }"
                    @click="editForm.dominantEmotion = emotion.name"
                  >
                    <i :class="emotion.icon" :style="{ color: emotion.color }"></i>
                    <div class="emotion-name">{{ emotion.name }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 生活指标区域 -->
            <div class="emotion-section">
              <h4 class="section-title">生活指标</h4>
              <div class="metrics-grid">
                <div class="metric-item">
                  <div class="metric-label">睡眠质量</div>
                  <div v-if="!isEditMode" class="metric-value">
                    <div class="quality-display" :class="getQualityClass(diaryData.sleepQuality)">
                      {{ getSleepQualityText(diaryData.sleepQuality) }}
                    </div>
                  </div>
                  <el-rate 
                    v-else
                    v-model="editForm.sleepQuality"
                    :max="5"
                    show-text
                    :texts="['很差', '较差', '一般', '良好', '很好']"
                  />
                </div>
                
                <div class="metric-item">
                  <div class="metric-label">压力水平</div>
                  <div v-if="!isEditMode" class="metric-value">
                    <div class="stress-display" :class="getStressClass(diaryData.stressLevel)">
                      {{ getStressLevelText(diaryData.stressLevel) }}
                    </div>
                  </div>
                  <el-rate 
                    v-else
                    v-model="editForm.stressLevel"
                    :max="5"
                    show-text
                    :texts="['很低', '较低', '中等', '较高', '很高']"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 基本信息卡片 -->
          <div class="detail-card">
            <h3 class="card-title">
              <i class="fas fa-calendar-day"></i>
              基本信息
            </h3>
            <div class="basic-info">
              <div class="info-row">
                <span class="info-label">记录日期：</span>
                <span class="info-value">{{ diaryData.diaryDate }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">创建时间：</span>
                <span class="info-value">{{ formatDateTime(diaryData.createdAt) }}</span>
              </div>
              <div class="info-row" v-if="diaryData.updatedAt !== diaryData.createdAt">
                <span class="info-label">更新时间：</span>
                <span class="info-value">{{ formatDateTime(diaryData.updatedAt) }}</span>
              </div>
            </div>
          </div>

          <!-- 详细内容 -->
          <div class="detail-card">
            <h3 class="card-title">
              <i class="fas fa-edit"></i>
              详细记录
            </h3>
            
            <!-- 情绪触发因素 -->
            <div class="content-section">
              <h4 class="section-title">情绪触发因素</h4>
              <div v-if="!isEditMode" class="content-text">
                {{ diaryData.emotionTriggers || '暂无记录' }}
              </div>
              <el-input
                v-else
                v-model="editForm.emotionTriggers"
                type="textarea"
                :rows="3"
                placeholder="今天什么事情影响了您的情绪？"
                maxlength="1000"
                show-word-limit
              />
            </div>

            <!-- 今日感想 -->
            <div class="content-section">
              <h4 class="section-title">今日感想</h4>
              <div v-if="!isEditMode" class="content-text diary-content">
                <div v-if="diaryData.diaryContent" v-html="formatDiaryContent(diaryData.diaryContent)"></div>
                <div v-else class="no-content">暂无记录</div>
              </div>
              <el-input
                v-else
                v-model="editForm.diaryContent"
                type="textarea"
                :rows="8"
                placeholder="记录您今天的感受、想法和体验..."
                maxlength="2000"
                show-word-limit
              />
            </div>
          </div>

        </div>

        <!-- 右侧：AI分析结果 -->
        <div class="analysis-section">
          <!-- AI情绪分析卡片 -->
          <div class="detail-card ai-analysis-card">
            <h3 class="card-title">
              <i class="fas fa-brain"></i>
              AI情绪分析
              <div class="analysis-status">
                <el-tag 
                  v-if="diaryData.aiAnalysisStatus === 'PENDING'" 
                  type="warning" 
                  effect="light"
                >
                  <i class="fas fa-clock"></i>
                  分析中...
                </el-tag>
                <el-tag 
                  v-else-if="diaryData.aiAnalysisStatus === 'COMPLETED'" 
                  type="success" 
                  effect="light"
                >
                  <i class="fas fa-check-circle"></i>
                  已完成
                </el-tag>
                <el-tag 
                  v-else-if="diaryData.aiAnalysisStatus === 'FAILED'" 
                  type="danger" 
                  effect="light"
                >
                  <i class="fas fa-exclamation-circle"></i>
                  分析失败
                </el-tag>
              </div>
            </h3>

            <div class="ai-analysis-content">
              <!-- 加载中状态 -->
              <div v-if="aiAnalysisLoading" class="analysis-loading">
                <el-skeleton :rows="3" animated />
              </div>

              <!-- AI分析结果 -->
              <div v-else-if="aiAnalysis && diaryData.aiAnalysisStatus === 'COMPLETED'" class="analysis-result">
                <!-- 情绪识别 -->
                <div class="analysis-section-item">
                  <h4 class="analysis-title">情绪识别</h4>
                  <div class="emotion-analysis">
                    <div class="primary-emotion">
                      <span class="emotion-label">主要情绪：</span>
                      <div class="emotion-value-wrapper">
                        <i :class="getEmotionIcon(aiAnalysis.primaryEmotion)" 
                           :style="{ color: getEmotionColor(aiAnalysis.primaryEmotion) }"
                           class="emotion-icon"></i>
                        <span class="emotion-value">{{ aiAnalysis.primaryEmotion }}</span>
                      </div>
                    </div>
                    <div class="emotion-intensity">
                      <span class="emotion-label">情绪强度：</span>
                      <el-progress 
                        :percentage="aiAnalysis.emotionScore" 
                        :color="getIntensityColor(aiAnalysis.emotionScore)"
                        :show-text="false"
                      />
                      <span class="intensity-text">{{ aiAnalysis.emotionScore }}/100</span>
                    </div>
                  </div>
                </div>

                <!-- 风险评估 -->
                <div class="analysis-section-item">
                  <h4 class="analysis-title">风险评估</h4>
                  <div class="risk-assessment">
                    <el-tag 
                      :type="getRiskTagType(aiAnalysis.riskLevel)" 
                      class="risk-tag"
                    >
                      {{ getRiskLevelText(aiAnalysis.riskLevel) }}
                    </el-tag>
                    <div class="risk-description" v-if="aiAnalysis.riskDescription">
                      {{ aiAnalysis.riskDescription }}
                    </div>
                  </div>
                </div>

                <!-- AI建议 -->
                <div class="analysis-section-item" v-if="aiAnalysis.suggestion">
                  <h4 class="analysis-title">AI建议</h4>
                  <div class="suggestions-list">
                    <div class="suggestion-item">
                      <i class="fas fa-lightbulb"></i>
                      {{ aiAnalysis.suggestion }}
                    </div>
                  </div>
                </div>

                <!-- 改善建议 -->
                <div class="analysis-section-item" v-if="aiAnalysis.improvementSuggestions">
                  <h4 class="analysis-title">改善建议</h4>
                  <div class="improvement-suggestions">
                    <div 
                      v-for="(suggestion, index) in aiAnalysis.improvementSuggestions" 
                      :key="index"
                      class="improvement-item"
                    >
                      <i class="fas fa-arrow-up"></i>
                      {{ suggestion }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分析失败状态 -->
              <div v-else-if="diaryData.aiAnalysisStatus === 'FAILED'" class="analysis-failed">
                <div class="failed-content">
                  <i class="fas fa-exclamation-triangle"></i>
                  <p>AI分析失败，请稍后重试</p>
                  <el-button type="primary" @click="triggerAnalysis">
                    重新分析
                  </el-button>
                </div>
              </div>

              <!-- 分析中状态 -->
              <div v-else-if="diaryData.aiAnalysisStatus === 'PENDING'" class="analysis-pending">
                <div class="pending-content">
                  <i class="fas fa-clock"></i>
                  <p>AI正在分析您的情绪日记...</p>
                  <p class="pending-tip">通常需要1-2分钟，请稍候</p>
                  <el-button @click="refreshAnalysisStatus" :loading="aiAnalysisLoading">
                    刷新状态
                  </el-button>
                </div>
              </div>

              <!-- 无分析数据 -->
              <div v-else class="no-analysis">
                <div class="no-analysis-content">
                  <i class="fas fa-robot"></i>
                  <p>暂无AI分析数据</p>
                  <el-button type="primary" @click="triggerAnalysis">
                    开始分析
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作提示卡片 -->
          <div class="detail-card tips-card">
            <h3 class="card-title">
              <i class="fas fa-info-circle"></i>
              温馨提示
            </h3>
            <div class="tips-content">
              <div class="tip-item">
                <i class="fas fa-heart"></i>
                记录情绪有助于提高自我觉察能力
              </div>
              <div class="tip-item">
                <i class="fas fa-chart-line"></i>
                定期回顾有助于发现情绪规律
              </div>
              <div class="tip-item">
                <i class="fas fa-user-friends"></i>
                如需专业帮助，请及时寻求支持
              </div>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <el-empty description="日记不存在或已被删除">
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getEmotionDiaryById, 
  updateEmotionDiary,
  getAiEmotionAnalysis,
  triggerAiEmotionAnalysis 
} from '@/api/emotionDiary'
import { formatDateTime } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()

// 响应式数据
const diaryData = ref(null)
const loading = ref(true)
const saving = ref(false)
const isEditMode = ref(false)
const aiAnalysis = ref(null)
const aiAnalysisLoading = ref(false)

// 编辑表单
const editForm = reactive({
  moodScore: null,
  dominantEmotion: '',
  emotionTriggers: '',
  diaryContent: '',
  sleepQuality: null,
  stressLevel: null
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

// 计算属性
const diaryId = computed(() => route.params.id)

// 方法
const loadDiaryDetail = async () => {
  loading.value = true
  await getEmotionDiaryById(diaryId.value, {
    onSuccess: (data) => {
      diaryData.value = data
      // 加载AI分析结果
      if (data.aiAnalysisStatus === 'COMPLETED') {
        loadAiAnalysis()
      }
    },
    onError: (error) => {
      ElMessage.error('加载日记详情失败：' + error.message)
    }
  })
  loading.value = false
}

const loadAiAnalysis = async () => {
  if (!diaryId.value) return
  
  aiAnalysisLoading.value = true
  await getAiEmotionAnalysis(diaryId.value, {
    onSuccess: (data) => {
      aiAnalysis.value = data
    },
    onError: (error) => {
      console.error('加载AI分析失败:', error)
      aiAnalysis.value = null
    }
  })
  aiAnalysisLoading.value = false
}

// 刷新分析状态（同时刷新日记状态和AI分析结果）
const refreshAnalysisStatus = async () => {
  if (!diaryId.value) return
  
  aiAnalysisLoading.value = true
  
  try {
    // 先刷新日记基本信息（包含aiAnalysisStatus）
    await getEmotionDiaryById(diaryId.value, {
      onSuccess: (data) => {
        diaryData.value = data
      },
      onError: (error) => {
        console.error('刷新日记状态失败:', error)
      }
    })
    
    // 如果状态为已完成，则加载AI分析结果
    if (diaryData.value && diaryData.value.aiAnalysisStatus === 'COMPLETED') {
      await getAiEmotionAnalysis(diaryId.value, {
        onSuccess: (data) => {
          aiAnalysis.value = data
          ElMessage.success('AI分析结果已更新')
        },
        onError: (error) => {
          console.error('加载AI分析结果失败:', error)
          aiAnalysis.value = null
        }
      })
    } else {
      // 状态不是已完成，清空分析结果
      aiAnalysis.value = null
      
      if (diaryData.value?.aiAnalysisStatus === 'PENDING') {
        ElMessage.info('AI分析仍在进行中，请稍后再试')
      } else if (diaryData.value?.aiAnalysisStatus === 'FAILED') {
        ElMessage.warning('AI分析失败，您可以重新触发分析')
      }
    }
    
  } catch (error) {
    console.error('刷新状态失败:', error)
    ElMessage.error('刷新失败，请重试')
  }
  
  aiAnalysisLoading.value = false
}

const triggerAnalysis = async () => {
  await triggerAiEmotionAnalysis(diaryId.value, {
    onSuccess: () => {
      ElMessage.success('AI分析已开始，请稍候...')
      // 更新状态为分析中
      diaryData.value.aiAnalysisStatus = 'PENDING'
      // 启动轮询检查分析结果
      startAiAnalysisPolling()
    }
  })
}

const enterEditMode = () => {
  isEditMode.value = true
  // 复制数据到编辑表单
  Object.assign(editForm, {
    moodScore: diaryData.value.moodScore,
    dominantEmotion: diaryData.value.dominantEmotion,
    emotionTriggers: diaryData.value.emotionTriggers,
    diaryContent: diaryData.value.diaryContent,
    sleepQuality: diaryData.value.sleepQuality,
    stressLevel: diaryData.value.stressLevel
  })
}

const cancelEdit = () => {
  isEditMode.value = false
  // 清空编辑表单
  Object.assign(editForm, {
    moodScore: null,
    dominantEmotion: '',
    emotionTriggers: '',
    diaryContent: '',
    sleepQuality: null,
    stressLevel: null
  })
}

const saveDiary = async () => {
  if (!editForm.moodScore) {
    ElMessage.warning('请选择情绪评分')
    return
  }

  saving.value = true
  
  await updateEmotionDiary(diaryId.value, editForm, {
    onSuccess: (data) => {
      ElMessage.success('日记更新成功！')
      diaryData.value = data
      isEditMode.value = false
      
      // 更新后AI分析状态已重置，清空当前的AI分析数据
      aiAnalysis.value = null
      
      // AI分析已自动加入队列，启动轮询检查结果
      ElMessage.info('情绪日记已更新，AI分析正在后台进行中...')
      startAiAnalysisPolling()
    },
    onError: (error) => {
      ElMessage.error('更新失败：' + error.message)
    }
  })
  
  saving.value = false
}

const goBack = () => {
  router.go(-1)
}

// AI分析轮询相关
let pollingTimer = null
const maxPollingAttempts = 20 // 最多轮询20次（10分钟）
let pollingAttempts = 0

// 启动AI分析结果轮询
const startAiAnalysisPolling = () => {
  if (!diaryId.value) return
  
  pollingAttempts = 0
  clearPollingTimer()
  
  pollingTimer = setInterval(async () => {
    pollingAttempts++
    
    try {
      // 使用统一的刷新状态方法
      await refreshAnalysisStatus()
      
      // 如果获取到分析结果，停止轮询
      if (aiAnalysis.value && diaryData.value.aiAnalysisStatus === 'COMPLETED') {
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

// 辅助方法
const getMoodDescription = (score) => {
  const descriptions = {
    1: '非常糟糕', 2: '糟糕', 3: '不好', 4: '略差', 5: '一般',
    6: '还可以', 7: '不错', 8: '很好', 9: '非常好', 10: '极好'
  }
  return descriptions[score] || '未知'
}

const getMoodClass = (score) => {
  if (score <= 3) return 'mood-bad'
  if (score <= 5) return 'mood-neutral'
  if (score <= 7) return 'mood-good'
  return 'mood-excellent'
}

const getEmotionIcon = (emotion) => {
  const iconMap = {
    '开心': 'fas fa-laugh',
    '平静': 'fas fa-smile',
    '焦虑': 'fas fa-frown',
    '悲伤': 'fas fa-sad-tear',
    '兴奋': 'fas fa-grin-beam',
    '疲惫': 'fas fa-tired',
    '惊讶': 'fas fa-surprise',
    '困惑': 'fas fa-meh'
  }
  return iconMap[emotion] || 'fas fa-meh'
}

const getEmotionColor = (emotion) => {
  const colorMap = {
    '开心': '#FBBF24',
    '平静': '#60A5FA',
    '焦虑': '#EF4444',
    '悲伤': '#6B7280',
    '兴奋': '#10B981',
    '疲惫': '#8B5CF6',
    '惊讶': '#F59E0B',
    '困惑': '#64748B'
  }
  return colorMap[emotion] || '#64748B'
}

const formatDiaryContent = (content) => {
  return content.replace(/\n/g, '<br>')
}

const getSleepQualityText = (quality) => {
  const texts = ['未评分', '很差', '较差', '一般', '良好', '很好']
  return texts[quality] || '未评分'
}

const getStressLevelText = (level) => {
  const texts = ['未评分', '很低', '较低', '中等', '较高', '很高']
  return texts[level] || '未评分'
}

const getQualityClass = (quality) => {
  if (quality <= 2) return 'quality-bad'
  if (quality <= 3) return 'quality-neutral'
  return 'quality-good'
}

const getStressClass = (level) => {
  if (level >= 4) return 'stress-high'
  if (level >= 3) return 'stress-medium'
  return 'stress-low'
}

const getIntensityColor = (intensity) => {
  if (intensity >= 80) return '#EF4444'
  if (intensity >= 60) return '#F59E0B'
  if (intensity >= 40) return '#FBBF24'
  return '#10B981'
}

const getRiskTagType = (riskLevel) => {
  const typeMap = {
    0: 'success',   // 正常
    1: 'success',   // 关注
    2: 'warning',   // 预警
    3: 'danger'     // 危机
  }
  return typeMap[riskLevel] || 'info'
}

const getRiskLevelText = (riskLevel) => {
  const textMap = {
    0: '正常',
    1: '关注',
    2: '预警',
    3: '危机'
  }
  return textMap[riskLevel] || '未知'
}

// 组件卸载时清理轮询
onUnmounted(() => {
  clearPollingTimer()
})

// 组件挂载
onMounted(() => {
  loadDiaryDetail()
})
</script>

<style scoped>

.diary-detail-page {
  min-height: 100vh;
  background: #f9fafb;
}

/* 页面头部样式 */
.page-header {
  background: #4A90E2;
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

.header-content .breathing-animation {
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

.header-actions .el-button--primary {
  background: white;
  border-color: #D1D5DB;
  color: #4A90E2;
}

.header-actions .el-button--primary:hover {
  background: #F9FAFB;
  border-color: #9CA3AF;
  color: #3B82F6;
}

.header-actions .el-button--success {
  background: #22C55E;
  border-color: #16A34A;
  color: white;
}

.header-actions .el-button--success:hover {
  background: #16A34A;
  border-color: #15803D;
}

.header-actions .el-button:not(.back-btn):not(.el-button--primary):not(.el-button--success) {
  background: #6B7280;
  border-color: #9CA3AF;
  color: white;
}

.header-actions .el-button:not(.back-btn):not(.el-button--primary):not(.el-button--success):hover {
  background: #4B5563;
  border-color: #6B7280;
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

.loading-container, .error-state {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 2rem;
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.diary-detail-section, .analysis-section {
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

/* 情绪综合卡片样式 */
.emotion-summary-card {
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  border: 1px solid #E2E8F0;
}

.emotion-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #E5E7EB;
}

.emotion-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #7ED321;
  border-radius: 2px;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #374151;
}

.card-title i {
  color: #7ED321;
  font-size: 1.25rem;
}

.analysis-status {
  margin-left: auto;
}

.basic-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: #6B7280;
  width: 100px;
}

.info-value {
  color: #374151;
}

.mood-score-display {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.score-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
}

/* 情绪评分样式 */
.mood-bad { background: linear-gradient(135deg, #EF4444, #DC2626); }
.mood-neutral { background: linear-gradient(135deg, #F59E0B, #D97706); }
.mood-good { background: linear-gradient(135deg, #10B981, #059669); }
.mood-excellent { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }

/* 编辑模式的评分选择器 */
.mood-edit-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #E5E7EB;
}

.mood-selectors {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.mood-range-label {
  font-size: 0.875rem;
  color: #6B7280;
  min-width: 4rem;
  text-align: center;
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

.score-info {
  flex: 1;
}

.score-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
}

.score-range {
  font-size: 0.875rem;
  color: #6B7280;
}

.emotion-display {
  display: flex;
  gap: 0.5rem;
}

.emotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(103, 126, 234, 0.1);
  border-radius: 0.5rem;
  font-weight: 500;
}

.emotion-edit .emotion-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.emotion-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 2px solid #E5E7EB;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.emotion-card:hover {
  border-color: #7ED321;
}

.emotion-card.selected {
  border-color: #7ED321;
  background: #F0FDF4;
}

.emotion-card i {
  font-size: 1.5rem;
}

.emotion-name {
  font-size: 0.875rem;
  font-weight: 500;
}

.content-section {
  margin-bottom: 1.5rem;
}

.section-title {
  margin: 0 0 0.75rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.content-text {
  line-height: 1.6;
  color: #4B5563;
}

.diary-content {
  white-space: pre-line;
}

.no-content {
  color: #9CA3AF;
  font-style: italic;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.metric-item {
  text-align: center;
}

.metric-label {
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.quality-display, .stress-display {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-weight: 500;
  display: inline-block;
}

.quality-bad, .stress-high { background: #FEE2E2; color: #DC2626; }
.quality-neutral, .stress-medium { background: #FEF3C7; color: #D97706; }
.quality-good, .stress-low { background: #D1FAE5; color: #059669; }

/* AI分析卡片优化 */
.ai-analysis-card {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
}

.ai-analysis-content {
  margin-top: 1rem;
}

.analysis-loading {
  padding: 1rem;
}

.analysis-result {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.analysis-section-item {
  padding: 1rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 0.5rem;
}

.analysis-title {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.emotion-analysis {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.primary-emotion, .emotion-intensity {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.emotion-value-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: rgba(126, 211, 33, 0.1);
  border-radius: 0.5rem;
  border: 1px solid rgba(126, 211, 33, 0.2);
}

.emotion-icon {
  font-size: 1.25rem;
}

.emotion-label {
  font-weight: 500;
  color: #6B7280;
  min-width: 80px;
}

.emotion-value {
  font-weight: 600;
  color: #374151;
}

.intensity-text {
  margin-left: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.risk-assessment {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.risk-tag {
  font-weight: 500;
}

.risk-score {
  font-size: 0.875rem;
  color: #6B7280;
}

.suggestions-list, .improvement-suggestions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.suggestion-item, .improvement-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 0.25rem;
  font-size: 0.875rem;
  line-height: 1.4;
}

.suggestion-item i, .improvement-item i {
  color: #FBBF24;
  margin-top: 0.1rem;
}

.analysis-failed, .analysis-pending, .no-analysis {
  text-align: center;
  padding: 2rem 1rem;
}

.failed-content, .pending-content, .no-analysis-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.failed-content i, .pending-content i, .no-analysis-content i {
  font-size: 2rem;
  color: #9CA3AF;
}

.pending-tip {
  font-size: 0.875rem;
  color: #6B7280;
  margin: 0;
}

/* 温馨提示卡片 */
.tips-card {
  background: #FFFEF7;
  border: 1px solid #FEF08A;
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
  
  .header-content .breathing-animation {
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
  
  .emotion-edit .emotion-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .mood-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .mood-selector {
    width: 2.5rem;
    height: 2.5rem;
  }
  
  .detail-card {
    padding: 1.5rem;
    border-radius: 1.5rem;
  }
  
  .card-title {
    font-size: 1.25rem;
  }
}
</style>

