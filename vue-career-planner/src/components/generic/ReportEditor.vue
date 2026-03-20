<template>
  <div class="report-editor">
    <el-card>
      <template #header>
        <div class="editor-header">
          <h3>{{ title }}</h3>
          <div class="editor-actions">
            <el-button @click="togglePreview">
              {{ isPreview ? '编辑' : '预览' }}
            </el-button>
            <el-button type="primary" @click="handleSave">保存</el-button>
          </div>
        </div>
      </template>
      
      <div class="editor-content">
        <!-- 编辑模式 -->
        <el-input
          v-if="!isPreview"
          v-model="content"
          type="textarea"
          :rows="15"
          placeholder="请输入Markdown内容"
          class="markdown-editor"
        />
        
        <!-- 预览模式 -->
        <div v-else class="markdown-preview">
          <div v-html="renderedContent"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  title: string
  modelValue: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'save', value: string): void
}>()

const isPreview = ref(false)
const content = ref(props.modelValue)

// 监听modelValue变化
import { watch } from 'vue'
watch(
  () => props.modelValue,
  (newValue) => {
    content.value = newValue
  }
)

// 模拟Markdown渲染
const renderedContent = computed(() => {
  // 这里可以使用实际的Markdown解析库，如marked.js
  // 现在只是简单的处理
  let rendered = content.value
  // 处理标题
  rendered = rendered.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  rendered = rendered.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  rendered = rendered.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  // 处理粗体
  rendered = rendered.replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
  // 处理斜体
  rendered = rendered.replace(/\*(.*)\*/gim, '<em>$1</em>')
  // 处理链接
  rendered = rendered.replace(/\[([^\]]+)\]\(([^\)]+)\)/gim, '<a href="$2" target="_blank">$1</a>')
  // 处理列表
  rendered = rendered.replace(/^\* (.*$)/gim, '<li>$1</li>')
  rendered = rendered.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
  return rendered
})

const togglePreview = () => {
  isPreview.value = !isPreview.value
}

const handleSave = () => {
  emit('update:modelValue', content.value)
  emit('save', content.value)
}
</script>

<style scoped>
.report-editor {
  width: 100%;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.editor-actions {
  display: flex;
  gap: 10px;
}

.editor-content {
  margin-top: 20px;
}

.markdown-editor {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
}

.markdown-preview {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  min-height: 300px;
  line-height: 1.6;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.markdown-preview ul {
  margin: 10px 0;
  padding-left: 20px;
}

.markdown-preview li {
  margin: 5px 0;
}

.markdown-preview a {
  color: #409eff;
  text-decoration: none;
}

.markdown-preview a:hover {
  text-decoration: underline;
}
</style>