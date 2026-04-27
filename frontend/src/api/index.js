import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const dbApi = axios.create({
  baseURL: 'http://localhost:3000/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

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

api.interceptors.response.use(
  response => response.data,
  error => {
    // 处理 401 错误：Token 过期或无效
    if (error.response && error.response.status === 401) {
      console.error('Token 已过期或无效，自动登出')
      // 清除本地存储的用户数据
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('selfIntroduction')
      localStorage.removeItem('careerAnalysis')
      localStorage.removeItem('careerRecommendations')
      // 跳转到登录页（避免无限循环）
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

dbApi.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

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

export const aiAPI = {
  chat: (message, mode, interview_style, session_id) => api.post('/ai/llm/chat/', { message, mode, interview_style, session_id }),
  chatStream: (message, mode, interview_style, session_id) => {
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
  careerRecommendation: (self_introduction) => api.post('/ai/llm/career/recommendation/', { self_introduction }, { timeout: 120000 }),
  careerPlanGeneration: (self_introduction, selected_careers) => api.post('/ai/llm/career/plan/', { self_introduction, selected_careers }, { timeout: 120000 }),
  careerReport: (user_profile, target_job) => api.post('/career-report/', { user_profile, target_job }, { timeout: 120000 }),
  saveChatRecord: (session_id, user_message, assistant_message, mode) => api.post('/ai/llm/save-chat/', { session_id, user_message, assistant_message, mode }),
  saveCareerEvaluation: (session_id, selected_careers, evaluation_result) => api.post('/ai/records/save-evaluation/', { session_id, selected_careers, evaluation_result }),
  saveCareerPlan: (session_id, report_content, radar_data) => api.post('/ai/records/save-plan/', { session_id, report_content, radar_data }),
  saveCareerRecommendation: (session_id, self_introduction, recommendations) => api.post('/ai/records/save-recommendation/', { session_id, self_introduction, recommendations })
}

export const companyAPI = {
  search: (keyword) => dbApi.get('/search', { params: { q: keyword } }),
  getByCity: (city) => dbApi.get(`/city/${city}`),
  getByJob: (job) => dbApi.get(`/job/${job}`),
  aiSearch: (params) => dbApi.post('/ai-search', params),
  getStats: () => dbApi.get('/companies/stats')
}

export default api

export const jobFairAPI = {
  getJobFairs: (page = 1, pageSize = 8) => api.get('/company-reviews/job-fairs/', {
    params: { page, page_size: pageSize }
  }),
  getFilterOptions: () => api.get('/company-reviews/job-fairs/filter_options/')
}

export const companyInfoAPI = {
  getCompanyInfos: (page = 1, pageSize = 10) => api.get('/company-reviews/company-infos/', {
    params: { page, page_size: pageSize }
  })
}

export const computerCareerAPI = {
  getComputerCareers: (page = 1, pageSize = 10) => api.get('/career-evaluation/computer-careers/', {
    params: { page, page_size: pageSize }
  })
}
