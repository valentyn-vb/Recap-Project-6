import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Tag from 'primevue/tag'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as authApi from '../api/authApi'
import { createAppRouter } from '../router'
import ProfileView from './ProfileView.vue'

vi.mock('../api/authApi', () => ({
  register: vi.fn(),
  login: vi.fn(),
  getProfile: vi.fn(),
  saveProfile: vi.fn(),
}))

const mockedApi = vi.mocked(authApi)

function axiosError(status: number): AxiosError {
  return new AxiosError('failed', 'ERR_BAD_REQUEST', undefined, undefined, {
    status,
    statusText: String(status),
    data: {},
    headers: {},
    config: {} as never,
  })
}

async function mountProfile() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createAppRouter(createMemoryHistory())
  await router.push('/')
  return mount(ProfileView, { global: { plugins: [pinia, router, PrimeVue] } })
}

describe('ProfileView', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('shows a loading state while the profile is being fetched', async () => {
    mockedApi.getProfile.mockReturnValue(new Promise(() => {}))

    const wrapper = await mountProfile()

    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('renders name, cohort and one tag per focus area', async () => {
    mockedApi.getProfile.mockResolvedValue({
      id: 1,
      name: 'Alice',
      cohort: 'NF-2026',
      focusAreas: ['vue', 'spring'],
    })

    const wrapper = await mountProfile()
    await flushPromises()

    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('NF-2026')
    const tags = wrapper.findAllComponents(Tag)
    expect(tags).toHaveLength(2)
    expect(tags.map((tag) => tag.text())).toEqual(['vue', 'spring'])
  })

  it('shows a friendly empty state on 404 without crashing', async () => {
    mockedApi.getProfile.mockRejectedValue(axiosError(404))

    const wrapper = await mountProfile()
    await flushPromises()

    expect(wrapper.find('[data-testid="empty"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('No profile yet')
  })

  it('shows a generic error on other failures', async () => {
    mockedApi.getProfile.mockRejectedValue(axiosError(500))

    const wrapper = await mountProfile()
    await flushPromises()

    expect(wrapper.find('[data-testid="error"]').exists()).toBe(true)
  })
})
