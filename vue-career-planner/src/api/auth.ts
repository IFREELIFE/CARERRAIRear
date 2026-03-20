import axios from '../utils/axios'

interface LoginRequest {
  username: string
  password: string
}

interface LoginData {
  accessToken: string
  refreshToken: string
  expiresIn: number
  role: string
}

interface RefreshRequest {
  refreshToken: string
}

interface RefreshData {
  accessToken: string
  refreshToken: string
  expiresIn: number
  role: string
}

interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

interface Result<T> {
  code: number
  msg: string
  data: T
}

export const authApi = {
  login: (data: LoginRequest): Promise<Result<LoginData>> => {
    return axios.post('/auth/login', data)
  },
  
  logout: (refreshToken: string): Promise<Result<null>> => {
    return axios.post('/auth/logout', { refreshToken })
  },
  
  refreshToken: (data: RefreshRequest): Promise<Result<RefreshData>> => {
    return axios.post('/auth/refresh', data)
  },
  
  changePassword: (data: ChangePasswordRequest): Promise<Result<null>> => {
    return axios.post('/auth/change-password', data)
  }
}

export const login = authApi.login
export const logout = authApi.logout
export const refreshToken = authApi.refreshToken
export const changePassword = authApi.changePassword
