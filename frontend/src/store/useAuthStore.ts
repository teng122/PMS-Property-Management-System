import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { AuthResponse, AuthUser, Role } from "@/types";

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  setAuth: (data: AuthResponse) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  hasRole: (role: Role) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      user: null,
      setAuth: (data) =>
        set({
          token: data.token,
          refreshToken: data.refreshToken,
          user: { username: data.username, role: data.role },
        }),
      setTokens: (accessToken, refreshToken) =>
        set({
          token: accessToken,
          refreshToken: refreshToken,
        }),
      logout: () => set({ token: null, refreshToken: null, user: null }),
      hasRole: (role) => get().user?.role === role,
    }),
    { name: "hotel-auth" }
  )
);
