import { http } from './http'
import type { LearningSession, SessionPayload } from './types'

export async function listSessions(goalId: number): Promise<LearningSession[]> {
  const response = await http.get<LearningSession[]>(`/goals/${goalId}/sessions`)
  return response.data
}

export async function createSession(goalId: number, payload: SessionPayload): Promise<LearningSession> {
  const response = await http.post<LearningSession>(`/goals/${goalId}/sessions`, payload)
  return response.data
}

export async function updateSession(
  goalId: number,
  id: number,
  payload: SessionPayload,
): Promise<LearningSession> {
  const response = await http.put<LearningSession>(`/goals/${goalId}/sessions/${id}`, payload)
  return response.data
}

export async function deleteSession(goalId: number, id: number): Promise<void> {
  await http.delete(`/goals/${goalId}/sessions/${id}`)
}
