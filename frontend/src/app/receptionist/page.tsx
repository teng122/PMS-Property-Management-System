"use client";

import { RefreshCw } from "lucide-react";
import { useAvailableRooms } from "@/hooks/useRooms";
import { Card, CardBody, LoadingBlock, ErrorBlock, EmptyBlock, Badge } from "@/components/ui";
import { RoomStatusBadge } from "@/components/StatusBadge";
import { formatCurrency, errorMessage, ROOM_STATUS_STYLE } from "@/lib/utils";

export default function StaffRoomGridPage() {
  const { data, isLoading, isError, error, isFetching } = useAvailableRooms();

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Sơ đồ trạng thái phòng</h1>
          <p className="mt-1 text-sm text-slate-500">Theo dõi tình trạng phòng theo thời gian thực.</p>
        </div>
        <span className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-500 shadow-xs">
          <RefreshCw className={`h-3.5 w-3.5 ${isFetching ? "animate-spin text-brand-500" : ""}`} /> Tự làm mới 30s
        </span>
      </div>

      <div className="flex flex-wrap gap-2">
        {Object.values(ROOM_STATUS_STYLE).map((s) => (
          <Badge key={s.label} className={s.className}>
            {s.label}
          </Badge>
        ))}
      </div>

      <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700">
        ⚠️ API <code>/rooms/search</code> hiện chỉ trả phòng <b>AVAILABLE</b>. Muốn thấy đủ
        OCCUPIED/DIRTY/CLEANING cần backend bổ sung <code>GET /api/rooms</code> (list-all).
      </div>

      {isLoading ? (
        <LoadingBlock />
      ) : isError ? (
        <ErrorBlock message={errorMessage(error)} />
      ) : (data ?? []).length === 0 ? (
        <EmptyBlock message="Không có phòng trống." />
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-6">
          {(data ?? []).map((room) => (
            <Card
              key={room.id}
              className="transition-all duration-200 hover:-translate-y-0.5 hover:shadow-elevated"
            >
              <CardBody className="flex flex-col items-center gap-1.5 py-4 text-center">
                <div className="text-xl font-bold tracking-tight text-slate-900">{room.roomNumber}</div>
                <div className="text-xs font-medium text-slate-400">{room.type}</div>
                <RoomStatusBadge status={room.status} />
                <div className="text-xs text-slate-500">{formatCurrency(room.price)}</div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
