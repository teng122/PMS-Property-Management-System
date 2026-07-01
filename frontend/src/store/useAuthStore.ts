import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { AuthResponse, AuthUser, Role } from "@/types";

interface AuthState {
  token: string | null;
  user: AuthUser | null;
  setAuth: (data: AuthResponse) => void;
  logout: () => void;
  hasRole: (role: Role) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      setAuth: (data) =>
        set({ token: data.token, user: { username: data.username, role: data.role } }),
      logout: () => set({ token: null, user: null }),
      hasRole: (role) => get().user?.role === role,
    }),
    { name: "hotel-auth" }
  )
);
