import { http } from './http'
import type { Credentials, LoginResponse, ProfilePayload, RegisterResponse, UserProfile } from './types'

export async function register(credentials: Credentials): Promise<RegisterResponse> {
  const response = await http.post<RegisterResponse>('/auth/register', credentials)
  return response.data
}

export async function login(credentials: Credentials): Promise<LoginResponse> {
  const response = await http.post<LoginResponse>('/auth/login', credentials)
  return response.data
}

export async function getProfile(): Promise<UserProfile> {
  const response = await http.get<UserProfile>('/profile')
  return response.data
}

export async function saveProfile(payload: ProfilePayload): Promise<UserProfile> {
  const response = await http.put<UserProfile>('/profile', payload)
  return response.data
}
