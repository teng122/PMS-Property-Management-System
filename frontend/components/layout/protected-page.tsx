"use client";

import { ReactNode } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/layout/app-shell";
import { LoadingState } from "@/components/ui/states";
import { hasAnyRole } from "@/lib/auth/permissions";
import { useAuthStore } from "@/stores/auth-store";
import type { Role } from "@/types/api";

export function ProtectedPage({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const isHydrated = useAuthStore((state) => state.isHydrated);

  if (!isHydrated) return <LoadingState label="Đang kiểm tra phiên đăng nhập..." />;

  if (!user) {
    router.replace("/login");
    return <LoadingState label="Đang chuyển về trang đăng nhập..." />;
  }

  if (!hasAnyRole(user.role, roles)) {
    router.replace("/dashboard");
    return <LoadingState label="Bạn không có quyền truy cập trang này." />;
  }

  return <AppShell>{children}</AppShell>;
}
