import { defineStore } from 'pinia'

export const useJobStore = defineStore('job', {
  state: () => ({
    // 当前选中的岗位ID
    selectedJobId: '',
    // 岗位筛选条件
    jobFilters: {
      status: '', // 生效/暂停
      keyword: '' // 岗位名称模糊搜索
    },
    // 候选人筛选条件
    candidateFilters: {
      jobId: '',
      matchScoreMin: 0,
      matchScoreMax: 100,
     投递时间范围: {
        start: '',
        end: ''
      }
    }
  }),
  actions: {
    // 设置选中的岗位ID
    setSelectedJobId(jobId: string) {
      this.selectedJobId = jobId
      this.candidateFilters.jobId = jobId
    },
    // 更新岗位筛选条件
    updateJobFilters(filters: Partial<typeof this.jobFilters>) {
      this.jobFilters = { ...this.jobFilters, ...filters }
    },
    // 更新候选人筛选条件
    updateCandidateFilters(filters: Partial<typeof this.candidateFilters>) {
      this.candidateFilters = { ...this.candidateFilters, ...filters }
    },
    // 重置所有筛选条件
    resetFilters() {
      this.selectedJobId = ''
      this.jobFilters = {
        status: '',
        keyword: ''
      }
      this.candidateFilters = {
        jobId: '',
        matchScoreMin: 0,
        matchScoreMax: 100,
        投递时间范围: {
          start: '',
          end: ''
        }
      }
    }
  }
})