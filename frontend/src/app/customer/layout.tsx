"use client";

import { Search, ConciergeBell } from "lucide-react";
import { AuthGuard } from "@/components/AuthGuard";
import { AppShell } from "@/components/AppShell";

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard role="CUSTOMER">
      <AppShell
        title="Khách hàng"
        accent="text-blue-600"
        nav={[
          { href: "/customer", label: "Tìm phòng", icon: <Search className="h-4 w-4" /> },
          { href: "/customer/services", label: "Gọi dịch vụ", icon: <ConciergeBell className="h-4 w-4" /> },
        ]}
      >
        {children}
      </AppShell>
    </AuthGuard>
  );
}
