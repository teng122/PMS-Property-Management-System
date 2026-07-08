"use client";

import { create } from "zustand";
import type { SessionUser } from "@/types/api";

interface AuthState {
  user: SessionUser | null;
  isHydrated: boolean;
  setUser: (user: SessionUser | null) => void;
  markHydrated: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isHydrated: false,
  setUser: (user) => set({ user }),
  markHydrated: () => set({ isHydrated: true })
}));
