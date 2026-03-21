/**
 * API接口统一导出文件
 * 功能描述：统一管理和导出所有API接口，方便其他模块引用
 */

// 用户认证相关API
export * from './user'

// 知识库相关API
export * from './knowledgeCategory'
export * from './knowledgeArticle'
export * from './userFavorite'

// 心理咨询相关API
export * from './consultationSession'

// 心理测试相关API
export * from './psychologicalTest'

// 情绪日记相关API
export * from './emotionDiary'

// 可以在这里继续导出其他模块的API
// export * from './post'
// export * from './comment'
// export * from './checkin'
