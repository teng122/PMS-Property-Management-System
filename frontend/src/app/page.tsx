"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
import { homeForRole } from "@/hooks/useAuth";
import { LoadingBlock } from "@/components/ui";

export default function RootPage() {
  const router = useRouter();
  const user = useAuthStore((s) => s.user);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    router.replace(user ? homeForRole(user.role) : "/login");
  }, [user, router, hydrated]);

  return <LoadingBlock label="Đang chuyển hướng..." />;
}
