<template>
  <div class="resume-upload">
    <h2>上传简历</h2>
    <el-card shadow="hover">
      <el-upload
        class="upload-demo"
        action="#"
        :on-preview="handlePreview"
        :on-remove="handleRemove"
        :file-list="fileList"
        :auto-upload="false"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 PDF 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <el-button type="success" style="margin-top: 20px;">上传</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { UploadFile, UploadFiles } from 'element-plus'

const fileList = ref([])

const handlePreview = (file: UploadFile) => {
  console.log(file)
}

const handleRemove = (file: UploadFile, fileList: UploadFiles) => {
  console.log(file, fileList)
}
</script>

<style scoped lang="scss">
.resume-upload {
  padding: 20px;
  h2 {
    margin-bottom: 20px;
  }
}
</style>
