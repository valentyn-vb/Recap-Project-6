<script setup lang="ts">
import { isAxiosError } from 'axios'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import Tag from 'primevue/tag'
import Textarea from 'primevue/textarea'
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getGoal } from '../api/goalsApi'
import { createSession, deleteSession, listSessions, updateSession } from '../api/sessionsApi'
import type { Goal, LearningSession } from '../api/types'

type LoadStatus = 'loading' | 'loaded' | 'notfound' | 'error'

const route = useRoute()
const goalId = Number(route.params.id)

const status = ref<LoadStatus>('loading')
const goal = ref<Goal | null>(null)
const sessions = ref<LearningSession[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const dateInput = ref('')
const durationInput = ref('')
const notesInput = ref('')
const tagsInput = ref('')
const saving = ref(false)
const formError = ref<string | null>(null)

const deleteTarget = ref<LearningSession | null>(null)
const deleting = ref(false)

const dialogHeading = computed(() => (editingId.value === null ? 'Log a session' : 'Edit session'))
const durationValue = computed(() => Number(durationInput.value))
const canSave = computed(
  () => dateInput.value.trim().length > 0 && Number.isFinite(durationValue.value) && durationValue.value > 0,
)

function parseTags(input: string): string[] {
  return input
    .split(',')
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0)
}

async function load(): Promise<void> {
  status.value = 'loading'
  try {
    goal.value = await getGoal(goalId)
    sessions.value = await listSessions(goalId)
    status.value = 'loaded'
  } catch (error: unknown) {
    if (isAxiosError(error) && error.response?.status === 404) {
      status.value = 'notfound'
    } else {
      status.value = 'error'
    }
  }
}

onMounted(load)

function openCreate(): void {
  editingId.value = null
  dateInput.value = ''
  durationInput.value = ''
  notesInput.value = ''
  tagsInput.value = ''
  formError.value = null
  dialogVisible.value = true
}

function openEdit(session: LearningSession): void {
  editingId.value = session.id
  dateInput.value = session.date
  durationInput.value = String(session.durationMinutes)
  notesInput.value = session.notes ?? ''
  tagsInput.value = session.tags.join(', ')
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
    date: dateInput.value,
    durationMinutes: durationValue.value,
    notes: notesInput.value,
    tags: parseTags(tagsInput.value),
  }
  try {
    if (editingId.value === null) {
      const created = await createSession(goalId, payload)
      sessions.value = [created, ...sessions.value]
    } else {
      const updated = await updateSession(goalId, editingId.value, payload)
      sessions.value = sessions.value.map((session) =>
        session.id === updated.id ? updated : session,
      )
    }
    dialogVisible.value = false
  } catch {
    formError.value = 'Could not save the session — please try again.'
  } finally {
    saving.value = false
  }
}

function confirmDelete(session: LearningSession): void {
  deleteTarget.value = session
}

async function performDelete(): Promise<void> {
  const target = deleteTarget.value
  if (target === null) {
    return
  }
  deleting.value = true
  try {
    await deleteSession(goalId, target.id)
    sessions.value = sessions.value.filter((session) => session.id !== target.id)
    deleteTarget.value = null
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <main class="goal-detail">
    <RouterLink to="/goals" class="back-link" data-testid="back-link">← Back to goals</RouterLink>

    <ProgressSpinner v-if="status === 'loading'" data-testid="loading" />

    <div v-else-if="status === 'notfound'" data-testid="notfound">
      <p>That goal doesn’t exist (or isn’t yours).</p>
    </div>

    <Message v-else-if="status === 'error'" severity="error" data-testid="error">
      Could not load this goal — please try again later.
    </Message>

    <template v-else-if="status === 'loaded' && goal">
      <header class="goal-head">
        <h1>{{ goal.title }}</h1>
        <Tag :value="goal.status" />
      </header>
      <p v-if="goal.description" class="goal-description">{{ goal.description }}</p>

      <section class="sessions">
        <div class="sessions-header">
          <h2>Learning sessions</h2>
          <Button data-testid="new-session" label="Log a session" icon="pi pi-plus" @click="openCreate" />
        </div>

        <p v-if="sessions.length === 0" data-testid="sessions-empty">
          No sessions logged yet.
        </p>

        <ul v-else class="session-list" data-testid="session-list">
          <li
            v-for="session in sessions"
            :key="session.id"
            class="session-row"
            :data-testid="`session-${session.id}`"
          >
            <div class="session-main">
              <span class="session-date">{{ session.date }}</span>
              <span class="session-duration">{{ session.durationMinutes }} min</span>
              <span v-if="session.notes" class="session-notes">{{ session.notes }}</span>
              <span class="session-tags">
                <Tag v-for="tag in session.tags" :key="tag" :value="tag" severity="secondary" />
              </span>
            </div>
            <div class="session-actions">
              <Button
                :data-testid="`edit-session-${session.id}`"
                label="Edit"
                severity="secondary"
                size="small"
                @click="openEdit(session)"
              />
              <Button
                :data-testid="`delete-session-${session.id}`"
                label="Delete"
                severity="danger"
                size="small"
                @click="confirmDelete(session)"
              />
            </div>
          </li>
        </ul>
      </section>
    </template>

    <Dialog v-model:visible="dialogVisible" :header="dialogHeading" modal :style="{ width: '28rem' }">
      <div class="form-field">
        <label for="session-date">Date</label>
        <InputText id="session-date" data-testid="session-date" v-model="dateInput" type="date" />
      </div>
      <div class="form-field">
        <label for="session-duration">Duration (minutes)</label>
        <InputText
          id="session-duration"
          data-testid="session-duration"
          v-model="durationInput"
          type="number"
          min="1"
        />
      </div>
      <div class="form-field">
        <label for="session-notes">Notes</label>
        <Textarea id="session-notes" data-testid="session-notes" v-model="notesInput" rows="3" />
      </div>
      <div class="form-field">
        <label for="session-tags">Tags (comma-separated)</label>
        <InputText
          id="session-tags"
          data-testid="session-tags"
          v-model="tagsInput"
          placeholder="e.g. vue, router"
        />
      </div>

      <Message v-if="formError" severity="error" data-testid="form-error">{{ formError }}</Message>

      <template #footer>
        <Button label="Cancel" severity="secondary" text @click="dialogVisible = false" />
        <Button
          data-testid="session-save"
          label="Save"
          :disabled="!canSave || saving"
          :loading="saving"
          @click="submit"
        />
      </template>
    </Dialog>

    <Dialog
      :visible="deleteTarget !== null"
      header="Delete session"
      modal
      :closable="false"
      :style="{ width: '24rem' }"
    >
      <p v-if="deleteTarget">Delete the session logged on {{ deleteTarget.date }}?</p>
      <template #footer>
        <Button label="Cancel" severity="secondary" text @click="deleteTarget = null" />
        <Button
          data-testid="confirm-delete-session"
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
.back-link {
  display: inline-block;
  margin-bottom: 16px;
  text-decoration: none;
  color: inherit;
}

.goal-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goal-description {
  color: var(--p-text-muted-color, #64748b);
}

.sessions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.session-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  border: 1px solid var(--p-content-border-color, #e2e8f0);
  border-radius: 8px;
}

.session-main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.session-date {
  font-weight: 600;
}

.session-actions {
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
