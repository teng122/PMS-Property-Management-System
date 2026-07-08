"use client";

import Link from "next/link";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge, statusLabel } from "@/components/ui/status-badge";
import { bookingApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { useAuthStore } from "@/stores/auth-store";
import type { RoomSearchResult } from "@/types/api";

const today = new Date().toISOString().slice(0, 10);

const schema = z.object({
  checkIn: z.string().min(1, "Chọn ngày nhận phòng"),
  checkOut: z.string().min(1, "Chọn ngày trả phòng")
}).refine((data) => data.checkOut >= data.checkIn, {
  path: ["checkOut"],
  message: "Ngày trả phòng phải sau hoặc bằng ngày nhận phòng"
});

type SearchForm = z.infer<typeof schema>;

export default function SearchPage() {
  const user = useAuthStore((state) => state.user);
  const [rooms, setRooms] = useState<RoomSearchResult[]>([]);
  const [selectedDates, setSelectedDates] = useState<SearchForm | null>(null);
  const [bookingMessage, setBookingMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<SearchForm>({
    resolver: zodResolver(schema),
    defaultValues: { checkIn: today, checkOut: today }
  });

  const searchMutation = useMutation({
    mutationFn: (values: SearchForm) => bookingApi.searchAvailableRooms(values.checkIn, values.checkOut),
    onSuccess: (data, values) => {
      setSelectedDates(values);
      setRooms(data);
    }
  });

  const bookingMutation = useMutation({
    mutationFn: (roomId: string) => {
      if (!selectedDates) throw new Error("Vui lòng tìm phòng trước.");
      return bookingApi.createOnline({
        roomId,
        checkInDate: selectedDates.checkIn,
        checkOutDate: selectedDates.checkOut
      });
    },
    onSuccess: (booking) => {
      setBookingMessage(`Đã tạo đặt phòng ${booking.id.slice(0, 8)}. Trạng thái hiện tại: ${statusLabel(booking.status)}. Vào Đặt phòng của tôi để theo dõi đặt cọc.`);
    }
  });

  return (
    <main className="main" style={{ maxWidth: 1180, margin: "0 auto" }}>
      <div className="topbar">
        <div>
          <h1 className="page-title">Tìm phòng trống</h1>
          <p className="muted">Chọn ngày lưu trú để xem các phòng còn phù hợp.</p>
        </div>
        <div className="actions">
          {user ? <Link className="button secondary" href="/dashboard">Vào trang làm việc</Link> : <Link className="button" href="/login">Đăng nhập</Link>}
        </div>
      </div>

      <Card>
        <CardHeader title="Khoảng ngày lưu trú" description="Ngày nhận phòng và trả phòng dự kiến của khách." />
        <form className="toolbar" onSubmit={handleSubmit((values) => searchMutation.mutate(values))}>
          <div className="field" style={{ minWidth: 180 }}>
            <label>Check-in</label>
            <input className="input" type="date" {...register("checkIn")} />
            {errors.checkIn ? <span className="field-error">{errors.checkIn.message}</span> : null}
          </div>
          <div className="field" style={{ minWidth: 180 }}>
            <label>Check-out</label>
            <input className="input" type="date" {...register("checkOut")} />
            {errors.checkOut ? <span className="field-error">{errors.checkOut.message}</span> : null}
          </div>
          <Button type="submit" disabled={searchMutation.isPending} style={{ alignSelf: "end" }}>
            {searchMutation.isPending ? "Đang tìm..." : "Tìm phòng"}
          </Button>
        </form>
        {searchMutation.isPending ? <LoadingState /> : null}
        {searchMutation.isError ? (
          <ErrorState message={extractErrorMessage(searchMutation.error)} />
        ) : null}
        {bookingMessage ? <div className="toast" style={{ marginBottom: 16 }}>{bookingMessage}</div> : null}
        {bookingMutation.isError ? <ErrorState message={extractErrorMessage(bookingMutation.error)} /> : null}
        <DataTable
          rows={rooms}
          getRowKey={(room) => room.id}
          searchableText={(room) => `${room.roomNumber} ${room.roomType} ${room.status}`}
          emptyLabel="Chưa có phòng phù hợp"
          columns={[
            { key: "room", header: "Phòng", render: (room) => <strong>{room.roomNumber}</strong> },
            { key: "type", header: "Loại", render: (room) => room.roomType },
            { key: "price", header: "Giá", render: (room) => `$${Number(room.price || 0).toFixed(2)}` },
            { key: "status", header: "Trạng thái", render: (room) => <StatusBadge status={room.status} /> },
            {
              key: "actions",
              header: "Thao tác",
              render: (room) =>
                user?.role === "CUSTOMER" ? (
                  <Button disabled={bookingMutation.isPending} onClick={() => bookingMutation.mutate(room.id)}>
                    Đặt phòng
                  </Button>
                ) : (
                  <span className="muted">Đăng nhập bằng tài khoản khách hàng để đặt phòng</span>
                )
            }
          ]}
        />
      </Card>
    </main>
  );
}
