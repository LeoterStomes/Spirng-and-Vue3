<template>
  <div class="data-analytics-container">

    <!-- 系统概览卡片 -->
    <div class="overview-cards" v-loading="loading">
      <div class="overview-card">
        <div class="card-icon users">
          <i class="fas fa-users"></i>
        </div>
        <div class="card-content">
          <div class="card-title">总用户数</div>
          <div class="card-value">{{ formatNumber(systemOverview.totalUsers) }}</div>
          <div class="card-subtitle">活跃用户: {{ formatNumber(systemOverview.activeUsers) }}</div>
        </div>
      </div>

      <div class="overview-card">
        <div class="card-icon diaries">
          <i class="fas fa-heart"></i>
        </div>
        <div class="card-content">
          <div class="card-title">情绪日记</div>
          <div class="card-value">{{ formatNumber(systemOverview.totalDiaries) }}</div>
          <div class="card-subtitle">今日新增: {{ formatNumber(systemOverview.todayNewDiaries) }}</div>
        </div>
      </div>

      <div class="overview-card">
        <div class="card-icon sessions">
          <i class="fas fa-comments"></i>
        </div>
        <div class="card-content">
          <div class="card-title">咨询会话</div>
          <div class="card-value">{{ formatNumber(systemOverview.totalSessions) }}</div>
          <div class="card-subtitle">今日新增: {{ formatNumber(systemOverview.todayNewSessions) }}</div>
        </div>
      </div>

      <div class="overview-card">
        <div class="card-icon mood">
          <i class="fas fa-smile"></i>
        </div>
        <div class="card-content">
          <div class="card-title">平均情绪</div>
          <div class="card-value">{{ systemOverview.avgMoodScore || '0.0' }}/10</div>
          <div class="card-subtitle">情绪健康指数</div>
        </div>
      </div>
    </div>

     <!-- 情绪可视化中心 -->
     <div class="chart-section">
       <div class="visualization-toggle">
         <el-button-group>
           <el-button 
             :type="visualizationMode === 'classic' ? 'primary' : 'default'"
             @click="visualizationMode = 'classic'"
           >
             <i class="fas fa-cubes"></i>
             经典热力图
           </el-button>
           <el-button 
             :type="visualizationMode === 'hub' ? 'primary' : 'default'"
             @click="visualizationMode = 'hub'"
           >
             <i class="fas fa-magic"></i>
             多元可视化
           </el-button>
         </el-button-group>
       </div>
       
       <EmotionHeatmap3D v-if="visualizationMode === 'classic'" :heatmapData="emotionHeatmap" />
       <EmotionVisualizationHub v-if="visualizationMode === 'hub'" :heatmapData="{ ...emotionHeatmap, emotionTrend: emotionTrend }" />
     </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 情绪趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>
            <i class="fas fa-line-chart"></i>
            情绪趋势分析
          </h3>
        </div>
        <div class="chart-content">
          <div ref="emotionTrendChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </div>

      <!-- 咨询统计图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>
            <i class="fas fa-chart-pie"></i>
            咨询会话统计
          </h3>
        </div>
        <div class="chart-content">
          <div class="consultation-stats">
            <div class="stat-item">
              <div class="stat-label">总会话数</div>
              <div class="stat-value">{{ formatNumber(consultationStats.totalSessions) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">平均时长</div>
              <div class="stat-value">{{ consultationStats.avgDurationMinutes || '0' }}分钟</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">活跃用户</div>
              <div class="stat-value">{{ formatNumber(systemOverview.activeUsers) }}</div>
            </div>
          </div>
          <div ref="consultationChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </div>

      <!-- 用户活跃度图 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>
            <i class="fas fa-user-friends"></i>
            用户活跃度趋势
          </h3>
        </div>
        <div class="chart-content">
          <div ref="userActivityChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </div>

    <!-- 情绪标签云 -->
    <div class="emotion-tags-section" v-if="consultationStats.topEmotionTags">
      <h3>
        <i class="fas fa-tags"></i>
        高频情绪标签
      </h3>
      <div class="emotion-tags-cloud">
        <div 
          v-for="(count, tag) in consultationStats.topEmotionTags" 
          :key="tag"
          class="emotion-tag"
          :style="{ '--tag-size': Math.min(count / maxTagCount * 2 + 1, 3) }"
        >
          {{ tag }}
          <span class="tag-count">({{ count }})</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import EmotionHeatmap3D from '@/components/common/EmotionHeatmap3D.vue'
import EmotionVisualizationHub from '@/components/common/EmotionVisualizationHub.vue'
import { getDataAnalytics } from '@/api/dataAnalytics'

// ECharts温暖治愈主题配置
const warmTheme = {
  color: ['#ffeaa7', '#fab1a0', '#fd79a8', '#a29bfe', '#74b9ff', '#55a3ff', '#00b894', '#fdcb6e'],
  backgroundColor: 'rgba(255, 255, 255, 0.1)',
  textStyle: {
    color: '#2d3436',
    fontFamily: 'system-ui, -apple-system, sans-serif'
  },
  title: {
    textStyle: {
      color: '#2d3436',
      fontWeight: 600
    }
  },
  legend: {
    textStyle: {
      color: '#636e72'
    }
  },
  grid: {
    borderColor: 'rgba(244, 162, 97, 0.2)'
  },
  categoryAxis: {
    axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
    axisTick: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
    axisLabel: { color: '#636e72' },
    splitLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.1)' } }
  },
  valueAxis: {
    axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
    axisTick: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
    axisLabel: { color: '#636e72' },
    splitLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.1)' } }
  }
}

// 响应式数据
const loading = ref(false)
const visualizationMode = ref('classic')

const analyticsData = ref({
  systemOverview: {
    totalUsers: 0,
    activeUsers: 0,
    totalDiaries: 0,
    totalSessions: 0,
    avgMoodScore: 0,
    todayNewUsers: 0,
    todayNewDiaries: 0,
    todayNewSessions: 0
  },
  emotionHeatmap: {
    gridData: [],
    emotionDistribution: {},
    peakEmotionTime: '00:00',
    dateRange: ''
  },
  emotionTrend: [],
  consultationStats: {
    totalSessions: 0,
    avgDurationMinutes: 0,
    dailyTrend: [],
    topEmotionTags: {}
  },
  userActivity: []
})

// 计算属性
const systemOverview = computed(() => analyticsData.value.systemOverview)
const emotionHeatmap = computed(() => analyticsData.value.emotionHeatmap)
const emotionTrend = computed(() => analyticsData.value.emotionTrend)
const consultationStats = computed(() => analyticsData.value.consultationStats)
const userActivity = computed(() => analyticsData.value.userActivity)

const maxTagCount = computed(() => {
  const counts = Object.values(consultationStats.value.topEmotionTags || {})
  return counts.length > 0 ? Math.max(...counts) : 1
})

// ECharts图表实例
let emotionTrendChart = null
let consultationChart = null
let userActivityChart = null

// 图表DOM引用
const emotionTrendChartRef = ref(null)
const consultationChartRef = ref(null)
const userActivityChartRef = ref(null)

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  
  try {
    await getDataAnalytics(
      {},
      {
        onSuccess: (res) => {
          analyticsData.value = res
          nextTick(() => {
            initCharts()
          })
        },
        onError: (error) => {
          console.error('获取数据分析失败:', error)
          ElMessage.error('获取数据失败，请稍后重试')
        }
      }
    )
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initEmotionTrendChart()
  initConsultationChart()
  initUserActivityChart()
}

// 初始化情绪趋势图
const initEmotionTrendChart = () => {
  if (!emotionTrendChartRef.value) return
  
  // 销毁现有图表
  if (emotionTrendChart) {
    emotionTrendChart.dispose()
  }

  emotionTrendChart = echarts.init(emotionTrendChartRef.value)
  const trendData = emotionTrend.value

  const option = {
    title: {
      text: '情绪趋势分析',
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#2d3436'
      },
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#fab1a0',
      borderWidth: 1,
      textStyle: {
        color: '#2d3436'
      }
    },
    legend: {
      data: ['平均情绪评分', '记录数量'],
      top: 40,
      textStyle: {
        color: '#636e72'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 80,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.3)'
        }
      },
      axisLabel: {
        color: '#636e72'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '情绪评分',
        position: 'left',
        axisLabel: {
          color: '#636e72'
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(244, 162, 97, 0.3)'
          }
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(244, 162, 97, 0.1)'
          }
        }
      },
      {
        type: 'value',
        name: '记录数量',
        position: 'right',
        axisLabel: {
          color: '#636e72'
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(244, 162, 97, 0.3)'
          }
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '平均情绪评分',
        type: 'line',
        data: trendData.map(item => item.avgMoodScore),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#ffeaa7'
        },
        itemStyle: {
          color: '#ffeaa7'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(255, 234, 167, 0.8)' },
              { offset: 1, color: 'rgba(255, 234, 167, 0.1)' }
            ]
          }
        }
      },
      {
        name: '记录数量',
        type: 'line',
        yAxisIndex: 1,
        data: trendData.map(item => item.recordCount),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#fab1a0'
        },
        itemStyle: {
          color: '#fab1a0'
        }
      }
    ]
  }

  emotionTrendChart.setOption(option)
}

// 初始化咨询统计图
const initConsultationChart = () => {
  if (!consultationChartRef.value) return
  
  // 销毁现有图表
  if (consultationChart) {
    consultationChart.dispose()
  }

  consultationChart = echarts.init(consultationChartRef.value)
  const dailyTrend = consultationStats.value.dailyTrend || []

  const option = {
    title: {
      text: '咨询活动统计',
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#2d3436'
      },
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#fab1a0',
      borderWidth: 1,
      textStyle: {
        color: '#2d3436'
      }
    },
    legend: {
      data: ['会话数量', '参与用户数'],
      top: 40,
      textStyle: {
        color: '#636e72'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 80,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dailyTrend.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.3)'
        }
      },
      axisLabel: {
        color: '#636e72'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#636e72'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.3)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.1)'
        }
      }
    },
    series: [
      {
        name: '会话数量',
        type: 'bar',
        data: dailyTrend.map(item => item.sessionCount),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#74b9ff' },
              { offset: 1, color: '#0984e3' }
            ]
          }
        },
        barWidth: '40%'
      },
      {
        name: '参与用户数',
        type: 'bar',
        data: dailyTrend.map(item => item.userCount),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#fdcb6e' },
              { offset: 1, color: '#f39c12' }
            ]
          }
        },
        barWidth: '40%'
      }
    ]
  }

  consultationChart.setOption(option)
}

// 初始化用户活跃度图
const initUserActivityChart = () => {
  if (!userActivityChartRef.value) return
  
  // 销毁现有图表
  if (userActivityChart) {
    userActivityChart.dispose()
  }

  userActivityChart = echarts.init(userActivityChartRef.value)
  const activityData = userActivity.value

  const option = {
    title: {
      text: '用户活跃度趋势',
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#2d3436'
      },
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#fab1a0',
      borderWidth: 1,
      textStyle: {
        color: '#2d3436'
      }
    },
    legend: {
      data: ['活跃用户', '新增用户', '日记用户', '咨询用户'],
      top: 40,
      textStyle: {
        color: '#636e72'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 80,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: activityData.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.3)'
        }
      },
      axisLabel: {
        color: '#636e72'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#636e72'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.3)'
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(244, 162, 97, 0.1)'
        }
      }
    },
    series: [
      {
        name: '活跃用户',
        type: 'line',
        data: activityData.map(item => item.activeUsers),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#a29bfe'
        },
        itemStyle: {
          color: '#a29bfe'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(162, 155, 254, 0.4)' },
              { offset: 1, color: 'rgba(162, 155, 254, 0.1)' }
            ]
          }
        }
      },
      {
        name: '新增用户',
        type: 'line',
        data: activityData.map(item => item.newUsers),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#fdcb6e'
        },
        itemStyle: {
          color: '#fdcb6e'
        }
      },
      {
        name: '日记用户',
        type: 'line',
        data: activityData.map(item => item.diaryUsers),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#00b894'
        },
        itemStyle: {
          color: '#00b894'
        }
      },
      {
        name: '咨询用户',
        type: 'line',
        data: activityData.map(item => item.consultationUsers),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#fab1a0'
        },
        itemStyle: {
          color: '#fab1a0'
        }
      }
    ]
  }

  userActivityChart.setOption(option)
}


// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.data-analytics-container {
  padding: 24px;
  background: linear-gradient(135deg, #fdf7f0 0%, #fef9f3 50%, #fff5ee 100%);
  min-height: 100vh;
  position: relative;
  
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="warm-dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="10" cy="10" r="1.5" fill="%23ffeaa7" opacity="0.1"/><circle cx="5" cy="5" r="1" fill="%23fab1a0" opacity="0.08"/><circle cx="15" cy="15" r="0.8" fill="%23fd79a8" opacity="0.06"/></pattern></defs><rect width="100" height="100" fill="url(%23warm-dots)"/></svg>');
    pointer-events: none;
    z-index: 0;
  }
  
  > * {
    position: relative;
    z-index: 1;
  }
}


.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;

  .overview-card {
    background: white;
    padding: 24px;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }

    .card-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: white;

      &.users {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.diaries {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.sessions {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.mood {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }

    .card-content {
      flex: 1;

      .card-title {
        font-size: 14px;
        color: #7f8c8d;
        margin-bottom: 4px;
      }

      .card-value {
        font-size: 24px;
        font-weight: 700;
        color: #2c3e50;
        margin-bottom: 4px;
      }

      .card-subtitle {
        font-size: 12px;
        color: #95a5a6;
      }
    }
  }
}

.chart-section {
  margin-bottom: 30px;
  
  .visualization-toggle {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
    
    .el-button-group {
      box-shadow: 0 4px 12px rgba(244, 162, 97, 0.3);
      border-radius: 25px;
      overflow: hidden;
      
      .el-button {
        border: none;
        background: rgba(255, 255, 255, 0.9);
        color: #2d3436;
        font-weight: 500;
        padding: 12px 24px;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 234, 167, 0.8);
          transform: translateY(-1px);
        }
        
        &.el-button--primary {
          background: linear-gradient(135deg, #ffeaa7, #fab1a0);
          color: #2d3436;
          box-shadow: 0 2px 8px rgba(244, 162, 97, 0.4);
        }
        
        i {
          margin-right: 8px;
          font-size: 16px;
        }
      }
    }
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 24px;

  .chart-card {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;

    &.full-width {
      grid-column: 1 / -1;
    }

    .chart-header {
      padding: 20px;
      border-bottom: 1px solid #eee;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #2c3e50;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          color: #3498db;
        }
      }
    }

    .chart-content {
      padding: 20px;
      height: 300px;
      position: relative;

      canvas {
        width: 100% !important;
        height: 100% !important;
      }

      .consultation-stats {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20px;

        .stat-item {
          text-align: center;

          .stat-label {
            font-size: 12px;
            color: #7f8c8d;
            margin-bottom: 4px;
          }

          .stat-value {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
          }
        }
      }
    }
  }
}

.emotion-tags-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  h3 {
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #2c3e50;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #3498db;
    }
  }

  .emotion-tags-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .emotion-tag {
      padding: 8px 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 20px;
      font-size: calc(12px * var(--tag-size));
      font-weight: 500;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      }

      .tag-count {
        opacity: 0.8;
        font-size: 0.9em;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }

  .charts-grid {
    grid-template-columns: 1fr;

    .chart-card .chart-content {
      height: 250px;
    }
  }
}
</style>
