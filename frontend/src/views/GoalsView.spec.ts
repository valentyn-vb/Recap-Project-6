import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as goalsApi from '../api/goalsApi'
import { createAppRouter } from '../router'
import type { Goal } from '../api/types'
import GoalsView from './GoalsView.vue'

vi.mock('../api/goalsApi', () => ({
  listGoals: vi.fn(),
  getGoal: vi.fn(),
  createGoal: vi.fn(),
  updateGoal: vi.fn(),
  deleteGoal: vi.fn(),
}))

const mockedApi = vi.mocked(goalsApi)

function goal(overrides: Partial<Goal> = {}): Goal {
  return {
    id: 1,
    title: 'Learn Vue',
    description: 'Router + Pinia',
    status: 'PLANNED',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

async function mountView() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createAppRouter(createMemoryHistory())
  await router.push('/goals')
  await router.isReady()
  return mount(GoalsView, {
    global: { plugins: [pinia, router, PrimeVue], stubs: { teleport: true } },
  })
}

describe('GoalsView', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('shows a loading state while goals are being fetched', async () => {
    mockedApi.listGoals.mockReturnValue(new Promise(() => {}))

    const wrapper = await mountView()

    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('renders one row per goal with its status', async () => {
    mockedApi.listGoals.mockResolvedValue([
      goal({ id: 1, title: 'Learn Vue', status: 'IN_PROGRESS' }),
      goal({ id: 2, title: 'Learn Spring', status: 'DONE' }),
    ])

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="goal-1"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="goal-2"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Learn Vue')
    expect(wrapper.text()).toContain('In progress')
    expect(wrapper.text()).toContain('Done')
  })

  it('shows an empty state when the user has no goals', async () => {
    mockedApi.listGoals.mockResolvedValue([])

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="empty"]').exists()).toBe(true)
  })

  it('shows an error state when loading fails', async () => {
    mockedApi.listGoals.mockRejectedValue(new Error('boom'))

    const wrapper = await mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="error"]').exists()).toBe(true)
  })

  it('creates a goal and prepends it to the list', async () => {
    mockedApi.listGoals.mockResolvedValue([])
    mockedApi.createGoal.mockResolvedValue(goal({ id: 7, title: 'Learn Vue' }))

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="new-goal"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="goal-title"]').setValue('Learn Vue')
    await wrapper.get('[data-testid="goal-save"]').trigger('click')
    await flushPromises()

    expect(mockedApi.createGoal).toHaveBeenCalledWith({
      title: 'Learn Vue',
      description: '',
      status: 'PLANNED',
    })
    expect(wrapper.find('[data-testid="goal-7"]').exists()).toBe(true)
  })

  it('edits an existing goal in place', async () => {
    mockedApi.listGoals.mockResolvedValue([goal({ id: 1, title: 'Old title' })])
    mockedApi.updateGoal.mockResolvedValue(goal({ id: 1, title: 'New title', status: 'DONE' }))

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="edit-1"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="goal-title"]').setValue('New title')
    await wrapper.get('[data-testid="goal-save"]').trigger('click')
    await flushPromises()

    expect(mockedApi.updateGoal).toHaveBeenCalledWith(1, {
      title: 'New title',
      description: 'Router + Pinia',
      status: 'PLANNED',
    })
    expect(wrapper.text()).toContain('New title')
    expect(wrapper.text()).not.toContain('Old title')
  })

  it('deletes a goal after confirmation', async () => {
    mockedApi.listGoals.mockResolvedValue([goal({ id: 1, title: 'Learn Vue' })])
    mockedApi.deleteGoal.mockResolvedValue()

    const wrapper = await mountView()
    await flushPromises()

    await wrapper.get('[data-testid="delete-1"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="confirm-delete"]').trigger('click')
    await flushPromises()

    expect(mockedApi.deleteGoal).toHaveBeenCalledWith(1)
    expect(wrapper.find('[data-testid="goal-1"]').exists()).toBe(false)
  })
})
