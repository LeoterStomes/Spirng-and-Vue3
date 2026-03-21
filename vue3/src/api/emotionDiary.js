import request from '@/utils/request'

/**
 * 创建或更新情绪日记
 * 功能描述：创建新的情绪日记记录，同一天已存在记录则更新
 * 入参：{ diaryDate: string, moodScore: number, dominantEmotion?: string, emotionTriggers?: string, diaryContent?: string, sleepQuality?: number, stressLevel?: number }
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, dominantEmotion: string, ... }
 * url地址：/emotion-diary
 * 请求方式：POST
 */
export function createOrUpdateEmotionDiary(params, callbacks = {}) {
  return request.post('/emotion-diary', params, {
    successMsg: '情绪日记保存成功',
    ...callbacks
  })
}

/**
 * 创建情绪日记（编辑模式）
 * 功能描述：在编辑模式下创建情绪日记，不检查重复
 * 入参：{ diaryDate: string, moodScore: number, dominantEmotion?: string, emotionTriggers?: string, diaryContent?: string, sleepQuality?: number, stressLevel?: number }
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, dominantEmotion: string, ... }
 * url地址：/emotion-diary?isEditMode=true
 * 请求方式：POST
 */
export function createEmotionDiaryInEditMode(params, callbacks = {}) {
  return request.post('/emotion-diary?isEditMode=true', params, {
    successMsg: '情绪日记保存成功',
    ...callbacks
  })
}

/**
 * 更新情绪日记
 * 功能描述：更新指定的情绪日记记录
 * 入参：{ id: number, moodScore?: number, dominantEmotion?: string, emotionTriggers?: string, diaryContent?: string, sleepQuality?: number, stressLevel?: number }
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, ... }
 * url地址：/emotion-diary/{id}
 * 请求方式：PUT
 */
export function updateEmotionDiary(id, params, callbacks = {}) {
  return request.put(`/emotion-diary/${id}`, params, {
    successMsg: '情绪日记更新成功',
    ...callbacks
  })
}

/**
 * 根据ID获取情绪日记详情
 * 功能描述：根据日记ID获取情绪日记详细信息
 * 入参：{ id: number }
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, dominantEmotion: string, ... }
 * url地址：/emotion-diary/{id}
 * 请求方式：GET
 */
export function getEmotionDiaryById(id, callbacks = {}) {
  return request.get(`/emotion-diary/${id}`, null, callbacks)
}

/**
 * 根据日期获取情绪日记
 * 功能描述：获取指定日期的情绪日记记录
 * 入参：{ date: string (yyyy-MM-dd) }
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, dominantEmotion: string, ... }
 * url地址：/emotion-diary/date/{date}
 * 请求方式：GET
 */
export function getEmotionDiaryByDate(date, callbacks = {}) {
  return request.get(`/emotion-diary/date/${date}`, null, callbacks)
}

/**
 * 获取今日情绪日记
 * 功能描述：获取当前用户今天的情绪日记记录
 * 入参：无
 * 返回参数：{ id: number, userId: number, diaryDate: string, moodScore: number, moodScoreDesc: string, dominantEmotion: string, ... }
 * url地址：/emotion-diary/today
 * 请求方式：GET
 */
export function getTodayEmotionDiary(callbacks = {}) {
  return request.get('/emotion-diary/today', null, callbacks)
}


/**
 * 分页查询情绪日记
 * 功能描述：根据条件分页查询用户的情绪日记列表
 * 入参：{ current?: number, size?: number, startDate?: string, endDate?: string, minMoodScore?: number, maxMoodScore?: number, dominantEmotion?: string, sleepQuality?: number, stressLevel?: number }
 * 返回参数：{ records: Array, total: number, current: number, size: number, pages: number }
 * url地址：/emotion-diary/page
 * 请求方式：GET
 */
export function getEmotionDiaryPage(params, callbacks = {}) {
  return request.get('/emotion-diary/page', params, callbacks)
}

/**
 * 删除情绪日记
 * 功能描述：删除指定的情绪日记记录
 * 入参：{ id: number }
 * 返回参数：成功信息
 * url地址：/emotion-diary/{id}
 * 请求方式：DELETE
 */
export function deleteEmotionDiary(id, callbacks = {}) {
  return request.delete(`/emotion-diary/${id}`, {
    successMsg: '情绪日记删除成功',
    ...callbacks
  })
}

/**
 * 获取情绪日记统计数据
 * 功能描述：获取用户的情绪日记统计分析数据
 * 入参：{ days?: number }
 * 返回参数：{ totalDays: number, recordedDays: number, completionRate: number, averageMoodScore: number, maxMoodScore: number, minMoodScore: number, positiveDays: number, negativeDays: number, neutralDays: number, moodTrend: Array, emotionDistribution: Object, suggestions: Array }
 * url地址：/emotion-diary/statistics
 * 请求方式：GET
 */
export function getEmotionDiaryStatistics(params, callbacks = {}) {
  return request.get('/emotion-diary/statistics', params, callbacks)
}

// ========== 管理员API ==========

/**
 * 管理员分页查询情绪日记
 * 功能描述：管理员查看所有用户的情绪日记记录
 * 入参：{ current?: number, size?: number, userId?: number, username?: string, startDate?: string, endDate?: string, minMoodScore?: number, maxMoodScore?: number, dominantEmotion?: string }
 * 返回参数：{ records: Array, total: number, current: number, size: number, pages: number }
 * url地址：/emotion-diary/admin/page
 * 请求方式：GET
 */
export function getAdminEmotionDiariesPage(params, callbacks = {}) {
  return request.get('/emotion-diary/admin/page', params, callbacks)
}

/**
 * 管理员获取情绪统计数据
 * 功能描述：获取全局情绪日记统计分析数据
 * 入参：{ days?: number, userId?: number }
 * 返回参数：EmotionDiaryStatisticsDTO
 * url地址：/emotion-diary/admin/statistics
 * 请求方式：GET
 */
export function getAdminEmotionStatistics(params, callbacks = {}) {
  return request.get('/emotion-diary/admin/statistics', params, callbacks)
}

/**
 * 管理员删除情绪日记
 * 功能描述：管理员删除指定的情绪日记记录
 * 入参：id (路径参数)
 * 返回参数：无
 * url地址：/emotion-diary/admin/{id}
 * 请求方式：DELETE
 */
export function adminDeleteEmotionDiary(id, callbacks = {}) {
  return request.delete(`/emotion-diary/admin/${id}`, {
    successMsg: '删除成功',
    ...callbacks
  })
}

/**
 * 管理员获取系统概览
 * 功能描述：获取情绪日记模块的系统概览数据
 * 入参：无
 * 返回参数：EmotionDiaryStatisticsDTO
 * url地址：/emotion-diary/admin/overview
 * 请求方式：GET
 */
export function getAdminEmotionOverview(callbacks = {}) {
  return request.get('/emotion-diary/admin/overview', null, callbacks)
}

/**
 * 获取AI情绪分析结果
 * 功能描述：获取指定日记的AI情绪分析结果
 * 入参：{ id: number }
 * 返回参数：EmotionAnalysisResult
 * url地址：/emotion-diary/{id}/ai-analysis
 * 请求方式：GET
 */
export function getAiEmotionAnalysis(id, callbacks = {}) {
  return request.get(`/emotion-diary/${id}/ai-analysis`, null, callbacks)
}

/**
 * 手动触发AI情绪分析
 * 功能描述：手动触发指定日记的AI情绪分析
 * 入参：{ id: number }
 * 返回参数：无
 * url地址：/emotion-diary/{id}/ai-analysis
 * 请求方式：POST
 */
export function triggerAiEmotionAnalysis(id, callbacks = {}) {
  return request.post(`/emotion-diary/${id}/ai-analysis`, null, {
    successMsg: 'AI分析已触发，请稍后查看结果',
    ...callbacks
  })
}

/**
 * 管理员手动触发AI情绪分析
 * 功能描述：管理员手动触发指定日记的AI情绪分析，支持重复分析
 * 入参：{ id: number }
 * 返回参数：无
 * url地址：/emotion-diary/admin/{id}/ai-analysis
 * 请求方式：POST
 */
export function adminTriggerAiEmotionAnalysis(id, callbacks = {}) {
  return request.post(`/emotion-diary/admin/${id}/ai-analysis`, null, {
    successMsg: 'AI分析已加入队列，请稍后查看结果',
    ...callbacks
  })
}

/**
 * 管理员批量触发AI情绪分析
 * 功能描述：管理员批量触发多个日记的AI情绪分析
 * 入参：{ diaryIds: number[] }
 * 返回参数：{ totalCount: number, successCount: number, failCount: number, failReasons: string[] }
 * url地址：/emotion-diary/admin/batch-ai-analysis
 * 请求方式：POST
 */
export function adminBatchTriggerAiEmotionAnalysis(diaryIds, callbacks = {}) {
  return request.post('/emotion-diary/admin/batch-ai-analysis', diaryIds, {
    successMsg: '批量AI分析任务已提交',
    ...callbacks
  })
}
