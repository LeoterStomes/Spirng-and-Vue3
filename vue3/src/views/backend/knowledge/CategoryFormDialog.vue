<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      size="default"
    >
      <el-form-item label="分类名称" prop="categoryName">
        <el-input
          v-model="formData.categoryName"
          placeholder="请输入分类名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分类描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          placeholder="请输入分类描述（可选）"
          :rows="3"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="999"
          :step="1"
          placeholder="排序号"
          style="width: 150px"
        />
        <div class="form-tip">
          数字越小排序越靠前
        </div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1" size="large">
            <span style="color: #67C23A">
              <i class="fas fa-check-circle"></i>
              启用
            </span>
          </el-radio>
          <el-radio :label="0" size="large">
            <span style="color: #F56C6C">
              <i class="fas fa-ban"></i>
              禁用
            </span>
          </el-radio>
        </el-radio-group>
        <div class="form-tip">
          禁用的分类不会在前台显示
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" size="default">
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitLoading"
          size="default"
        >
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createCategory, updateCategory } from '@/api/knowledgeCategory'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  category: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref(null)
const submitLoading = ref(false)

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEditMode = computed(() => {
  return props.category && props.category.id
})

const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑分类' : '新建分类'
})

const categoryOptions = computed(() => {
  // 过滤掉当前正在编辑的分类，避免循环引用
  return props.categories.filter(cat => {
    if (isEditMode.value) {
      return cat.id !== props.category?.id
    }
    return true
  })
})

// 表单数据
const formData = reactive({
  categoryName: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 100, message: '分类名称长度在1到100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '分类描述长度不能超过500个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', min: 0, max: 999, message: '排序号必须在0-999之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 监听对话框显示状态
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    initFormData()
  }
})

// 方法
const initFormData = () => {
  if (isEditMode.value) {
    // 编辑模式：填入现有数据
    Object.assign(formData, {
      categoryName: props.category.categoryName || '',
      description: props.category.description || '',
      sortOrder: props.category.sortOrder || 0,
      status: props.category.status ?? 1
    })
  } else {
    // 新建模式：重置表单
    Object.assign(formData, {
      categoryName: '',
      description: '',
      sortOrder: 0,
      status: 1
    })
  }
  
  // 清除验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleSubmit = () => {
  formRef.value?.validate((valid) => {
    if (!valid) {
      return
    }

    submitLoading.value = true

    // 准备提交数据
    const submitData = {
      categoryName: formData.categoryName.trim(),
      description: formData.description?.trim() || null,
      sortOrder: formData.sortOrder,
      status: formData.status
    }

    // 移除空值
    Object.keys(submitData).forEach(key => {
      if (submitData[key] === '' || submitData[key] === null) {
        delete submitData[key]
      }
    })

    // 调用API
    const config = {
      onSuccess: () => {
        ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
        emit('success')
        handleClose()
        submitLoading.value = false
      },
      onError: (error) => {
        console.error('操作失败:', error)
        ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
        submitLoading.value = false
      }
    }

    if (isEditMode.value) {
      updateCategory(props.category.id, submitData, config)
    } else {
      createCategory(submitData, config)
    }
  })
}

const handleCancel = () => {
  handleClose()
}

const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-dialog__header) {
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-dialog__title) {
  color: #303133;
  font-weight: 600;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-radio) {
  margin-right: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
