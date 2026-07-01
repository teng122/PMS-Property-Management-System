"use client";

import { useState } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useRoom } from "@/hooks/useRooms";
import { useReserveBooking } from "@/hooks/useBookings";
import { useAuthStore } from "@/store/useAuthStore";
import { Button, Card, CardBody, CardHeader, Input, Label, LoadingBlock, ErrorBlock } from "@/components/ui";
import { formatCurrency, nightsBetween, errorMessage } from "@/lib/utils";

export default function BookingFormPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const params = useSearchParams();
  const router = useRouter();
  const username = useAuthStore((s) => s.user?.username);

  const today = new Date().toISOString().slice(0, 10);
  const [checkInDate, setCheckIn] = useState(params.get("checkIn") ?? today);
  const [checkOutDate, setCheckOut] = useState(params.get("checkOut") ?? today);
  const [customerName, setCustomerName] = useState(username ?? "");

  const { data: room, isLoading, isError, error } = useRoom(roomId);
  const reserve = useReserveBooking();

  if (isLoading) return <LoadingBlock />;
  if (isError || !room) return <ErrorBlock message={errorMessage(error, "Không tìm thấy phòng")} />;

  const nights = nightsBetween(checkInDate, checkOutDate);

  function submit(e: React.FormEvent) {
    e.preventDefault();
    reserve.mutate(
      { roomId, customerName, checkInDate, checkOutDate },
      { onSuccess: (b) => router.push(`/customer/payment/${b.id}`) }
    );
  }

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold">Đặt phòng {room.roomNumber}</h1>
      <Card>
        <CardHeader className="flex items-center justify-between">
          <span className="font-medium">
            Phòng {room.roomNumber} · {room.type}
          </span>
          <span className="font-bold text-blue-600">{formatCurrency(room.price)}/đêm</span>
        </CardHeader>
        <CardBody>
          <form onSubmit={submit} className="space-y-3">
            <div>
              <Label>Tên khách</Label>
              <Input value={customerName} onChange={(e) => setCustomerName(e.target.value)} required />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label>Nhận phòng</Label>
                <Input type="date" min={today} value={checkInDate} onChange={(e) => setCheckIn(e.target.value)} />
              </div>
              <div>
                <Label>Trả phòng</Label>
                <Input type="date" min={checkInDate} value={checkOutDate} onChange={(e) => setCheckOut(e.target.value)} />
              </div>
            </div>

            <div className="rounded-lg bg-slate-50 p-3 text-sm">
              <div className="flex justify-between">
                <span>{formatCurrency(room.price)} × {nights} đêm</span>
                <b>{formatCurrency(room.price * nights)}</b>
              </div>
              <p className="mt-1 text-xs text-amber-600">
                * Sau khi đặt, bạn có 15 phút để thanh toán trước khi đơn tự hủy.
              </p>
            </div>

            {reserve.isError && <ErrorBlock message={errorMessage(reserve.error)} />}

            <Button type="submit" className="w-full" loading={reserve.isPending}>
              Xác nhận đặt phòng
            </Button>
          </form>
        </CardBody>
      </Card>
    </div>
  );
}
