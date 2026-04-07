<template>
  <div class="article-test">
    <h2>文章内容保存测试</h2>
    
    <el-form
      ref="formRef"
      :model="form"
      label-width="120px"
    >
      <el-form-item label="文章标题">
        <el-input
          v-model="form.title"
          placeholder="请输入标题"
        />
      </el-form-item>
      
      <el-form-item label="分类">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择分类"
        >
          <el-option
            label="情绪管理"
            :value="1"
          />
          <el-option
            label="压力管理"
            :value="2"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="文章内容">
        <el-input 
          v-model="form.content" 
          type="textarea" 
          :rows="10" 
          placeholder="请输入文章内容"
        />
        <div style="margin-top: 10px; color: #666;">
          当前内容长度: {{ form.content.length }} 字符
        </div>
      </el-form-item>
      
      <el-form-item>
        <el-button
          type="primary"
          @click="testCreate"
        >
          测试创建文章
        </el-button>
        <el-button
          type="success"
          :disabled="!lastCreatedId"
          @click="testEdit"
        >
          测试编辑功能
        </el-button>
        <el-button
          type="info"
          :disabled="!lastCreatedId"
          @click="testPreview"
        >
          测试预览功能
        </el-button>
        <el-button @click="clearForm">
          清空表单
        </el-button>
      </el-form-item>
    </el-form>
    
    <div class="debug-info">
      <h3>调试信息</h3>
      <pre>{{ JSON.stringify(form, null, 2) }}</pre>
      
      <h3>提交数据</h3>
      <pre>{{ JSON.stringify(submitData, null, 2) }}</pre>
      
      <h3>响应结果</h3>
      <pre>{{ JSON.stringify(response, null, 2) }}</pre>
      
      <div
        v-if="lastCreatedId"
        class="test-actions"
      >
        <h3>测试操作</h3>
        <p>最后创建的文章ID: <strong>{{ lastCreatedId }}</strong></p>
        <p>点击上方按钮测试编辑和预览功能是否能正确显示内容。</p>
      </div>
    </div>
    
    <!-- 编辑对话框 -->
    <ArticleEditDialog
      v-model="editDialogVisible"
      :article="currentArticle"
      :categories="testCategories"
      @success="handleEditSuccess"
    />

    <!-- 预览对话框 -->
    <ArticlePreviewDialog
      v-model="previewDialogVisible"
      :article="previewArticle"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createArticle, getArticleById } from '@/api/knowledgeArticle'
import ArticleEditDialog from './ArticleEditDialog.vue'
import ArticlePreviewDialog from './ArticlePreviewDialog.vue'

const formRef = ref(null)
const response = ref(null)
const submitData = ref(null)
const lastCreatedId = ref(null)

// 对话框控制
const editDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const currentArticle = ref(null)
const previewArticle = ref(null)

// 测试用分类数据
const testCategories = [
  { id: 1, categoryName: '情绪管理' },
  { id: 2, categoryName: '压力管理' }
]

const form = reactive({
  title: '测试文章标题 - 如何管理焦虑情绪',
  categoryId: 1,
  summary: '这是一个测试摘要，用于验证文章摘要是否能正确保存和显示。',
  content: `<h2>什么是焦虑</h2>
<p>焦虑是一种正常的情绪反应，每个人都会经历。当我们面临压力、威胁或重要决定时，焦虑可以帮助我们保持警觉和专注。</p>

<h3>焦虑的常见症状</h3>
<ul>
<li><strong>身体症状：</strong>心跳加速、出汗、肌肉紧张、呼吸急促</li>
<li><strong>情绪症状：</strong>恐惧、不安、烦躁、紧张</li>
<li><strong>认知症状：</strong>担心、反复思考、注意力难以集中</li>
<li><strong>行为症状：</strong>回避、坐立不安、寻求安慰</li>
</ul>

<h3>管理焦虑的有效方法</h3>
<p>以下是一些科学验证的焦虑管理技巧：</p>

<ol>
<li><strong>深呼吸练习</strong>
   <p>通过控制呼吸来激活身体的放松反应。尝试4-7-8呼吸法：吸气4秒，屏气7秒，呼气8秒。</p></li>

<li><strong>正念冥想</strong>
   <p>专注于当下，接受自己的感受而不做判断。每天练习10-15分钟可以显著减少焦虑水平。</p></li>

<li><strong>认知重构</strong>
   <p>识别和挑战消极的想法模式，用更客观、现实的思维替代灾难性思维。</p></li>
</ol>

<blockquote>
<p><em>"焦虑不是你的敌人，而是你内心的保护机制。学会与它和谐相处，才能真正获得内心的平静。"</em> - 心理健康专家</p>
</blockquote>

<h3>何时寻求专业帮助</h3>
<p>如果焦虑严重影响了你的日常生活、工作或人际关系，建议寻求专业心理咨询师或医生的帮助。</p>`,
  coverImage: '',
  tags: '焦虑管理,心理健康,情绪调节,压力缓解',
  status: 0
})

const testCreate = async () => {
  try {
    // 准备提交数据
    submitData.value = {
      ...form
    }
    
    console.log('提交数据:', submitData.value)
    
    // 调用API
    const result = await createArticle(submitData.value)
    response.value = result
    
    console.log('API响应:', result)
    
    // 保存创建的文章ID用于测试
    if (result && result.id) {
      lastCreatedId.value = result.id
    }
    
    ElMessage.success('文章创建成功')
    
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败: ' + error.message)
    response.value = { error: error.message }
  }
}

const testEdit = async () => {
  if (!lastCreatedId.value) {
    ElMessage.warning('请先创建一篇文章')
    return
  }
  
  try {
    console.log('🔍 测试获取文章详情用于编辑:', lastCreatedId.value)
    const articleDetail = await getArticleById(lastCreatedId.value)
    console.log('📄 获取到的文章详情:', articleDetail)
    
    currentArticle.value = articleDetail
    editDialogVisible.value = true
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败: ' + error.message)
  }
}

const testPreview = async () => {
  if (!lastCreatedId.value) {
    ElMessage.warning('请先创建一篇文章')
    return
  }
  
  try {
    console.log('🔍 测试获取文章详情用于预览:', lastCreatedId.value)
    const articleDetail = await getArticleById(lastCreatedId.value)
    console.log('📄 获取到的文章详情:', articleDetail)
    
    previewArticle.value = articleDetail
    previewDialogVisible.value = true
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败: ' + error.message)
  }
}

const handleEditSuccess = () => {
  editDialogVisible.value = false
  ElMessage.success('文章编辑成功')
}

const clearForm = () => {
  Object.assign(form, {
    title: '',
    categoryId: null,
    summary: '',
    content: '',
    coverImage: '',
    tags: '',
    status: 0
  })
  response.value = null
  submitData.value = null
  lastCreatedId.value = null
}
</script>

<style scoped>
.article-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.debug-info {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.debug-info h3 {
  margin: 20px 0 10px 0;
  color: #333;
}

.debug-info pre {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
}

.test-actions {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 6px;
  margin-top: 20px;
  border-left: 4px solid #3b82f6;
}

.test-actions h3 {
  color: #1e40af;
  margin-top: 0;
}

.test-actions p {
  margin-bottom: 8px;
  color: #374151;
}
</style>