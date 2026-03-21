<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑文章' : '新建文章'"
    width="900px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
    class="article-edit-dialog"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <div class="form-grid">
        <div class="form-left">
          <el-form-item label="文章标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入文章标题"
              maxlength="200"
              show-word-limit
              clearable
            />
          </el-form-item>
          
          <el-form-item label="所属分类" prop="categoryId">
            <el-select 
              v-model="form.categoryId" 
              placeholder="请选择分类"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="文章摘要" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="4"
              placeholder="请输入文章摘要（可选）"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="标签">
            <el-select
              v-model="form.tagArray"
              multiple
              filterable
              allow-create
              placeholder="请输入或选择标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in commonTags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="封面图片">
            <div class="cover-upload">
              <el-upload
                action="#"
                :http-request="handleUploadRequest"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept="image/*"
                class="cover-uploader"
              >
                <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
                <div v-else class="cover-placeholder">
                  <i class="fas fa-plus"></i>
                  <p>点击上传封面</p>
                </div>
              </el-upload>
              <div class="cover-actions" v-if="form.coverImage">
                <el-button type="danger" size="small" @click="removeCover">
                  移除封面
                </el-button>
              </div>
            </div>
          </el-form-item>
        </div>
        
        <div class="form-right">
          <el-form-item label="发布状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio-button :label="0">草稿</el-radio-button>
              <el-radio-button :label="1">立即发布</el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="作者信息">
            <el-input
              v-model="form.authorName"
              placeholder="默认为当前用户"
              clearable
            />
          </el-form-item>
        </div>
      </div>
      
      <el-form-item label="文章内容" prop="content">
        <RichTextEditor
          v-model="form.content"
          :show-security-tip="false"
          placeholder="请输入文章内容，支持富文本格式\n\n可以使用加粗、斜体、列表、标题等格式来丰富文章内容。"
          :max-char-count="5000"
          :toolbar-keys="[
            'bold', 'italic', 'underline', 'color', 'bgColor', '|',
            'fontSize', 'fontFamily', '|',
            'header1', 'header2', 'header3', '|',
            'bulletedList', 'numberedList', 'blockquote', '|',
            'insertLink', '|',
            'undo', 'redo'
          ]"
          min-height="400px"
          @change="handleContentChange"
          @created="handleEditorCreated"
          ref="richTextEditorRef"
        />
      </el-form-item>
    </el-form>

    <!-- 预览区域 -->
    <div v-if="showPreview" class="preview-section">
      <h3>内容预览</h3>
      <div class="preview-content" v-html="formatContent(form.content)"></div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="togglePreview">
          {{ showPreview ? '隐藏预览' : '预览效果' }}
        </el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          {{ isEdit ? '保存修改' : '创建文章' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createArticle, updateArticle } from '@/api/knowledgeArticle'
import { uploadBusinessFile } from '@/api/FileApi'
import { generateUUID } from '@/utils/uuidUtils'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

// Props & Emits
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  article: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref(null)
const submitting = ref(false)
const showPreview = ref(false)
const richTextEditorRef = ref(null)
const editorInstance = ref(null)
const businessId = ref(null) // 预生成的业务UUID
const uploadedFiles = ref([]) // 存储已上传的文件信息

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.article?.id)

// 表单数据
const defaultForm = {
  title: '',
  categoryId: null,
  summary: '',
  content: '',
  coverImage: '',
  tagArray: [],
  status: 0,
  authorName: ''
}

const form = reactive({ ...defaultForm })

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择文章分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback(new Error('请输入文章内容'))
          return
        }
        
        // 移除HTML标签，获取纯文本长度
        const plainText = value.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim()
        
        if (plainText.length < 10) {
          callback(new Error('文章内容至少需要10个字符'))
          return
        }
        
        callback()
      }, 
      trigger: 'blur' 
    }
  ]
}

// 常用标签
const commonTags = [
  '情绪管理', '焦虑', '抑郁', '压力', '睡眠', 
  '冥想', '正念', '放松', '心理健康', '自我成长',
  '人际关系', '工作压力', '学习方法', '生活技巧'
]

// UUID预生成，组件初始化时创建
const initBusinessId = () => {
  businessId.value = generateUUID()
  console.log('📝 预生成业务UUID:', businessId.value)
}

// 方法
const initForm = () => {
  // 先清空表单
  Object.assign(form, { ...defaultForm })
  
  if (props.article) {
    // 编辑模式：赋值文章数据，使用现有文章ID
    Object.assign(form, {
      ...props.article,
      tagArray: props.article.tags ? props.article.tags.split(',') : []
    })
    businessId.value = props.article.id // 编辑时使用现有ID
    
    console.log('📝 初始化编辑表单:')
    console.log('   - 文章ID:', props.article.id)
    console.log('   - 业务ID:', businessId.value)
    console.log('   - 标题:', props.article.title)
  } else {
    // 新建模式：生成新的UUID
    initBusinessId()
    console.log('🆕 初始化新建表单，业务ID:', businessId.value)
  }
  
  // 重置上传文件列表
  uploadedFiles.value = []
  
  // 清空表单验证
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleSubmit = async () => {
  // 先验证表单
  try {
    await formRef.value.validate()
  } catch (validationError) {
    // 表单验证失败，不显示错误消息，Element Plus会自动显示验证错误
    console.log('表单验证失败:', validationError)
    return
  }

  // 表单验证通过，开始提交
  submitting.value = true
  
  try {
    // 准备提交数据
    const submitData = {
      ...form,
      tags: form.tagArray.join(',')
    }
    delete submitData.tagArray
    
    // 如果是新建模式，添加预生成的UUID
    if (!isEdit.value) {
      submitData.id = businessId.value
    }
    
    // 添加调试日志
    console.log('📝 提交表单数据:', {
      是否编辑: isEdit.value,
      原始表单: form,
      处理后数据: submitData,
      文章ID: props.article?.id
    })
    
    // 表单验证已经处理了内容检查，这里不需要重复验证
    
    console.log('✅ 内容验证通过:', submitData.content.substring(0, 100) + '...')

    if (isEdit.value) {
      updateArticle(props.article.id, submitData, {
        successMsg: '文章更新成功',
        onSuccess: (result) => {
          console.log('📝 更新文章响应:', result)
          submitting.value = false
          emit('success')
        },
        onError: (error) => {
          console.error('❌ 更新失败:', error)
          ElMessage.error('更新失败: ' + error.message)
          submitting.value = false
        }
      })
    } else {
      createArticle(submitData, {
        successMsg: '文章创建成功',
        onSuccess: (result) => {
          console.log('🎆 创建文章响应:', result)
          submitting.value = false
          emit('success')
        },
        onError: (error) => {
          console.error('❌ 创建失败:', error)
          ElMessage.error('创建失败: ' + error.message)
          submitting.value = false
        }
      })
    }
  } catch (error) {
    // 这里只处理表单验证等其他错误
    console.error('❌ 表单处理失败:', error)
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, { ...defaultForm })
  showPreview.value = false
  
  // 清空富文本编辑器内容
  if (richTextEditorRef.value) {
    richTextEditorRef.value.clear()
  }
  
  // 重置编辑器实例引用
  editorInstance.value = null
  
  // 重置UUID和上传文件记录
  businessId.value = null
  uploadedFiles.value = []
  
  emit('update:modelValue', false)
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

// 文件上传相关 - UUID预生成方案
const handleUploadRequest = async ({ file, onSuccess, onError }) => {
  try {
    console.log('📤 开始上传文件:', file.name, '业务ID:', businessId.value)
    
    await uploadBusinessFile(file, {
      businessType: 'ARTICLE',
      businessId: businessId.value,
      businessField: 'cover'
    }, {
      onSuccess: (response) => {
        console.log('📤 文件上传成功:', response)
        uploadedFiles.value.push(response)
        form.coverImage = response.filePath
        onSuccess(response) // 通知el-upload上传成功
      },
      onError: (error) => {
        console.error('📤 文件上传失败:', error)
        onError(error) // 通知el-upload上传失败
      }
    })
  } catch (error) {
    console.error('📤 上传请求异常:', error)
    onError(error)
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  console.log('📤 上传成功回调:', response)
 
}

const handleUploadError = (error) => {
  console.error('📤 上传错误回调:', error)

}

const removeCover = () => {
  form.coverImage = ''
  // 清除上传文件记录
  uploadedFiles.value = uploadedFiles.value.filter(file => file.businessField !== 'cover')

}

// 内容格式化
const formatContent = (content) => {
  console.log('🎨 编辑对话框 formatContent 被调用:')
  console.log('   - 原始内容类型:', typeof content)
  console.log('   - 原始内容长度:', content ? content.length : '无内容')
  console.log('   - 原始内容预览:', content ? content.substring(0, 100) + '...' : '无内容')
  
  if (!content) {
    console.log('   - 返回空字符串')
    return ''
  }
  
  // 如果已经是HTML格式（来自富文本编辑器），直接返回
  if (content.includes('<') && content.includes('>')) {
    console.log('   - 检测到HTML格式，直接返回')
    return content
  }
  
  // 基本的文本格式化（用于向后兼容）
  let formatted = content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  console.log('   - 格式化后长度:', formatted.length)
  console.log('   - 格式化后预览:', formatted.substring(0, 100) + '...')
  
  return formatted
}

// 监听 props 变化
watch(() => props.modelValue, (visible) => {
  if (visible) {
    // 使用 nextTick 确保在下一个事件循环中初始化表单
    nextTick(() => {
      initForm()
    })
  }
})

// 富文本编辑器事件处理
const handleContentChange = (data) => {
  console.log('📝 富文本内容变化:', {
    htmlLength: data.html ? data.html.length : 0,
    textLength: data.text ? data.text.length : 0,
    htmlPreview: data.html ? data.html.substring(0, 100) + '...' : '无内容',
    textPreview: data.text ? data.text.substring(0, 50) + '...' : '无内容'
  })
  
  // 更新表单中的内容
  form.content = data.html
}

const handleEditorCreated = (editor) => {
  editorInstance.value = editor
  console.log('🎉 富文本编辑器创建成功:', editor)
  
  // 如果表单中有内容，设置到编辑器中
  if (form.content && editor) {
    nextTick(() => {
      editor.setHtml(form.content)
      console.log('📋 设置编辑器内容:', form.content.substring(0, 100) + '...')
    })
  }
}

// 监听文章数据变化
watch(() => props.article, (newArticle) => {
  if (props.modelValue && newArticle) {
    // 只有在对话框显示状态下才初始化
    nextTick(() => {
      initForm()
      
      // 如果编辑器已创建，设置内容
      if (editorInstance.value && newArticle.content) {
        setTimeout(() => {
          editorInstance.value.setHtml(newArticle.content)
          console.log('🔄 更新编辑器内容:', newArticle.content.substring(0, 100) + '...')
        }, 100)
      }
    })
  }
}, { deep: true })

// 监听表单内容变化，同步到编辑器
watch(() => form.content, (newContent) => {
  if (editorInstance.value && newContent !== editorInstance.value.getHtml()) {
    console.log('🔄 同步内容到编辑器:', newContent ? newContent.substring(0, 100) + '...' : '空内容')
    nextTick(() => {
      editorInstance.value.setHtml(newContent || '')
    })
  }
})
</script>

<style scoped>
.article-edit-dialog :deep(.el-dialog) {
  max-width: 95vw;
}

.article-edit-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

.form-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.form-left,
.form-right {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 封面上传样式 */
.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.cover-uploader {
  display: inline-block;
}

.cover-uploader :deep(.el-upload) {
  border: 2px dashed #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.cover-uploader :deep(.el-upload):hover {
  border-color: #3b82f6;
}

.cover-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 200px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8b949e;
  background: #f6f8fa;
}

.cover-placeholder i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.cover-placeholder p {
  margin: 0;
  font-size: 0.875rem;
}

.cover-actions {
  display: flex;
  gap: 0.5rem;
}

/* 内容编辑器样式 */
.content-editor {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 1rem;
}

.content-textarea :deep(.el-textarea__inner) {

  font-size: 0.875rem;
  line-height: 1.6;
}

.editor-help {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  font-size: 0.75rem;
  color: #6b7280;
}

.editor-help p {
  margin: 0 0 0.5rem 0;
  font-weight: 500;
}

.editor-help ul {
  margin: 0;
  padding-left: 1rem;
}

.editor-help li {
  margin-bottom: 0.25rem;
}

/* 预览区域样式 */
.preview-section {
  margin-top: 2rem;
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.preview-section h3 {
  margin: 0 0 1rem 0;
  color: #374151;
  font-size: 1.125rem;
}

.preview-content {
  background: #f9fafb;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-size: 1rem;
  line-height: 1.7;
}

.preview-content :deep(h2),
.preview-content :deep(h3) {
  color: #111827;
  margin: 1.5rem 0 1rem 0;
}

.preview-content :deep(p) {
  margin-bottom: 1rem;
}

.preview-content :deep(ul),
.preview-content :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}

.preview-content :deep(blockquote) {
  border-left: 4px solid #f59e0b;
  background: #fffbeb;
  padding: 1rem 1.5rem;
  margin: 1rem 0;
  border-radius: 0 8px 8px 0;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .content-editor {
    grid-template-columns: 1fr;
  }
  
  .cover-image,
  .cover-placeholder {
    width: 100%;
    max-width: 300px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
  }
}
</style>