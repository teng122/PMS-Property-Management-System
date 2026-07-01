"use client";

import { Sparkles } from "lucide-react";
import { AuthGuard } from "@/components/AuthGuard";
import { AppShell } from "@/components/AppShell";

export default function HousekeeperLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard role="HOUSEKEEPER">
      <AppShell
        title="Lao công"
        accent="text-green-600"
        nav={[{ href: "/housekeeper", label: "Phòng cần dọn", icon: <Sparkles className="h-4 w-4" /> }]}
      >
        {children}
      </AppShell>
    </AuthGuard>
  );
}
