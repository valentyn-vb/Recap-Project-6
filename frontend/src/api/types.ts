export interface Credentials {
  email: string
  password: string
}

export interface RegisterResponse {
  id: number
  email: string
  token: string
}

export interface LoginResponse {
  token: string
}

export interface ProfilePayload {
  name: string
  cohort: string
  focusAreas: string[]
}

export interface UserProfile extends ProfilePayload {
  id: number
}

export type GoalStatus = 'PLANNED' | 'IN_PROGRESS' | 'DONE'

export interface GoalPayload {
  title: string
  description: string
  status: GoalStatus
}

export interface Goal {
  id: number
  title: string
  description: string | null
  status: GoalStatus
  createdAt: string
  updatedAt: string
}

export interface SessionPayload {
  date: string
  durationMinutes: number
  notes: string
  tags: string[]
}

export interface LearningSession {
  id: number
  goalId: number
  date: string
  durationMinutes: number
  notes: string | null
  tags: string[]
}
