import { apiClient } from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types";

const BASE = "/identity-service/api/auth";

export const authApi = {
  login: (data: LoginRequest) => apiClient.post<AuthResponse>(`${BASE}/login`, data),
  register: (data: RegisterRequest) => apiClient.post(`${BASE}/register`, data),
};
