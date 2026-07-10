<script setup lang="ts">
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import Textarea from 'primevue/textarea'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { createGoal, deleteGoal, listGoals, updateGoal } from '../api/goalsApi'
import type { Goal, GoalStatus } from '../api/types'

type LoadStatus = 'loading' | 'loaded' | 'error'

const STATUS_OPTIONS: { label: string; value: GoalStatus }[] = [
  { label: 'Planned', value: 'PLANNED' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Done', value: 'DONE' },
]

const STATUS_SEVERITY: Record<GoalStatus, string> = {
  PLANNED: 'secondary',
  IN_PROGRESS: 'info',
  DONE: 'success',
}

const status = ref<LoadStatus>('loading')
const goals = ref<Goal[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const title = ref('')
const description = ref('')
const goalStatus = ref<GoalStatus>('PLANNED')
const saving = ref(false)
const formError = ref<string | null>(null)

const deleteTarget = ref<Goal | null>(null)
const deleting = ref(false)

const dialogHeading = computed(() => (editingId.value === null ? 'New goal' : 'Edit goal'))
const canSave = computed(() => title.value.trim().length > 0)

async function load(): Promise<void> {
  status.value = 'loading'
  try {
    goals.value = await listGoals()
    status.value = 'loaded'
  } catch {
    status.value = 'error'
  }
}

onMounted(load)

function statusLabel(value: GoalStatus): string {
  return STATUS_OPTIONS.find((option) => option.value === value)?.label ?? value
}

function openCreate(): void {
  editingId.value = null
  title.value = ''
  description.value = ''
  goalStatus.value = 'PLANNED'
  formError.value = null
  dialogVisible.value = true
}

function openEdit(goal: Goal): void {
  editingId.value = goal.id
  title.value = goal.title
  description.value = goal.description ?? ''
  goalStatus.value = goal.status
  formError.value = null
  dialogVisible.value = true
}

async function submit(): Promise<void> {
  if (!canSave.value) {
    return
  }
  saving.value = true
  formError.value = null
  const payload = {
    title: title.value.trim(),
    description: description.value,
    status: goalStatus.value,
  }
  try {
    if (editingId.value === null) {
      const created = await createGoal(payload)
      goals.value = [created, ...goals.value]
    } else {
      const updated = await updateGoal(editingId.value, payload)
      goals.value = goals.value.map((goal) => (goal.id === updated.id ? updated : goal))
    }
    dialogVisible.value = false
  } catch {
    formError.value = 'Could not save the goal — please try again.'
  } finally {
    saving.value = false
  }
}

function confirmDelete(goal: Goal): void {
  deleteTarget.value = goal
}

async function performDelete(): Promise<void> {
  const target = deleteTarget.value
  if (target === null) {
    return
  }
  deleting.value = true
  try {
    await deleteGoal(target.id)
    goals.value = goals.value.filter((goal) => goal.id !== target.id)
    deleteTarget.value = null
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <main class="goals">
    <header class="goals-header">
      <h1>My goals</h1>
      <Button data-testid="new-goal" label="New goal" icon="pi pi-plus" @click="openCreate" />
    </header>

    <ProgressSpinner v-if="status === 'loading'" data-testid="loading" />

    <Message v-else-if="status === 'error'" severity="error" data-testid="error">
      Could not load your goals — please try again later.
    </Message>

    <p v-else-if="goals.length === 0" data-testid="empty">
      No goals yet — create your first one to start tracking your learning.
    </p>

    <ul v-else class="goal-list" data-testid="goal-list">
      <li v-for="goal in goals" :key="goal.id" class="goal-row" :data-testid="`goal-${goal.id}`">
        <div class="goal-main">
          <RouterLink :to="`/goals/${goal.id}`" class="goal-title" :data-testid="`open-${goal.id}`">
            {{ goal.title }}
          </RouterLink>
          <Tag :value="statusLabel(goal.status)" :severity="STATUS_SEVERITY[goal.status]" />
        </div>
        <div class="goal-actions">
          <Button
            :data-testid="`edit-${goal.id}`"
            label="Edit"
            severity="secondary"
            size="small"
            @click="openEdit(goal)"
          />
          <Button
            :data-testid="`delete-${goal.id}`"
            label="Delete"
            severity="danger"
            size="small"
            @click="confirmDelete(goal)"
          />
        </div>
      </li>
    </ul>

    <Dialog v-model:visible="dialogVisible" :header="dialogHeading" modal :style="{ width: '28rem' }">
      <div class="form-field">
        <label for="goal-title">Title</label>
        <InputText id="goal-title" data-testid="goal-title" v-model="title" />
      </div>
      <div class="form-field">
        <label for="goal-description">Description</label>
        <Textarea id="goal-description" data-testid="goal-description" v-model="description" rows="3" />
      </div>
      <div class="form-field">
        <label for="goal-status">Status</label>
        <Select
          id="goal-status"
          data-testid="goal-status"
          v-model="goalStatus"
          :options="STATUS_OPTIONS"
          option-label="label"
          option-value="value"
        />
      </div>

      <Message v-if="formError" severity="error" data-testid="form-error">{{ formError }}</Message>

      <template #footer>
        <Button label="Cancel" severity="secondary" text @click="dialogVisible = false" />
        <Button
          data-testid="goal-save"
          label="Save"
          :disabled="!canSave || saving"
          :loading="saving"
          @click="submit"
        />
      </template>
    </Dialog>

    <Dialog
      :visible="deleteTarget !== null"
      header="Delete goal"
      modal
      :closable="false"
      :style="{ width: '24rem' }"
    >
      <p v-if="deleteTarget">
        Delete “{{ deleteTarget.title }}”? This also removes its learning sessions.
      </p>
      <template #footer>
        <Button label="Cancel" severity="secondary" text @click="deleteTarget = null" />
        <Button
          data-testid="confirm-delete"
          label="Delete"
          severity="danger"
          :loading="deleting"
          @click="performDelete"
        />
      </template>
    </Dialog>
  </main>
</template>

<style scoped>
.goals-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.goal-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border: 1px solid var(--p-content-border-color, #e2e8f0);
  border-radius: 8px;
}

.goal-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goal-title {
  font-weight: 600;
  text-decoration: none;
  color: inherit;
}

.goal-actions {
  display: flex;
  gap: 8px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}
</style>
