import request from '@/utils/request'

/**
 * 创建知识文章
 * 功能描述：用户创建新的知识文章
 * 入参：{ categoryId: number, title: string, summary?: string, content: string, coverImage?: string, tags?: string, status?: number }
 * 返回参数：{ id: number, title: string, summary: string, content: string, coverImage: string, tags: string, authorName: string, status: number, statusText: string, createdAt: string }
 * url地址：/knowledge/article
 * 请求方式：POST
 */
export function createArticle(params, config = {}) {
  return request.post('/knowledge/article', params, {
    successMsg: '创建文章成功',
    ...config
  })
}

/**
 * 更新知识文章
 * 功能描述：用户更新自己的知识文章或管理员更新任意文章
 * 入参：{ categoryId?: number, title?: string, summary?: string, content?: string, coverImage?: string, tags?: string, status?: number }
 * 返回参数：{ id: number, title: string, summary: string, content: string, coverImage: string, tags: string, authorName: string, status: number, statusText: string, updatedAt: string }
 * url地址：/knowledge/article/{id}
 * 请求方式：PUT
 */
export function updateArticle(id, params, config = {}) {
  return request.put(`/knowledge/article/${id}`, params, {
    successMsg: '更新文章成功',
    ...config
  })
}

/**
 * 删除知识文章
 * 功能描述：用户删除自己的文章或管理员删除任意文章
 * 入参：无
 * 返回参数：删除成功信息
 * url地址：/knowledge/article/{id}
 * 请求方式：DELETE
 */
export function deleteArticle(id, config = {}) {
  return request.delete(`/knowledge/article/${id}`, {
    successMsg: '删除文章成功',
    ...config
  })
}

/**
 * 获取知识文章详情
 * 功能描述：获取文章详细内容，包含收藏状态
 * 入参：无
 * 返回参数：{ id: number, categoryId: number, categoryName: string, title: string, summary: string, content: string, coverImage: string, tags: string, tagArray: array, authorId: number, authorName: string, readCount: number, status: number, statusText: string, isFavorited: boolean, publishedAt: string, createdAt: string, updatedAt: string }
 * url地址：/knowledge/article/{id}
 * 请求方式：GET
 */
export function getArticleById(id, config = {}) {
  return request.get(`/knowledge/article/${id}`, null, config)
}

/**
 * 阅读知识文章
 * 功能描述：阅读文章，自动增加阅读次数
 * 入参：无
 * 返回参数：{ id: number, title: string, content: string, readCount: number, isFavorited: boolean }
 * url地址：/knowledge/article/{id}/read
 * 请求方式：POST
 */
export function readArticle(id, config = {}) {
  return request.post(`/knowledge/article/${id}/read`, null, config)
}

/**
 * 发布知识文章
 * 功能描述：作者发布自己的草稿文章
 * 入参：无
 * 返回参数：{ id: number, title: string, status: number, statusText: string, publishedAt: string }
 * url地址：/knowledge/article/{id}/publish
 * 请求方式：POST
 */
export function publishArticle(id, config = {}) {
  return request.post(`/knowledge/article/${id}/publish`, null, {
    successMsg: '发布文章成功',
    ...config
  })
}

/**
 * 下线知识文章
 * 功能描述：作者或管理员下线已发布的文章
 * 入参：无
 * 返回参数：{ id: number, title: string, status: number, statusText: string }
 * url地址：/knowledge/article/{id}/offline
 * 请求方式：POST
 */
export function offlineArticle(id, config = {}) {
  return request.post(`/knowledge/article/${id}/offline`, null, {
    successMsg: '下线文章成功',
    ...config
  })
}

/**
 * 分页查询知识文章列表
 * 功能描述：分页查询文章列表，支持多种筛选和排序条件
 * 入参：{ keyword?: string, categoryId?: number, title?: string, tags?: string, authorId?: number, status?: number, startDate?: string, endDate?: string, sortField?: string, sortDirection?: string, currentPage?: number, size?: number }
 * 返回参数：{ records: array, total: number, current: number, size: number }
 * url地址：/knowledge/article/page
 * 请求方式：GET
 */
export function getArticlePage(params, config = {}) {
  return request.get('/knowledge/article/page', params, config)
}

/**
 * 更新文章状态
 * 功能描述：管理员更新文章状态（发布/下线/草稿）
 * 入参：{ status: number }
 * 返回参数：更新成功信息
 * url地址：/knowledge/article/{id}/status
 * 请求方式：PUT
 */
export function updateArticleStatus(id, params, config = {}) {
  return request.put(`/knowledge/article/${id}/status`, params, {
    successMsg: '状态更新成功',
    ...config
  })
}

/**
 * 批量删除文章
 * 功能描述：管理员批量删除文章
 * 入参：{ ids: number[] }
 * 返回参数：删除成功信息
 * url地址：/knowledge/article/batch
 * 请求方式：DELETE
 */
export function batchDeleteArticles(params, config = {}) {
  return request.delete('/knowledge/article/batch', {
    successMsg: '批量删除成功',
    data: params,
    ...config
  })
}

/**
 * 获取文章统计信息
 * 功能描述：获取文章相关统计数据
 * 入参：无
 * 返回参数：统计数据对象
 * url地址：/knowledge/article/statistics
 * 请求方式：GET
 */
export function getArticleStatistics(config = {}) {
  return request.get('/knowledge/article/statistics', null, config)
}