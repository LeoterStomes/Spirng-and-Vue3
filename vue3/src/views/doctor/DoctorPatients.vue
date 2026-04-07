<template>
  <div class="doctor-patients">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <h2>我的患者</h2>
      <el-button
        type="primary"
        @click="showBindDialog = true"
      >
        <i class="fas fa-user-plus" /> 绑定新患者
      </el-button>
    </div>

    <!-- 搜索与筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索患者姓名 / 用户名"
        prefix-icon="Search"
        clearable
        style="width: 280px"
        @keyup.enter="fetchPatients"
        @clear="fetchPatients"
      />
      <el-select
        v-model="riskFilter"
        placeholder="风险等级"
        clearable
        style="width: 140px"
        @change="fetchPatients"
      >
        <el-option
          label="正常"
          :value="0"
        />
        <el-option
          label="低风险"
          :value="1"
        />
        <el-option
          label="中风险"
          :value="2"
        />
        <el-option
          label="高风险"
          :value="3"
        />
      </el-select>
      <el-button @click="fetchPatients">
        搜索
      </el-button>
    </div>

    <!-- 患者列表 -->
    <el-table
      v-loading="loading"
      :data="patients"
      stripe
      style="width: 100%"
      @row-click="goDetail"
    >
      <el-table-column
        label="患者"
        min-width="180"
      >
        <template #default="{ row }">
          <div class="patient-cell">
            <el-avatar :size="36">
              {{ (row.nickname || row.username || '?').charAt(0) }}
            </el-avatar>
            <div>
              <p class="cell-name">
                {{ row.nickname || row.username }}
              </p>
              <p class="cell-sub">
                ID: {{ row.patientId }}
              </p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="风险等级"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-tag
            :type="riskColor(row.riskLevel)"
            size="small"
            effect="plain"
          >
            {{ riskLabel(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="diagnosisSummary"
        label="诊断摘要"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        label="绑定时间"
        width="170"
        align="center"
      >
        <template #default="{ row }">
          {{ row.bindAt || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="160"
        align="center"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click.stop="goDetail(row)"
          >
            详情
          </el-button>
          <el-popconfirm
            title="确定解绑该患者?"
            @confirm="handleUnbind(row.patientId)"
          >
            <template #reference>
              <el-button
                link
                type="danger"
                size="small"
                @click.stop
              >
                解绑
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchPatients"
        @size-change="fetchPatients"
      />
    </div>

    <!-- 绑定患者对话框 -->
    <el-dialog
      v-model="showBindDialog"
      title="绑定新患者"
      width="420px"
      destroy-on-close
    >
      <el-form @submit.prevent="handleBind">
        <el-form-item label="患者关键词">
          <el-input
            v-model="bindKeyword"
            placeholder="输入用户名 / 手机号 / 邮箱"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="binding"
          @click="handleBind"
        >
          绑定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyPatients, bindPatient, unbindPatient } from '@/api/doctor'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const patients = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const riskFilter = ref(null)

const showBindDialog = ref(false)
const bindKeyword = ref('')
const binding = ref(false)

const riskLabel = (level) => ({ 0: '正常', 1: '低风险', 2: '中风险', 3: '高风险' }[level] ?? '未评估')
const riskColor = (level) => ({ 0: 'success', 1: 'info', 2: 'warning', 3: 'danger' }[level] ?? '')

const fetchPatients = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (riskFilter.value !== null && riskFilter.value !== '') params.riskLevel = riskFilter.value
    const res = await getMyPatients(params)
    patients.value = res?.records || []
    total.value = Number(res?.total || 0)
  } finally {
    loading.value = false
  }
}

const goDetail = (row) => {
  router.push(`/doctor/patients/${row.patientId}`)
}

const handleBind = async () => {
  if (!bindKeyword.value.trim()) { ElMessage.warning('请输入关键词'); return }
  binding.value = true
  try {
    await bindPatient(bindKeyword.value.trim())
    showBindDialog.value = false
    bindKeyword.value = ''
    await fetchPatients()
  } finally {
    binding.value = false
  }
}

const handleUnbind = async (patientId) => {
  await unbindPatient(patientId)
  fetchPatients()
}

onMounted(fetchPatients)
</script>

<style lang="scss" scoped>
.doctor-patients { max-width: 1200px; margin: 0 auto; }

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 1.3rem; color: #1f2937; }
}

.filter-bar {
  display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap;
}

.patient-cell {
  display: flex; align-items: center; gap: 10px;
  .cell-name { margin: 0; font-weight: 500; color: #1f2937; }
  .cell-sub { margin: 2px 0 0; font-size: 0.75rem; color: #9ca3af; }
}

.pagination-wrap {
  display: flex; justify-content: flex-end; margin-top: 16px;
}

:deep(.el-table) {
  border-radius: 8px;
  .el-table__row { cursor: pointer; }
}
</style>
