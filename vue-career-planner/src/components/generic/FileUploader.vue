<template>
  <div class="file-uploader">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :data="uploadData"
      :multiple="multiple"
      :limit="limit"
      :file-list="fileList"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :before-upload="beforeUpload"
      :accept="accept"
      :show-file-list="showFileList"
      :drag="drag"
    >
      <template v-if="drag">
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template v-if="hint" class="el-upload__tip">
          {{ hint }}
        </template>
      </template>
      <template v-else>
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          <span>点击上传</span>
        </el-button>
        <template v-if="hint" class="el-upload__tip">
          {{ hint }}
        </template>
      </template>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

interface FileItem {
  name: string
  url: string
  uid: string
  status: string
}

interface Props {
  uploadUrl: string
  multiple?: boolean
  limit?: number
  accept?: string
  showFileList?: boolean
  drag?: boolean
  hint?: string
  headers?: any
  uploadData?: any
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  limit: 1,
  accept: '*',
  showFileList: true,
  drag: false,
  hint: '',
  headers: () => ({}),
  uploadData: () => ({})
})

const emit = defineEmits<{
  (e: 'success', response: any, file: File): void
  (e: 'error', error: any, file: File): void
  (e: 'remove', file: FileItem): void
  (e: 'change', fileList: FileItem[]): void
}>()

const fileList = ref<FileItem[]>([])

const handleSuccess = (response: any, file: File, fileList: FileItem[]) => {
  ElMessage.success('文件上传成功')
  emit('success', response, file)
  emit('change', fileList)
}

const handleError = (error: any, file: File, fileList: FileItem[]) => {
  ElMessage.error('文件上传失败')
  emit('error', error, file)
}

const handleRemove = (file: FileItem, fileList: FileItem[]) => {
  emit('remove', file)
  emit('change', fileList)
}

const beforeUpload = (file: File) => {
  // 可以在这里添加文件类型和大小的验证
  return true
}
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.el-upload-dragger {
  width: 100% !important;
  height: 200px !important;
}
</style>