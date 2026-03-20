<template>
  <div class="data-source-management">
    <div class="page-header">
      <h1>数据源管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handleUpload" :disabled="isProcessing">
          <el-icon><Upload /></el-icon>
          上传数据源
        </el-button>
      </div>
    </div>
    
    <!-- 上传区域 -->
    <el-card shadow="hover" v-if="!uploaded">
      <div class="upload-section">
        <el-upload
          class="upload-area"
          :drag="true"
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls"
          :disabled="isProcessing"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p>点击或拖拽文件到此处上传</p>
            <p class="upload-hint">支持 .xlsx, .xls 格式文件</p>
          </div>
        </el-upload>
        
        <div class="upload-tips">
          <h3>上传说明：</h3>
          <ul>
            <li>请确保 Excel 文件包含以下字段：站点、岗位编码、岗位名称、职责描述、任职要求</li>
            <li>系统会自动校验重复数据（站点 + 岗位编码）</li>
            <li>支持批量导入，单次最多 10000 条记录</li>
          </ul>
        </div>
      </div>
    </el-card>
    
    <!-- 数据校验区域 -->
    <el-card shadow="hover" v-else-if="showValidation">
      <template #header>
        <div class="card-header">
          <span>数据校验</span>
          <div class="header-actions">
            <el-button @click="cancelValidation">取消</el-button>
            <el-button type="primary" @click="confirmImport" :loading="isProcessing">
              确认导入
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="validation-section">
        <div class="validation-stats">
          <el-statistic title="总记录数" :value="validationData.total" />
          <el-statistic title="有效记录" :value="validationData.valid" prefix="<el-icon><Check /></el-icon>" />
          <el-statistic title="重复记录" :value="validationData.duplicate" prefix="<el-icon><Warning /></el-icon>" />
        </div>
        
        <div class="validation-table">
          <el-table :data="previewData" style="width: 100%" border>
            <el-table-column prop="id" label="序号" width="80" />
            <el-table-column prop="site" label="站点" />
            <el-table-column prop="code" label="岗位编码" />
            <el-table-column prop="name" label="岗位名称" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'valid' ? 'success' : 'warning'">
                  {{ scope.row.status === 'valid' ? '有效' : '重复' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="removeRow(scope.row)" v-if="scope.row.status === 'duplicate'">
                  剔除
                </el-button>
                <el-button size="small" type="primary" @click="overrideRow(scope.row)" v-else>
                  覆盖
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 导入进度区域 -->
    <el-card shadow="hover" v-else-if="showProgress">
      <template #header>
        <div class="card-header">
          <span>导入进度</span>
          <div class="header-actions">
            <el-button @click="pauseImport" v-if="importStatus === 'processing'">
              暂停
            </el-button>
            <el-button @click="resumeImport" v-else-if="importStatus === 'paused'">
              继续
            </el-button>
            <el-button type="danger" @click="cancelImport">
              取消
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="progress-section">
        <div class="overall-progress">
          <div class="progress-info">
            <span>总体进度：{{ progressData.overall }}%</span>
            <span>{{ progressData.processed }} / {{ progressData.total }} 条</span>
          </div>
          <el-progress :percentage="progressData.overall" :status="progressData.status" />
        </div>
        
        <div class="status-progress">
          <div class="status-item">
            <span class="status-label">解析中</span>
            <div class="status-bar">
              <div class="status-fill parsing" :style="{ width: progressData.parsing + '%' }"></div>
            </div>
            <span class="status-count">{{ progressData.parsingCount }} 条</span>
          </div>
          <div class="status-item">
            <span class="status-label">队列中</span>
            <div class="status-bar">
              <div class="status-fill queued" :style="{ width: progressData.queued + '%' }"></div>
            </div>
            <span class="status-count">{{ progressData.queuedCount }} 条</span>
          </div>
          <div class="status-item">
            <span class="status-label">处理中</span>
            <div class="status-bar">
              <div class="status-fill processing" :style="{ width: progressData.processing + '%' }"></div>
            </div>
            <span class="status-count">{{ progressData.processingCount }} 条</span>
          </div>
          <div class="status-item">
            <span class="status-label">成功</span>
            <div class="status-bar">
              <div class="status-fill success" :style="{ width: progressData.success + '%' }"></div>
            </div>
            <span class="status-count">{{ progressData.successCount }} 条</span>
          </div>
          <div class="status-item">
            <span class="status-label">失败</span>
            <div class="status-bar">
              <div class="status-fill failure" :style="{ width: progressData.failure + '%' }"></div>
            </div>
            <span class="status-count">{{ progressData.failureCount }} 条</span>
          </div>
        </div>
        
        <div class="failure-log" v-if="progressData.failureCount > 0">
          <h4>失败记录</h4>
          <el-button type="primary" size="small" @click="exportErrorLog">
            导出错误日志
          </el-button>
          <el-table :data="failureRecords" style="width: 100%" border>
            <el-table-column prop="id" label="序号" width="80" />
            <el-table-column prop="site" label="站点" />
            <el-table-column prop="code" label="岗位编码" />
            <el-table-column prop="name" label="岗位名称" />
            <el-table-column prop="error" label="错误原因" />
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 历史记录 -->
    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>导入历史</span>
        </div>
      </template>
      
      <div class="history-section">
        <el-table :data="importHistory" style="width: 100%" border>
          <el-table-column prop="id" label="导入ID" width="120" />
          <el-table-column prop="filename" label="文件名" />
          <el-table-column prop="total" label="总记录" width="100" />
          <el-table-column prop="success" label="成功" width="100" />
          <el-table-column prop="failure" label="失败" width="100" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="导入时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="viewDetails(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled, Check, Warning } from '@element-plus/icons-vue'

const uploaded = ref(false)
const showValidation = ref(false)
const showProgress = ref(false)
const isProcessing = ref(false)
const importStatus = ref<'idle' | 'processing' | 'paused' | 'completed' | 'failed'>('idle')

const file = ref<File | null>(null)

// 校验数据
const validationData = reactive({
  total: 100,
  valid: 85,
  duplicate: 15
})

// 预览数据
const previewData = ref([
  { id: 1, site: '北京', code: 'BJ-001', name: '前端工程师', status: 'valid' },
  { id: 2, site: '上海', code: 'SH-001', name: '后端工程师', status: 'valid' },
  { id: 3, site: '广州', code: 'GZ-001', name: '产品经理', status: 'duplicate' },
  { id: 4, site: '深圳', code: 'SZ-001', name: 'UI设计师', status: 'valid' },
  { id: 5, site: '杭州', code: 'HZ-001', name: '测试工程师', status: 'duplicate' }
])

// 进度数据
const progressData = reactive({
  overall: 45,
  total: 100,
  processed: 45,
  status: 'processing',
  parsing: 10,
  parsingCount: 10,
  queued: 30,
  queuedCount: 30,
  processing: 25,
  processingCount: 25,
  success: 20,
  successCount: 20,
  failure: 5,
  failureCount: 5
})

// 失败记录
const failureRecords = ref([
  { id: 1, site: '广州', code: 'GZ-001', name: '产品经理', error: '岗位编码格式错误' },
  { id: 2, site: '杭州', code: 'HZ-001', name: '测试工程师', error: '站点信息缺失' },
  { id: 3, site: '成都', code: 'CD-001', name: '运维工程师', error: '职责描述为空' },
  { id: 4, site: '武汉', code: 'WH-001', name: '数据分析师', error: '任职要求为空' },
  { id: 5, site: '西安', code: 'XA-001', name: '算法工程师', error: '岗位名称重复' }
])

// 导入历史
const importHistory = ref([
  { id: 'IMP-20240312-001', filename: '岗位数据.xlsx', total: 100, success: 95, failure: 5, status: '已完成', createTime: '2024-03-12 10:00:00' },
  { id: 'IMP-20240311-001', filename: '岗位数据_20240311.xlsx', total: 200, success: 198, failure: 2, status: '已完成', createTime: '2024-03-11 15:30:00' },
  { id: 'IMP-20240310-001', filename: '春季招聘岗位.xlsx', total: 150, success: 145, failure: 5, status: '已完成', createTime: '2024-03-10 09:15:00' }
])

const handleFileChange = (file: any) => {
  this.file = file.raw
  // 模拟文件解析
  isProcessing.value = true
  setTimeout(() => {
    isProcessing.value = false
    uploaded.value = true
    showValidation.value = true
    ElMessage.success('文件解析完成')
  }, 1000)
}

const beforeUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
  }
  return isExcel && isLt10M
}

const handleUpload = () => {
  // 模拟上传
  ElMessage.info('请选择文件上传')
}

const cancelValidation = () => {
  uploaded.value = false
  showValidation.value = false
  file.value = null
}

const confirmImport = async () => {
  try {
    isProcessing.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    showValidation.value = false
    showProgress.value = true
    importStatus.value = 'processing'
    ElMessage.success('导入任务已开始')
    
    // 模拟进度更新
    const updateProgress = setInterval(() => {
      if (progressData.overall < 100) {
        progressData.overall += 5
        progressData.processed = Math.floor(progressData.total * progressData.overall / 100)
        progressData.successCount = progressData.processed - progressData.failureCount
        
        if (progressData.overall === 100) {
          clearInterval(updateProgress)
          importStatus.value = 'completed'
          progressData.status = 'success'
          ElMessage.success('导入完成')
        }
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    isProcessing.value = false
  }
}

const removeRow = (row: any) => {
  const index = previewData.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    previewData.value.splice(index, 1)
    validationData.total--
    validationData.duplicate--
  }
}

const overrideRow = (row: any) => {
  ElMessage.success('已选择覆盖')
}

const pauseImport = () => {
  importStatus.value = 'paused'
  ElMessage.info('导入已暂停')
}

const resumeImport = () => {
  importStatus.value = 'processing'
  ElMessage.info('导入已继续')
}

const cancelImport = () => {
  ElMessageBox.confirm('确定要取消导入吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    showProgress.value = false
    uploaded.value = false
    importStatus.value = 'idle'
    ElMessage.success('导入已取消')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const exportErrorLog = () => {
  ElMessage.success('错误日志导出成功')
}

const viewDetails = (row: any) => {
  ElMessage.info(`查看导入 ${row.id} 的详情`)
}

const getStatusType = (status: string) => {
  switch (status) {
    case '已完成':
      return 'success'
    case '处理中':
      return 'warning'
    case '失败':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped lang="scss">
.data-source-management {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8e8e8;
  
  h1 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #1890ff;
  }
}

.upload-section {
  display: flex;
  gap: 40px;
  
  .upload-area {
    flex: 1;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 60px 20px;
    text-align: center;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #1890ff;
    }
    
    .upload-icon {
      font-size: 48px;
      color: #1890ff;
      margin-bottom: 16px;
    }
    
    .upload-text {
      p {
        margin: 8px 0;
        color: #666;
        
        &.upload-hint {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
  
  .upload-tips {
    width: 300px;
    
    h3 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 8px;
        color: #666;
        font-size: 14px;
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.validation-section {
  .validation-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .validation-table {
    margin-top: 20px;
  }
}

.progress-section {
  .overall-progress {
    margin-bottom: 30px;
    
    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: #666;
    }
  }
  
  .status-progress {
    margin-bottom: 30px;
    
    .status-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      .status-label {
        width: 80px;
        font-size: 14px;
        color: #666;
      }
      
      .status-bar {
        flex: 1;
        height: 8px;
        background-color: #f0f0f0;
        border-radius: 4px;
        margin: 0 16px;
        overflow: hidden;
        
        .status-fill {
          height: 100%;
          border-radius: 4px;
          transition: width 0.3s ease;
          
          &.parsing {
            background-color: #1890ff;
          }
          
          &.queued {
            background-color: #722ed1;
          }
          
          &.processing {
            background-color: #faad14;
          }
          
          &.success {
            background-color: #52c41a;
          }
          
          &.failure {
            background-color: #ff4d4f;
          }
        }
      }
      
      .status-count {
        width: 60px;
        text-align: right;
        font-size: 14px;
        color: #666;
      }
    }
  }
  
  .failure-log {
    margin-top: 30px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
    
    .el-button {
      margin-bottom: 16px;
    }
  }
}

.history-section {
  margin-top: 20px;
}

.mt-20 {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .upload-section {
    flex-direction: column;
    
    .upload-tips {
      width: 100%;
    }
  }
  
  .validation-stats {
    grid-template-columns: 1fr !important;
  }
}
</style>