import request from '@/utils/request'

/**
 * 收藏文章
 * 功能描述：用户收藏指定的知识文章
 * 入参：无
 * 返回参数：收藏成功信息
 * url地址：/knowledge/favorite/{articleId}
 * 请求方式：POST
 */
export function favoriteArticle(articleId, config = {}) {
  return request.post(`/knowledge/favorite/${articleId}`, null, {
    successMsg: '收藏成功',
    ...config
  })
}

/**
 * 取消收藏文章
 * 功能描述：用户取消收藏指定的知识文章
 * 入参：无
 * 返回参数：取消收藏成功信息
 * url地址：/knowledge/favorite/{articleId}
 * 请求方式：DELETE
 */
export function unfavoriteArticle(articleId, config = {}) {
  return request.delete(`/knowledge/favorite/${articleId}`, {
    successMsg: '取消收藏成功',
    ...config
  })
}

/**
 * 检查文章收藏状态
 * 功能描述：检查当前用户是否已收藏指定文章
 * 入参：无
 * 返回参数：boolean（true表示已收藏，false表示未收藏）
 * url地址：/knowledge/favorite/{articleId}/status
 * 请求方式：GET
 */
export function checkFavoriteStatus(articleId, config = {}) {
  return request.get(`/knowledge/favorite/${articleId}/status`, null, config)
}

/**
 * 分页查询用户收藏的文章
 * 功能描述：分页查询当前用户收藏的知识文章列表
 * 入参：{ title?: string, categoryId?: number, sortField?: string, sortDirection?: string, currentPage?: number, size?: number }
 * 返回参数：{ records: array, total: number, current: number, size: number }
 * url地址：/knowledge/favorite/page
 * 请求方式：GET
 */
export function getUserFavoritePage(params, config = {}) {
  return request.get('/knowledge/favorite/page', params, config)
}

/**
 * 获取用户收藏文章总数
 * 功能描述：获取当前用户收藏的文章总数
 * 入参：无
 * 返回参数：number（收藏文章总数）
 * url地址：/knowledge/favorite/count
 * 请求方式：GET
 */
export function getUserFavoriteCount(config = {}) {
  return request.get('/knowledge/favorite/count', null, config)
}