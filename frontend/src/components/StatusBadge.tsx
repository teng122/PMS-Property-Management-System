"use client";

import { Badge } from "@/components/ui";
import { cn } from "@/lib/utils";
import { BOOKING_STATUS_STYLE, ROOM_STATUS_STYLE } from "@/lib/utils";
import type { BookingStatus, RoomStatus } from "@/types";

export function RoomStatusBadge({ status }: { status: RoomStatus }) {
  const s = ROOM_STATUS_STYLE[status];
  return (
    <Badge className={s.className}>
      <span className={cn("h-1.5 w-1.5 rounded-full", s.dot)} />
      {s.label}
    </Badge>
  );
}

export function BookingStatusBadge({ status }: { status: BookingStatus }) {
  const s = BOOKING_STATUS_STYLE[status];
  return (
    <Badge className={s.className}>
      <span className={cn("h-1.5 w-1.5 rounded-full", s.dot)} />
      {s.label}
    </Badge>
  );
}
