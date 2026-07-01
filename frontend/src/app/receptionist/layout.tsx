"use client";

import { LayoutGrid, ClipboardList } from "lucide-react";
import { AuthGuard } from "@/components/AuthGuard";
import { AppShell } from "@/components/AppShell";

export default function ReceptionistLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard role="RECEPTIONIST">
      <AppShell
        title="Lễ tân"
        accent="text-indigo-600"
        nav={[
          { href: "/receptionist", label: "Sơ đồ phòng", icon: <LayoutGrid className="h-4 w-4" /> },
          { href: "/receptionist/bookings", label: "Đặt phòng", icon: <ClipboardList className="h-4 w-4" /> },
        ]}
      >
        {children}
      </AppShell>
    </AuthGuard>
  );
}
