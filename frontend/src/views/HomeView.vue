<script setup lang="ts">
import { storeToRefs } from 'pinia'
import Button from 'primevue/button'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const { email, isAuthenticated } = storeToRefs(useAuthStore())
</script>

<template>
  <main class="home">
    <h1>Learning Companion</h1>

    <template v-if="isAuthenticated">
      <p>Welcome back, {{ email }}.</p>
      <p>
        <RouterLink to="/profile">
          <Button label="View my profile" icon="pi pi-user" />
        </RouterLink>
      </p>
    </template>

    <template v-else>
      <p>Track your goals, learning sessions and resources — all in one place.</p>
      <div class="cta">
        <RouterLink to="/signup">
          <Button label="Sign up" />
        </RouterLink>
        <RouterLink to="/login">
          <Button label="Log in" severity="secondary" />
        </RouterLink>
      </div>
    </template>
  </main>
</template>

<style scoped>
.cta {
  display: flex;
  gap: 12px;
}
</style>
