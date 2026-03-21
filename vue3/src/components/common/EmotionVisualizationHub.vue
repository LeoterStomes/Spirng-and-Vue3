<template>
  <div class="emotion-visualization-hub">
    <!-- 可视化模式选择器 -->
    <div class="visualization-selector">
      <div class="selector-header">
        <h3 class="selector-title">
          <i class="fas fa-eye"></i>
          情绪可视化中心
        </h3>
        <div class="selector-subtitle">选择最适合的数据展示方式</div>
      </div>
      
      <div class="mode-tabs">
        <div 
          v-for="mode in visualizationModes" 
          :key="mode.key"
          class="mode-tab"
          :class="{ active: currentMode === mode.key }"
          @click="switchMode(mode.key)"
        >
          <i :class="mode.icon"></i>
          <span>{{ mode.name }}</span>
          <div class="mode-description">{{ mode.description }}</div>
        </div>
      </div>
    </div>

    <!-- 可视化内容区域 -->
    <div class="visualization-content">


      <!-- 情绪日历视图 -->
      <div v-if="currentMode === 'calendar'" class="visualization-panel">
        <div class="panel-header">
          <h4><i class="fas fa-calendar-alt"></i> 情绪日历</h4>
          <div class="panel-info">记录每一天的情绪轨迹，发现生活中的美好时光</div>
        </div>
        <div class="calendar-container">
          <!-- 日历控制栏 -->
          <div class="calendar-controls">
            <div class="calendar-header">
              <button @click="previousMonth" class="nav-btn prev">
                <i class="fas fa-chevron-left"></i>
              </button>
              <div class="month-info">
                <h5 class="month-title">{{ currentMonthYear }}</h5>
                <div class="month-stats">
                  本月记录 {{ monthlyEmotionCount }} 天 · 平均心情 {{ monthlyAvgMood }}/10
                </div>
              </div>
              <button @click="nextMonth" class="nav-btn next">
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
            
            <!-- 情绪图例 -->
            <div class="emotion-legend">
              <div class="legend-title">
                <i class="fas fa-palette"></i>
                心情色彩
              </div>
              <div class="legend-items">
                <div class="legend-item">
                  <div class="legend-color excellent"></div>
                  <span>极佳 (9-10)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color good"></div>
                  <span>良好 (7-8)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color normal"></div>
                  <span>一般 (5-6)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color low"></div>
                  <span>较低 (3-4)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color poor"></div>
                  <span>不佳 (1-2)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 日历主体 -->
          <div class="calendar-grid">
            <div class="weekday-header">
              <div class="weekday" v-for="day in weekdays" :key="day">
                <span class="weekday-text">{{ day }}</span>
              </div>
            </div>
            <div class="calendar-body">
              <div 
                v-for="date in calendarDates" 
                :key="date.key"
                class="calendar-date"
                :class="{ 
                  'other-month': date.isOtherMonth,
                  'today': date.isToday,
                  'has-data': date.emotionData,
                  'weekend': isWeekend(date.date),
                  'excellent-mood': date.emotionData && date.emotionData.avgMoodScore >= 9,
                  'good-mood': date.emotionData && date.emotionData.avgMoodScore >= 7 && date.emotionData.avgMoodScore < 9,
                  'normal-mood': date.emotionData && date.emotionData.avgMoodScore >= 5 && date.emotionData.avgMoodScore < 7,
                  'low-mood': date.emotionData && date.emotionData.avgMoodScore >= 3 && date.emotionData.avgMoodScore < 5,
                  'poor-mood': date.emotionData && date.emotionData.avgMoodScore < 3
                }"
                @click="showDateDetail(date)"
              >
                <div class="date-content">
                  <div class="date-number">{{ date.day }}</div>
                  <div v-if="date.emotionData" class="emotion-display">
                    <div class="emotion-score">{{ date.emotionData.avgMoodScore?.toFixed(1) }}</div>
                    <div class="emotion-emoji">{{ getEmotionEmoji(date.emotionData.dominantEmotion, date.emotionData.avgMoodScore) }}</div>
                    <div class="emotion-name">{{ date.emotionData.dominantEmotion }}</div>
                  </div>
                  <div v-else class="no-data">
                    <div class="no-data-icon">·</div>
                  </div>
                  
                  <!-- 特殊标记 -->
                  <div v-if="date.isToday" class="today-marker">
                    <i class="fas fa-star"></i>
                  </div>
                  
                  <!-- 多条记录指示器 -->
                  <div v-if="date.emotionData && date.emotionData.recordCount > 1" class="record-count">
                    {{ date.emotionData.recordCount }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 月度情绪总结 -->
          <div class="monthly-summary">
            <div class="summary-title">
              <i class="fas fa-chart-line"></i>
              本月情绪概览
            </div>
            <div class="summary-cards">
              <div class="summary-card best-day">
                <div class="card-icon">🌟</div>
                <div class="card-content">
                  <div class="card-label">最佳一天</div>
                  <div class="card-value">{{ bestDayOfMonth }}</div>
                </div>
              </div>
              <div class="summary-card most-emotion">
                <div class="card-icon">{{ mostFrequentEmotionEmoji }}</div>
                <div class="card-content">
                  <div class="card-label">主要情绪</div>
                  <div class="card-value">{{ mostFrequentEmotion }}</div>
                </div>
              </div>
              <div class="summary-card active-days">
                <div class="card-icon">📝</div>
                <div class="card-content">
                  <div class="card-label">记录天数</div>
                  <div class="card-value">{{ monthlyEmotionCount }}天</div>
                </div>
              </div>
              <div class="summary-card mood-trend">
                <div class="card-icon">{{ moodTrendIcon }}</div>
                <div class="card-content">
                  <div class="card-label">情绪趋势</div>
                  <div class="card-value">{{ moodTrendText }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>


      <!-- 流式图表 -->
      <div v-if="currentMode === 'stream'" class="visualization-panel">
        <div class="panel-header">
          <h4><i class="fas fa-water"></i> 情绪流动图</h4>
          <div class="panel-info">展示情绪随时间的流动变化</div>
        </div>
        <div class="stream-container">
          <div ref="streamChart" class="stream-chart"></div>
          <div class="stream-controls">
            <div class="control-group">
              <label>时间粒度:</label>
              <select v-model="streamGranularity" @change="updateStreamChart">
                <option value="hour">小时</option>
                <option value="day">天</option>
                <option value="week">周</option>
              </select>
            </div>
            <div class="control-group">
              <label>平滑度:</label>
              <input 
                type="range" 
                v-model="streamSmoothing" 
                min="0" 
                max="1" 
                step="0.1"
                @input="updateStreamChart"
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="情绪详情" 
      width="600px"
      class="emotion-detail-dialog"
    >
      <div v-if="selectedEmotionData" class="emotion-detail-content">
        <div class="detail-header">
          <div class="detail-date">
            {{ formatDetailDate(selectedEmotionData.date) }}
          </div>
          <div class="detail-emotion">
            {{ getEmotionEmoji(selectedEmotionData.dominantEmotion, selectedEmotionData.avgMoodScore) }}
            {{ selectedEmotionData.dominantEmotion }}
          </div>
        </div>
        <div class="detail-metrics">
          <div class="metric-item primary">
            <div class="metric-icon">😊</div>
            <div class="metric-content">
              <div class="metric-label">情绪评分</div>
              <div class="metric-value">{{ selectedEmotionData.avgMoodScore?.toFixed(1) || '0.0' }}/10</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon">📝</div>
            <div class="metric-content">
              <div class="metric-label">记录次数</div>
              <div class="metric-value">{{ selectedEmotionData.recordCount || 0 }}次</div>
            </div>
          </div>
          <div class="metric-item" v-if="selectedEmotionData.positiveRatio !== undefined">
            <div class="metric-icon">🌟</div>
            <div class="metric-content">
              <div class="metric-label">积极情绪占比</div>
              <div class="metric-value">{{ (selectedEmotionData.positiveRatio ).toFixed(1) }}%</div>
            </div>
          </div>
          <div class="metric-item" v-if="selectedEmotionData.negativeRatio !== undefined">
            <div class="metric-icon">⚡</div>
            <div class="metric-content">
              <div class="metric-label">消极情绪占比</div>
              <div class="metric-value">{{ (selectedEmotionData.negativeRatio ).toFixed(1) }}%</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  heatmapData: {
    type: Object,
    default: () => ({
      gridData: [],
      emotionDistribution: {},
      peakEmotionTime: '00:00',
      dateRange: '',
      emotionTrend: [] // 添加情绪趋势数据
    })
  }
})

// 响应式数据
const currentMode = ref('calendar')
const showDetailDialog = ref(false)
const selectedEmotionData = ref(null)
const currentMonth = ref(new Date().getMonth())
const currentYear = ref(new Date().getFullYear())
const streamGranularity = ref('hour')
const streamSmoothing = ref(0.5)

// 图表引用
const timelineChart = ref(null)
const streamChart = ref(null)

// 可视化模式配置
const visualizationModes = [
  {
    key: 'calendar',
    name: '情绪日历',
    icon: 'fas fa-calendar-alt',
    description: '精美日历展示每日情绪轨迹'
  },
  {
    key: 'stream',
    name: '流动图',
    icon: 'fas fa-water',
    description: '情绪随时间的流动变化'
  }
]

// 计算属性

// 月度情绪统计
const monthlyEmotionCount = computed(() => {
  return calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData
  ).length
})

const monthlyAvgMood = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData
  )
  
  if (currentMonthDates.length === 0) return '0.0'
  
  const totalMood = currentMonthDates.reduce((sum, date) => 
    sum + (date.emotionData.avgMoodScore || 0), 0
  )
  
  return (totalMood / currentMonthDates.length).toFixed(1)
})

const bestDayOfMonth = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData
  )
  
  if (currentMonthDates.length === 0) return '暂无数据'
  
  const bestDay = currentMonthDates.reduce((best, date) => 
    (date.emotionData.avgMoodScore || 0) > (best.emotionData?.avgMoodScore || 0) ? date : best
  )
  
  return `${bestDay.day}日`
})

const mostFrequentEmotion = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData
  )
  
  if (currentMonthDates.length === 0) return '暂无数据'
  
  const emotionCount = {}
  currentMonthDates.forEach(date => {
    const emotion = date.emotionData.dominantEmotion
    if (emotion && emotion !== '未知') {
      emotionCount[emotion] = (emotionCount[emotion] || 0) + 1
    }
  })
  
  if (Object.keys(emotionCount).length === 0) return '暂无数据'
  
  const mostFrequent = Object.entries(emotionCount).reduce((a, b) => 
    emotionCount[a[0]] > emotionCount[b[0]] ? a : b
  )
  
  return mostFrequent ? mostFrequent[0] : '暂无数据'
})

const mostFrequentEmotionEmoji = computed(() => {
  // 获取当月最频繁情绪对应的平均评分
  const currentMonthDates = calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData && date.emotionData.dominantEmotion === mostFrequentEmotion.value
  )
  
  if (currentMonthDates.length === 0) return '😐'
  
  const avgScore = currentMonthDates.reduce((sum, date) => sum + date.emotionData.avgMoodScore, 0) / currentMonthDates.length
  return getEmotionEmoji(mostFrequentEmotion.value, avgScore)
})

const moodTrendIcon = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => 
    !date.isOtherMonth && date.emotionData
  ).sort((a, b) => a.date - b.date) // 按日期排序
  
  if (currentMonthDates.length < 4) return '📊'
  
  // 取最近7天和之前7天的数据进行对比
  const recentDays = currentMonthDates.slice(-7)
  const earlierDays = currentMonthDates.slice(-14, -7)
  
  if (recentDays.length === 0) return '📊'
  
  const recentAvg = recentDays.reduce((sum, date) => sum + date.emotionData.avgMoodScore, 0) / recentDays.length
  
  if (earlierDays.length === 0) return '📊'
  
  const earlierAvg = earlierDays.reduce((sum, date) => sum + date.emotionData.avgMoodScore, 0) / earlierDays.length
  
  const difference = recentAvg - earlierAvg
  
  if (difference > 0.5) return '📈'
  if (difference < -0.5) return '📉'
  return '➡️'
})

const moodTrendText = computed(() => {
  const icon = moodTrendIcon.value
  if (icon === '📈') return '上升'
  if (icon === '📉') return '下降'
  return '平稳'
})

const currentMonthYear = computed(() => {
  const date = new Date(currentYear.value, currentMonth.value)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const calendarDates = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())
  
  const dates = []
  const currentDate = new Date(startDate)
  const today = new Date()
  
  for (let i = 0; i < 42; i++) { // 6 weeks * 7 days
    const isOtherMonth = currentDate.getMonth() !== month
    const isToday = currentDate.toDateString() === today.toDateString()
    
    dates.push({
      key: `${currentDate.getFullYear()}-${currentDate.getMonth()}-${currentDate.getDate()}`,
      day: currentDate.getDate(),
      date: new Date(currentDate),
      isOtherMonth,
      isToday,
      emotionData: getEmotionDataForDate(currentDate)
    })
    
    currentDate.setDate(currentDate.getDate() + 1)
  }
  
  return dates
})


// 方法
const switchMode = (mode) => {
  currentMode.value = mode
  nextTick(() => {
    initCurrentModeChart()
  })
}

const getDayName = (dayIndex) => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[dayIndex] || `第${dayIndex + 1}天`
}


const getEmotionEmoji = (emotion, avgMoodScore) => {
  // 基于平均情绪评分来映射emoji，而不是语义匹配
  // 情绪评分范围: 1-10分
  // 1-4: 消极情绪, 5: 中性, 6-10: 积极情绪
  
  if (avgMoodScore === undefined || avgMoodScore === null) {
    return '😐' // 无评分数据时的默认表情
  }
  
  const score = Number(avgMoodScore)
  
  // 根据情绪评分区间映射对应的emoji
  if (score >= 9) {
    return '🤩' // 9-10分: 极好 - 非常兴奋
  } else if (score >= 8) {
    return '😄' // 8分: 很好 - 开心愉悦
  } else if (score >= 7) {
    return '😊' // 7分: 不错 - 微笑满意
  } else if (score >= 6) {
    return '🙂' // 6分: 还可以 - 轻微积极
  } else if (score >= 5) {
    return '😐' // 5分: 一般 - 中性平静
  } else if (score >= 4) {
    return '😔' // 4分: 略差 - 轻微失落
  } else if (score >= 3) {
    return '😞' // 3分: 不好 - 明显不开心
  } else if (score >= 2) {
    return '😢' // 2分: 糟糕 - 悲伤
  } else {
    return '😰' // 1分: 非常糟糕 - 极度焦虑痛苦
  }
}

const getEmotionColor = (moodScore) => {
  if (moodScore >= 8) return '#00b894'
  if (moodScore >= 6) return '#fdcb6e'
  if (moodScore >= 4) return '#fab1a0'
  return '#fd79a8'
}

// 格式化本地日期，避免时区问题
const formatLocalDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

const getEmotionDataForDate = (date) => {
  // 从后端的emotionTrend数据中查找指定日期的情绪数据
  if (!props.heatmapData || !Array.isArray(props.heatmapData.emotionTrend)) {
    return null
  }
  
  const dateString = formatLocalDate(date) // 格式化为 YYYY-MM-DD
  const emotionData = props.heatmapData.emotionTrend?.find(item => {
    // 处理日期格式，支持多种可能的格式
    let itemDate
    if (typeof item.date === 'string') {
      itemDate = item.date.split('T')[0] // 如果是ISO字符串，取日期部分
    } else if (item.date instanceof Date) {
      itemDate = formatLocalDate(item.date)
    } else if (Array.isArray(item.date) && item.date.length >= 3) {
      // 如果是数组格式 [year, month, day]
      const [year, month, day] = item.date
      itemDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    } else {
      return false
    }
    
    return itemDate === dateString
  })
  
  if (emotionData) {
    return {
      avgMoodScore: Number(emotionData.avgMoodScore) || 0,
      dominantEmotion: emotionData.dominantEmotion || '未知',
      recordCount: Number(emotionData.recordCount) || 1,
      positiveRatio: Number(emotionData.positiveRatio) || 0,
      negativeRatio: Number(emotionData.negativeRatio) || 0
    }
  }
  
  return null
}

const isWeekend = (date) => {
  const day = date.getDay()
  return day === 0 || day === 6
}

const showDateDetail = (date) => {
  if (date.emotionData) {
    selectedEmotionData.value = {
      date: date.date,
      day: date.day,
      ...date.emotionData
    }
    showDetailDialog.value = true
  }
}

const formatDetailDate = (date) => {
  if (!date) return ''
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  }
  return date.toLocaleDateString('zh-CN', options)
}

const previousMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const initCurrentModeChart = () => {
  switch (currentMode.value) {
    case 'stream':
      initStreamChart()
      break
  }
}


const initStreamChart = () => {
  if (!streamChart.value) return
  
  const chart = echarts.init(streamChart.value)
  
  // 生成流式数据
  const streamData = []
  const emotions = Object.keys(props.heatmapData.emotionDistribution || {})
  
  emotions.forEach((emotion, index) => {
    const data = Array(24).fill().map((_, hour) => {
      return Math.random() * 100 + Math.sin(hour / 24 * Math.PI * 2) * 20
    })
    
    streamData.push({
      name: emotion,
      type: 'line',
      stack: 'emotion',
      areaStyle: {
        opacity: 0.7
      },
      data,
      smooth: true
    })
  })
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: emotions
    },
    xAxis: {
      type: 'category',
      data: Array(24).fill().map((_, i) => i.toString().padStart(2, '0') + ':00')
    },
    yAxis: {
      type: 'value'
    },
    series: streamData
  }
  
  chart.setOption(option)
}

const updateStreamChart = () => {
  initStreamChart()
}

// 生命周期
onMounted(() => {
  initCurrentModeChart()
})

watch(() => currentMode.value, () => {
  nextTick(() => {
    initCurrentModeChart()
  })
})

watch(() => props.heatmapData, () => {
  nextTick(() => {
    initCurrentModeChart()
  })
}, { deep: true })
</script>

<style lang="scss" scoped>
.emotion-visualization-hub {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(255, 250, 245, 0.9));
  border-radius: 20px;
  box-shadow: 
    0 8px 32px rgba(244, 162, 97, 0.15),
    0 2px 8px rgba(255, 183, 77, 0.1);
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 221, 210, 0.3);
}

.visualization-selector {
  padding: 30px;
  background: linear-gradient(135deg, rgba(255, 234, 167, 0.3), rgba(250, 177, 160, 0.3));
  border-bottom: 1px solid rgba(244, 162, 97, 0.2);

  .selector-header {
    text-align: center;
    margin-bottom: 25px;

    .selector-title {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 700;
      color: #2d3436;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;

      i {
        color: #e84393;
      }
    }

    .selector-subtitle {
      color: #636e72;
      font-size: 14px;
      font-style: italic;
    }
  }

  .mode-tabs {
    display: flex;
    gap: 15px;
    justify-content: center;
    flex-wrap: wrap;

    .mode-tab {
      background: rgba(255, 255, 255, 0.8);
      padding: 20px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      text-align: center;
      min-width: 140px;
      border: 2px solid transparent;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 20px rgba(244, 162, 97, 0.3);
        background: rgba(255, 234, 167, 0.8);
      }

      &.active {
        background: linear-gradient(135deg, #ffeaa7, #fab1a0);
        border-color: #e84393;
        box-shadow: 0 6px 16px rgba(244, 162, 97, 0.4);

        i {
          color: #2d3436;
        }

        span {
          color: #2d3436;
          font-weight: 600;
        }

        .mode-description {
          color: #636e72;
        }
      }

      i {
        font-size: 24px;
        color: #e84393;
        margin-bottom: 8px;
      }

      span {
        display: block;
        font-weight: 500;
        color: #2d3436;
        margin-bottom: 6px;
      }

      .mode-description {
        font-size: 11px;
        color: #74b9ff;
        line-height: 1.3;
      }
    }
  }
}

.visualization-content {
  padding: 30px;
}

.visualization-panel {
  .panel-header {
    margin-bottom: 25px;
    text-align: center;

    h4 {
      margin: 0 0 8px 0;
      font-size: 18px;
      color: #2d3436;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;

      i {
        color: #e84393;
      }
    }

    .panel-info {
      color: #636e72;
      font-size: 13px;
      font-style: italic;
    }
  }
}

// 日历样式
.calendar-container {
  .calendar-controls {
    margin-bottom: 25px;
    
    .calendar-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 16px;
      box-shadow: 0 2px 8px rgba(244, 162, 97, 0.1);

      .nav-btn {
        background: linear-gradient(135deg, #ffeaa7, #fab1a0);
        border: none;
        border-radius: 12px;
        padding: 12px 16px;
        cursor: pointer;
        color: #2d3436;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(244, 162, 97, 0.3);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(244, 162, 97, 0.4);
        }

        &.prev, &.next {
          font-size: 16px;
        }
      }

      .month-info {
        text-align: center;
        flex: 1;

        .month-title {
          margin: 0 0 5px 0;
          font-size: 22px;
          color: #2d3436;
          font-weight: 700;
        }

        .month-stats {
          font-size: 13px;
          color: #636e72;
          font-style: italic;
        }
      }
    }

    .emotion-legend {
      background: rgba(255, 255, 255, 0.5);
      padding: 15px 20px;
      border-radius: 12px;
      border: 1px solid rgba(244, 162, 97, 0.2);

      .legend-title {
        font-size: 14px;
        font-weight: 600;
        color: #2d3436;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          color: #e84393;
        }
      }

      .legend-items {
        display: flex;
        gap: 20px;
        justify-content: center;
        flex-wrap: wrap;

        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #636e72;

          .legend-color {
            width: 16px;
            height: 16px;
            border-radius: 4px;
            
            &.excellent { background: linear-gradient(135deg, #00b894, #00cec9); }
            &.good { background: linear-gradient(135deg, #fdcb6e, #e17055); }
            &.normal { background: linear-gradient(135deg, #74b9ff, #0984e3); }
            &.low { background: linear-gradient(135deg, #fab1a0, #fd79a8); }
            &.poor { background: linear-gradient(135deg, #fd79a8, #e84393); }
          }
        }
      }
    }
  }

  .calendar-grid {
    background: rgba(255, 255, 255, 0.4);
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 4px 16px rgba(244, 162, 97, 0.1);
    margin-bottom: 25px;

    .weekday-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 8px;
      margin-bottom: 15px;

      .weekday {
        text-align: center;
        padding: 12px;
        background: rgba(255, 234, 167, 0.3);
        border-radius: 8px;

        .weekday-text {
          font-size: 14px;
          color: #2d3436;
          font-weight: 600;
        }
      }
    }

    .calendar-body {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 8px;

      .calendar-date {
        aspect-ratio: 1;
        background: rgba(255, 255, 255, 0.7);
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;
        border: 2px solid transparent;
        overflow: hidden;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(244, 162, 97, 0.3);
          border-color: rgba(255, 234, 167, 0.8);
        }

        &.other-month {
          opacity: 0.3;
        }

        &.today {
          border-color: #e84393;
          box-shadow: 0 0 20px rgba(232, 67, 147, 0.3);
          
          .today-marker {
            position: absolute;
            top: 4px;
            right: 4px;
            color: #e84393;
            font-size: 12px;
            animation: twinkle 2s ease-in-out infinite;
          }
        }

        &.weekend {
          background: rgba(255, 234, 167, 0.2);
        }

        &.excellent-mood {
          background: linear-gradient(135deg, rgba(0, 184, 148, 0.2), rgba(0, 206, 201, 0.2));
        }

        &.good-mood {
          background: linear-gradient(135deg, rgba(253, 203, 110, 0.2), rgba(225, 112, 85, 0.2));
        }

        &.normal-mood {
          background: linear-gradient(135deg, rgba(116, 185, 255, 0.2), rgba(9, 132, 227, 0.2));
        }

        &.low-mood {
          background: linear-gradient(135deg, rgba(250, 177, 160, 0.2), rgba(253, 121, 168, 0.2));
        }

        &.poor-mood {
          background: linear-gradient(135deg, rgba(253, 121, 168, 0.2), rgba(232, 67, 147, 0.2));
        }

        .date-content {
          padding: 8px;
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: space-between;
          position: relative;

          .date-number {
            font-size: 16px;
            font-weight: 600;
            color: #2d3436;
          }

          .emotion-display {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 2px;

            .emotion-score {
              font-size: 10px;
              font-weight: 600;
              color: #636e72;
              background: rgba(255, 255, 255, 0.8);
              padding: 1px 4px;
              border-radius: 4px;
            }

            .emotion-emoji {
              font-size: 16px;
            }

            .emotion-name {
              font-size: 8px;
              color: #636e72;
              text-align: center;
              line-height: 1;
            }
          }

          .no-data {
            display: flex;
            align-items: center;
            justify-content: center;
            flex: 1;

            .no-data-icon {
              color: #ddd;
              font-size: 20px;
            }
          }

          .record-count {
            position: absolute;
            top: 2px;
            left: 2px;
            background: #e84393;
            color: white;
            font-size: 8px;
            font-weight: 600;
            padding: 1px 4px;
            border-radius: 6px;
            min-width: 12px;
            text-align: center;
          }
        }
      }
    }
  }

  .monthly-summary {
    background: linear-gradient(135deg, rgba(255, 234, 167, 0.2), rgba(250, 177, 160, 0.2));
    border-radius: 16px;
    padding: 25px;
    border: 1px solid rgba(244, 162, 97, 0.2);

    .summary-title {
      font-size: 16px;
      font-weight: 600;
      color: #2d3436;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 10px;

      i {
        color: #e84393;
      }
    }

    .summary-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 15px;

      .summary-card {
        background: rgba(255, 255, 255, 0.8);
        padding: 20px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        gap: 15px;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(244, 162, 97, 0.1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba(244, 162, 97, 0.2);
        }

        .card-icon {
          font-size: 24px;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 234, 167, 0.3);
          border-radius: 10px;
        }

        .card-content {
          flex: 1;

          .card-label {
            font-size: 12px;
            color: #636e72;
            margin-bottom: 4px;
          }

          .card-value {
            font-size: 16px;
            font-weight: 600;
            color: #2d3436;
          }
        }
      }
    }
  }
}

@keyframes twinkle {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}


// 流式图样式
.stream-container {
  .stream-chart {
    width: 100%;
    height: 400px;
    margin-bottom: 20px;
  }

  .stream-controls {
    display: flex;
    gap: 30px;
    justify-content: center;
    align-items: center;

    .control-group {
      display: flex;
      align-items: center;
      gap: 10px;

      label {
        font-size: 12px;
        color: #636e72;
        font-weight: 500;
      }

      select {
        padding: 6px 12px;
        border: 1px solid rgba(244, 162, 97, 0.3);
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.8);
        color: #2d3436;
      }

      input[type="range"] {
        width: 100px;
      }
    }
  }
}

// 详情弹窗样式
:deep(.emotion-detail-dialog) {
  .el-dialog__body {
    padding: 25px;
  }

  .emotion-detail-content {
    .detail-header {
      text-align: center;
      margin-bottom: 25px;
      padding: 20px;
      background: linear-gradient(135deg, rgba(255, 234, 167, 0.3), rgba(250, 177, 160, 0.3));
      border-radius: 16px;
      border: 1px solid rgba(244, 162, 97, 0.2);

      .detail-date {
        font-size: 18px;
        font-weight: 600;
        color: #2d3436;
        margin-bottom: 8px;
      }

      .detail-emotion {
        font-size: 20px;
        color: #e84393;
        font-weight: 500;
      }
    }

    .detail-metrics {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 15px;
      margin-bottom: 25px;

      .metric-item {
        background: rgba(255, 255, 255, 0.8);
        padding: 18px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        gap: 12px;
        transition: all 0.3s ease;
        border: 1px solid rgba(244, 162, 97, 0.1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(244, 162, 97, 0.2);
        }

        &.primary {
          background: linear-gradient(135deg, rgba(255, 234, 167, 0.4), rgba(250, 177, 160, 0.4));
          border-color: rgba(232, 67, 147, 0.3);
        }

        .metric-icon {
          font-size: 24px;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 234, 167, 0.3);
          border-radius: 10px;
        }

        .metric-content {
          flex: 1;

          .metric-label {
            font-size: 12px;
            color: #636e72;
            margin-bottom: 4px;
            font-weight: 500;
          }

          .metric-value {
            font-size: 16px;
            font-weight: 600;
            color: #2d3436;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .mode-tabs {
    .mode-tab {
      min-width: 120px;
      padding: 15px;
    }
  }

  .radar-container {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .visualization-selector,
  .visualization-content {
    padding: 20px;
  }

  .mode-tabs {
    gap: 10px;

    .mode-tab {
      min-width: 100px;
      padding: 12px;

      i {
        font-size: 20px;
      }

      .mode-description {
        display: none;
      }
    }
  }

  .timeline-insights {
    flex-direction: column;
    gap: 15px;

    .insight-card {
      max-width: none;
    }
  }

  .heatmap-2d-container {
    .time-labels,
    .heatmap-grid {
      font-size: 10px;
    }

    .day-row .hour-cells .hour-cell {
      width: 14px;
      height: 14px;
    }
  }
}
</style>
