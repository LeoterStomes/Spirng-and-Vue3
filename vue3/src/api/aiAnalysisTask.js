import request from '@/utils/request'

/**
 * 分页查询AI分析任务
 * 功能描述：管理员查看AI分析任务队列
 * 入参：{ current?: number, size?: number, status?: string, taskType?: string, userId?: number, username?: string, priority?: number, startTime?: string, endTime?: string, failedOnly?: boolean, retryableOnly?: boolean }
 * 返回参数：{ records: Array, total: number, current: number, size: number, pages: number }
 * url地址：/ai-analysis-task/page
 * 请求方式：GET
 */
export function getAiAnalysisTaskPage(params, callbacks = {}) {
  return request.get('/ai-analysis-task/page', params, callbacks)
}

/**
 * 获取队列统计信息
 * 功能描述：获取AI分析队列的统计数据
 * 入参：无
 * 返回参数：{ totalTasks: number, pendingTasks: number, processingTasks: number, completedTasks: number, failedTasks: number, retryableTasks: number, taskTypeStats: object }
 * url地址：/ai-analysis-task/statistics
 * 请求方式：GET
 */
export function getAiAnalysisTaskStatistics(callbacks = {}) {
  return request.get('/ai-analysis-task/statistics', null, callbacks)
}

/**
 * 重试失败的任务
 * 功能描述：重新执行失败的AI分析任务
 * 入参：{ taskId: number }
 * 返回参数：无
 * url地址：/ai-analysis-task/{taskId}/retry
 * 请求方式：POST
 */
export function retryAiAnalysisTask(taskId, callbacks = {}) {
  return request.post(`/ai-analysis-task/${taskId}/retry`, null, {
    successMsg: '任务重试已触发',
    ...callbacks
  })
}

/**
 * 批量重试失败的任务
 * 功能描述：批量重新执行失败的AI分析任务
 * 入参：{ taskIds: number[] }
 * 返回参数：{ totalCount: number, successCount: number, failCount: number, failReasons: string[] }
 * url地址：/ai-analysis-task/batch-retry
 * 请求方式：POST
 */
export function batchRetryAiAnalysisTasks(taskIds, callbacks = {}) {
  return request.post('/ai-analysis-task/batch-retry', taskIds, {
    successMsg: '批量重试任务已提交',
    ...callbacks
  })
}

