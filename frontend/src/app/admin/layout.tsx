"use client";

import { LayoutDashboard, ConciergeBell, Receipt } from "lucide-react";
import { AuthGuard } from "@/components/AuthGuard";
import { AppShell } from "@/components/AppShell";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard role="ADMIN">
      <AppShell
        title="Quản lý"
        accent="text-purple-600"
        nav={[
          { href: "/admin", label: "Tổng quan", icon: <LayoutDashboard className="h-4 w-4" /> },
          { href: "/admin/amenities", label: "Dịch vụ", icon: <ConciergeBell className="h-4 w-4" /> },
          { href: "/admin/invoices", label: "Hóa đơn", icon: <Receipt className="h-4 w-4" /> },
        ]}
      >
        {children}
      </AppShell>
    </AuthGuard>
  );
}
