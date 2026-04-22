import axios from 'axios'

// 原来的API配置（你的Django后端）
const api = axios.create({
  baseURL: '/api',
  timeout: 120000, // 进一步增加超时时间到120秒，以处理LLM生成的长时间运行
  headers: {
    'Content-Type': 'application/json'
  }
})

// 新增：数据库查询API配置（我们刚建的Node服务）
const dbApi = axios.create({
  baseURL: 'http://localhost:3000/api',  // Node后端地址
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器（只对原API生效）
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Token ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

dbApi.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

// ============ 原有API接口 ============
export const authAPI = {
  login: (data) => api.post('/users/auth/login/', data),
  register: (data) => api.post('/users/auth/register/', data),
  logout: () => api.post('/users/auth/logout/')
}

export const userAPI = {
  getProfile: () => api.get('/users/profile/'),
  updateProfile: (data) => api.put('/users/profile/', data),
  uploadAvatar: (formData) => api.post('/users/profile/', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// AI聊天相关
export const aiAPI = {
  chat: (message, mode, interview_style, session_id) => api.post('/ai/llm/chat/', { message, mode, interview_style, session_id }),
  chatStream: (message, mode, interview_style, session_id) => {
    // 保持原来的流式响应
    return fetch('/api/ai/llm/chat/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(localStorage.getItem('token') ? { 'Authorization': `Token ${localStorage.getItem('token')}` } : {})
      },
      body: JSON.stringify({ message, mode, interview_style, session_id })
    })
  },
  getHistory: (sessionId) => api.get(`/ai/llm/chat/history/?session_id=${sessionId}`),
  careerRecommendation: (self_introduction) => api.post('/ai/llm/career/recommendation/', { self_introduction }, {
    timeout: 120000 // 为职业推荐单独设置超时时间，确保与全局配置一致
  }),
  careerPlanGeneration: (self_introduction, selected_careers) => api.post('/ai/llm/career/plan/', { self_introduction, selected_careers }, {
    timeout: 120000 // 为职业规划生成单独设置超时时间
  }),
  saveChatRecord: (session_id, user_message, assistant_message, mode) => api.post('/ai/llm/save-chat/', { session_id, user_message, assistant_message, mode }),
  saveCareerEvaluation: (session_id, selected_careers, evaluation_result) => api.post('/ai/records/save-evaluation/', { session_id, selected_careers, evaluation_result }),
  saveCareerPlan: (session_id, report_content, radar_data) => api.post('/ai/records/save-plan/', { session_id, report_content, radar_data }),
  saveCareerRecommendation: (session_id, self_introduction, recommendations) => api.post('/ai/records/save-recommendation/', { session_id, self_introduction, recommendations })
}

// ============ 新增：数据库查询API ============
export const companyAPI = {
  // 关键词搜索
  search: (keyword) => dbApi.get('/search', { params: { q: keyword } }),
  
  // 按城市查询
  getByCity: (city) => dbApi.get(`/city/${city}`),
  
  // 按职位查询
  getByJob: (job) => dbApi.get(`/job/${job}`),
  
  // AI专用组合搜索（给AI助手用）
  aiSearch: (params) => dbApi.post('/ai-search', params),
  
  // 获取统计数据
  getStats: () => dbApi.get('/companies/stats')
}

// 导出默认的api实例（保持兼容）
export default api

// 双选会相关API
export const jobFairAPI = {
  // 获取双选会列表
  getJobFairs: (page = 1, pageSize = 8) => api.get('/company-reviews/job-fairs/', {
    params: { page, page_size: pageSize }
  }),
  
  // 获取双选会筛选选项
  getFilterOptions: () => api.get('/company-reviews/job-fairs/filter_options/')
}

// 公司职位信息API
export const companyInfoAPI = {
  // 获取公司职位信息列表
  getCompanyInfos: (page = 1, pageSize = 10) => api.get('/company-reviews/company-infos/', {
    params: { page, page_size: pageSize }
  })
}

// 计算机职业API
export const computerCareerAPI = {
  // 获取计算机职业列表
  getComputerCareers: (page = 1, pageSize = 10) => api.get('/career-evaluation/computer-careers/', {
    params: { page, page_size: pageSize }
  })
}