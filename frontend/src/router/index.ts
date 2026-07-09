import { createRouter, type Router, type RouterHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import SignUpView from '../views/SignUpView.vue'

export function createAppRouter(history: RouterHistory): Router {
  const router = createRouter({
    history,
    routes: [
      { path: '/', name: 'home', component: HomeView },
      { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
      { path: '/signup', name: 'signup', component: SignUpView, meta: { guestOnly: true } },
      { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
    ],
  })

  router.beforeEach((to) => {
    const auth = useAuthStore()
    if (to.meta.requiresAuth === true && !auth.isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (to.meta.guestOnly === true && auth.isAuthenticated) {
      return { path: '/' }
    }
    return true
  })

  return router
}
