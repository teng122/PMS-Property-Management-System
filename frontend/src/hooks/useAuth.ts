"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api/auth";
import { useAuthStore } from "@/store/useAuthStore";
import type { LoginRequest, RegisterRequest, Role } from "@/types";

const HOME_BY_ROLE: Record<Role, string> = {
  CUSTOMER: "/customer",
  RECEPTIONIST: "/receptionist",
  HOUSEKEEPER: "/housekeeper",
  ADMIN: "/admin",
};

export function homeForRole(role: Role) {
  return HOME_BY_ROLE[role];
}

export function useLogin() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data).then((r) => r.data),
    onSuccess: (data) => {
      setAuth(data);
      router.push(HOME_BY_ROLE[data.role] ?? "/");
    },
  });
}

export function useRegister() {
  const router = useRouter();
  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data).then((r) => r.data),
    onSuccess: () => router.push("/login?registered=1"),
  });
}
