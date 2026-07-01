"use client";

import { useParams, useRouter } from "next/navigation";
import { LogIn, LogOut } from "lucide-react";
import { useBooking, useCheckIn } from "@/hooks/useBookings";
import { useRoom } from "@/hooks/useRooms";
import { Button, Card, CardBody, CardHeader, LoadingBlock, ErrorBlock } from "@/components/ui";
import { BookingStatusBadge } from "@/components/StatusBadge";
import { formatDate, errorMessage } from "@/lib/utils";

export default function StaffBookingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data: booking, isLoading, isError, error } = useBooking(id);
  const { data: room } = useRoom(booking?.roomId);
  const checkIn = useCheckIn();

  if (isLoading) return <LoadingBlock />;
  if (isError || !booking) return <ErrorBlock message={errorMessage(error)} />;

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold">Chi tiết đặt phòng</h1>
      <Card>
        <CardHeader className="flex items-center justify-between">
          <span className="font-medium">{booking.customerName}</span>
          <BookingStatusBadge status={booking.status} />
        </CardHeader>
        <CardBody className="space-y-2 text-sm">
          <Row label="Phòng" value={room ? `${room.roomNumber} (${room.type})` : booking.roomId} />
          <Row label="Nhận phòng" value={formatDate(booking.checkInDate)} />
          <Row label="Trả phòng" value={formatDate(booking.checkOutDate)} />
          <Row label="Mã booking" value={booking.id} />

          {checkIn.isError && <ErrorBlock message={errorMessage(checkIn.error)} />}

          <div className="flex gap-2 pt-2">
            {booking.status === "CONFIRMED" && (
              <Button
                loading={checkIn.isPending}
                onClick={() => checkIn.mutate(booking)}
                className="flex-1"
              >
                <LogIn className="h-4 w-4" /> Check-in
              </Button>
            )}
            {booking.status === "CHECKED_IN" && (
              <Button
                variant="danger"
                className="flex-1"
                onClick={() => router.push(`/receptionist/checkout/${booking.id}`)}
              >
                <LogOut className="h-4 w-4" /> Bắt đầu Check-out
              </Button>
            )}
            {booking.status === "PENDING_PAYMENT" && (
              <p className="text-sm text-amber-600">Đơn chưa thanh toán — chờ khách hoàn tất.</p>
            )}
          </div>
        </CardBody>
      </Card>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <span className="text-slate-500">{label}</span>
      <span className="text-right font-medium">{value}</span>
    </div>
  );
}
