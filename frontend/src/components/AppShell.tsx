"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, Hotel } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { cn } from "@/lib/utils";
import { ROLE_LABEL } from "@/types";

export interface NavItem {
  href: string;
  label: string;
  icon?: React.ReactNode;
}

// Ánh xạ accent (text-*-600 do layout truyền vào) → bộ màu đầy đủ cho chrome.
// Dùng class literal để Tailwind JIT nhận diện.
interface AccentTheme {
  text: string;
  activeBg: string;
  activeText: string;
  dot: string;
  gradient: string;
}
const ACCENT_THEME: Record<string, AccentTheme> = {
  "text-blue-600": {
    text: "text-blue-600",
    activeBg: "bg-blue-50",
    activeText: "text-blue-700",
    dot: "bg-blue-500",
    gradient: "from-blue-500 to-indigo-600",
  },
  "text-indigo-600": {
    text: "text-indigo-600",
    activeBg: "bg-indigo-50",
    activeText: "text-indigo-700",
    dot: "bg-indigo-500",
    gradient: "from-indigo-500 to-violet-600",
  },
  "text-purple-600": {
    text: "text-purple-600",
    activeBg: "bg-purple-50",
    activeText: "text-purple-700",
    dot: "bg-purple-500",
    gradient: "from-purple-500 to-fuchsia-600",
  },
  "text-green-600": {
    text: "text-green-600",
    activeBg: "bg-emerald-50",
    activeText: "text-emerald-700",
    dot: "bg-emerald-500",
    gradient: "from-emerald-500 to-teal-600",
  },
};
const DEFAULT_THEME: AccentTheme = ACCENT_THEME["text-indigo-600"];

function initials(name?: string) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] : parts[0]?.[1] ?? "";
  return (first + last).toUpperCase();
}

export function AppShell({
  title,
  accent,
  nav,
  children,
}: {
  title: string;
  accent: string; // tailwind text color class, vd "text-blue-600"
  nav: NavItem[];
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const theme = ACCENT_THEME[accent] ?? DEFAULT_THEME;

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  return (
    <div className="min-h-screen">
      {/* thin accent line at very top */}
      <div className={cn("h-1 w-full bg-gradient-to-r", theme.gradient)} />

      <header className="sticky top-0 z-30 border-b border-slate-200/70 glass">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-3">
            <div
              className={cn(
                "flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br text-white shadow-soft",
                theme.gradient
              )}
            >
              <Hotel className="h-5 w-5" />
            </div>
            <div className="leading-tight">
              <div className="flex items-center gap-2">
                <span className="text-[0.95rem] font-semibold tracking-tight text-slate-900">
                  Smart Hotel
                </span>
                <span
                  className={cn(
                    "rounded-md px-1.5 py-0.5 text-[0.7rem] font-semibold",
                    theme.activeBg,
                    theme.activeText
                  )}
                >
                  {title}
                </span>
              </div>
              <span className="hidden text-xs text-slate-400 sm:block">
                Property Management System
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <div className="text-sm font-medium text-slate-800">{user?.username}</div>
              <div className="text-xs text-slate-400">
                {user ? ROLE_LABEL[user.role] : ""}
              </div>
            </div>
            <div
              className={cn(
                "flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br text-xs font-semibold text-white shadow-soft",
                theme.gradient
              )}
              title={user?.username}
            >
              {initials(user?.username)}
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 transition hover:bg-red-50 hover:text-red-600"
              title="Đăng xuất"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>

        <nav className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-3 pb-2">
          {nav.map((item) => {
            const active =
              pathname === item.href ||
              (item.href !== "/" && pathname.startsWith(item.href + "/"));
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-medium transition",
                  active
                    ? cn(theme.activeBg, theme.activeText)
                    : "text-slate-500 hover:bg-slate-100 hover:text-slate-800"
                )}
              >
                {item.icon}
                {item.label}
              </Link>
            );
          })}
        </nav>
      </header>

      <main className="mx-auto max-w-6xl animate-fade-in px-4 py-8">{children}</main>
    </div>
  );
}
