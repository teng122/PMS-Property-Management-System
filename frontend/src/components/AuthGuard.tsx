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
  const [ready, setReady] = useState(false);

  useEffect(() => {
    // Chờ hydrate zustand-persist ở client trước khi quyết định.
    if (!user) {
      router.replace("/login");
      return;
    }
    if (user.role !== role) {
      router.replace(homeForRole(user.role));
      return;
    }
    setReady(true);
  }, [user, role, router]);

  if (!ready) return <LoadingBlock label="Đang kiểm tra phiên đăng nhập..." />;
  return <>{children}</>;
}
