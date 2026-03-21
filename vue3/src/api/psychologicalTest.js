import request from '@/utils/request'

/**
 * 提交心理测试并生成报告
 */
export function submitPsychologicalTest(params, callbacks = {}) {
  return request.post('/psychological-test/reports', params, {
    successMsg: '测试已提交',
    ...callbacks
  })
}

/**
 * 获取当前用户报告列表
 */
export function getMyPsychologicalReports(callbacks = {}) {
  return request.get('/psychological-test/reports', null, callbacks)
}

/**
 * 获取报告详情（仅所属用户可访问）
 */
export function getMyPsychologicalReportDetail(reportId, callbacks = {}) {
  return request.get(`/psychological-test/reports/${reportId}`, null, callbacks)
}

/**
 * 从报告创建 AI 对话会话（并返回 sessionId）
 */
export function startAiChatFromPsychologicalReport(reportId, params, callbacks = {}) {
  return request.post(`/psychological-test/reports/${reportId}/start-ai-chat`, params, {
    successMsg: '已为你创建对话，会话正在处理中',
    ...callbacks
  })
}

