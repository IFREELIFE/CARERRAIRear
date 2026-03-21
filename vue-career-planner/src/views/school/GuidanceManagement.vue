<template>
  <div class="guidance-management">
    <h2>辅导管理</h2>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="老师信息管理" name="teachers">
        <el-card shadow="hover" class="management-card">
          <template #header>
            <div class="card-header">
              <h3>学校老师信息</h3>
              <el-button type="primary" size="small" @click="dialogVisible = true">
                <el-icon><Plus /></el-icon>
                添加老师
              </el-button>
            </div>
          </template>
          <el-table
            :data="teachers"
            style="width: 100%"
            border
            stripe
          >
            <el-table-column prop="id" label="老师ID" width="100" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="subject" label="专业领域" width="180" />
            <el-table-column prop="phone" label="联系电话" width="150" />
            <el-table-column prop="guidanceTime" label="指导时间" width="200" />
            <el-table-column prop="location" label="指导地点" width="150" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="editTeacher(scope.row)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="deleteTeacher(scope.row.id)"
                  style="margin-left: 8px"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="辅导纪要登记" name="records">
        <el-card shadow="hover" class="management-card">
          <template #header>
            <div class="card-header">
              <h3>辅导纪要登记</h3>
            </div>
          </template>
          <el-form :model="recordForm" label-width="120px" class="record-form">
            <el-form-item label="学生姓名">
              <el-select v-model="recordForm.studentId" placeholder="请选择学生">
                <el-option 
                  v-for="student in students" 
                  :key="student.id" 
                  :label="student.name" 
                  :value="student.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="辅导老师">
              <el-select v-model="recordForm.teacherId" placeholder="请选择老师">
                <el-option 
                  v-for="teacher in teachers" 
                  :key="teacher.id" 
                  :label="teacher.name" 
                  :value="teacher.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="辅导日期">
              <el-date-picker
                v-model="recordForm.guidanceDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="性格优劣势">
              <el-input
                v-model="recordForm.personality"
                type="textarea"
                placeholder="请输入学生的性格优劣势，如：表达内向、面试紧张"
                rows="4"
              />
            </el-form-item>
            <el-form-item label="发展建议">
              <el-input
                v-model="recordForm.suggestions"
                type="textarea"
                placeholder="请输入对学生发展的建议"
                rows="4"
              />
            </el-form-item>
            <el-form-item label="学生信息">
              <el-card v-if="selectedStudent" :body-style="{ padding: '16px' }">
                <h4>{{ selectedStudent.name }} - {{ selectedStudent.major }}</h4>
                <p><strong>年级：</strong>{{ selectedStudent.grade }}</p>
                <p><strong>12维能力评估：</strong></p>
                <div class="student-dimensions">
                  <div 
                    v-for="(item, index) in selectedStudent.twelveDimension.slice(0, 6)" 
                    :key="index"
                    class="dimension-item"
                  >
                    <span>{{ item.dimension }}: {{ item.score }}</span>
                  </div>
                </div>
              </el-card>
              <el-empty v-else description="请选择学生查看信息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitRecord">提交纪要</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 老师信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑老师' : '添加老师'"
      width="500px"
    >
      <el-form :model="teacherForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="teacherForm.name" placeholder="请输入老师姓名" />
        </el-form-item>
        <el-form-item label="专业领域">
          <el-input v-model="teacherForm.subject" placeholder="请输入专业领域" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="teacherForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="指导时间">
          <el-input v-model="teacherForm.guidanceTime" placeholder="请输入指导时间，如：每周一 14:00-16:00" />
        </el-form-item>
        <el-form-item label="指导地点">
          <el-input v-model="teacherForm.location" placeholder="请输入指导地点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTeacher">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 激活的标签页
const activeTab = ref('teachers')

// 老师列表
const teachers = ref([
  {
    id: 'T001',
    name: '王老师',
    subject: '计算机科学与技术',
    phone: '13800138001',
    guidanceTime: '每周一 14:00-16:00',
    location: '教学楼 A301'
  },
  {
    id: 'T002',
    name: '李老师',
    subject: '软件工程',
    phone: '13800138002',
    guidanceTime: '每周二 10:00-12:00',
    location: '教学楼 A302'
  },
  {
    id: 'T003',
    name: '张老师',
    subject: '数据科学',
    phone: '13800138003',
    guidanceTime: '每周三 14:00-16:00',
    location: '教学楼 A303'
  }
])

// 学生列表
const students = ref([
  {
    id: '2021001',
    name: '张三',
    major: '计算机科学与技术',
    grade: '2024级',
    twelveDimension: [
      { dimension: "学习能力", score: 85 },
      { dimension: "团队协作", score: 78 },
      { dimension: "沟通能力", score: 72 },
      { dimension: "专业技能", score: 88 },
      { dimension: "问题解决", score: 75 },
      { dimension: "创新思维", score: 68 }
    ]
  },
  {
    id: '2021002',
    name: '李四',
    major: '软件工程',
    grade: '2024级',
    twelveDimension: [
      { dimension: "学习能力", score: 80 },
      { dimension: "团队协作", score: 85 },
      { dimension: "沟通能力", score: 88 },
      { dimension: "专业技能", score: 75 },
      { dimension: "问题解决", score: 70 },
      { dimension: "创新思维", score: 72 }
    ]
  },
  {
    id: '2021003',
    name: '王五',
    major: '数据科学与大数据技术',
    grade: '2024级',
    twelveDimension: [
      { dimension: "学习能力", score: 90 },
      { dimension: "团队协作", score: 72 },
      { dimension: "沟通能力", score: 68 },
      { dimension: "专业技能", score: 92 },
      { dimension: "问题解决", score: 85 },
      { dimension: "创新思维", score: 80 }
    ]
  }
])

// 选中的学生
const selectedStudent = ref<any>(null)

// 老师信息对话框
const dialogVisible = ref(false)
const isEditing = ref(false)
const teacherForm = ref({
  id: '',
  name: '',
  subject: '',
  phone: '',
  guidanceTime: '',
  location: ''
})

// 辅导纪要表单
const recordForm = ref({
  studentId: '',
  teacherId: '',
  guidanceDate: '',
  personality: '',
  suggestions: ''
})

// 监听学生选择，更新选中的学生信息
watch(() => recordForm.value.studentId, (newStudentId) => {
  if (newStudentId) {
    selectedStudent.value = students.value.find(student => student.id === newStudentId)
  } else {
    selectedStudent.value = null
  }
})

// 添加老师
// 编辑老师
const editTeacher = (teacher: any) => {
  isEditing.value = true
  teacherForm.value = { ...teacher }
  dialogVisible.value = true
}

// 保存老师信息
const saveTeacher = () => {
  if (!teacherForm.value.name || !teacherForm.value.subject || !teacherForm.value.phone || !teacherForm.value.guidanceTime || !teacherForm.value.location) {
    ElMessage.error('请填写完整的老师信息')
    return
  }
  
  if (isEditing.value) {
    // 编辑现有老师
    const index = teachers.value.findIndex(t => t.id === teacherForm.value.id)
    if (index !== -1) {
      teachers.value[index] = { ...teacherForm.value }
      ElMessage.success('老师信息更新成功')
    }
  } else {
    // 添加新老师
    const newTeacher = {
      ...teacherForm.value,
      id: `T${String(teachers.value.length + 1).padStart(3, '0')}`
    }
    teachers.value.push(newTeacher)
    ElMessage.success('老师添加成功')
  }
  
  dialogVisible.value = false
}

// 删除老师
const deleteTeacher = (id: string) => {
  ElMessageBox.confirm('确定要删除该老师吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = teachers.value.findIndex(t => t.id === id)
    if (index !== -1) {
      teachers.value.splice(index, 1)
      ElMessage.success('老师删除成功')
    }
  }).catch(() => {})
}

// 提交辅导纪要
const submitRecord = () => {
  if (!recordForm.value.studentId || !recordForm.value.teacherId || !recordForm.value.guidanceDate || !recordForm.value.personality || !recordForm.value.suggestions) {
    ElMessage.error('请填写完整的辅导纪要信息')
    return
  }
  
  // 模拟提交
  ElMessage.success('辅导纪要提交成功，系统已触发后台任务')
  resetForm()
}

// 重置表单
const resetForm = () => {
  recordForm.value = {
    studentId: '',
    teacherId: '',
    guidanceDate: '',
    personality: '',
    suggestions: ''
  }
  selectedStudent.value = null
}
</script>

<style scoped lang="scss">
.guidance-management {
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
    }
  }
  
  .record-form {
    margin-top: 20px;
    
    .student-dimensions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 10px;
      margin-top: 10px;
      
      .dimension-item {
        font-size: 14px;
        color: #666;
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .guidance-management {
    padding: 10px;
    
    .management-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
      }
    }
  }
}
</style>
