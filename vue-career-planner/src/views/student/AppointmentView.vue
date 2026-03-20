<template>
  <div class="appointment-view">
    <h2>预约1v1指导</h2>
    
    <el-card shadow="hover" class="appointment-card">
      <div class="appointment-header">
        <h3>选择指导老师</h3>
        <p class="appointment-desc">请选择您想预约的指导老师</p>
      </div>
      
      <div class="teachers-list">
        <div class="teacher-item" v-for="(teacher, index) in teachers" :key="index">
          <div class="teacher-info">
            <el-avatar :size="60" :src="teacher.avatar" />
            <div class="teacher-details">
              <h4>{{ teacher.name }}</h4>
              <p class="teacher-title">{{ teacher.title }}</p>
              <p class="teacher-expertise">专长：{{ teacher.expertise }}</p>
            </div>
          </div>
          <el-button type="primary" size="small" @click="selectTeacher(teacher)">
            选择
          </el-button>
        </div>
      </div>
      
      <div v-if="selectedTeacher" class="time-selection">
        <h3>选择预约时间</h3>
        <div class="time-slots">
          <div class="time-slot" v-for="(slot, index) in timeSlots" :key="index"
               :class="{ 'selected': selectedTime === slot, 'disabled': slot.disabled }"
               @click="selectTime(slot)"
               :disabled="slot.disabled">
            <span class="slot-date">{{ slot.date }}</span>
            <span class="slot-time">{{ slot.time }}</span>
          </div>
        </div>
        
        <div class="appointment-actions">
          <el-button @click="cancelSelection">取消</el-button>
          <el-button type="primary" @click="confirmAppointment" :disabled="!selectedTime">
            确认预约
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-card shadow="hover" class="my-appointments">
      <h3>我的预约</h3>
      <div v-if="myAppointments.length > 0" class="appointments-list">
        <div class="appointment-item" v-for="(appointment, index) in myAppointments" :key="index">
          <div class="appointment-info">
            <h4>{{ appointment.teacher }}</h4>
            <p class="appointment-time">{{ appointment.date }} {{ appointment.time }}</p>
            <p class="appointment-status" :class="appointment.status">{{ appointment.statusText }}</p>
          </div>
          <el-button size="small" @click="cancelAppointment(index)" :disabled="appointment.status !== 'pending'">
            取消预约
          </el-button>
        </div>
      </div>
      <el-empty v-else description="暂无预约记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

// 老师列表
const teachers = ref([
  {
    id: 1,
    name: '王老师',
    title: '职业规划师',
    expertise: '计算机科学、软件开发',
    avatar: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20teacher%20portrait%20male&image_size=square'
  },
  {
    id: 2,
    name: '李老师',
    title: '就业指导专家',
    expertise: '简历优化、面试技巧',
    avatar: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20teacher%20portrait%20female&image_size=square'
  },
  {
    id: 3,
    name: '张老师',
    title: '行业导师',
    expertise: '互联网行业、产品经理',
    avatar: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20teacher%20portrait%20middle%20age&image_size=square'
  }
])

// 时间槽
const timeSlots = ref([
  { date: '2026-03-15', time: '14:00-15:00', disabled: false },
  { date: '2026-03-15', time: '15:00-16:00', disabled: true },
  { date: '2026-03-16', time: '09:00-10:00', disabled: false },
  { date: '2026-03-16', time: '10:00-11:00', disabled: false },
  { date: '2026-03-17', time: '14:00-15:00', disabled: false },
  { date: '2026-03-17', time: '15:00-16:00', disabled: false }
])

// 我的预约
const myAppointments = ref([
  {
    id: 1,
    teacher: '王老师',
    date: '2026-03-10',
    time: '14:00-15:00',
    status: 'completed',
    statusText: '已完成'
  },
  {
    id: 2,
    teacher: '李老师',
    date: '2026-03-15',
    time: '09:00-10:00',
    status: 'pending',
    statusText: '待确认'
  }
])

const selectedTeacher = ref<any>(null)
const selectedTime = ref<any>(null)

// 选择老师
const selectTeacher = (teacher: any) => {
  selectedTeacher.value = teacher
  selectedTime.value = null
}

// 选择时间
const selectTime = (slot: any) => {
  if (!slot.disabled) {
    selectedTime.value = slot
  }
}

// 取消选择
const cancelSelection = () => {
  selectedTeacher.value = null
  selectedTime.value = null
}

// 确认预约
const confirmAppointment = () => {
  if (selectedTeacher.value && selectedTime.value) {
    myAppointments.value.push({
      id: myAppointments.value.length + 1,
      teacher: selectedTeacher.value.name,
      date: selectedTime.value.date,
      time: selectedTime.value.time,
      status: 'pending',
      statusText: '待确认'
    })
    ElMessage.success('预约成功，等待老师确认')
    cancelSelection()
  }
}

// 取消预约
const cancelAppointment = (index: number) => {
  myAppointments.value.splice(index, 1)
  ElMessage.success('预约已取消')
}
</script>

<style scoped lang="scss">
.appointment-view {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .appointment-card {
    margin-bottom: 20px;
    border-radius: 12px;
    
    .appointment-header {
      margin-bottom: 20px;
      
      h3 {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
      
      .appointment-desc {
        margin: 0;
        color: #666;
        font-size: 14px;
      }
    }
    
    .teachers-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 20px;
      
      .teacher-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }
        
        .teacher-info {
          display: flex;
          align-items: center;
          gap: 16px;
          
          .teacher-details {
            h4 {
              margin: 0 0 4px;
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
            
            .teacher-title {
              margin: 0 0 4px;
              font-size: 14px;
              color: #1890ff;
            }
            
            .teacher-expertise {
              margin: 0;
              font-size: 12px;
              color: #666;
            }
          }
        }
      }
    }
    
    .time-selection {
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
      
      h3 {
        margin: 0 0 16px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
      
      .time-slots {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
        gap: 12px;
        margin-bottom: 20px;
        
        .time-slot {
          padding: 12px;
          border: 1px solid #e8e8e8;
          border-radius: 8px;
          text-align: center;
          cursor: pointer;
          transition: all 0.3s ease;
          
          &:hover:not(.disabled) {
            border-color: #1890ff;
            color: #1890ff;
          }
          
          &.selected {
            border-color: #1890ff;
            background-color: #e6f7ff;
            color: #1890ff;
          }
          
          &.disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
          
          .slot-date {
            display: block;
            font-size: 14px;
            margin-bottom: 4px;
          }
          
          .slot-time {
            display: block;
            font-size: 12px;
          }
        }
      }
      
      .appointment-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
    }
  }
  
  .my-appointments {
    border-radius: 12px;
    
    h3 {
      margin: 0 0 16px;
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
    
    .appointments-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .appointment-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        
        .appointment-info {
          h4 {
            margin: 0 0 4px;
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }
          
          .appointment-time {
            margin: 0 0 4px;
            font-size: 14px;
            color: #666;
          }
          
          .appointment-status {
            margin: 0;
            font-size: 12px;
            font-weight: 500;
            
            &.pending {
              color: #e6a23c;
            }
            
            &.completed {
              color: #67c23a;
            }
            
            &.cancelled {
              color: #f56c6c;
            }
          }
        }
      }
    }
  }
}
</style>