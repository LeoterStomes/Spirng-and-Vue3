<template>
  <div class="psychological-test-page">
    <div class="header-section">
      <div class="header-content">
        <div class="header-left">
          <div class="breathing-animation">
            <i class="fas fa-clipboard-check"></i>
          </div>
          <div class="header-text">
            <h2>心理测试</h2>
            <p>提交测评答案，生成你的AI心理测试报告，并可一键进入AI助手咨询。</p>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <el-row :gutter="20">
        <el-col :span="12">
          <!-- 测试选择 -->
          <el-card class="card-section" shadow="never">
            <h3 class="card-title">
              <i class="fas fa-layer-group"></i>
              请选择要做的测试
            </h3>

            <div class="test-cards">
              <el-card
                class="test-card"
                :class="{ active: testType === 'DEPRESSION' }"
                shadow="never"
                @click="testType = 'DEPRESSION'"
              >
                <div class="test-icon"><i class="fas fa-heartbeat"></i></div>
                <div class="test-name">抑郁倾向测试（简化版）</div>
                <div class="test-desc">帮助你初步了解情绪状态与风险水平。</div>
              </el-card>

              <el-card
                class="test-card"
                :class="{ active: testType === 'PERSONALITY' }"
                shadow="never"
                @click="testType = 'PERSONALITY'"
              >
                <div class="test-icon"><i class="fas fa-user-shield"></i></div>
                <div class="test-name">人格倾向测试（简化版）</div>
                <div class="test-desc">帮助你更好理解自己的性格与应对方式。</div>
              </el-card>

              <el-card
                class="test-card"
                :class="{ active: testType === 'ENNEAGRAM_108' }"
                shadow="never"
                @click="testType = 'ENNEAGRAM_108'"
              >
                <div class="test-icon"><i class="fas fa-project-diagram"></i></div>
                <div class="test-name">九型人格测试（108题）</div>
                <div class="test-desc">根据108题统计九种人格记号数，识别主要人格倾向。</div>
              </el-card>
            </div>

            <div class="form-group">
              <label class="form-label">报告标题（可选）</label>
              <el-input v-model="title" placeholder="例如：我的心理状态测评报告" maxlength="200" show-word-limit />
            </div>
          </el-card>

          <!-- 测试表单 -->
          <el-card class="card-section" shadow="never" style="margin-top: 18px;">
            <h3 class="card-title">
              <i class="fas fa-question-circle"></i>
              {{ testType === 'DEPRESSION' ? '抑郁倾向测试' : (testType === 'PERSONALITY' ? '人格倾向测试' : '九型人格测试（108题）') }}
            </h3>

            <div class="question-list">
              <div
                v-for="q in questions"
                :key="q.id"
                class="question-item"
              >
                <div class="question-text">{{ q.text }}</div>
                <el-radio-group v-model="answers[q.id]" class="radio-group">
                  <el-radio
                    v-for="opt in q.options"
                    :key="opt.score"
                    :label="opt.score"
                  >
                    {{ opt.label }}
                  </el-radio>
                </el-radio-group>
              </div>
            </div>

            <div class="action-buttons">
              <el-button @click="resetAnswers">
                重置
              </el-button>
              <el-button
                type="primary"
                :loading="submitting"
                @click="submitTest"
              >
                提交并生成报告
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <!-- 报告列表 -->
          <el-card class="card-section" shadow="never">
            <h3 class="card-title">
              <i class="fas fa-file-alt"></i>
              我的测试报告
            </h3>

            <el-table :data="reportList" style="width: 100%" v-loading="loadingReports" height="360">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="testTypeDisplayName" label="类型" width="140" />
              <el-table-column prop="statusDisplayName" label="状态" width="120" />
              <el-table-column label="操作" width="140">
                <template #default="scope">
                  <el-button size="small" @click="selectReport(scope.row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="!loadingReports && reportList.length === 0" class="empty-hint">
              暂无报告记录。完成测试后会在这里生成。
            </div>
          </el-card>

          <!-- 报告详情 -->
          <el-card class="card-section" shadow="never" style="margin-top: 18px;">
            <h3 class="card-title">
              <i class="fas fa-book-open"></i>
              报告详情
            </h3>

            <div v-if="activeReport">
              <el-descriptions border :column="1">
                <el-descriptions-item label="标题">{{ activeReport.title || '未命名报告' }}</el-descriptions-item>
                <el-descriptions-item label="类型">{{ activeReport.testTypeDisplayName || activeReport.testType }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ activeReport.statusDisplayName || activeReport.status }}</el-descriptions-item>
                <el-descriptions-item label="总分" v-if="activeReport.totalScore !== null && activeReport.totalScore !== undefined">
                  {{ activeReport.totalScore }}
                </el-descriptions-item>
                <el-descriptions-item label="风险等级" v-if="activeReport.riskLevel !== null && activeReport.riskLevel !== undefined">
                  {{ activeReport.riskLevel }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="report-block" v-if="activeReport.status === 'COMPLETED'">
                <h4 class="sub-title">报告摘要</h4>
                <p class="report-text">{{ activeReport.reportSummary }}</p>

                <h4 class="sub-title" style="margin-top: 14px;">报告正文</h4>
                <div class="report-text report-content" v-html="formatReportText(activeReport.reportContent)"></div>

                <div class="action-buttons" style="justify-content: flex-end;">
                  <el-button
                    type="primary"
                    @click="startAiChat"
                    :loading="startingAi"
                  >
                    一键进入AI助手
                  </el-button>
                </div>
              </div>

              <div class="report-block" v-else>
                <div class="loading-hint">
                  报告正在生成中，请稍后查看。
                </div>
                <div v-if="activeReport.status === 'FAILED'" class="error-hint">
                  生成失败：{{ activeReport.errorMessage }}
                </div>
              </div>
            </div>

            <div v-else class="empty-hint">
              请选择一份报告查看详情。
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  submitPsychologicalTest,
  getMyPsychologicalReports,
  getMyPsychologicalReportDetail,
  startAiChatFromPsychologicalReport
} from '@/api/psychologicalTest'

const router = useRouter()

const testType = ref('DEPRESSION')
const title = ref('')
const submitting = ref(false)
const startingAi = ref(false)

const loadingReports = ref(false)
const reportList = ref([])
const activeReport = ref(null)

const pollTimer = ref(null)
const pollReportId = ref(null)

// 简化版题库：前端仅负责采集答案并交给AI，具体结构由后端prompt解析
const depressionQuestions = [
  { id: 'D1', text: '最近两周，我感到情绪低落或难以开心。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] },
  { id: 'D2', text: '我对日常活动的兴趣明显下降。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] },
  { id: 'D3', text: '我感到疲惫或精力不足。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] },
  { id: 'D4', text: '我难以入睡/睡眠质量下降。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] },
  { id: 'D5', text: '我感到自责、内疚或觉得自己不够好。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] },
  { id: 'D6', text: '我感到焦虑或担忧明显增加。', options: [{ score: 0, label: '几乎没有' }, { score: 1, label: '偶尔有' }, { score: 2, label: '经常有' }, { score: 3, label: '非常明显' }] }
]

const personalityQuestions = [
  { id: 'P1', text: '我更喜欢独处来充电。', options: [{ score: 0, label: '完全不符合' }, { score: 1, label: '有点符合' }, { score: 2, label: '较符合' }, { score: 3, label: '非常符合' }] },
  { id: 'P2', text: '我在压力下会更倾向于冷静分析而不是立刻行动。', options: [{ score: 0, label: '完全不符合' }, { score: 1, label: '有点符合' }, { score: 2, label: '较符合' }, { score: 3, label: '非常符合' }] },
  { id: 'P3', text: '我通常会提前规划，确保事情更可控。', options: [{ score: 0, label: '完全不符合' }, { score: 1, label: '有点符合' }, { score: 2, label: '较符合' }, { score: 3, label: '非常符合' }] },
  { id: 'P4', text: '我在表达情感时较谨慎，但内心感受很丰富。', options: [{ score: 0, label: '完全不符合' }, { score: 1, label: '有点符合' }, { score: 2, label: '较符合' }, { score: 3, label: '非常符合' }] },
  { id: 'P5', text: '我遇到问题时会倾向于寻求他人建议。', options: [{ score: 0, label: '几乎不' }, { score: 1, label: '有时' }, { score: 2, label: '经常' }, { score: 3, label: '总是' }] }
]

// 九型人格测试（108题）
const enneagramQuestions = [
  { id: 'E1', typeNo: 9, text: '我很容易迷惑。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E2', typeNo: 1, text: '我不想成为一个喜欢批评的人，但很难做到。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E3', typeNo: 5, text: '我喜欢研究宇宙的道理、哲理。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E4', typeNo: 7, text: '我很注意自己是否年轻，因为那是找乐子的本钱。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E5', typeNo: 8, text: '我喜欢独自自主，一切以自己为中心。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E6', typeNo: 2, text: '当我有困难时，我会试着不让人知道。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E7', typeNo: 4, text: '被人误解对我而言是一件十分痛苦的事。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E8', typeNo: 2, text: '施比受会给我更大的满足感。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E9', typeNo: 6, text: '我常常设想最糟的结果而使自己陷入苦恼中。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E10', typeNo: 6, text: '我常常试探或考验朋友、伴侣的忠诚。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E11', typeNo: 8, text: '我看不起那些不像我一样坚强的人，有时我会用种种方式羞辱他们。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E12', typeNo: 9, text: '身体上的舒适对我非常重要。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E13', typeNo: 4, text: '我能触碰生活中的悲伤和不幸。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E14', typeNo: 1, text: '别人不能完成他的分内事，会令我失望和愤怒。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E15', typeNo: 9, text: '我时常拖延问题，不去解决。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E16', typeNo: 7, text: '我喜欢戏剧性、多彩多姿的生活。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E17', typeNo: 4, text: '我认为自己非常的不完善。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E18', typeNo: 7, text: '我对感官的需求特别强烈，喜欢美食、服装、身体的触觉刺激，并纵情享受。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E19', typeNo: 5, text: '当别人请教我一些问题，我会巨细无遗地分析得很清楚。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E20', typeNo: 3, text: '我习惯推销自己，从不觉得难为情。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E21', typeNo: 7, text: '有时我会放纵和做出僭越的事。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E22', typeNo: 2, text: '帮助不到别人会让我觉得痛苦。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E23', typeNo: 5, text: '我不喜欢人家问我关于广泛、笼统的问题。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E24', typeNo: 8, text: '在某些方面我有放纵的倾向（例如食物、药物等）。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E25', typeNo: 9, text: '我宁愿适应别人，包括我的伴侣，而不会反抗他们。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E26', typeNo: 6, text: '我最不喜欢的一件事就是虚伪。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E27', typeNo: 8, text: '我知错能改，但由于执著好强，周围的人还是感觉到压力。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E28', typeNo: 7, text: '我常觉得很多事情都很好玩，很有趣，人生真是快乐。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E29', typeNo: 6, text: '我有时很欣赏自己充满权威，有时却又优柔寡断，依赖别人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E30', typeNo: 2, text: '我习惯付出多于接受。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E31', typeNo: 6, text: '面对威胁时，我一是变得焦虑，一是对抗迎面而来的危险。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E32', typeNo: 5, text: '我通常是等别人来接近我，而不是我去接过他们。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E33', typeNo: 3, text: '我喜欢当主角，希望得到大家的注意。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E34', typeNo: 9, text: '别人批评我，我也不会回应和辩解，因为我不想发生任何争执与冲突。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E35', typeNo: 6, text: '我有时期待别人的指导，有时却忽略别人的忠告径直去做我想做的事。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E36', typeNo: 9, text: '我经常忘记自己的需要。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E37', typeNo: 6, text: '在重大危机中，我通常能克服我对自己的质疑和内心的焦虑。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E38', typeNo: 3, text: '我是一个天生的推销员，说服别人对我来说是一件轻易的事。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E39', typeNo: 9, text: '我不相信一个我一直都无法了解的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E40', typeNo: 8, text: '我爱依惯例行事，不大喜欢改变。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E41', typeNo: 9, text: '我很在乎家人，在家中表现得忠诚和包容。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E42', typeNo: 5, text: '我被动而优柔寡断。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E43', typeNo: 5, text: '我很有包容力，彬彬有礼，但跟人的感情互动不深。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E44', typeNo: 8, text: '我沉默寡言，好像不会关心别人似的。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E45', typeNo: 6, text: '当沉浸在工作或我擅长的领域时，别人会觉得我冷酷无情。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E46', typeNo: 6, text: '我常常保持警觉。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E47', typeNo: 5, text: '我不喜欢要对人尽义务的感觉。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E48', typeNo: 5, text: '如果不能完美地表态，这宁愿不说。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E49', typeNo: 7, text: '我的计划比我实际完成的还要多。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E50', typeNo: 8, text: '我野心勃勃，喜欢挑战和登上高峰的经验。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E51', typeNo: 5, text: '我倾向于独断行并自己解决问题。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E52', typeNo: 4, text: '我很多时候感到被遗弃。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E53', typeNo: 4, text: '我常常表现得十分忧郁的样子，充满痛苦而且内向。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E54', typeNo: 4, text: '初见陌生人时，我会表现得很冷漠、高傲。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E55', typeNo: 1, text: '我的面部表情严肃而生硬。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E56', typeNo: 4, text: '我很飘忽，常常不知自己下一刻想要什么。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E57', typeNo: 1, text: '我常对自己挑剔，期望不断改善自己的缺点，以成为一个完美的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E58', typeNo: 4, text: '我感受特别深刻，并怀疑那些总是很快乐的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E59', typeNo: 3, text: '我做事有效率，也会找捷径，模仿力特强。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E60', typeNo: 1, text: '我讲理、重实用。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E61', typeNo: 4, text: '我有很强的创造天分和想象力，喜欢将事情重新整合。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E62', typeNo: 9, text: '我不要求得到很多的注意力。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E63', typeNo: 1, text: '我喜欢每件事都井然有序，但别人会认为我过分执著。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E64', typeNo: 4, text: '我渴望拥有完美的心灵伴侣。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E65', typeNo: 3, text: '我常夸耀自己，对自己的能力十分有信心。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E66', typeNo: 8, text: '如果周遭的人行为太过分时，我准会让他难堪。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E67', typeNo: 3, text: '我外向、精力充沛，喜欢不断追求成就，这使我的自我感觉十分良好。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E68', typeNo: 6, text: '我是一位忠实的朋友和伙伴。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E69', typeNo: 2, text: '我知道如何让别人喜欢我。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E70', typeNo: 3, text: '我很少看到别人的功劳和好处。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E71', typeNo: 2, text: '我很容易知道别人的功劳和好处。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E72', typeNo: 3, text: '我嫉妒心强，喜欢跟别人比较。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E73', typeNo: 1, text: '我对别人做的事总是不放心，批评一番后，自己会动手再做。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E74', typeNo: 3, text: '别人会说我常带着面具做人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E75', typeNo: 6, text: '有时我会激怒对方，引来慕名其妙的吵架，其实是想试探对方爱不爱我。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E76', typeNo: 8, text: '我会极力保护我所爱的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E77', typeNo: 3, text: '我常常刻意保持兴奋的情绪。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E78', typeNo: 7, text: '我只喜欢与有趣的人为友，对一些闷蛋却懒得交往，即使他们看来很有深度。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E79', typeNo: 2, text: '我常往外跑，四处帮助别人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E80', typeNo: 3, text: '有时我会讲求效率而牺牲完美和原则。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E81', typeNo: 1, text: '我似乎不太懂得幽默，没有弹性。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E82', typeNo: 2, text: '我待人热情而有耐性。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E83', typeNo: 5, text: '在人群中我时常感到害羞和不安。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E84', typeNo: 8, text: '我喜欢效率，讨厌拖泥带水。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E85', typeNo: 2, text: '帮助别人达致快乐和成功是我重要的成就。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E86', typeNo: 2, text: '付出时，别人若不欣然接纳，我便会挫折感。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E87', typeNo: 1, text: '我的肢体硬梆梆的，不习惯别人热情的付出。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E88', typeNo: 5, text: '我对大部分的社交集会不太有兴趣，除非那是我熟识的和喜爱的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E89', typeNo: 2, text: '很多时我会有强烈的寂寞感。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E90', typeNo: 2, text: '人们很乐意向我表白他们所遭遇的问题。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E91', typeNo: 1, text: '我不但不会说甜言蜜语，而且别人会觉得我唠叨不停。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E92', typeNo: 7, text: '我常担心自由被剥夺，因此不爱作承诺。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E93', typeNo: 3, text: '我喜欢告诉别人我所做的事和所知的一切。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E94', typeNo: 9, text: '我很容易认同别人我所做的事和所知的一切。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E95', typeNo: 8, text: '我要求光明正大，为此不惜与人发生冲突。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E96', typeNo: 8, text: '我很有正义感，有时会支持不利的一方。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E97', typeNo: 1, text: '我注重小节而效率不高。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E98', typeNo: 9, text: '我容易感到沮丧和麻木更多于愤怒。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E99', typeNo: 5, text: '我不喜欢那些侵略性或过度情绪化的人。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E100', typeNo: 4, text: '我非常情绪化，一天的喜怒哀乐多变。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E101', typeNo: 5, text: '我不想别人知道我的感受与想法，除非我告诉他们。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E102', typeNo: 1, text: '我喜欢刺激和紧张的关系，而不是稳定和依赖的关系。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E103', typeNo: 7, text: '我很少用心去听别人的心情，只喜欢说说俏皮话和笑话。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E104', typeNo: 1, text: '我是循规蹈矩的人，秩序对我十分有意义。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E105', typeNo: 4, text: '我很难找到一种我真正感到被爱的关系。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E106', typeNo: 1, text: '假如我想要结束一段关系，我不是直接告诉对方就是激怒他来让他离开我。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E107', typeNo: 9, text: '我温和平静，不自夸，不爱与人竞争。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] },
  { id: 'E108', typeNo: 9, text: '我有时善良可爱，有时有粗野暴躁，很难捉摸。', options: [{ score: 1, label: '符合' }, { score: 0, label: '不符合' }] }
]

const questions = computed(() => {
  if (testType.value === 'DEPRESSION') return depressionQuestions
  if (testType.value === 'PERSONALITY') return personalityQuestions
  return enneagramQuestions
})

// answers：用于提交到后端，key=题目id value=score
const answers = reactive({})

const resetAnswers = () => {
  Object.keys(answers).forEach(k => delete answers[k])
}

const validateAllAnswered = () => {
  for (const q of questions.value) {
    if (answers[q.id] === undefined || answers[q.id] === null) return false
  }
  return true
}

const submitTest = async () => {
  if (!validateAllAnswered()) {
    ElMessage.warning('请先完成所有题目选择')
    return
  }

  submitting.value = true
  try {
    const payload = {
      testType: testType.value,
      title: title.value || undefined,
      answers: {
        scores: { ...answers },
        filledAt: Date.now(),
        version: 1
      }
    }

    // 九型人格额外携带分型统计，便于后端AI生成更准确的报告
    if (testType.value === 'ENNEAGRAM_108') {
      const typeCounts = {}
      for (let i = 1; i <= 9; i++) typeCounts[i] = 0

      for (const q of enneagramQuestions) {
        const selected = Number(answers[q.id] ?? 0)
        if (selected === 1) {
          typeCounts[q.typeNo] = (typeCounts[q.typeNo] || 0) + 1
        }
      }

      const sorted = Object.entries(typeCounts).sort((a, b) => b[1] - a[1])
      payload.answers.typeCounts = typeCounts
      payload.answers.topTypes = sorted.slice(0, 3).map(([type, count]) => ({ type: Number(type), count }))
      payload.answers.scoringRule = '统计每道“符合=1，不符合=0”，按题目标注的九型编号累计，最高者为主要人格倾向。'
    }

    const resp = await submitPsychologicalTest(payload)
    const reportId = resp?.id
    if (!reportId) {
      throw new Error('后端未返回reportId')
    }

    ElMessage.success(resp?.status === 'COMPLETED' ? '报告已生成完成' : '已提交成功，报告正在生成中…')
    pollReportId.value = reportId
    activeReport.value = resp

    // 刷新列表
    await refreshReports()

    // 仅在仍处于生成中时轮询
    if (resp?.status === 'PROCESSING') {
      startPollingReport(reportId)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const refreshReports = async () => {
  loadingReports.value = true
  try {
    const list = await getMyPsychologicalReports()
    reportList.value = list || []
  } finally {
    loadingReports.value = false
  }
}

const selectReport = async (row) => {
  activeReport.value = row
  if (row?.id) {
    try {
      const detail = await getMyPsychologicalReportDetail(row.id)
      activeReport.value = detail
    } catch (e) {
      console.warn('加载报告详情失败:', e?.message || e)
    }
  }
}

const startPollingReport = (reportId) => {
  // 防重入
  if (pollTimer.value) clearInterval(pollTimer.value)

  pollTimer.value = setInterval(async () => {
    if (!pollReportId.value) return

    try {
      const detail = await getMyPsychologicalReportDetail(pollReportId.value)
      activeReport.value = detail

      if (detail?.status === 'COMPLETED' || detail?.status === 'FAILED') {
        clearInterval(pollTimer.value)
        pollTimer.value = null
        pollReportId.value = null
        await refreshReports()
      }
    } catch (e) {
      console.warn('轮询报告失败:', e?.message || e)
    }
  }, 2000)
}

const formatReportText = (text) => {
  // 前端简单换行渲染
  return String(text || '').replace(/\n/g, '<br/>')
}

const startAiChat = async () => {
  if (!activeReport.value?.id) return
  startingAi.value = true
  try {
    const session = await startAiChatFromPsychologicalReport(activeReport.value.id, {
      sessionTitle: `心理测试报告咨询 - ${activeReport.value.title || activeReport.value.id}`
    })

    if (!session?.sessionId) {
      throw new Error('后端未返回sessionId')
    }

    // 跳转到AI咨询页面，并让咨询页根据query自动触发首次AI回复
    router.push({
      path: '/consultation',
      query: {
        sessionId: session.sessionId
      }
    })
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '进入AI对话失败')
  } finally {
    startingAi.value = false
  }
}

onMounted(async () => {
  await refreshReports()
})

onBeforeUnmount(() => {
  if (pollTimer.value) clearInterval(pollTimer.value)
})
</script>

<style scoped>
.psychological-test-page {
  min-height: 100vh;
  background: #f5f7fb;
}

.header-section {
  background: #ffffff;
  color: #111827;
  border-bottom: 1px solid #e5e7eb;
  padding: 2rem 0;
}

.header-content {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 1rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.breathing-animation i {
  font-size: 1.9rem;
  color: #2563eb;
}

.header-text h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.85rem;
  font-weight: 700;
}

.header-text p {
  margin: 0;
  font-size: 0.96rem;
  color: #4b5563;
}

.main-content {
  max-width: 1160px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
}

.card-section {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #374151;
}

.test-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.test-card {
  cursor: pointer;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 12px;
  transition: all 0.2s ease;
  background: #ffffff;
}

.test-card.active {
  border-color: #2563eb;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.13);
  background: #f8fbff;
}

.test-icon i {
  font-size: 1.4rem;
  color: #2563eb;
}

.test-name {
  font-weight: 700;
  margin-top: 6px;
}

.test-desc {
  font-size: 0.9rem;
  color: #6b7280;
  margin-top: 6px;
  line-height: 1.5;
}

.form-group {
  margin-top: 12px;
}

.form-label {
  display: block;
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 6px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.question-item {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fbfdff;
}

.question-text {
  font-weight: 650;
  color: #374151;
  margin-bottom: 10px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.empty-hint {
  padding: 20px 0;
  text-align: center;
  color: #9ca3af;
  font-size: 0.9rem;
}

.report-block {
  margin-top: 12px;
}

.sub-title {
  margin: 0 0 8px 0;
  font-size: 1rem;
  font-weight: 800;
  color: #374151;
}

.report-text {
  white-space: pre-wrap;
  color: #4b5563;
  line-height: 1.7;
  font-size: 0.95rem;
}

.report-content {
  border: 1px solid #e5e7eb;
  padding: 12px;
  border-radius: 12px;
  background: #fbfdff;
}

.loading-hint {
  padding: 14px;
  color: #6b7280;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  background: #f8fafc;
}

.error-hint {
  margin-top: 10px;
  color: #dc2626;
  background: rgba(220, 38, 38, 0.05);
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(220, 38, 38, 0.2);
}
</style>

