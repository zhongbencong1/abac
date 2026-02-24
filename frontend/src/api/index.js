import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

export const attributeApi = {
  list: () => api.get('/attributes'),
  get: (id) => api.get(`/attributes/${id}`),
  create: (data) => api.post('/attributes', data),
  update: (id, data) => api.put(`/attributes/${id}`, data),
  delete: (id) => api.delete(`/attributes/${id}`),
}

export const policyApi = {
  list: () => api.get('/policies'),
  get: (id) => api.get(`/policies/${id}`),
  create: (data) => api.post('/policies', data),
  update: (id, data) => api.put(`/policies/${id}`, data),
  delete: (id) => api.delete(`/policies/${id}`),
}

export const authzApi = {
  check: (data) => api.post('/authz/check', data),
}

export const auditApi = {
  logs: (page = 0, size = 20) => api.get('/audit/logs', { params: { page, size } }),
}

export const userApi = {
  list: () => api.get('/users'),
  get: (id) => api.get(`/users/${id}`),
  getSubjectAttrs: (id) => api.get(`/users/${id}/subject-attrs`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`),
}

export default api
