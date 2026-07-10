import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as goalsApi from '../api/goalsApi'
import * as sessionsApi from '../api/sessionsApi'
import { createAppRouter } from '../router'
import { saveAuth } from '../lib/tokenStorage'
import type { Goal, LearningSession } from '../api/types'
import GoalDetailView from './GoalDetailView.vue'

vi.mock('../api/goalsApi', () => ({
  listGoals: vi.fn(),
  getGoal: vi.fn(),
  createGoal: vi.fn(),
  updateGoal: vi.fn(),
  deleteGoal: vi.fn(),
}))

vi.mock('../api/sessionsApi', () => ({
  listSessions: vi.fn(),
  createSession: vi.fn(),
  updateSession: vi.fn(),
  deleteSession: vi.fn(),
}))

const mockedGoals = vi.mocked(goalsApi)
const mockedSessions = vi.mocked(sessionsApi)

function goal(overrides: Partial<Goal> = {}): Goal {
  return {
    id: 1,
    title: 'Learn Vue',
    description: 'Router + Pinia',
    status: 'IN_PROGRESS',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

function session(overrides: Partial<LearningSession> = {}): LearningSession {
  return {
    id: 10,
    goalId: 1,
    date: '2026-01-15',
    durationMinutes: 60,
    notes: 'notes',
    tags: ['vue'],
    ...overrides,
  }
}

function axiosError(status: number): AxiosError {
  return new AxiosError('failed', 'ERR_BAD_REQUEST', undefined, undefined, {
    status,
    statusText: String(status),
    data: {},
    headers: {},
    config: {} as never,
  })
}

async function mountView() {
  // Authenticate so the requiresAuth guard keeps us on /goals/:id (rather than
  // bouncing to /login, which would drop the :id route param).
  saveAuth({ token: 'jwt', email: 'alice@example.com' })
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createAppRouter(createMemoryHistory())
  await router.push('/goals/1')
  await router.isReady()
  return mount(GoalDetailView, {
    global: { plugins: [pinia, router, PrimeVue], stubs: { teleport: true } },
  })
}

describe('GoalDetailView', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('shows a loading state initially', async () => {
    mockedGoals.getGoal.mockReturnValue(new Promise(() => {}))

    const wrapper = await mountView()

    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('shows a not-found state when the goal 404s', async () => {
    mockedGoals.getGoal.mockRejectedValue(axiosError(404))

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="notfound"]').exists()).toBe(true)
  })

  it('shows an error state on other load failures', async () => {
    mockedGoals.getGoal.mockRejectedValue(axiosError(500))

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="error"]').exists()).toBe(true)
  })

  it('renders the goal and its sessions', async () => {
    mockedGoals.getGoal.mockResolvedValue(goal({ title: 'Learn Vue' }))
    mockedSessions.listSessions.mockResolvedValue([session({ id: 10, durationMinutes: 45 })])

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.text()).toContain('Learn Vue')
    expect(wrapper.find('[data-testid="session-10"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('45 min')
  })

  it('shows an empty state when there are no sessions', async () => {
    mockedGoals.getGoal.mockResolvedValue(goal())
    mockedSessions.listSessions.mockResolvedValue([])

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="sessions-empty"]').exists()).toBe(true)
  })

  it('logs a new session and prepends it', async () => {
    mockedGoals.getGoal.mockResolvedValue(goal())
    mockedSessions.listSessions.mockResolvedValue([])
    mockedSessions.createSession.mockResolvedValue(session({ id: 20 }))

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="new-session"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="session-date"]').setValue('2026-01-15')
    await wrapper.get('[data-testid="session-duration"]').setValue('60')
    await wrapper.get('[data-testid="session-tags"]').setValue('vue, router')
    await wrapper.get('[data-testid="session-save"]').trigger('click')
    await flushPromises()

    expect(mockedSessions.createSession).toHaveBeenCalledWith(1, {
      date: '2026-01-15',
      durationMinutes: 60,
      notes: '',
      tags: ['vue', 'router'],
    })
    expect(wrapper.find('[data-testid="session-20"]').exists()).toBe(true)
  })

  it('edits an existing session', async () => {
    mockedGoals.getGoal.mockResolvedValue(goal())
    mockedSessions.listSessions.mockResolvedValue([session({ id: 10, durationMinutes: 60 })])
    mockedSessions.updateSession.mockResolvedValue(session({ id: 10, durationMinutes: 90 }))

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="edit-session-10"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="session-duration"]').setValue('90')
    await wrapper.get('[data-testid="session-save"]').trigger('click')
    await flushPromises()

    expect(mockedSessions.updateSession).toHaveBeenCalledWith(1, 10, {
      date: '2026-01-15',
      durationMinutes: 90,
      notes: 'notes',
      tags: ['vue'],
    })
    expect(wrapper.text()).toContain('90 min')
  })

  it('deletes a session after confirmation', async () => {
    mockedGoals.getGoal.mockResolvedValue(goal())
    mockedSessions.listSessions.mockResolvedValue([session({ id: 10 })])
    mockedSessions.deleteSession.mockResolvedValue()

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="delete-session-10"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="confirm-delete-session"]').trigger('click')
    await flushPromises()

    expect(mockedSessions.deleteSession).toHaveBeenCalledWith(1, 10)
    expect(wrapper.find('[data-testid="session-10"]').exists()).toBe(false)
  })
})
