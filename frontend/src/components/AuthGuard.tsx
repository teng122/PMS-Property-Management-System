"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
import { homeForRole } from "@/hooks/useAuth";
import { LoadingBlock } from "@/components/ui";
import type { Role } from "@/types";

/** Bảo vệ route: yêu cầu đăng nhập + đúng role. */
export function AuthGuard({ role, children }: { role: Role; children: React.ReactNode }) {
  const router = useRouter();
  const user = useAuthStore((s) => s.user);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;

    if (!user) {
      router.replace("/login");
      return;
    }
    if (user.role !== role) {
      router.replace(homeForRole(user.role));
      return;
    }
  }, [user, role, router, hydrated]);

  if (!hydrated || !user || user.role !== role) {
    return <LoadingBlock label="Đang kiểm tra phiên đăng nhập..." />;
  }

  return <>{children}</>;
}
