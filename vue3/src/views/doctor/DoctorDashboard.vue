<template>
  <div class="doctor-dashboard">
    <!-- 欢迎条幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ displayName }}</h2>
        <p>以下是您的患者管理概览</p>
      </div>
      <div class="quick-actions">
        <el-button
          type="primary"
          @click="$router.push('/doctor/patients')"
        >
          <i class="fas fa-hospital-user" /> 查看患者
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div
      v-loading="loading"
      class="stats-grid"
    >
      <div class="stat-card active-patients">
        <div class="stat-icon">
          <i class="fas fa-users" />
        </div>
        <div class="stat-body">
          <p class="stat-value">
            {{ stats.totalPatients ?? '-' }}
          </p>
          <p class="stat-label">
            在管患者
          </p>
        </div>
      </div>
      <div class="stat-card risk-patients">
        <div class="stat-icon">
          <i class="fas fa-exclamation-triangle" />
        </div>
        <div class="stat-body">
          <p class="stat-value">
            {{ stats.highRiskCount ?? '-' }}
          </p>
          <p class="stat-label">
            高风险患者
          </p>
        </div>
      </div>
      <div class="stat-card total-notes">
        <div class="stat-icon">
          <i class="fas fa-clipboard-list" />
        </div>
        <div class="stat-body">
          <p class="stat-value">
            {{ stats.activeDiariesThisWeek ?? '-' }}
          </p>
          <p class="stat-label">
            近7日日记数
          </p>
        </div>
      </div>
      <div class="stat-card recent-reports">
        <div class="stat-icon">
          <i class="fas fa-file-medical-alt" />
        </div>
        <div class="stat-body">
          <p class="stat-value">
            {{ stats.activeSessionsThisWeek ?? '-' }}
          </p>
          <p class="stat-label">
            近7日会话数
          </p>
        </div>
      </div>
    </div>

    <!-- 近期患者快速入口 -->
    <el-card
      shadow="never"
      class="recent-card"
    >
      <template #header>
        <div class="card-header">
          <span><i class="fas fa-clock" /> 近期关注</span>
          <el-button
            link
            type="primary"
            @click="$router.push('/doctor/patients')"
          >
            查看全部
          </el-button>
        </div>
      </template>
      <el-empty
        v-if="!recentPatients.length"
        description="暂无绑定患者"
      />
      <div
        v-else
        class="recent-list"
      >
        <div
          v-for="p in recentPatients"
          :key="p.patientId"
          class="recent-item"
          @click="$router.push(`/doctor/patients/${p.patientId}`)"
        >
          <el-avatar :size="40">
            {{ (p.nickname || p.username || '?').charAt(0) }}
          </el-avatar>
          <div class="recent-info">
            <p class="recent-name">
              {{ p.nickname || p.username }}
            </p>
            <p class="recent-risk">
              <el-tag
                :type="riskColor(p.riskLevel)"
                size="small"
                effect="plain"
              >
                {{ riskLabel(p.riskLevel) }}
              </el-tag>
            </p>
          </div>
          <i class="fas fa-chevron-right recent-arrow" />
        </div>
      </div>
    </el-card>

    <el-row
      :gutter="16"
      class="chart-row"
    >
      <el-col
        :md="12"
        :sm="24"
      >
        <el-card
          shadow="never"
          class="recent-card"
        >
          <template #header>
            <div class="card-header">
              <span><i class="fas fa-chart-pie" /> 风险分布</span>
            </div>
          </template>
          <div class="risk-list">
            <div
              v-for="item in riskItems"
              :key="item.level"
              class="risk-item"
            >
              <span class="risk-name">
                <el-tag
                  :type="item.type"
                  size="small"
                  effect="plain"
                >
                  {{ item.label }}
                </el-tag>
              </span>
              <span class="risk-value">{{ item.value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col
        :md="12"
        :sm="24"
      >
        <el-card
          shadow="never"
          class="recent-card"
        >
          <template #header>
            <div class="card-header">
              <span><i class="fas fa-wave-square" /> 活跃趋势（7天）</span>
            </div>
          </template>
          <div class="trend-box">
            <p>日记记录：<b>{{ stats.activeDiariesThisWeek ?? 0 }}</b></p>
            <p>咨询会话：<b>{{ stats.activeSessionsThisWeek ?? 0 }}</b></p>
            <p class="trend-hint">
              用于快速判断近期随访活跃度和干预频率。
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getDoctorDashboard, getMyPatients } from '@/api/doctor'

const userStore = useUserStore()
const loading = ref(false)
const stats = ref({})
const recentPatients = ref([])
const riskItems = computed(() => {
  const dist = stats.value?.riskDistribution || {}
  return [
    { level: 0, label: '正常', value: Number(dist[0] || 0), type: 'success' },
    { level: 1, label: '低风险', value: Number(dist[1] || 0), type: 'info' },
    { level: 2, label: '中风险', value: Number(dist[2] || 0), type: 'warning' },
    { level: 3, label: '高风险', value: Number(dist[3] || 0), type: 'danger' }
  ]
})

const displayName = computed(() => userStore.displayName)
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const riskLabel = (level) => {
  const map = { 0: '正常', 1: '低风险', 2: '中风险', 3: '高风险' }
  return map[level] ?? '未评估'
}
const riskColor = (level) => {
  const map = { 0: 'success', 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] ?? ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const [dashRes, patientsRes] = await Promise.all([
      getDoctorDashboard(),
      getMyPatients({ page: 1, size: 5 })
    ])
    stats.value = dashRes || {}
    recentPatients.value = patientsRes?.records || []
  } catch (e) {
    console.error('Dashboard fetch error:', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.doctor-dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 24px;
  h2 { margin: 0 0 4px; font-size: 1.4rem; }
  p { margin: 0; opacity: 0.85; font-size: 0.9rem; }
  .quick-actions {
    .el-button { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4); color: white;
      &:hover { background: rgba(255,255,255,0.35); }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
}
.stat-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.25rem;
}
.active-patients .stat-icon { background: #d1fae5; color: #059669; }
.risk-patients .stat-icon { background: #fee2e2; color: #dc2626; }
.total-notes .stat-icon { background: #dbeafe; color: #2563eb; }
.recent-reports .stat-icon { background: #fef3c7; color: #d97706; }

.stat-body {
  .stat-value { font-size: 1.5rem; font-weight: 700; color: #1f2937; margin: 0; }
  .stat-label { font-size: 0.8rem; color: #6b7280; margin: 4px 0 0; }
}

.recent-card {
  border-radius: 12px;
  .card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f9fafb; }
}
.recent-info { flex: 1; min-width: 0; }
.recent-name { margin: 0; font-weight: 500; color: #1f2937; }
.recent-risk { margin: 4px 0 0; }
.recent-arrow { color: #d1d5db; }
.chart-row { margin-top: 10px; }
.risk-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.risk-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
}
.risk-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}
.trend-box {
  p { margin: 10px 0; color: #374151; }
  b { color: #111827; }
  .trend-hint { font-size: 12px; color: #9ca3af; margin-top: 14px; }
}

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 500px) {
  .stats-grid { grid-template-columns: 1fr; }
  .welcome-banner { flex-direction: column; gap: 12px; text-align: center; }
}
</style>
