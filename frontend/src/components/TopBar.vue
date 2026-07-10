<script setup lang="ts">
import { storeToRefs } from 'pinia'
import Button from 'primevue/button'
import Toolbar from 'primevue/toolbar'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const { email, isAuthenticated } = storeToRefs(auth)
const router = useRouter()

function goToLogin(): void {
  void router.push('/login')
}

function logOut(): void {
  auth.logout()
  void router.push('/')
}
</script>

<template>
  <Toolbar class="top-bar">
    <template #start>
      <RouterLink to="/" class="app-title">Learning Companion</RouterLink>
    </template>
    <template #end>
      <div v-if="isAuthenticated" class="user-area">
        <RouterLink to="/goals" data-testid="goals-link" class="nav-link">Goals</RouterLink>
        <RouterLink to="/profile" data-testid="profile-link" class="user-email">
          <i class="pi pi-user" aria-hidden="true" />
          {{ email }}
        </RouterLink>
        <Button
          data-testid="log-out"
          label="Log out"
          severity="secondary"
          size="small"
          @click="logOut"
        />
      </div>
      <Button
        v-else
        data-testid="sign-in"
        label="Sign in"
        size="small"
        @click="goToLogin"
      />
    </template>
  </Toolbar>
</template>

<style scoped>
.top-bar {
  border-radius: 0;
}

.app-title {
  font-weight: 600;
  text-decoration: none;
  color: inherit;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-email {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: inherit;
}

.nav-link {
  text-decoration: none;
  color: inherit;
  font-weight: 500;
}
</style>
