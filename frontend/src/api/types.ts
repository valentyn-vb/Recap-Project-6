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
