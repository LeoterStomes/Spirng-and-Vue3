import request from '@/utils/request'

/**
 * 获取综合数据分析
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getDataAnalytics(params, callbacks = {}) {
  return request.get('/data-analytics/overview', params, callbacks)
}

/**
 * 获取情绪热力图数据
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getEmotionHeatmap(params, callbacks = {}) {
  return request.get('/data-analytics/emotion-heatmap', params, callbacks)
}

/**
 * 获取系统概览数据
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getSystemOverview(params, callbacks = {}) {
  return request.get('/data-analytics/system-overview', params, callbacks)
}

/**
 * 获取情绪趋势数据
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getEmotionTrend(params, callbacks = {}) {
  return request.get('/data-analytics/emotion-trend', params, callbacks)
}

/**
 * 获取咨询会话统计
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getConsultationStats(params, callbacks = {}) {
  return request.get('/data-analytics/consultation-stats', params, callbacks)
}

/**
 * 获取用户活跃度数据
 * @param {object} params - 请求参数
 * @param {number} params.days - 分析天数
 * @param {object} callbacks - 回调函数
 * @returns {Promise}
 */
export function getUserActivity(params, callbacks = {}) {
  return request.get('/data-analytics/user-activity', params, callbacks)
}
