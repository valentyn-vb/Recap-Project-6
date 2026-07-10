import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createGoal, deleteGoal, getGoal, listGoals, updateGoal } from './goalsApi'
import { http } from './http'
import type { Goal } from './types'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockedHttp = vi.mocked(http)

const goal: Goal = {
  id: 1,
  title: 'Learn Vue',
  description: 'Router + Pinia',
  status: 'PLANNED',
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
}

describe('goalsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listGoals fetches the current user goals', async () => {
    mockedHttp.get.mockResolvedValue({ data: [goal] })

    const result = await listGoals()

    expect(mockedHttp.get).toHaveBeenCalledWith('/goals')
    expect(result).toEqual([goal])
  })

  it('getGoal fetches a single goal by id', async () => {
    mockedHttp.get.mockResolvedValue({ data: goal })

    const result = await getGoal(1)

    expect(mockedHttp.get).toHaveBeenCalledWith('/goals/1')
    expect(result).toEqual(goal)
  })

  it('createGoal posts the payload', async () => {
    mockedHttp.post.mockResolvedValue({ data: goal })

    const result = await createGoal({ title: 'Learn Vue', description: 'Router + Pinia', status: 'PLANNED' })

    expect(mockedHttp.post).toHaveBeenCalledWith('/goals', {
      title: 'Learn Vue',
      description: 'Router + Pinia',
      status: 'PLANNED',
    })
    expect(result).toEqual(goal)
  })

  it('updateGoal puts the payload to the goal id', async () => {
    mockedHttp.put.mockResolvedValue({ data: { ...goal, status: 'DONE' } })

    const result = await updateGoal(1, { title: 'Learn Vue', description: 'x', status: 'DONE' })

    expect(mockedHttp.put).toHaveBeenCalledWith('/goals/1', {
      title: 'Learn Vue',
      description: 'x',
      status: 'DONE',
    })
    expect(result.status).toBe('DONE')
  })

  it('deleteGoal deletes the goal id', async () => {
    mockedHttp.delete.mockResolvedValue({ data: undefined })

    await deleteGoal(1)

    expect(mockedHttp.delete).toHaveBeenCalledWith('/goals/1')
  })

  it('propagates errors from the http layer unchanged', async () => {
    const failure = new Error('boom')
    mockedHttp.get.mockRejectedValue(failure)

    await expect(listGoals()).rejects.toBe(failure)
  })
})
