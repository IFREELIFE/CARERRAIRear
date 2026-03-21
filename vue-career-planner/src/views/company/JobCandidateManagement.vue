<template>
  <div class="job-candidate-management">
    <h2>岗位投递管理</h2>
    
    <el-tabs v-model="activeTab">
      <!-- 本企岗位子模块 -->
      <el-tab-pane label="本企岗位" name="jobs">
        <el-card shadow="hover" class="management-card">
          <template #header>
            <div class="card-header">
              <h3>本企岗位列表</h3>
              <div class="filter-bar">
                <el-select v-model="jobStatus" placeholder="岗位状态" size="small" style="width: 120px; margin-right: 10px;">
                  <el-option label="全部" value="" />
                  <el-option label="生效" value="active" />
                  <el-option label="暂停" value="paused" />
                </el-select>
                <el-input
                  v-model="jobKeyword"
                  placeholder="搜索岗位名称"
                  prefix-icon="el-icon-search"
                  size="small"
                  style="width: 200px;"
                />
              </div>
            </div>
          </template>
          <el-table
            v-loading="jobLoading"
            :data="filteredJobs"
            style="width: 100%"
            border
            stripe
            @row-click="selectJob"
          >
            <el-table-column prop="name" label="岗位名称" width="200" />
            <el-table-column prop="recruitCount" label="招聘人数" width="100" />
            <el-table-column prop="status" label="发布状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
                  {{ scope.row.status === 'active' ? '生效' : '暂停' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="aiRequirements" label="AI能力要求画像">
              <template #default="scope">
                <div class="tag-group">
                  <el-tag 
                    v-for="tag in scope.row.aiRequirements" 
                    :key="tag"
                    size="small"
                    @click="showPersonaDetail(tag, scope.row)"
                    class="persona-tag"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small"
                  @click.stop="selectJob(scope.row)"
                >
                  查看候选人
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="jobCurrentPage"
              v-model:page-size="jobPageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredJobs.length"
              :prev-text="'上一页'"
              :next-text="'下一页'"
              :page-sizes-text="'每页条数'"
              :total-text="'共'"
              @size-change="handleJobSizeChange"
              @current-change="handleJobCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 候选人列表子模块 -->
      <el-tab-pane label="候选人列表" name="candidates">
        <el-card shadow="hover" class="management-card" v-if="selectedJob">
          <template #header>
            <div class="card-header">
              <h3>{{ selectedJob.name }} - 候选人列表</h3>
              <div class="filter-bar">
                <el-select v-model="matchScoreSort" placeholder="匹配度排序" size="small" style="width: 120px; margin-right: 10px;">
                  <el-option label="默认" value="" />
                  <el-option label="匹配度升序" value="asc" />
                  <el-option label="匹配度降序" value="desc" />
                </el-select>
                <el-date-picker
                  v-model="deliveryDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  style="width: 220px;"
                />
              </div>
            </div>
          </template>
          <el-table
            v-loading="candidateLoading"
            :data="sortedCandidates"
            style="width: 100%"
            border
            stripe
          >
            <el-table-column prop="name" label="姓名" width="100">
              <template #default="scope">
                <a href="#" @click.prevent="viewCandidateDetail(scope.row.id)">{{ scope.row.name }}</a>
              </template>
            </el-table-column>
            <el-table-column prop="school" label="学校" width="150" />
            <el-table-column prop="major" label="专业" width="150" />
            <el-table-column prop="matchedJob" label="匹配岗位" width="150" />
            <el-table-column prop="matchScore" label="匹配度" width="180">
              <template #default="scope">
                <div class="match-score-container">
                  <el-progress 
                    :percentage="scope.row.matchScore" 
                    :color="getMatchScoreColor(scope.row.matchScore)"
                    :stroke-width="10"
                    :show-text="false"
                  />
                  <span class="match-score-text">{{ scope.row.matchScore }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="deliveryTime" label="投递时间" width="180" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="candidateCurrentPage"
              v-model:page-size="candidatePageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="sortedCandidates.length"
              :prev-text="'上一页'"
              :next-text="'下一页'"
              :page-sizes-text="'每页条数'"
              :total-text="'共'"
              @size-change="handleCandidateSizeChange"
              @current-change="handleCandidateCurrentChange"
            />
          </div>
        </el-card>
        <el-card v-else shadow="hover" class="management-card">
          <el-empty description="请先从本企岗位列表中选择一个岗位" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 画像详情弹窗 -->
    <el-dialog
      v-model="personaDialogVisible"
      :title="`${selectedTag} - 画像详情`"
      width="500px"
    >
      <div class="persona-detail">
        <h4>岗位：{{ selectedJob?.name }}</h4>
        <p><strong>能力要求：</strong>{{ selectedTag }}</p>
        <p><strong>详细描述：</strong>该岗位对{{ selectedTag }}能力有较高要求，需要具备相关的专业知识和实践经验。</p>
        <p><strong>匹配建议：</strong>建议寻找具有相关技能和经验的候选人，重点考察其实际项目经验和技能掌握程度。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useJobStore } from '../../stores/jobStore'

const jobStore = useJobStore()

// 激活的标签页
const activeTab = ref('jobs')

// 岗位相关数据
const jobs = ref([
  {
    id: 'J001',
    name: '前端开发工程师',
    recruitCount: 5,
    status: 'active',
    aiRequirements: ['Vue3', 'TypeScript', 'React', '沟通能力', '团队协作']
  },
  {
    id: 'J002',
    name: '后端开发工程师',
    recruitCount: 3,
    status: 'active',
    aiRequirements: ['Java', 'Spring Boot', 'MySQL', '微服务', '问题解决']
  },
  {
    id: 'J003',
    name: '产品经理',
    recruitCount: 2,
    status: 'paused',
    aiRequirements: ['产品规划', '用户研究', '数据分析', '沟通能力', '项目管理']
  },
  {
    id: 'J004',
    name: 'UI设计师',
    recruitCount: 2,
    status: 'active',
    aiRequirements: ['Figma', 'Photoshop', 'UI设计', '用户体验', '创新思维']
  }
])

// 候选人相关数据
const candidates = ref([
  {
    id: 'C001',
    name: '张三',
    school: '北京大学',
    major: '计算机科学与技术',
    matchedJob: '前端开发工程师',
    matchScore: 92,
    deliveryTime: '2026-03-10 14:30:00'
  },
  {
    id: 'C002',
    name: '李四',
    school: '清华大学',
    major: '软件工程',
    matchedJob: '前端开发工程师',
    matchScore: 85,
    deliveryTime: '2026-03-09 10:15:00'
  },
  {
    id: 'C003',
    name: '王五',
    school: '复旦大学',
    major: '计算机科学与技术',
    matchedJob: '前端开发工程师',
    matchScore: 78,
    deliveryTime: '2026-03-08 16:45:00'
  },
  {
    id: 'C004',
    name: '赵六',
    school: '上海交通大学',
    major: '软件工程',
    matchedJob: '前端开发工程师',
    matchScore: 95,
    deliveryTime: '2026-03-07 09:30:00'
  }
])

// 筛选条件
const jobStatus = ref('')
const jobKeyword = ref('')
const matchScoreSort = ref('')
const deliveryDateRange = ref([])

// 分页相关
const jobCurrentPage = ref(1)
const jobPageSize = ref(10)
const candidateCurrentPage = ref(1)
const candidatePageSize = ref(10)

// 加载状态
const jobLoading = ref(false)
const candidateLoading = ref(false)

// 选中的岗位和标签
const selectedJob = ref<any>(null)
const selectedTag = ref('')
const personaDialogVisible = ref(false)

// 筛选后的岗位列表
const filteredJobs = computed(() => {
  let result = jobs.value
  
  // 状态筛选
  if (jobStatus.value) {
    result = result.filter(job => job.status === jobStatus.value)
  }
  
  // 关键词筛选
  if (jobKeyword.value) {
    const keyword = jobKeyword.value.toLowerCase()
    result = result.filter(job => job.name.toLowerCase().includes(keyword))
  }
  
  return result
})

// 排序后的候选人列表
const sortedCandidates = computed(() => {
  let result = candidates.value
  
  // 匹配度排序
  if (matchScoreSort.value === 'asc') {
    result = [...result].sort((a, b) => a.matchScore - b.matchScore)
  } else if (matchScoreSort.value === 'desc') {
    result = [...result].sort((a, b) => b.matchScore - a.matchScore)
  }
  
  return result
})

// 选择岗位
const selectJob = (job: any) => {
  selectedJob.value = job
  jobStore.setSelectedJobId(job.id)
  activeTab.value = 'candidates'
}

// 查看候选人详情
const viewCandidateDetail = (candidateId: string) => {
  // 这里应该跳转到智能简历筛选页面，并传递候选人ID
  // 暂时使用alert模拟
  alert(`查看候选人 ${candidateId} 的智能简历筛选`)
}

// 显示画像详情
const showPersonaDetail = (tag: string, job: any) => {
  selectedTag.value = tag
  selectedJob.value = job
  personaDialogVisible.value = true
}

// 获取匹配度颜色
const getMatchScoreColor = (score: number) => {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 分页处理
const handleJobSizeChange = (size: number) => {
  jobPageSize.value = size
  jobCurrentPage.value = 1
}

const handleJobCurrentChange = (current: number) => {
  jobCurrentPage.value = current
}

const handleCandidateSizeChange = (size: number) => {
  candidatePageSize.value = size
  candidateCurrentPage.value = 1
}

const handleCandidateCurrentChange = (current: number) => {
  candidateCurrentPage.value = current
}

// 监听岗位状态和关键词变化，重置分页
watch([jobStatus, jobKeyword], () => {
  jobCurrentPage.value = 1
})

// 监听匹配度排序变化，重置分页
watch(matchScoreSort, () => {
  candidateCurrentPage.value = 1
})
</script>

<style scoped lang="scss">
.job-candidate-management {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .management-card {
    margin-bottom: 24px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      
      .filter-bar {
        display: flex;
        align-items: center;
      }
    }
  }
  
  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .persona-tag {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background-color: #ecf5ff;
      color: #1890ff;
    }
  }
  
  .match-score-container {
    position: relative;
    
    .match-score-text {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 12px;
      font-weight: 500;
      color: #606266;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .persona-detail {
    p {
      margin: 10px 0;
      line-height: 1.5;
    }
  }
}

@media (max-width: 768px) {
  .job-candidate-management {
    padding: 10px;
    
    .management-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
        
        .filter-bar {
          width: 100%;
          flex-wrap: wrap;
          gap: 10px;
          
          .el-select,
          .el-input,
          .el-date-picker {
            width: 100% !important;
          }
        }
      }
    }
  }
}
</style>
