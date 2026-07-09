"use client";

import { ReactNode, useEffect } from "react";
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

  useEffect(() => {
    if (!isHydrated) return;

    if (!user) {
      router.replace("/login");
    } else if (!hasAnyRole(user.role, roles)) {
      router.replace("/dashboard");
    }
  }, [isHydrated, user, roles, router]);

  if (!isHydrated) return <LoadingState label="Đang kiểm tra phiên đăng nhập..." />;

  if (!user) {
    return <LoadingState label="Đang chuyển về trang đăng nhập..." />;
  }

  if (!hasAnyRole(user.role, roles)) {
    return <LoadingState label="Bạn không có quyền truy cập trang này. Đang chuyển hướng..." />;
  }

  return <AppShell>{children}</AppShell>;
}
