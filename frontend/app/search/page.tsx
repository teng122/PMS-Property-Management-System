"use client";

import Link from "next/link";
import { useMutation, useQueryClient } from "@tanstack/react-query";
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
import { AppShell } from "@/components/layout/app-shell";
import { useAuthStore } from "@/stores/auth-store";
import type { RoomSearchResult } from "@/types/api";

const today = new Date().toISOString().slice(0, 10);
const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);

const schema = z.object({
  checkIn: z.string().min(1, "Chọn ngày nhận phòng"),
  checkOut: z.string().min(1, "Chọn ngày trả phòng")
}).refine((data) => data.checkOut > data.checkIn, {
  path: ["checkOut"],
  message: "Ngày trả phòng phải sau ngày nhận phòng"
});

type SearchForm = z.infer<typeof schema>;

export default function SearchPage() {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const isHydrated = useAuthStore((state) => state.isHydrated);
  const [rooms, setRooms] = useState<RoomSearchResult[]>([]);
  const [selectedDates, setSelectedDates] = useState<SearchForm | null>(null);
  const [bookingMessage, setBookingMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<SearchForm>({
    resolver: zodResolver(schema),
    defaultValues: { checkIn: today, checkOut: tomorrow }
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
    onSuccess: async (booking) => {
      setBookingMessage(`Đã tạo đặt phòng ${booking.id.slice(0, 8)}. Trạng thái hiện tại: ${statusLabel(booking.status)}. Vào Đặt phòng của tôi để theo dõi đặt cọc.`);
      await queryClient.invalidateQueries({ queryKey: ["bookings"] });
      await queryClient.invalidateQueries({ queryKey: ["rooms"] });
      if (selectedDates) {
        searchMutation.mutate(selectedDates);
      }
    }
  });

  if (!isHydrated) return <LoadingState label="Đang tải..." />;

  const content = (
    <div style={!user ? { maxWidth: 1180, margin: "0 auto" } : undefined}>
      <div className="topbar">
        <div>
          <h1 className="page-title">Tìm phòng trống</h1>
          <p className="muted">Chọn ngày lưu trú để xem các phòng còn phù hợp.</p>
        </div>
        <div className="actions">
          {!user ? <Link className="button" href="/login">Đăng nhập</Link> : null}
        </div>
      </div>

      <Card>
        <CardHeader title="Khoảng ngày lưu trú" description="Ngày nhận phòng và trả phòng dự kiến của khách." />
        <form className="toolbar" style={{ alignItems: "flex-start" }} onSubmit={handleSubmit((values) => {
          setBookingMessage(null);
          bookingMutation.reset();
          searchMutation.mutate(values);
        })}>
          <div className="field" style={{ minWidth: 180 }}>
            <label>Check-in</label>
            <input className="input" type="date" {...register("checkIn")} />
            <div style={{ minHeight: 20 }}>
              {errors.checkIn ? <span className="field-error">{errors.checkIn.message}</span> : null}
            </div>
          </div>
          <div className="field" style={{ minWidth: 180 }}>
            <label>Check-out</label>
            <input className="input" type="date" {...register("checkOut")} />
            <div style={{ minHeight: 20 }}>
              {errors.checkOut ? <span className="field-error">{errors.checkOut.message}</span> : null}
            </div>
          </div>
          <Button type="submit" disabled={searchMutation.isPending} style={{ marginTop: 25 }}>
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
          searchableText={(room) => `${room.roomNumber} ${room.type}`}
          emptyLabel="Chưa có phòng phù hợp"
          columns={[
            { key: "room", header: "Phòng", width: "15%", render: (room) => <strong>{room.roomNumber}</strong> },
            { key: "type", header: "Loại", width: "25%", render: (room) => room.type },
            { key: "price", header: "Giá", width: "20%", render: (room) => `$${Number(room.price || 0).toFixed(2)}` },
            {
              key: "actions",
              header: "Thao tác",
              width: "40%",
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
    </div>
  );

  if (user) {
    return <AppShell>{content}</AppShell>;
  }

  return <main className="main">{content}</main>;
}
