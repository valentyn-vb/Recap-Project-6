import { http } from './http'
import type { Goal, GoalPayload } from './types'

export async function listGoals(): Promise<Goal[]> {
  const response = await http.get<Goal[]>('/goals')
  return response.data
}

export async function getGoal(id: number): Promise<Goal> {
  const response = await http.get<Goal>(`/goals/${id}`)
  return response.data
}

export async function createGoal(payload: GoalPayload): Promise<Goal> {
  const response = await http.post<Goal>('/goals', payload)
  return response.data
}

export async function updateGoal(id: number, payload: GoalPayload): Promise<Goal> {
  const response = await http.put<Goal>(`/goals/${id}`, payload)
  return response.data
}

export async function deleteGoal(id: number): Promise<void> {
  await http.delete(`/goals/${id}`)
}
