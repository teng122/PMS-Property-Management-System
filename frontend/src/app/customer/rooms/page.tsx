"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { BedDouble, CalendarDays, ArrowRight } from "lucide-react";
import { useAvailableRooms } from "@/hooks/useRooms";
import { Button, Card, CardBody, LoadingBlock, ErrorBlock, EmptyBlock } from "@/components/ui";
import { RoomStatusBadge } from "@/components/StatusBadge";
import { formatCurrency, errorMessage, nightsBetween } from "@/lib/utils";

export default function CustomerRoomsPage() {
  const params = useSearchParams();
  const checkIn = params.get("checkIn") ?? new Date().toISOString().slice(0, 10);
  const checkOut = params.get("checkOut") ?? checkIn;
  const typeFilter = params.get("type");

  const { data, isLoading, isError, error } = useAvailableRooms();

  // Backend /rooms/search không filter → lọc theo type ở client (FRONTEND_PLAN.md §0.5)
  const rooms = (data ?? []).filter((r) => !typeFilter || r.type === typeFilter);
  const nights = nightsBetween(checkIn, checkOut);

  if (isLoading) return <LoadingBlock />;
  if (isError) return <ErrorBlock message={errorMessage(error)} />;

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Phòng trống</h1>
          <p className="mt-1 text-sm text-slate-500">
            Tìm thấy <span className="font-semibold text-slate-700">{rooms.length}</span> phòng phù hợp
          </p>
        </div>
        <div className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-3.5 py-1.5 text-sm text-slate-600 shadow-xs">
          <CalendarDays className="h-4 w-4 text-brand-500" />
          {checkIn} → {checkOut}
          <span className="text-slate-300">·</span>
          <span className="font-medium text-slate-800">{nights} đêm</span>
        </div>
      </div>

      {rooms.length === 0 ? (
        <EmptyBlock message="Không có phòng phù hợp. Thử đổi loại phòng hoặc ngày khác." />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {rooms.map((room, i) => (
            <Card
              key={room.id}
              className="group overflow-hidden animate-fade-up transition-all duration-200 hover:-translate-y-1 hover:shadow-elevated"
              style={{ animationDelay: `${Math.min(i * 60, 400)}ms` }}
            >
              {/* Image / visual header */}
              <div className="relative flex h-36 items-center justify-center overflow-hidden bg-gradient-to-br from-brand-100 via-indigo-100 to-violet-100">
                <div className="absolute inset-0 bg-grid-slate bg-[length:20px_20px] opacity-40" />
                <BedDouble className="h-16 w-16 text-brand-400/80 transition-transform duration-300 group-hover:scale-110" />
                <span className="absolute left-3 top-3 rounded-full bg-white/90 px-2.5 py-0.5 text-xs font-semibold text-brand-700 shadow-sm backdrop-blur">
                  {room.type}
                </span>
                <div className="absolute right-3 top-3">
                  <RoomStatusBadge status={room.status} />
                </div>
              </div>

              <CardBody className="space-y-3">
                <div className="flex items-baseline justify-between">
                  <span className="text-lg font-semibold text-slate-900">Phòng {room.roomNumber}</span>
                  <div className="text-right">
                    <div className="text-lg font-bold text-brand-600">{formatCurrency(room.price)}</div>
                    <div className="text-xs text-slate-400">/ đêm</div>
                  </div>
                </div>

                <div className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-sm">
                  <span className="text-slate-500">Tổng {nights} đêm</span>
                  <span className="font-semibold text-slate-900">{formatCurrency(room.price * nights)}</span>
                </div>

                <Link href={`/customer/booking/${room.id}?checkIn=${checkIn}&checkOut=${checkOut}`} className="block">
                  <Button variant="brand" className="w-full">
                    Đặt ngay
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
                  </Button>
                </Link>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
