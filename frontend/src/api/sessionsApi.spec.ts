import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createSession, deleteSession, listSessions, updateSession } from './sessionsApi'
import { http } from './http'
import type { LearningSession } from './types'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockedHttp = vi.mocked(http)

const session: LearningSession = {
  id: 10,
  goalId: 1,
  date: '2026-01-15',
  durationMinutes: 60,
  notes: 'notes',
  tags: ['vue'],
}

describe('sessionsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listSessions fetches sessions nested under a goal', async () => {
    mockedHttp.get.mockResolvedValue({ data: [session] })

    const result = await listSessions(1)

    expect(mockedHttp.get).toHaveBeenCalledWith('/goals/1/sessions')
    expect(result).toEqual([session])
  })

  it('createSession posts the payload under the goal', async () => {
    mockedHttp.post.mockResolvedValue({ data: session })

    const result = await createSession(1, {
      date: '2026-01-15',
      durationMinutes: 60,
      notes: 'notes',
      tags: ['vue'],
    })

    expect(mockedHttp.post).toHaveBeenCalledWith('/goals/1/sessions', {
      date: '2026-01-15',
      durationMinutes: 60,
      notes: 'notes',
      tags: ['vue'],
    })
    expect(result).toEqual(session)
  })

  it('updateSession puts the payload to the nested session id', async () => {
    mockedHttp.put.mockResolvedValue({ data: { ...session, durationMinutes: 90 } })

    const result = await updateSession(1, 10, {
      date: '2026-02-20',
      durationMinutes: 90,
      notes: 'more',
      tags: ['spring'],
    })

    expect(mockedHttp.put).toHaveBeenCalledWith('/goals/1/sessions/10', {
      date: '2026-02-20',
      durationMinutes: 90,
      notes: 'more',
      tags: ['spring'],
    })
    expect(result.durationMinutes).toBe(90)
  })

  it('deleteSession deletes the nested session id', async () => {
    mockedHttp.delete.mockResolvedValue({ data: undefined })

    await deleteSession(1, 10)

    expect(mockedHttp.delete).toHaveBeenCalledWith('/goals/1/sessions/10')
  })
})
