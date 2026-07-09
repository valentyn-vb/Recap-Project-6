<script setup lang="ts">
import { isAxiosError } from 'axios'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Password from 'primevue/password'
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ProfileSaveError, useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const name = ref('')
const cohort = ref('')
const focusAreasInput = ref('')

const errorMessage = ref<string | null>(null)
const submitting = ref(false)

function parseFocusAreas(input: string): string[] {
  return input
    .split(',')
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0)
}

async function submit(): Promise<void> {
  errorMessage.value = null
  if (email.value.trim() === '' || password.value === '') {
    errorMessage.value = 'Email and password are required'
    return
  }

  submitting.value = true
  try {
    await auth.register(email.value.trim(), password.value, {
      name: name.value.trim(),
      cohort: cohort.value.trim(),
      focusAreas: parseFocusAreas(focusAreasInput.value),
    })
    await router.push('/')
  } catch (error: unknown) {
    if (error instanceof ProfileSaveError) {
      // account exists and the user is logged in — profile can be added later
      await router.push('/')
    } else if (isAxiosError(error) && error.response?.status === 409) {
      errorMessage.value = 'This email is already in use'
    } else {
      errorMessage.value = 'Sign-up failed — please try again'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="sign-up">
    <h1>Sign up</h1>
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
        autocomplete="new-password"
      />

      <label for="name">Name</label>
      <InputText id="name" v-model="name" data-testid="name" autocomplete="name" />

      <label for="cohort">Cohort</label>
      <InputText id="cohort" v-model="cohort" data-testid="cohort" />

      <label for="focus-areas">Focus areas (comma-separated)</label>
      <InputText
        id="focus-areas"
        v-model="focusAreasInput"
        data-testid="focus-areas"
        placeholder="e.g. vue, spring, sql"
      />

      <Button type="submit" label="Create account" :loading="submitting" />
    </form>
    <p class="switch-hint">
      Already have an account?
      <RouterLink to="/login">Log in</RouterLink>
    </p>
  </main>
</template>

<style scoped>
.sign-up form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 380px;
}

.sign-up label {
  margin-top: 8px;
  font-size: 14px;
}

.switch-hint {
  margin-top: 16px;
}
</style>
