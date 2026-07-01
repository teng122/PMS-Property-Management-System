"use client";

import { useState } from "react";
import Link from "next/link";
import { useBookings } from "@/hooks/useBookings";
import { Card, CardBody, LoadingBlock, ErrorBlock, EmptyBlock, Button } from "@/components/ui";
import { BookingStatusBadge } from "@/components/StatusBadge";
import { formatDate, errorMessage } from "@/lib/utils";
import type { BookingStatus } from "@/types";

const FILTERS: (BookingStatus | "ALL")[] = [
  "ALL",
  "PENDING_PAYMENT",
  "CONFIRMED",
  "CHECKED_IN",
  "CHECKED_OUT",
  "CANCELLED",
];

export default function StaffBookingsPage() {
  const [filter, setFilter] = useState<BookingStatus | "ALL">("ALL");
  const { data, isLoading, isError, error } = useBookings();

  const bookings = (data ?? []).filter((b) => filter === "ALL" || b.status === filter);

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Danh sách đặt phòng</h1>

      <div className="flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`rounded-lg px-3 py-1 text-sm ${
              filter === f ? "bg-indigo-600 text-white" : "border bg-white text-slate-600"
            }`}
          >
            {f}
          </button>
        ))}
      </div>

      {isLoading ? (
        <LoadingBlock />
      ) : isError ? (
        <ErrorBlock message={errorMessage(error)} />
      ) : bookings.length === 0 ? (
        <EmptyBlock message="Không có đơn phù hợp." />
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b bg-slate-50 text-left text-slate-500">
                <tr>
                  <th className="p-3">Khách</th>
                  <th className="p-3">Nhận</th>
                  <th className="p-3">Trả</th>
                  <th className="p-3">Trạng thái</th>
                  <th className="p-3"></th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id} className="border-b last:border-0 hover:bg-slate-50">
                    <td className="p-3 font-medium">{b.customerName}</td>
                    <td className="p-3">{formatDate(b.checkInDate)}</td>
                    <td className="p-3">{formatDate(b.checkOutDate)}</td>
                    <td className="p-3">
                      <BookingStatusBadge status={b.status} />
                    </td>
                    <td className="p-3 text-right">
                      <Link href={`/receptionist/bookings/${b.id}`}>
                        <Button variant="outline" className="px-3 py-1 text-xs">
                          Chi tiết
                        </Button>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
