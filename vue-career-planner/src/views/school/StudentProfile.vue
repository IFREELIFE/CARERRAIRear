<template>
  <div class="student-profile">
    <h2>学生档案</h2>
    
    <!-- 学生列表 -->
    <el-card shadow="hover" class="student-list-card">
      <template #header>
        <div class="card-header">
          <h3>待就业学生清单</h3>
          <el-input
            v-model="searchQuery"
            placeholder="搜索学生姓名或专业"
            prefix-icon="el-icon-search"
            size="small"
            style="width: 200px;"
          />
        </div>
      </template>
      <el-table
        :data="filteredStudents"
        style="width: 100%"
        border
        stripe
        @row-click="selectStudent"
        class="student-table"
      >
        <el-table-column prop="id" label="学号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="major" label="专业" width="180" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="gpa" label="GPA" width="80" />
        <el-table-column prop="status" label="就业状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '待就业' ? 'warning' : 'success'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small"
              @click.stop="selectStudent(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 学生详情 -->
    <el-card v-if="selectedStudent" shadow="hover" class="profile-card">
      <div class="student-header">
        <div class="student-avatar">
          <el-avatar :size="80" :src="selectedStudent.avatar" />
        </div>
        <div class="student-basic-info">
          <h3>{{ selectedStudent.name }}</h3>
          <div class="info-row">
            <span class="info-item"><strong>学号：</strong>{{ selectedStudent.id }}</span>
            <span class="info-item"><strong>专业：</strong>{{ selectedStudent.major }}</span>
            <span class="info-item"><strong>年级：</strong>{{ selectedStudent.grade }}</span>
            <span class="info-item"><strong>GPA：</strong>{{ selectedStudent.gpa }}</span>
          </div>
          <div class="info-row">
            <span class="info-item"><strong>就业状态：</strong>
              <el-tag :type="selectedStudent.status === '待就业' ? 'warning' : 'success'">
                {{ selectedStudent.status }}
              </el-tag>
            </span>
            <span class="info-item"><strong>授权状态：</strong>
              <el-tag type="success">已授权</el-tag>
            </span>
          </div>
        </div>
      </div>
      
      <!-- 12维能力雷达图 -->
      <div class="card-section">
        <h4>12维能力评估</h4>
        <PortraitRadarChart :data="selectedStudent.twelveDimension" />
      </div>
      
      <!-- 人岗匹配差距清单 -->
      <div class="card-section">
        <h4>人岗匹配差距清单</h4>
        <el-list v-if="selectedStudent.jobMatchGap.length > 0" class="gap-list">
          <el-list-item v-for="(gap, index) in selectedStudent.jobMatchGap" :key="index" class="gap-item">
            <el-card :body-style="{ padding: '16px' }">
              <div class="gap-header">
                <span class="gap-item-name">{{ gap.gapItem }}</span>
                <div class="gap-scores">
                  <span class="student-score">学生得分: {{ gap.studentScore }}</span>
                  <span class="require-score">岗位要求: {{ gap.requireScore }}</span>
                </div>
              </div>
              <div class="gap-suggestion">
                <strong>优化建议：</strong>{{ gap.suggestion }}
              </div>
            </el-card>
          </el-list-item>
        </el-list>
        <el-empty v-else description="暂无匹配差距数据" />
      </div>
      
      <!-- AI职业规划报告 -->
      <div class="card-section">
        <h4>AI职业规划报告</h4>
        <div ref="editorRef" class="ai-report"></div>
      </div>
    </el-card>
    
    <!-- 未选择学生时的提示 -->
    <el-card v-else shadow="hover" class="empty-card">
      <el-empty description="请从左侧列表选择学生查看详情" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import PortraitRadarChart from '../../components/PortraitRadarChart.vue'
import wangEditor from 'wangeditor'

// 学生列表数据
const students = ref([
  {
    id: "2021001",
    name: "张三",
    major: "计算机科学与技术",
    grade: "2024级",
    gpa: 3.8,
    status: "待就业",
    avatar: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20student%20avatar%20male&image_size=square",
    twelveDimension: [
      { dimension: "学习能力", score: 85 },
      { dimension: "团队协作", score: 78 },
      { dimension: "沟通能力", score: 72 },
      { dimension: "专业技能", score: 88 },
      { dimension: "问题解决", score: 75 },
      { dimension: "创新思维", score: 68 },
      { dimension: "领导力", score: 65 },
      { dimension: "时间管理", score: 70 },
      { dimension: "抗压能力", score: 76 },
      { dimension: "适应能力", score: 80 },
      { dimension: "分析能力", score: 82 },
      { dimension: "执行能力", score: 74 }
    ],
    jobMatchGap: [
      { 
        gapItem: "Java技能", 
        studentScore: 60, 
        requireScore: 85, 
        suggestion: "补充Java高级特性学习，如多线程、并发编程、JVM调优等"
      },
      { 
        gapItem: "面试表达", 
        studentScore: 55, 
        requireScore: 75, 
        suggestion: "增加模拟面试练习，提高表达能力和自信心"
      },
      { 
        gapItem: "项目经验", 
        studentScore: 65, 
        requireScore: 80, 
        suggestion: "参与更多实际项目，积累项目经验，特别是团队开发经验"
      }
    ],
    aiCareerReport: "<p>该生专业基础扎实，建议向后端开发方向发展。根据其12维能力评估，学习能力和专业技能表现突出，适合从事技术开发工作。</p><p>建议重点提升Java技能和面试表达能力，通过参加技术培训和模拟面试来弥补差距。同时，增加项目经验，特别是团队开发经验，以提高综合竞争力。</p><p>在职业规划方面，建议先从Java后端开发工程师开始，逐步积累经验，未来可以向技术架构师或技术管理方向发展。</p>"
  },
  {
    id: "2021002",
    name: "李四",
    major: "软件工程",
    grade: "2024级",
    gpa: 3.6,
    status: "待就业",
    avatar: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20student%20avatar%20female&image_size=square",
    twelveDimension: [
      { dimension: "学习能力", score: 80 },
      { dimension: "团队协作", score: 85 },
      { dimension: "沟通能力", score: 88 },
      { dimension: "专业技能", score: 75 },
      { dimension: "问题解决", score: 70 },
      { dimension: "创新思维", score: 72 },
      { dimension: "领导力", score: 78 },
      { dimension: "时间管理", score: 82 },
      { dimension: "抗压能力", score: 68 },
      { dimension: "适应能力", score: 76 },
      { dimension: "分析能力", score: 74 },
      { dimension: "执行能力", score: 80 }
    ],
    jobMatchGap: [
      { 
        gapItem: "前端技能", 
        studentScore: 65, 
        requireScore: 80, 
        suggestion: "加强前端框架学习，如React、Vue等，提高前端开发能力"
      },
      { 
        gapItem: "团队协作", 
        studentScore: 70, 
        requireScore: 85, 
        suggestion: "参与更多团队项目，提高团队协作能力和沟通能力"
      }
    ],
    aiCareerReport: "<p>该生沟通能力和团队协作能力突出，建议向产品经理或前端开发方向发展。根据其12维能力评估，领导力和时间管理能力也表现良好，适合需要与人协作的岗位。</p><p>建议重点提升前端技能，加强框架学习，同时继续发挥沟通和团队协作的优势。</p><p>在职业规划方面，建议先从前端开发工程师开始，逐步积累经验，未来可以向产品经理或技术管理方向发展。</p>"
  },
  {
    id: "2021003",
    name: "王五",
    major: "数据科学与大数据技术",
    grade: "2024级",
    gpa: 3.9,
    status: "待就业",
    avatar: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20student%20avatar%20male%20glasses&image_size=square",
    twelveDimension: [
      { dimension: "学习能力", score: 90 },
      { dimension: "团队协作", score: 72 },
      { dimension: "沟通能力", score: 68 },
      { dimension: "专业技能", score: 92 },
      { dimension: "问题解决", score: 85 },
      { dimension: "创新思维", score: 80 },
      { dimension: "领导力", score: 65 },
      { dimension: "时间管理", score: 75 },
      { dimension: "抗压能力", score: 82 },
      { dimension: "适应能力", score: 70 },
      { dimension: "分析能力", score: 95 },
      { dimension: "执行能力", score: 78 }
    ],
    jobMatchGap: [
      { 
        gapItem: "沟通能力", 
        studentScore: 68, 
        requireScore: 80, 
        suggestion: "加强沟通能力训练，提高表达能力和人际交往能力"
      },
      { 
        gapItem: "项目经验", 
        studentScore: 70, 
        requireScore: 85, 
        suggestion: "参与更多数据分析项目，积累实际项目经验"
      }
    ],
    aiCareerReport: "<p>该生专业技能和分析能力突出，建议向数据分析师或数据科学家方向发展。根据其12维能力评估，学习能力和问题解决能力也表现优秀，适合需要深入分析的技术岗位。</p><p>建议重点提升沟通能力，加强表达能力训练，同时积累更多实际项目经验。</p><p>在职业规划方面，建议先从数据分析师开始，逐步积累经验，未来可以向数据科学家或机器学习工程师方向发展。</p>"
  }
])

// 搜索查询
const searchQuery = ref('')

// 筛选后的学生列表
const filteredStudents = computed(() => {
  if (!searchQuery.value) {
    return students.value
  }
  const query = searchQuery.value.toLowerCase()
  return students.value.filter(student => 
    student.name.toLowerCase().includes(query) || 
    student.major.toLowerCase().includes(query)
  )
})

// 选中的学生
const selectedStudent = ref<any>(null)

// 选择学生
const selectStudent = (student: any) => {
  selectedStudent.value = student
  // 重新初始化编辑器内容
  if (editor) {
    editor.txt.html(student.aiCareerReport)
  }
}

// wangEditor实例
const editorRef = ref<HTMLElement | null>(null)
let editor: any = null

onMounted(() => {
  // 初始化wangEditor预览模式
  if (editorRef.value) {
    editor = new wangEditor(editorRef.value)
    editor.config.placeholder = 'AI职业规划报告'
    editor.config.readOnly = true
    editor.create()
    // 默认选择第一个学生
    if (students.value.length > 0) {
      selectStudent(students.value[0])
    }
  }
})

onUnmounted(() => {
  // 销毁wangEditor实例
  if (editor) {
    editor.destroy()
  }
})

// 监听选中学生变化，更新编辑器内容
watch(selectedStudent, (newStudent) => {
  if (newStudent && editor) {
    editor.txt.html(newStudent.aiCareerReport)
  }
})
</script>

<style scoped lang="scss">
.student-profile {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .student-list-card {
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
    }
    
    .student-table {
      margin-top: 16px;
      
      :deep(.el-table-row:hover) {
        background-color: #f5f7fa;
      }
      
      :deep(.el-table-column--selection) {
        width: 50px;
      }
    }
  }
  
  .profile-card {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .student-header {
      display: flex;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      background-color: #f9f9f9;
      
      .student-avatar {
        margin-right: 20px;
        
        img {
          border-radius: 50%;
          object-fit: cover;
        }
      }
      
      .student-basic-info {
        flex: 1;
        
        h3 {
          margin: 0 0 12px;
          font-size: 20px;
          font-weight: 600;
          color: #333;
        }
        
        .info-row {
          display: flex;
          gap: 20px;
          margin-bottom: 8px;
          flex-wrap: wrap;
          
          .info-item {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
    
    .card-section {
      padding: 24px;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      h4 {
        margin: 0 0 20px;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }
    
    .gap-list {
      .gap-item {
        padding: 0 0 16px 0;
        
        .el-card {
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
          }
          
          .gap-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .gap-item-name {
              font-size: 14px;
              font-weight: 600;
              color: #333;
            }
            
            .gap-scores {
              display: flex;
              gap: 16px;
              
              .student-score {
                font-size: 12px;
                color: #666;
              }
              
              .require-score {
                font-size: 12px;
                color: #f56c6c;
              }
            }
          }
          
          .gap-suggestion {
            font-size: 14px;
            color: #666;
            line-height: 1.5;
          }
        }
      }
    }
    
    .ai-report {
      min-height: 200px;
      padding: 20px;
      background-color: #f9f9f9;
      border-radius: 8px;
      border: 1px solid #e4e7ed;
      
      :deep(p) {
        margin: 0 0 16px;
        line-height: 1.6;
        color: #333;
      }
      
      :deep(p:last-child) {
        margin-bottom: 0;
      }
    }
  }
  
  .empty-card {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .student-profile {
    padding: 10px;
    
    .student-list-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
        
        .el-input {
          width: 100% !important;
        }
      }
    }
    
    .profile-card {
      .student-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
        
        .student-avatar {
          margin-right: 0;
        }
      }
      
      .info-row {
        flex-direction: column;
        gap: 8px;
      }
    }
  }
}
</style>