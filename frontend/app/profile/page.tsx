"use client";

import { useQuery } from "@tanstack/react-query";
import { Card, CardHeader } from "@/components/ui/card";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { identityApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { roleLabel } from "@/lib/auth/permissions";
import { useAuthStore } from "@/stores/auth-store";

export default function ProfilePage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"]}>
      <ProfileContent />
    </ProtectedPage>
  );
}

function ProfileContent() {
  const user = useAuthStore((state) => state.user);
  const detail = useQuery({
    queryKey: ["me", user?.id],
    queryFn: () => identityApi.getMe(user!.id),
    enabled: !!user?.id
  });

  return (
    <>
      <PageHeading title="Hồ sơ" description="Thông tin tài khoản đang đăng nhập." />
      <div className="grid cols-2">
        <Card>
          <CardHeader title="Phiên đăng nhập" />
          <div className="grid">
            <Info label="Mã tài khoản" value={user?.id || "N/A"} />
            <Info label="Username" value={user?.username || "N/A"} />
            <Info label="Role" value={roleLabel(user?.role)} />
          </div>
        </Card>
        <Card>
          <CardHeader title="Thông tin cá nhân" />
          {detail.isLoading ? <LoadingState /> : null}
          {detail.isError ? <ErrorState message={extractErrorMessage(detail.error)} /> : null}
          {detail.data ? (
            <div className="grid">
              <Info label="Họ tên" value={detail.data.fullName || "N/A"} />
              <Info label="Email" value={detail.data.email || "N/A"} />
              <Info label="Trạng thái" value={<StatusBadge status={detail.data.status} />} />
            </div>
          ) : null}
        </Card>
      </div>
    </>
  );
}

function Info({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="state" style={{ textAlign: "left", padding: 14 }}>
      <div className="muted" style={{ fontSize: 12, fontWeight: 800, textTransform: "uppercase" }}>{label}</div>
      <div style={{ marginTop: 6, overflowWrap: "anywhere" }}>{value}</div>
    </div>
  );
}
