<script setup lang="ts">
import { isAxiosError } from 'axios'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Password from 'primevue/password'
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const email = ref('')
const password = ref('')
const errorMessage = ref<string | null>(null)
const submitting = ref(false)

async function submit(): Promise<void> {
  errorMessage.value = null
  if (email.value.trim() === '' || password.value === '') {
    errorMessage.value = 'Email and password are required'
    return
  }

  submitting.value = true
  try {
    await auth.login(email.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.push(redirect)
  } catch (error: unknown) {
    if (isAxiosError(error) && error.response?.status === 401) {
      errorMessage.value = 'Invalid email or password'
    } else {
      errorMessage.value = 'Login failed — please try again'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login">
    <h1>Log in</h1>
    <form novalidate @submit.prevent="submit">
      <Message v-if="errorMessage" severity="error" data-testid="error">{{ errorMessage }}</Message>

      <label for="email">Email</label>
      <InputText id="email" v-model="email" data-testid="email" type="email" autocomplete="email" />

      <label for="password">Password</label>
      <Password
        v-model="password"
        input-id="password"
        data-testid="password"
        :feedback="false"
        toggle-mask
        autocomplete="current-password"
      />

      <Button type="submit" label="Log in" :loading="submitting" />
    </form>
    <p class="switch-hint">
      No account yet?
      <RouterLink to="/signup">Sign up</RouterLink>
    </p>
  </main>
</template>

<style scoped>
.login form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 380px;
}

.login label {
  margin-top: 8px;
  font-size: 14px;
}

.switch-hint {
  margin-top: 16px;
}
</style>
