<script setup lang="ts">
import { isAxiosError } from 'axios'
import Card from 'primevue/card'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import Tag from 'primevue/tag'
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getProfile } from '../api/authApi'
import type { UserProfile } from '../api/types'

type Status = 'loading' | 'loaded' | 'empty' | 'error'

const status = ref<Status>('loading')
const profile = ref<UserProfile | null>(null)

onMounted(async () => {
  try {
    profile.value = await getProfile()
    status.value = 'loaded'
  } catch (error: unknown) {
    if (isAxiosError(error) && error.response?.status === 404) {
      status.value = 'empty'
    } else {
      status.value = 'error'
    }
  }
})
</script>

<template>
  <main class="profile">
    <h1>My profile</h1>

    <ProgressSpinner v-if="status === 'loading'" data-testid="loading" />

    <Card v-else-if="status === 'loaded' && profile" data-testid="profile-card">
      <template #title>{{ profile.name }}</template>
      <template #subtitle>Cohort: {{ profile.cohort }}</template>
      <template #content>
        <div class="focus-areas">
          <Tag v-for="area in profile.focusAreas" :key="area" :value="area" />
        </div>
      </template>
    </Card>

    <div v-else-if="status === 'empty'" data-testid="empty">
      <p>No profile yet.</p>
      <p>
        Profile details are collected when you
        <RouterLink to="/signup">sign up</RouterLink> — editing will come later.
      </p>
    </div>

    <Message v-else severity="error" data-testid="error">
      Could not load your profile — please try again later.
    </Message>
  </main>
</template>

<style scoped>
.focus-areas {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
