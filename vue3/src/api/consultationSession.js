import request from '@/utils/request'

/**
 * 开始新的心理疏导会话
 * 功能描述：为用户创建新的心理疏导对话会话
 * 入参：{ sessionTitle?: string, initialMessage: string }
 * 返回参数：{ sessionId: string, userHash: number, initialMessage: string, startTime: number, expiryTime: number, status: string }
 * url地址：/psychological-chat/session/start
 * 请求方式：POST
 */
export function startChatSession(params, callbacks = {}) {
  return request.post('/psychological-chat/session/start', params, {
    successMsg: '会话创建成功',
    ...callbacks
  })
}


/**
 * 获取会话情绪分析结果
 * 功能描述：获取指定会话的最新情绪分析结果
 * 返回参数：{ primaryEmotion: string, emotionScore: number, isNegative: boolean, riskLevel: number, suggestion: string, icon: string, label: string, positive: number }
 * url地址：/psychological-chat/session/{sessionId}/emotion
 * 请求方式：GET
 */
export function getSessionEmotion(sessionId, callbacks = {}) {
  return request.get(`/psychological-chat/session/${sessionId}/emotion`, null, callbacks)
}

/**
 * 分页查询咨询会话
 * 功能描述：分页查询用户咨询会话记录
 * 入参：{ currentPage?: number, size?: number, userId?: number, emotionTag?: string, startDate?: string, endDate?: string, keyword?: string }
 * 返回参数：{ records: Array, total: number, current: number, size: number, pages: number }
 * url地址：/psychological-chat/sessions
 * 请求方式：GET
 */
export function getSessionsPage(params, callbacks = {}) {
  return request.get('/psychological-chat/sessions', params, callbacks)
}

/**
 * 删除咨询会话
 * 功能描述：删除指定的咨询会话及其相关消息
 * 返回参数：boolean
 * url地址：/psychological-chat/sessions/{sessionId}
 * 请求方式：DELETE
 */
export function deleteSession(sessionId, callbacks = {}) {
  return request.delete(`/psychological-chat/sessions/${sessionId}`, callbacks)
}

/**
 * 获取会话详情
 * 功能描述：根据会话ID获取详细信息
 * 返回参数：{ id: number, userId: number, userNickname: string, userAvatar: string, sessionTitle: string, startedAt: string, durationMinutes: number, messageCount: number, lastMessageContent: string, lastMessageTime: string, emotionTags: Array, primaryEmotion: string }
 * url地址：/psychological-chat/sessions/{sessionId}
 * 请求方式：GET
 */
export function getSessionDetail(sessionId, callbacks = {}) {
  return request.get(`/psychological-chat/sessions/${sessionId}`, null, callbacks)
}

/**
 * 获取会话消息列表
 * 功能描述：获取指定会话的所有消息
 * 返回参数：Array of { id: number, sessionId: number, senderType: number, senderTypeDesc: string, messageType: number, messageTypeDesc: string, content: string, emotionTag: string, aiModel: string, createdAt: string, contentLength: number }
 * url地址：/psychological-chat/sessions/{sessionId}/messages
 * 请求方式：GET
 */
export function getSessionMessages(sessionId, callbacks = {}) {
  return request.get(`/psychological-chat/sessions/${sessionId}/messages`, null, callbacks)
}

/**
 * 更新会话标题
 * 功能描述：更新指定会话的标题
 * 入参：{ sessionTitle: string }
 * 返回参数：boolean
 * url地址：/psychological-chat/sessions/{sessionId}/title
 * 请求方式：PUT
 */
export function updateSessionTitle(sessionId, params, callbacks = {}) {
  return request.put(`/psychological-chat/sessions/${sessionId}/title`, params, {
    successMsg: '标题更新成功',
    ...callbacks
  })
}


