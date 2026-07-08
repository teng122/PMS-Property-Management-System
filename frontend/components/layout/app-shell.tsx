"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { authApi } from "@/lib/api/services";
import { roleLabel } from "@/lib/auth/permissions";
import { navItems } from "@/lib/navigation";
import { useAuthStore } from "@/stores/auth-store";

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);

  const visibleItems = navItems.filter((item) => (user ? item.roles.includes(user.role) : false));

  async function handleLogout() {
    await authApi.logout();
    queryClient.clear();
    setUser(null);
    router.replace("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Link href="/dashboard" className="brand">
          <span className="brand-mark">H</span>
          <span>
            Smart Hotel
            <br />
            <span className="muted" style={{ color: "#94a3b8", fontSize: 13 }}>
              PMS Dashboard
            </span>
          </span>
        </Link>

        <nav className="nav-list" aria-label="Điều hướng chính">
          {visibleItems.map((item) => (
            <Link
              className={`nav-link ${pathname === item.href || pathname.startsWith(`${item.href}/`) ? "active" : ""}`}
              href={item.href}
              key={item.href}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div style={{ marginTop: "auto" }}>
          <div className="card" style={{ background: "rgba(255,255,255,0.08)", borderColor: "rgba(255,255,255,0.1)", boxShadow: "none" }}>
            <div style={{ fontWeight: 800 }}>{user?.username || "Guest"}</div>
            <div className="muted" style={{ color: "#cbd5e1", marginTop: 4 }}>
              {roleLabel(user?.role)}
            </div>
          </div>
          <Button variant="ghost" onClick={handleLogout} style={{ color: "white", borderColor: "rgba(255,255,255,0.18)", width: "100%", marginTop: 12 }}>
            Đăng xuất
          </Button>
        </div>
      </aside>
      <main className="main">{children}</main>
    </div>
  );
}
