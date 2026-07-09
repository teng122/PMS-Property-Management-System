"use client";

import Link from "next/link";
import { useMemo, useState, useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { Modal } from "@/components/ui/modal";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { extractErrorMessage } from "@/lib/api/error";
import { bookingApi, identityApi, roomApi } from "@/lib/api/services";
import { dateTime, money } from "@/lib/format";
import { useAuthStore } from "@/stores/auth-store";
import type { BookingResponse, BookingStatus, UUID } from "@/types/api";

const statusOrder: BookingStatus[] = [
  "PENDING",
  "AWAITING_DEPOSIT",
  "CONFIRMED",
  "CHECKED_IN",
  "CHECKED_OUT",
  "CANCELLED",
  "NO_SHOW"
];

export default function BookingsPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "CUSTOMER"]}>
      <BookingsContent />
    </ProtectedPage>
  );
}

function BookingsContent() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<UUID | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const bookings = useQuery({
    queryKey: ["bookings", user?.role],
    queryFn: user?.role === "CUSTOMER" ? bookingApi.myBookings : bookingApi.getAll,
    enabled: !!user
  });

  const rooms = useQuery({
    queryKey: ["rooms", "directory"],
    queryFn: roomApi.getAll,
    enabled: user?.role === "ADMIN" || user?.role === "RECEPTIONIST"
  });

  const users = useQuery({
    queryKey: ["users", "directory"],
    queryFn: identityApi.getUsers,
    enabled: user?.role === "ADMIN"
  });

  const roomNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const room of rooms.data || []) map.set(room.id, room.roomNumber);
    return map;
  }, [rooms.data]);

  const customerNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const customer of users.data || []) map.set(customer.id, customer.fullName || customer.username);
    return map;
  }, [users.data]);

  const counts = useMemo(() => {
    const map = new Map<BookingStatus, number>();
    for (const status of statusOrder) map.set(status, 0);
    for (const booking of bookings.data || []) map.set(booking.status, (map.get(booking.status) || 0) + 1);
    return map;
  }, [bookings.data]);

  return (
    <>
      <PageHeading
        title={user?.role === "CUSTOMER" ? "Đặt phòng của tôi" : "Quản lý đặt phòng"}
        description="Theo dõi đặt phòng, nhận phòng, trả phòng và các trạng thái xử lý."
        action={
          <div className="actions">
            {user?.role === "CUSTOMER" ? <Link className="button" href="/search">Tìm và đặt phòng</Link> : null}
            {user?.role === "ADMIN" || user?.role === "RECEPTIONIST" ? <Link className="button secondary" href="/walk-in">Nhận khách tại quầy</Link> : null}
          </div>
        }
      />

      <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(150px, 1fr))", marginBottom: 18 }}>
        {statusOrder.map((status) => (
          <Card key={status}>
            <StatusBadge status={status} />
            <div style={{ fontSize: 26, fontWeight: 900, marginTop: 8 }}>{counts.get(status) || 0}</div>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader title="Danh sách đặt phòng" description="Theo dõi toàn bộ đặt phòng và thao tác nghiệp vụ theo quyền tài khoản." />
        {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
        {bookings.isLoading ? <LoadingState /> : null}
        {bookings.isError ? <ErrorState message={extractErrorMessage(bookings.error)} /> : null}
        <DataTable
          rows={bookings.data || []}
          getRowKey={(booking) => booking.id}
          searchableText={(booking) =>
            `${booking.id} ${booking.customerId} ${customerNameById.get(booking.customerId) || ""} ${booking.roomId} ${roomNameById.get(booking.roomId) || ""} ${booking.status}`
          }
          columns={[
            { key: "id", header: "Mã đặt phòng", render: (booking) => <code>{booking.id.slice(0, 8)}</code> },
            {
              key: "guest",
              header: "Khách",
              render: (booking) => customerNameById.get(booking.customerId) || <code>{booking.customerId.slice(0, 8)}</code>
            },
            {
              key: "room",
              header: "Phòng",
              render: (booking) => roomNameById.get(booking.roomId) || <code>{booking.roomId.slice(0, 8)}</code>
            },
            { key: "checkIn", header: "Check-in", render: (booking) => dateTime(booking.checkInDate) },
            { key: "checkOut", header: "Check-out", render: (booking) => dateTime(booking.checkOutDate) },
            { key: "amount", header: "Tổng", render: (booking) => money(booking.totalAmount) },
            { key: "deposit", header: "Cọc", render: (booking) => money(booking.depositAmount) },
            { key: "status", header: "Trạng thái", render: (booking) => <StatusBadge status={booking.status} /> },
            {
              key: "actions",
              header: "Thao tác",
              render: (booking) => (
                <Button variant="secondary" onClick={() => setSelectedId(booking.id)}>
                  Chi tiết
                </Button>
              )
            }
          ]}
        />
      </Card>

      <BookingDetailModal
        bookingId={selectedId}
        onClose={() => setSelectedId(null)}
        onMessage={setMessage}
        onChanged={async () => {
          await queryClient.invalidateQueries({ queryKey: ["bookings"] });
        }}
        roomNameById={roomNameById}
        customerNameById={customerNameById}
      />
    </>
  );
}

function BookingDetailModal({
  bookingId,
  onClose,
  onMessage,
  onChanged,
  roomNameById,
  customerNameById
}: {
  bookingId: UUID | null;
  onClose: () => void;
  onMessage: (message: string) => void;
  onChanged: () => Promise<void>;
  roomNameById: Map<string, string>;
  customerNameById: Map<string, string>;
}) {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [showQR, setShowQR] = useState(false);

  useEffect(() => {
    setShowQR(false);
  }, [bookingId]);

  const detail = useQuery({
    queryKey: ["booking", bookingId],
    queryFn: () => bookingApi.getById(bookingId as UUID),
    enabled: !!bookingId,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === "PENDING" ? 3000 : false;
    }
  });

  const mutation = useMutation({
    mutationFn: async ({ action, booking }: { action: string; booking: BookingResponse }) => {
      switch (action) {
        case "deposit": {
          return bookingApi.payDeposit(booking.id, booking.depositAmount);
        }
        case "check-in":
          return bookingApi.checkIn(booking.id);
        case "no-show":
          return bookingApi.noShow(booking.id);
        case "check-out":
          return bookingApi.checkOut(booking.id);
        case "delete":
          if (!confirm("Xoá đặt phòng này?")) return null;
          await bookingApi.remove(booking.id);
          return null;
        default:
          return null;
      }
    },
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({ queryKey: ["booking", bookingId] });
      await onChanged();
      if (variables.action === "deposit") {
        setShowQR(false);
      }
      if (variables.action === "check-out") {
        onMessage("Đã trả phòng. Hoá đơn đang được chuẩn bị, vui lòng mở mục Hoá đơn để theo dõi và thanh toán.");
      } else if (variables.action === "delete") {
        onClose();
        onMessage("Đã xoá đặt phòng.");
      } else {
        onMessage("Đã cập nhật đặt phòng.");
      }
    }
  });

  const booking = detail.data;
  const isCustomer = user?.role === "CUSTOMER";
  const isReception = user?.role === "RECEPTIONIST" || user?.role === "ADMIN";

  return (
    <Modal open={!!bookingId} title="Chi tiết đặt phòng" onClose={onClose}>
      {detail.isLoading ? <LoadingState /> : null}
      {detail.isError ? <ErrorState message={extractErrorMessage(detail.error)} /> : null}
      {mutation.isError ? <ErrorState message={extractErrorMessage(mutation.error)} /> : null}
      {booking ? (
        <div className="grid">
          <div className="grid cols-2">
            <Info label="Mã đặt phòng" value={booking.id} />
            <Info label="Khách" value={customerNameById.get(booking.customerId) || booking.customerId} />
            <Info label="Phòng" value={roomNameById.get(booking.roomId) || booking.roomId} />
            <Info label="Trạng thái" value={<StatusBadge status={booking.status} />} />
            <Info label="Check-in" value={dateTime(booking.checkInDate)} />
            <Info label="Check-out" value={dateTime(booking.checkOutDate)} />
            <Info label="Tổng tiền" value={money(booking.totalAmount)} />
            <Info label="Tiền cọc" value={`${money(booking.depositAmount)} — ${booking.isDepositPaid ? "đã thu" : "chưa thu"}`} />
          </div>

          {booking.status === "PENDING" ? (
            <div className="toast">Đặt phòng đang được xử lý. Trạng thái sẽ tự cập nhật trong giây lát.</div>
          ) : null}

          {showQR && booking.status === "AWAITING_DEPOSIT" ? (
            <div className="grid cols-2" style={{ borderTop: "1px solid var(--line)", paddingTop: 18, marginTop: 18 }}>
              <div style={{ display: "flex", justifyContent: "center" }}>
                <img
                  src={`https://img.vietqr.io/image/970415-113366668888-compact2.png?amount=${booking.depositAmount * 1000}&addInfo=DEP${booking.id}`}
                  alt="VietQR đặt cọc"
                  style={{ width: "100%", maxWidth: 260, height: "auto", borderRadius: 16, border: "1px solid var(--line)" }}
                />
              </div>
              <div className="state" style={{ textAlign: "left", display: "flex", flexDirection: "column", justifyContent: "center" }}>
                <strong>Thanh toán đặt cọc</strong>
                <p>Số tiền cọc: <strong style={{ color: "var(--primary)" }}>{money(booking.depositAmount)}</strong></p>
                <p className="muted" style={{ fontSize: 13 }}>
                  Sau khi quét mã chuyển khoản thành công, vui lòng bấm nút xác nhận dưới đây để hoàn tất.
                </p>
              </div>
            </div>
          ) : null}

          <div className="actions">
            {showQR && booking.status === "AWAITING_DEPOSIT" ? (
              <>
                <Button
                  variant="success"
                  disabled={mutation.isPending}
                  onClick={() => mutation.mutate({ action: "deposit", booking })}
                >
                  {isCustomer ? "Xác nhận đã chuyển khoản" : "Xác nhận đã thu cọc"}
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => setShowQR(false)}
                >
                  Quay lại
                </Button>
              </>
            ) : (
              <>
                {isCustomer && booking.status === "AWAITING_DEPOSIT" ? (
                  <Button onClick={() => setShowQR(true)}>
                    Đặt cọc
                  </Button>
                ) : null}
                {isReception && booking.status === "AWAITING_DEPOSIT" ? (
                  <Button onClick={() => setShowQR(true)}>
                    Thu cọc
                  </Button>
                ) : null}
                {isReception && booking.status === "CONFIRMED" ? (
                  <>
                    <Button variant="success" disabled={mutation.isPending} onClick={() => mutation.mutate({ action: "check-in", booking })}>
                      Nhận phòng
                    </Button>
                    <Button variant="danger" disabled={mutation.isPending} onClick={() => mutation.mutate({ action: "no-show", booking })}>
                      Khách không đến
                    </Button>
                  </>
                ) : null}
                {isReception && booking.status === "CHECKED_IN" ? (
                  <Button disabled={mutation.isPending} onClick={() => mutation.mutate({ action: "check-out", booking })}>
                    Trả phòng
                  </Button>
                ) : null}
                {user?.role === "ADMIN" ? (
                  <Button variant="danger" disabled={mutation.isPending} onClick={() => mutation.mutate({ action: "delete", booking })}>
                    Xoá đặt phòng
                  </Button>
                ) : null}
                {isReception ? (
                  <Link className="button secondary" href={`/invoices?bookingId=${booking.id}`}>
                    Mở hoá đơn
                  </Link>
                ) : null}
              </>
            )}
          </div>
        </div>
      ) : null}
    </Modal>
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
