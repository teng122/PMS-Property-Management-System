"use client";

import { useQuery } from "@tanstack/react-query";
import { Card, CardHeader } from "@/components/ui/card";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { LoadingState } from "@/components/ui/states";
import { billingApi, bookingApi, housekeepingApi, roomApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { roleLabel } from "@/lib/auth/permissions";
import { useAuthStore } from "@/stores/auth-store";

export default function DashboardPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"]}>
      <DashboardContent />
    </ProtectedPage>
  );
}

function DashboardContent() {
  const user = useAuthStore((state) => state.user);

  const stats = useQuery({
    queryKey: ["dashboard", "stats"],
    queryFn: billingApi.stats,
    enabled: user?.role === "ADMIN"
  });

  const rooms = useQuery({
    queryKey: ["dashboard", "rooms"],
    queryFn: roomApi.getAll,
    enabled: user?.role === "ADMIN" || user?.role === "RECEPTIONIST"
  });

  const bookings = useQuery({
    queryKey: ["dashboard", "bookings", user?.role],
    queryFn: user?.role === "CUSTOMER" ? bookingApi.myBookings : bookingApi.getAll,
    enabled: user?.role === "ADMIN" || user?.role === "RECEPTIONIST" || user?.role === "CUSTOMER"
  });

  const housekeeping = useQuery({
    queryKey: ["dashboard", "housekeeping"],
    queryFn: () => housekeepingApi.tasks({ status: "PENDING" }),
    enabled: user?.role === "ADMIN" || user?.role === "RECEPTIONIST" || user?.role === "STAFF"
  });

  return (
    <>
      <PageHeading
        title={`Xin chào, ${user?.username || "bạn"}`}
        description={`Vai trò hiện tại: ${roleLabel(user?.role)}. Tổng quan vận hành trong ngày.`}
      />

      <div className="grid cols-4">
        {user?.role === "ADMIN" ? (
          <MetricCard
            title="Doanh thu"
            value={stats.data ? `$${Number(stats.data.totalRevenue).toFixed(2)}` : stats.isLoading ? "..." : "N/A"}
            hint={stats.isError ? extractErrorMessage(stats.error) : "Tổng doanh thu PAID"}
          />
        ) : null}
        {(user?.role === "ADMIN" || user?.role === "RECEPTIONIST") ? (
          <MetricCard
            title="Tổng phòng"
            value={rooms.data?.length ?? (rooms.isLoading ? "..." : "N/A")}
            hint={rooms.isError ? extractErrorMessage(rooms.error) : "Kho phòng hiện tại"}
          />
        ) : null}
        {(user?.role === "ADMIN" || user?.role === "RECEPTIONIST" || user?.role === "CUSTOMER") ? (
          <MetricCard
            title={user.role === "CUSTOMER" ? "Đặt phòng của tôi" : "Đặt phòng"}
            value={bookings.data?.length ?? (bookings.isLoading ? "..." : "N/A")}
            hint={bookings.isError ? extractErrorMessage(bookings.error) : "Đơn đặt phòng hiện có"}
          />
        ) : null}
        {(user?.role === "ADMIN" || user?.role === "RECEPTIONIST" || user?.role === "STAFF") ? (
          <MetricCard
            title="Task dọn phòng"
            value={housekeeping.data?.length ?? (housekeeping.isLoading ? "..." : "N/A")}
            hint={housekeeping.isError ? extractErrorMessage(housekeeping.error) : "Task PENDING"}
          />
        ) : null}
      </div>

      <div style={{ height: 18 }} />

      <Card>
        <CardHeader
          title="Luồng vận hành chính"
          description="Các bước nghiệp vụ chính từ đặt phòng, nhận phòng, trả phòng đến phục vụ và dọn phòng."
        />
        {stats.isLoading || bookings.isLoading ? <LoadingState /> : null}
        <div className="grid cols-3">
          <Workflow title="1. Đặt phòng" text="Khách chọn phòng, gửi yêu cầu đặt và hoàn tất tiền cọc khi phòng được giữ thành công." />
          <Workflow title="2. Lễ tân" text="Lễ tân xử lý khách vãng lai, nhận phòng, khách không đến, trả phòng và thanh toán." />
          <Workflow title="3. Vận hành" text="Nhân viên xử lý dịch vụ phòng và các công việc dọn phòng theo trạng thái." />
        </div>
      </Card>
    </>
  );
}

function MetricCard({ title, value, hint }: { title: string; value: string | number; hint?: string }) {
  return (
    <Card>
      <div className="muted">{title}</div>
      <div style={{ fontSize: 32, fontWeight: 900, letterSpacing: "-0.04em", marginTop: 8 }}>{value}</div>
      {hint ? <div className="muted" style={{ marginTop: 8, fontSize: 13 }}>{hint}</div> : null}
    </Card>
  );
}

function Workflow({ title, text }: { title: string; text: string }) {
  return (
    <div className="state" style={{ textAlign: "left" }}>
      <strong>{title}</strong>
      <p className="muted" style={{ marginBottom: 0 }}>{text}</p>
    </div>
  );
}
