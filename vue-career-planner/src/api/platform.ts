import axios from '../utils/axios'

// 租户管理
export const tenantApi = {
  // 获取租户列表
  getTenants: (params: { page: number; size: number; keyword?: string }) => {
    return axios.get('/platform/tenants', { params })
  },
  
  // 创建租户
  createTenant: (data: any) => {
    return axios.post('/platform/tenants', data)
  },
  
  // 更新租户
  updateTenant: (id: number, data: any) => {
    return axios.put(`/platform/tenants/${id}`, data)
  },
  
  // 删除租户
  deleteTenant: (id: number) => {
    return axios.delete(`/platform/tenants/${id}`)
  }
}

// 用户/角色管理
export const userApi = {
  // 获取用户列表
  getUsers: (params: { page: number; size: number; keyword?: string; role?: string }) => {
    return axios.get('/platform/users', { params })
  },
  
  // 创建用户
  createUser: (data: any) => {
    return axios.post('/platform/users', data)
  },
  
  // 更新用户
  updateUser: (id: number, data: any) => {
    return axios.put(`/platform/users/${id}`, data)
  },
  
  // 删除用户
  deleteUser: (id: number) => {
    return axios.delete(`/platform/users/${id}`)
  }
}

// 导入批次管理
export const batchApi = {
  // 获取批次列表
  getBatches: (params: { page: number; size: number; status?: string }) => {
    return axios.get('/platform/batches', { params })
  },
  
  // 创建批次
  createBatch: (data: any) => {
    return axios.post('/platform/batches', data)
  },
  
  // 获取批次详情
  getBatchDetail: (id: number) => {
    return axios.get(`/platform/batches/${id}`)
  }
}

// Batch Task 监控
export const taskApi = {
  // 获取任务列表
  getTasks: (params: { page: number; size: number; status?: string; batchId?: number }) => {
    return axios.get('/platform/tasks', { params })
  },
  
  // 获取任务详情
  getTaskDetail: (id: number) => {
    return axios.get(`/platform/tasks/${id}`)
  },
  
  // 取消任务
  cancelTask: (id: number) => {
    return axios.post(`/platform/tasks/${id}/cancel`)
  }
}

// 职业图谱管理
export const careerGraphApi = {
  // 获取职业节点列表
  getNodes: (params: { page: number; size: number; keyword?: string }) => {
    return axios.get('/platform/career/nodes', { params })
  },
  
  // 创建职业节点
  createNode: (data: any) => {
    return axios.post('/platform/career/nodes', data)
  },
  
  // 更新职业节点
  updateNode: (id: number, data: any) => {
    return axios.put(`/platform/career/nodes/${id}`, data)
  },
  
  // 删除职业节点
  deleteNode: (id: number) => {
    return axios.delete(`/platform/career/nodes/${id}`)
  },
  
  // 获取职业边列表
  getEdges: (params: { page: number; size: number; sourceId?: number; targetId?: number }) => {
    return axios.get('/platform/career/edges', { params })
  },
  
  // 创建职业边
  createEdge: (data: any) => {
    return axios.post('/platform/career/edges', data)
  },
  
  // 删除职业边
  deleteEdge: (id: number) => {
    return axios.delete(`/platform/career/edges/${id}`)
  }
}