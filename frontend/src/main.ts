import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import 'primeicons/primeicons.css'
import { createApp } from 'vue'
import { createWebHistory } from 'vue-router'
import './style.css'
import { setUnauthorizedHandler } from './api/http'
import App from './App.vue'
import { createAppRouter } from './router'
import { useAuthStore } from './stores/auth'

const pinia = createPinia()
const router = createAppRouter(createWebHistory())

// 401 from the API → drop the session and go to the login page
setUnauthorizedHandler(() => {
  useAuthStore(pinia).logout()
  void router.push('/login')
})

createApp(App)
  .use(pinia)
  .use(router)
  .use(PrimeVue, { theme: { preset: Aura } })
  .mount('#app')
