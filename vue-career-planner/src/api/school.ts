import axios from '../utils/axios'

// 学生列表管理
export const studentApi = {
  // 获取学生列表
  getStudents: (params: { page: number; size: number; keyword?: string; status?: string }) => {
    return axios.get('/school/students', { params })
  },
  
  // 获取学生详情
  getStudentDetail: (id: number) => {
    return axios.get(`/school/students/${id}`)
  },
  
  // 更新学生信息
  updateStudent: (id: number, data: any) => {
    return axios.put(`/school/students/${id}`, data)
  }
}

// 学生档案管理
export const studentProfileApi = {
  // 获取学生档案
  getProfile: (id: number) => {
    return axios.get(`/school/students/${id}/profile`)
  },
  
  // 更新学生档案
  updateProfile: (id: number, data: any) => {
    return axios.put(`/school/students/${id}/profile`, data)
  }
}

// 辅导笔记管理
export const guidanceNoteApi = {
  // 获取辅导笔记列表
  getNotes: (params: { page: number; size: number; studentId?: number; keyword?: string }) => {
    return axios.get('/school/guidance-notes', { params })
  },
  
  // 创建辅导笔记
  createNote: (data: any) => {
    return axios.post('/school/guidance-notes', data)
  },
  
  // 更新辅导笔记
  updateNote: (id: number, data: any) => {
    return axios.put(`/school/guidance-notes/${id}`, data)
  },
  
  // 删除辅导笔记
  deleteNote: (id: number) => {
    return axios.delete(`/school/guidance-notes/${id}`)
  }
}

// 学生画像管理
export const studentPersonaApi = {
  // 获取学生画像
  getPersona: (id: number) => {
    return axios.get(`/school/students/${id}/persona`)
  },
  
  // 生成学生画像
  generatePersona: (id: number) => {
    return axios.post(`/school/students/${id}/persona/generate`)
  }
}

// 面试反馈管理
export const interviewFeedbackApi = {
  // 获取面试反馈列表
  getFeedbacks: (params: { page: number; size: number; studentId?: number; status?: string }) => {
    return axios.get('/school/interview-feedbacks', { params })
  },
  
  // 创建面试反馈
  createFeedback: (data: any) => {
    return axios.post('/school/interview-feedbacks', data)
  },
  
  // 更新面试反馈
  updateFeedback: (id: number, data: any) => {
    return axios.put(`/school/interview-feedbacks/${id}`, data)
  }
}