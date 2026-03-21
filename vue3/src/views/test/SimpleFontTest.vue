<template>
  <div class="simple-editor-test">
    <h2>简化版字体测试</h2>
    
    <!-- 简化版编辑器 -->
    <div class="editor-wrapper">
      <WangToolbar 
        :editor="editorRef" 
        :defaultConfig="toolbarConfig" 
        mode="default" 
        style="border-bottom: 1px solid #ccc"
      />
      <WangEditor
        v-model="content"
        :defaultConfig="editorConfig"
        mode="default"
        style="height: 400px; overflow-y: hidden;"
        @onCreated="handleCreated"
        @onChange="handleChange"
      />
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h3>调试信息</h3>
      <div>
        <strong>编辑器是否创建:</strong> {{ !!editorRef }}
      </div>
      <div>
        <strong>可用菜单:</strong> {{ availableMenus.join(', ') }}
      </div>
      <div>
        <strong>字体菜单状态:</strong> {{ fontMenuStatus }}
      </div>
      <div>
        <strong>当前内容:</strong>
        <pre>{{ content }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, reactive } from 'vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor as WangEditor, Toolbar as WangToolbar } from '@wangeditor/editor-for-vue'

const content = ref('<p>测试文本，请选择这段文字来尝试更改字体</p>')
const editorRef = shallowRef(null)
const availableMenus = ref([])
const fontMenuStatus = ref('未知')

// 最简化的编辑器配置
const editorConfig = reactive({
  placeholder: '请输入内容...',
  MENU_CONF: {
    fontFamily: {
      fontFamilyList: [
        'Arial',
        'Helvetica', 
        'Georgia',
        '"Times New Roman"',
        '"Microsoft YaHei"',
        '"SimSun"',
        '"SimHei"'
      ]
    }
  }
})

// 最简化的工具栏配置 - 只保留字体相关
const toolbarConfig = reactive({
  toolbarKeys: [
    'fontFamily', 'fontSize', '|',
    'bold', 'italic', '|',
    'color'
  ]
})

const handleCreated = (editor) => {
  editorRef.value = editor
  console.log('编辑器创建成功:', editor)
  
  // 获取所有可用菜单
  try {
    const menus = editor.getAllMenuKeys()
    availableMenus.value = menus
    console.log('所有可用菜单:', menus)
    
    // 检查字体菜单
    if (menus.includes('fontFamily')) {
      fontMenuStatus.value = '✅ 字体菜单已启用'
      console.log('字体菜单配置:', editor.getMenuConfig('fontFamily'))
    } else {
      fontMenuStatus.value = '❌ 字体菜单未启用'
    }
  } catch (error) {
    console.error('获取菜单信息失败:', error)
    fontMenuStatus.value = '❌ 获取菜单信息失败'
  }
}

const handleChange = (editor) => {
  console.log('内容变化:', editor.getHtml())
}

// 组件销毁时清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})
</script>

<style scoped>
.simple-editor-test {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.editor-wrapper {
  border: 1px solid #ccc;
  margin: 20px 0;
}

.debug-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  margin-top: 20px;
}

.debug-info h3 {
  margin-top: 0;
}

.debug-info div {
  margin: 10px 0;
}

.debug-info pre {
  background: white;
  padding: 10px;
  border-radius: 3px;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
}
</style>