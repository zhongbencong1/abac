import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/Overview.vue'), meta: { title: '概览' } },
  { path: '/users', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
  { path: '/attributes', component: () => import('../views/AttributeManage.vue'), meta: { title: '属性管理' } },
  { path: '/policies', component: () => import('../views/PolicyManage.vue'), meta: { title: '策略配置' } },
  { path: '/authz', component: () => import('../views/AuthzTest.vue'), meta: { title: '鉴权校验' } },
  { path: '/audit', component: () => import('../views/AuditLog.vue'), meta: { title: '审计日志' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - ABAC` : 'ABAC'
})

export default router
