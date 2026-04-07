<template>
  <div
    v-loading="loading"
    class="patient-detail"
  >
    <div class="top-bar">
      <el-button @click="$router.push('/doctor/patients')">
        <i class="fas fa-arrow-left" /> 返回患者列表
      </el-button>
      <el-button
        type="primary"
        :loading="savingProfile"
        @click="saveProfile"
      >
        保存档案
      </el-button>
    </div>

    <el-row :gutter="16">
      <el-col
        :lg="16"
        :md="24"
      >
        <el-card
          shadow="never"
          class="card"
        >
          <template #header>
            <div class="card-header">
              患者档案
            </div>
          </template>
          <el-form label-width="110px">
            <el-form-item label="患者昵称">
              <el-input
                :model-value="overview.nickname || overview.username || '-'"
                disabled
              />
            </el-form-item>
            <el-form-item label="风险等级">
              <el-select
                v-model="profile.riskLevel"
                style="width: 100%"
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
            </el-form-item>
            <el-form-item label="沟通风格">
              <el-select
                v-model="profile.communicationStyle"
                style="width: 100%"
              >
                <el-option
                  label="温和引导"
                  value="gentle"
                />
                <el-option
                  label="直接明确"
                  value="direct"
                />
                <el-option
                  label="鼓励支持"
                  value="encouraging"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="主诉">
              <el-input
                v-model="profile.chiefComplaint"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
            <el-form-item label="诊断摘要">
              <el-input
                v-model="profile.diagnosisSummary"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
            <el-form-item label="治疗计划">
              <el-input
                v-model="profile.treatmentPlan"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
            <el-form-item label="用药备注">
              <el-input
                v-model="profile.medicationNotes"
                type="textarea"
                :rows="2"
              />
            </el-form-item>
            <el-form-item label="性格特征">
              <el-input
                v-model="profile.personalityTraits"
                type="textarea"
                :rows="2"
              />
            </el-form-item>
            <el-form-item label="标签JSON">
              <el-input
                v-model="profile.tags"
                type="textarea"
                :rows="2"
                placeholder="例如: [&quot;失眠&quot;,&quot;学业压力&quot;]"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card
          shadow="never"
          class="card"
        >
          <template #header>
            <div class="card-header">
              临床备注
            </div>
          </template>
          <el-form inline>
            <el-form-item label="类型">
              <el-select
                v-model="newNote.noteType"
                style="width: 160px"
              >
                <el-option
                  label="日常"
                  value="daily"
                />
                <el-option
                  label="咨询"
                  value="consultation"
                />
                <el-option
                  label="风险预警"
                  value="risk_alert"
                />
                <el-option
                  label="随访"
                  value="followup"
                />
              </el-select>
            </el-form-item>
          </el-form>
          <el-input
            v-model="newNote.content"
            type="textarea"
            :rows="4"
            placeholder="请输入临床备注..."
          />
          <div class="note-action">
            <el-button
              type="primary"
              :loading="savingNote"
              @click="submitNote"
            >
              添加备注
            </el-button>
          </div>

          <div
            v-if="notes.length"
            class="note-list"
          >
            <div
              v-for="n in notes"
              :key="n.id"
              class="note-item"
            >
              <div class="note-title">
                <el-tag
                  size="small"
                  effect="plain"
                >
                  {{ n.noteType }}
                </el-tag>
                <span>{{ formatTime(n.createdAt) }}</span>
              </div>
              <div class="note-content">
                {{ n.content }}
              </div>
            </div>
          </div>
          <el-empty
            v-else
            description="暂无备注"
          />
        </el-card>
      </el-col>

      <el-col
        :lg="8"
        :md="24"
      >
        <el-card
          shadow="never"
          class="card"
        >
          <template #header>
            <div class="card-header">
              最近动态
            </div>
          </template>
          <div class="mini-section">
            <h4>近期日记</h4>
            <el-empty
              v-if="!overview.recentDiaries?.length"
              description="暂无"
              :image-size="60"
            />
            <div
              v-else
              class="mini-list"
            >
              <div
                v-for="d in overview.recentDiaries"
                :key="d.id"
                class="mini-item"
              >
                <span>情绪分: {{ d.moodScore ?? '-' }}</span>
                <span>{{ formatTime(d.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="mini-section">
            <h4>近期会话</h4>
            <el-empty
              v-if="!overview.recentSessions?.length"
              description="暂无"
              :image-size="60"
            />
            <div
              v-else
              class="mini-list"
            >
              <div
                v-for="s in overview.recentSessions"
                :key="s.id"
                class="mini-item"
              >
                <span>{{ s.title || '咨询会话' }}</span>
                <span>{{ formatTime(s.startedAt) }}</span>
              </div>
            </div>
          </div>
          <div class="mini-section">
            <h4>近期测试报告</h4>
            <el-empty
              v-if="!overview.recentReports?.length"
              description="暂无"
              :image-size="60"
            />
            <div
              v-else
              class="mini-list"
            >
              <div
                v-for="r in overview.recentReports"
                :key="r.id"
                class="mini-item"
              >
                <span>{{ r.title || r.testType || '报告' }}</span>
                <span>{{ formatTime(r.createdAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getPatientOverview,
  getPatientProfile,
  updatePatientProfile,
  addClinicalNote,
  getClinicalNotes
} from '@/api/doctor'

const route = useRoute()
const patientId = route.params.patientId

const loading = ref(false)
const savingProfile = ref(false)
const savingNote = ref(false)

const overview = ref({})
const notes = ref([])
const profile = reactive({
  chiefComplaint: '',
  diagnosisSummary: '',
  riskLevel: 0,
  treatmentPlan: '',
  medicationNotes: '',
  personalityTraits: '',
  communicationStyle: 'gentle',
  tags: '',
  status: 'active'
})
const newNote = reactive({
  noteType: 'daily',
  content: ''
})

const formatTime = (v) => {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const [overviewRes, profileRes, noteRes] = await Promise.all([
      getPatientOverview(patientId),
      getPatientProfile(patientId),
      getClinicalNotes(patientId, { page: 1, size: 20 })
    ])
    overview.value = overviewRes || {}
    if (profileRes) Object.assign(profile, profileRes)
    notes.value = noteRes?.records || []
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  savingProfile.value = true
  try {
    await updatePatientProfile(patientId, profile)
  } finally {
    savingProfile.value = false
  }
}

const submitNote = async () => {
  if (!newNote.content.trim()) {
    ElMessage.warning('请输入备注内容')
    return
  }
  savingNote.value = true
  try {
    await addClinicalNote(patientId, {
      noteType: newNote.noteType,
      content: newNote.content.trim()
    })
    newNote.content = ''
    const listRes = await getClinicalNotes(patientId, { page: 1, size: 20 })
    notes.value = listRes?.records || []
  } finally {
    savingNote.value = false
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.patient-detail {
  max-width: 1280px;
  margin: 0 auto;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card {
  border-radius: 12px;
  margin-bottom: 16px;
}
.card-header {
  font-weight: 600;
  color: #1f2937;
}
.note-action {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.note-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.note-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
}
.note-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  color: #6b7280;
  font-size: 12px;
}
.note-content {
  color: #1f2937;
  white-space: pre-wrap;
}
.mini-section {
  margin-bottom: 14px;
  h4 {
    margin: 0 0 8px;
    font-size: 14px;
    color: #374151;
  }
}
.mini-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mini-item {
  font-size: 12px;
  color: #4b5563;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
</style>
