<template>
  <div class="job-management">
    <div class="page-header">
      <h1>岗位管理</h1>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新增岗位
      </el-button>
    </div>
    
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索岗位名称"
          style="width: 300px"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="岗位状态" clearable style="width: 150px; margin-left: 10px">
          <el-option label="招聘中" value="招聘中" />
          <el-option label="已暂停" value="已暂停" />
          <el-option label="已结束" value="已结束" />
        </el-select>
      </div>
      
      <el-table :data="filteredJobs" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="岗位ID" width="80" />
        <el-table-column prop="title" label="岗位名称" width="180" />
        <el-table-column prop="department" label="所属部门" width="120" />
        <el-table-column prop="location" label="工作地点" width="120" />
        <el-table-column prop="salary" label="薪资范围" width="150" />
        <el-table-column prop="experience" label="经验要求" width="100" />
        <el-table-column prop="education" label="学历要求" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicants" label="投递人数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewJob(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteJob(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑岗位' : '新增岗位'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="jobForm" :rules="jobRules" ref="jobFormRef" label-width="100px">
        <el-form-item label="岗位名称" prop="title">
          <el-input v-model="jobForm.title" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="jobForm.department" placeholder="请选择所属部门" style="width: 100%">
            <el-option label="技术部" value="技术部" />
            <el-option label="产品部" value="产品部" />
            <el-option label="设计部" value="设计部" />
            <el-option label="运营部" value="运营部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="人事部" value="人事部" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="jobForm.location" placeholder="请输入工作地点" />
        </el-form-item>
        <el-form-item label="薪资范围" prop="salary">
          <el-input v-model="jobForm.salary" placeholder="例如：15k-25k" />
        </el-form-item>
        <el-form-item label="经验要求" prop="experience">
          <el-select v-model="jobForm.experience" placeholder="请选择经验要求" style="width: 100%">
            <el-option label="不限" value="不限" />
            <el-option label="1-3年" value="1-3年" />
            <el-option label="3-5年" value="3-5年" />
            <el-option label="5-10年" value="5-10年" />
            <el-option label="10年以上" value="10年以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="学历要求" prop="education">
          <el-select v-model="jobForm.education" placeholder="请选择学历要求" style="width: 100%">
            <el-option label="不限" value="不限" />
            <el-option label="大专" value="大专" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input
            v-model="jobForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入岗位描述"
          />
        </el-form-item>
        <el-form-item label="任职要求" prop="requirements">
          <el-input
            v-model="jobForm.requirements"
            type="textarea"
            :rows="4"
            placeholder="请输入任职要求"
          />
        </el-form-item>
        <el-form-item label="岗位状态" prop="status">
          <el-radio-group v-model="jobForm.status">
            <el-radio label="招聘中">招聘中</el-radio>
            <el-radio label="已暂停">已暂停</el-radio>
            <el-radio label="已结束">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitJob" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="viewDialogVisible" title="岗位详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ currentJob.title }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ currentJob.department }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ currentJob.location }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ currentJob.salary }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ currentJob.experience }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ currentJob.education }}</el-descriptions-item>
        <el-descriptions-item label="岗位状态">
          <el-tag :type="getStatusType(currentJob.status)">{{ currentJob.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="投递人数">{{ currentJob.applicants }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">{{ currentJob.description }}</el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="2">{{ currentJob.requirements }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentJob.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentJob.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jobFormRef = ref()

const jobForm = ref({
  id: '',
  title: '',
  department: '',
  location: '',
  salary: '',
  experience: '',
  education: '',
  description: '',
  requirements: '',
  status: '招聘中'
})

const currentJob = ref<any>({})

const jobRules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  department: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  location: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  salary: [{ required: true, message: '请输入薪资范围', trigger: 'blur' }],
  experience: [{ required: true, message: '请选择经验要求', trigger: 'change' }],
  education: [{ required: true, message: '请选择学历要求', trigger: 'change' }],
  status: [{ required: true, message: '请选择岗位状态', trigger: 'change' }]
}

const jobs = ref([
  {
    id: '1',
    title: '高级前端工程师',
    department: '技术部',
    location: '北京',
    salary: '25k-40k',
    experience: '3-5年',
    education: '本科',
    status: '招聘中',
    applicants: 45,
    description: '负责公司核心产品的前端开发工作，参与产品需求分析、技术方案设计、代码编写和优化。',
    requirements: '熟练掌握 Vue3/React，熟悉 TypeScript，有大型项目经验优先。',
    createTime: '2024-03-01 10:00',
    updateTime: '2024-03-10 15:30'
  },
  {
    id: '2',
    title: '产品经理',
    department: '产品部',
    location: '上海',
    salary: '20k-35k',
    experience: '3-5年',
    education: '本科',
    status: '招聘中',
    applicants: 32,
    description: '负责产品规划、需求分析、原型设计，推动产品迭代优化。',
    requirements: '3年以上产品经验，有B端产品经验优先，良好的沟通协调能力。',
    createTime: '2024-03-02 14:00',
    updateTime: '2024-03-08 11:20'
  },
  {
    id: '3',
    title: 'UI设计师',
    department: '设计部',
    location: '深圳',
    salary: '15k-25k',
    experience: '1-3年',
    education: '本科',
    status: '招聘中',
    applicants: 28,
    description: '负责产品UI设计，包括界面设计、交互设计、视觉规范制定。',
    requirements: '熟练使用 Figma/Sketch，有良好的审美和设计能力。',
    createTime: '2024-03-03 09:00',
    updateTime: '2024-03-09 16:45'
  },
  {
    id: '4',
    title: 'Java后端工程师',
    department: '技术部',
    location: '北京',
    salary: '20k-35k',
    experience: '3-5年',
    education: '本科',
    status: '已暂停',
    applicants: 56,
    description: '负责后端服务开发，参与系统架构设计，优化系统性能。',
    requirements: '熟练掌握 Java，熟悉 Spring Boot，有微服务经验优先。',
    createTime: '2024-02-15 11:00',
    updateTime: '2024-03-05 10:30'
  },
  {
    id: '5',
    title: '数据分析师',
    department: '运营部',
    location: '杭州',
    salary: '18k-30k',
    experience: '1-3年',
    education: '本科',
    status: '招聘中',
    applicants: 19,
    description: '负责数据分析、报表制作、数据挖掘，为业务决策提供支持。',
    requirements: '熟练使用 SQL、Python，熟悉数据可视化工具。',
    createTime: '2024-03-05 13:00',
    updateTime: '2024-03-10 09:15'
  },
  {
    id: '6',
    title: '运营专员',
    department: '运营部',
    location: '上海',
    salary: '10k-15k',
    experience: '不限',
    education: '大专',
    status: '已结束',
    applicants: 78,
    description: '负责日常运营工作，包括内容运营、活动策划、用户维护。',
    requirements: '有运营经验优先，良好的沟通能力和执行力。',
    createTime: '2024-01-20 10:00',
    updateTime: '2024-02-28 17:00'
  },
  {
    id: '7',
    title: '测试工程师',
    department: '技术部',
    location: '北京',
    salary: '15k-25k',
    experience: '1-3年',
    education: '本科',
    status: '招聘中',
    applicants: 23,
    description: '负责软件测试工作，编写测试用例，执行测试，跟踪缺陷。',
    requirements: '熟悉测试流程和工具，有自动化测试经验优先。',
    createTime: '2024-03-06 15:00',
    updateTime: '2024-03-10 14:20'
  },
  {
    id: '8',
    title: '人力资源专员',
    department: '人事部',
    location: '深圳',
    salary: '8k-12k',
    experience: '不限',
    education: '本科',
    status: '招聘中',
    applicants: 34,
    description: '负责招聘、员工关系、培训等人力资源管理工作。',
    requirements: '有人力资源经验优先，良好的沟通协调能力。',
    createTime: '2024-03-07 09:30',
    updateTime: '2024-03-09 11:45'
  }
])

const filteredJobs = computed(() => {
  let result = jobs.value
  
  if (searchKeyword.value) {
    result = result.filter(job => 
      job.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  if (statusFilter.value) {
    result = result.filter(job => job.status === statusFilter.value)
  }
  
  total.value = result.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const getStatusType = (status: string) => {
  switch (status) {
    case '招聘中':
      return 'success'
    case '已暂停':
      return 'warning'
    case '已结束':
      return 'info'
    default:
      return ''
  }
}

const openDialog = (job?: any) => {
  if (job) {
    isEdit.value = true
    jobForm.value = { ...job }
  } else {
    isEdit.value = false
    jobForm.value = {
      id: '',
      title: '',
      department: '',
      location: '',
      salary: '',
      experience: '',
      education: '',
      description: '',
      requirements: '',
      status: '招聘中'
    }
  }
  dialogVisible.value = true
}

const resetForm = () => {
  if (jobFormRef.value) {
    jobFormRef.value.resetFields()
  }
}

const submitJob = async () => {
  if (!jobFormRef.value) return
  
  jobFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        submitLoading.value = true
        await new Promise(resolve => setTimeout(resolve, 500))
        
        if (isEdit.value) {
          const index = jobs.value.findIndex(j => j.id === jobForm.value.id)
          if (index > -1) {
            jobs.value[index] = {
              ...jobForm.value,
              applicants: jobs.value[index].applicants,
              createTime: jobs.value[index].createTime,
              updateTime: new Date().toLocaleString('zh-CN')
            }
          }
          ElMessage.success('岗位更新成功')
        } else {
          const newJob = {
            ...jobForm.value,
            id: Date.now().toString(),
            applicants: 0,
            createTime: new Date().toLocaleString('zh-CN'),
            updateTime: new Date().toLocaleString('zh-CN')
          }
          jobs.value.unshift(newJob)
          ElMessage.success('岗位创建成功')
        }
        
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const viewJob = (job: any) => {
  currentJob.value = job
  viewDialogVisible.value = true
}

const deleteJob = (job: any) => {
  ElMessageBox.confirm('确定要删除该岗位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = jobs.value.findIndex(j => j.id === job.id)
    if (index > -1) {
      jobs.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

onMounted(() => {
  total.value = jobs.value.length
})
</script>

<style scoped lang="scss">
.job-management {
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
    color: #333;
  }
}

.search-bar {
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>