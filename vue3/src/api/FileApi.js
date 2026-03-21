import request from '@/utils/request'

/**
 * @description 直接上传业务文件（用于更新场景和UUID预生成场景）
 * @param {File} file - 原生File对象
 * @param {object} businessInfo - 业务信息
 * @param {string} businessInfo.businessType - 业务类型 (e.g., 'ARTICLE')
 * @param {string} businessInfo.businessId - 业务实体ID（可以是UUID或数字ID）
 * @param {string} businessInfo.businessField - 业务字段 (e.g., 'cover')
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 文件信息
 */
export function uploadBusinessFile(file, businessInfo, callbacks = {}) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('businessType', businessInfo.businessType);
  formData.append('businessId', businessInfo.businessId);
  formData.append('businessField', businessInfo.businessField);
  
  return request.post('/file/upload', formData, {
    successMsg: '文件上传成功',
    ...callbacks
  });
}

/**
 * @description 上传单个文件至临时存储区
 * @param {File} file - 原生File对象
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 包含临时文件ID的响应数据
 */
export function uploadTempFile(file, callbacks = {}) {
  const formData = new FormData();
  formData.append('file', file);
  
  return request.post('/file/upload/temp', formData, {
    successMsg: '文件预上传成功',
    ...callbacks
  });
}

/**
 * @description 上传临时业务文件（用于策略A两阶段提交）
 * @param {File} file - 原生File对象
 * @param {object} businessInfo - 业务信息
 * @param {string} businessInfo.businessType - 业务类型 (e.g., 'USER_AVATAR')
 * @param {string} businessInfo.businessField - 业务字段 (e.g., 'avatar')
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 包含临时文件ID的响应数据
 */
export function uploadTempBusinessFile(file, businessInfo, callbacks = {}) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('businessType', businessInfo.businessType);
  formData.append('businessField', businessInfo.businessField);
  
  return request.post('/file/upload/temp-business', formData, {
    successMsg: '头像预上传成功',
    ...callbacks
  });
}

/**
 * @description 确认临时文件，将其与业务实体正式关联
 * @param {number} tempFileId - 临时文件ID
 * @param {object} associationInfo - 业务关联信息
 * @param {string} associationInfo.businessType - 业务类型 (e.g., 'ARTICLE')
 * @param {string} associationInfo.businessId - 业务实体ID
 * @param {string} associationInfo.businessField - 业务字段 (e.g., 'cover')
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 确认后的文件信息
 */
export function confirmTempFile(tempFileId, associationInfo, callbacks = {}) {
  return request.put(`/file/confirm/${tempFileId}`, associationInfo, {
    successMsg: '文件确认成功',
    ...callbacks
  });
}

/**
 * @description 基于UUID创建业务实体的文件查询
 * @param {string} businessType - 业务类型
 * @param {string} businessId - 业务UUID
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 关联的文件列表
 */
export function getFilesByUUID(businessType, businessId, callbacks = {}) {
  return request.get(`/file/business/${businessType}/${businessId}`, null, callbacks);
}

/**
 * @description 获取业务字段的文件
 * @param {string} businessType - 业务类型
 * @param {string} businessId - 业务ID（支持UUID和数字ID）
 * @param {string} businessField - 业务字段名
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 文件列表
 */
export function getFilesByBusinessField(businessType, businessId, businessField, callbacks = {}) {
  return request.get(`/file/business/${businessType}/${businessId}/${businessField}`, null, callbacks);
}

/**
 * @description 删除文件
 * @param {number} fileId - 文件ID
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 删除结果
 */
export function deleteFile(fileId, callbacks = {}) {
  return request.delete(`/file/${fileId}`, {
    successMsg: '文件删除成功',
    ...callbacks
  });
}

/**
 * @description 批量删除业务文件
 * @param {string} businessType - 业务类型
 * @param {string} businessId - 业务ID（支持UUID和数字ID）
 * @param {string} [businessField] - 业务字段名（可选）
 * @param {object} [callbacks={}] - 回调函数对象
 * @returns {Promise<Object>} 删除结果
 */
export function deleteFilesByBusiness(businessType, businessId, businessField, callbacks = {}) {
  const params = businessField ? { businessField } : {};
  return request.delete(`/file/business/${businessType}/${businessId}`, {
    successMsg: '批量删除文件成功',
    ...callbacks
  });
}



