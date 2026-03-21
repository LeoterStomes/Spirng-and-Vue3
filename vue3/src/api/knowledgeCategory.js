import request from '@/utils/request'

/**
 * 创建知识分类
 * 功能描述：管理员创建新的知识文章分类
 * 入参：{ parentId?: number, categoryName: string, categoryCode?: string, description?: string, sortOrder?: number, status?: number }
 * 返回参数：{ id: number, categoryName: string, categoryCode: string, description: string, sortOrder: number, status: number, statusText: string, createdAt: string }
 * url地址：/knowledge/category
 * 请求方式：POST
 */
export function createCategory(params, config = {}) {
  return request.post('/knowledge/category', params, {
    successMsg: '创建分类成功',
    ...config
  })
}

/**
 * 更新知识分类
 * 功能描述：管理员更新知识文章分类信息
 * 入参：{ parentId?: number, categoryName?: string, categoryCode?: string, description?: string, sortOrder?: number, status?: number }
 * 返回参数：{ id: number, categoryName: string, categoryCode: string, description: string, sortOrder: number, status: number, statusText: string, updatedAt: string }
 * url地址：/knowledge/category/{id}
 * 请求方式：PUT
 */
export function updateCategory(id, params, config = {}) {
  return request.put(`/knowledge/category/${id}`, params, {
    successMsg: '更新分类成功',
    ...config
  })
}

/**
 * 删除知识分类
 * 功能描述：管理员删除指定的知识文章分类
 * 入参：无
 * 返回参数：删除成功信息
 * url地址：/knowledge/category/{id}
 * 请求方式：DELETE
 */
export function deleteCategory(id, config = {}) {
  return request.delete(`/knowledge/category/${id}`, {
    successMsg: '删除分类成功',
    ...config
  })
}

/**
 * 获取知识分类详情
 * 功能描述：根据ID获取知识分类的详细信息，包含统计数据
 * 入参：无
 * 返回参数：{ id: number, categoryName: string, categoryCode: string, description: string, sortOrder: number, status: number, statusText: string, childCount: number, articleCount: number, createdAt: string, updatedAt: string }
 * url地址：/knowledge/category/{id}
 * 请求方式：GET
 */
export function getCategoryById(id, config = {}) {
  return request.get(`/knowledge/category/${id}`, null, config)
}

/**
 * 分页查询知识分类列表
 * 功能描述：分页查询知识分类列表，支持按多种条件筛选
 * 入参：{ parentId?: number, categoryName?: string, categoryCode?: string, status?: number, currentPage?: number, size?: number }
 * 返回参数：{ records: array, total: number, current: number, size: number }
 * url地址：/knowledge/category/page
 * 请求方式：GET
 */
export function getCategoryPage(params, config = {}) {
  return request.get('/knowledge/category/page', params, config)
}

/**
 * 获取知识分类树
 * 功能描述：获取所有启用的知识分类，按树形结构返回
 * 入参：无
 * 返回参数：分类树列表 [{ id: number, categoryName: string, categoryCode: string, description: string, sortOrder: number, children?: array }]
 * url地址：/knowledge/category/tree
 * 请求方式：GET
 */
export function getCategoryTree(config = {}) {
  return request.get('/knowledge/category/tree', null, config)
}