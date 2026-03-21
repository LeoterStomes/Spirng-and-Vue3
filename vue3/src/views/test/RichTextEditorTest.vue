<template>
  <div class="rich-text-editor-test">
    <div class="test-header">
      <h2>富文本编辑器功能测试</h2>
      <p>测试颜色选项、字体设置和各种格式化功能</p>
    </div>

    <div class="test-content">
      <div class="editor-section">
        <h3>富文本编辑器</h3>
        <RichTextEditor
          v-model="content"
          placeholder="请测试各种格式化功能：字体、颜色、大小等..."
          :max-char-count="5000"
          :show-word-count="true"
          :show-security-tip="true"
          :min-height="'400px'"
          @change="handleContentChange"
          @created="handleEditorCreated"
        />
      </div>

      <div class="preview-section">
        <h3>实时预览</h3>
        <div class="content-preview" v-html="content"></div>
      </div>

      <div class="info-section">
        <h3>编辑器信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>HTML内容：</label>
            <textarea readonly :value="content" rows="6"></textarea>
          </div>
          <div class="info-item">
            <label>纯文本内容：</label>
            <textarea readonly :value="textContent" rows="6"></textarea>
          </div>
          <div class="info-item">
            <label>字符统计：</label>
            <span>{{ textContent.length }} 字符</span>
          </div>
        </div>
      </div>

      <div class="test-buttons">
        <el-button @click="clearContent">清空内容</el-button>
        <el-button @click="insertSampleText" type="primary">插入示例文本</el-button>
        <el-button @click="setSampleHtml" type="success">设置示例HTML</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElButton, ElMessage } from 'element-plus'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

// 响应式数据
const content = ref('')
const textContent = ref('')
const editorRef = ref(null)

// 编辑器事件处理
const handleContentChange = (data) => {
  textContent.value = data.text
  console.log('内容变更：', data)
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
  ElMessage.success('富文本编辑器创建成功')
  console.log('编辑器实例：', editor)
}

// 操作方法
const clearContent = () => {
  content.value = ''
  ElMessage.info('内容已清空')
}

const insertSampleText = () => {
  const sampleText = '这是一段测试文本，用于验证富文本编辑器的功能。'
  if (editorRef.value) {
    editorRef.value.insertText(sampleText)
    ElMessage.success('已插入示例文本')
  }
}

const setSampleHtml = () => {
  const sampleHtml = `
    <h3 style="color: #4A90E2;">心理健康知识分享</h3>
    <p style="font-family: 'Source Han Sans CN', sans-serif; font-size: 16px;">
      心理健康是指个体在心理、情感和社会方面的完全健康状态。
    </p>
    <p style="color: #7ED321; background-color: #E8F5E8; padding: 8px;">
      <strong>积极心态的重要性：</strong>
    </p>
    <ul>
      <li style="color: #F5A623;">保持乐观的人生观</li>
      <li style="color: #9013FE;">学会情绪管理</li>
      <li style="color: #FF6B6B;">建立良好的人际关系</li>
    </ul>
    <p style="text-align: center; font-size: 14px; color: #999999;">
      <em>——温馨提示：关注心理健康，享受美好生活</em>
    </p>
  `
  content.value = sampleHtml
  ElMessage.success('已设置示例HTML内容')
}
</script>

<style scoped>
.rich-text-editor-test {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

.test-header {
  text-align: center;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.test-header h2 {
  color: #4A90E2;
  margin-bottom: 0.5rem;
  font-family: 'Source Han Sans CN', sans-serif;
}

.test-header p {
  color: #666666;
  margin: 0;
}

.test-content {
  display: grid;
  gap: 2rem;
}

.editor-section,
.preview-section,
.info-section {
  background: white;
  padding: 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.editor-section h3,
.preview-section h3,
.info-section h3 {
  color: #333333;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #4A90E2;
  font-family: 'Source Han Sans CN', sans-serif;
}

.content-preview {
  min-height: 200px;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  background: #fafafa;
  line-height: 1.6;
}

.info-grid {
  display: grid;
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-item label {
  font-weight: 500;
  color: #4A90E2;
}

.info-item textarea {
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.4;
  resize: vertical;
  background: #f8f9fa;
}

.info-item span {
  padding: 0.5rem;
  background: #e3f2fd;
  border-radius: 0.375rem;
  font-weight: 500;
  color: #4A90E2;
}

.test-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  padding: 2rem;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (min-width: 768px) {
  .test-content {
    grid-template-columns: 1fr 1fr;
    grid-template-areas: 
      "editor editor"
      "preview info"
      "buttons buttons";
  }
  
  .editor-section {
    grid-area: editor;
  }
  
  .preview-section {
    grid-area: preview;
  }
  
  .info-section {
    grid-area: info;
  }
  
  .test-buttons {
    grid-area: buttons;
  }
}

@media (min-width: 1024px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>