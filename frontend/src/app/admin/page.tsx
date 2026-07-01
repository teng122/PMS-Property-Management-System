"use client";

import { BedDouble, DoorOpen, ClipboardList, CalendarCheck } from "lucide-react";
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
} from "recharts";
import { useAvailableRooms } from "@/hooks/useRooms";
import { useBookings } from "@/hooks/useBookings";
import { Card, CardBody, CardHeader, CardTitle, LoadingBlock } from "@/components/ui";
import type { BookingStatus } from "@/types";

const COLORS = ["#facc15", "#3b82f6", "#6366f1", "#94a3b8", "#ef4444"];

export default function AdminDashboardPage() {
  const rooms = useAvailableRooms();
  const bookings = useBookings();

  if (rooms.isLoading || bookings.isLoading) return <LoadingBlock />;

  const availableCount = rooms.data?.length ?? 0;
  const allBookings = bookings.data ?? [];

  const statusCounts = allBookings.reduce<Record<string, number>>((acc, b) => {
    acc[b.status] = (acc[b.status] ?? 0) + 1;
    return acc;
  }, {});

  const order: BookingStatus[] = [
    "PENDING_PAYMENT",
    "CONFIRMED",
    "CHECKED_IN",
    "CHECKED_OUT",
    "CANCELLED",
  ];
  const chartData = order
    .map((s) => ({ name: s, value: statusCounts[s] ?? 0 }))
    .filter((d) => d.value > 0);

  const activeGuests = allBookings.filter((b) => b.status === "CHECKED_IN").length;
  const pending = allBookings.filter((b) => b.status === "PENDING_PAYMENT").length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Tổng quan hệ thống</h1>
        <p className="mt-1 text-sm text-slate-500">Bức tranh vận hành khách sạn theo thời gian thực.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat icon={<BedDouble className="h-5 w-5" />} label="Phòng trống" value={availableCount} gradient="from-emerald-500 to-teal-600" />
        <Stat icon={<CalendarCheck className="h-5 w-5" />} label="Tổng đặt phòng" value={allBookings.length} gradient="from-blue-500 to-indigo-600" />
        <Stat icon={<DoorOpen className="h-5 w-5" />} label="Khách đang ở" value={activeGuests} gradient="from-indigo-500 to-violet-600" />
        <Stat icon={<ClipboardList className="h-5 w-5" />} label="Chờ thanh toán" value={pending} gradient="from-amber-500 to-orange-600" />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Phân bố trạng thái đặt phòng</CardTitle>
          </CardHeader>
          <CardBody>
            {chartData.length === 0 ? (
              <p className="py-10 text-center text-sm text-slate-400">Chưa có dữ liệu.</p>
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={chartData} dataKey="value" nameKey="name" outerRadius={90} label>
                    {chartData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ghi chú doanh thu</CardTitle>
          </CardHeader>
          <CardBody className="space-y-2 text-sm text-slate-600">
            <p>
              ⚠️ Backend chưa có <code>GET /api/invoices</code> (list) nên chưa thể tổng hợp doanh
              thu tự động. Xem FRONTEND_PLAN.md §10.5.
            </p>
            <p>Tạm thời tra cứu từng hóa đơn theo mã ở tab <b>Hóa đơn</b>.</p>
          </CardBody>
        </Card>
      </div>
    </div>
  );
}

function Stat({
  icon,
  label,
  value,
  gradient,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
  gradient: string;
}) {
  return (
    <Card className="transition-all duration-200 hover:-translate-y-0.5 hover:shadow-elevated">
      <CardBody className="flex items-center gap-4">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br text-white shadow-soft ${gradient}`}
        >
          {icon}
        </div>
        <div>
          <div className="text-2xl font-bold tracking-tight text-slate-900">{value}</div>
          <div className="text-xs font-medium text-slate-500">{label}</div>
        </div>
      </CardBody>
    </Card>
  );
}
