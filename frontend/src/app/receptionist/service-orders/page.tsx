"use client";

import { useState } from "react";
import { ConciergeBell, Check } from "lucide-react";
import { useAllOrders, useUpdateOrderStatus } from "@/hooks/useAmenities";
import {
  Button,
  Card,
  CardBody,
  LoadingBlock,
  ErrorBlock,
  EmptyBlock,
  Badge,
} from "@/components/ui";
import { formatCurrency, errorMessage } from "@/lib/utils";
import type { OrderStatus } from "@/types";

const TABS: { key: OrderStatus; label: string; color: string }[] = [
  { key: "PENDING", label: "Chờ giao", color: "bg-indigo-600 text-white" },
  { key: "DELIVERED", label: "Đã giao", color: "bg-green-600 text-white" },
  { key: "BILLED", label: "Đã thanh toán", color: "bg-slate-600 text-white" },
  { key: "CANCELLED", label: "Đã hủy", color: "bg-rose-600 text-white" },
];

export default function ServiceOrdersPage() {
  const [tab, setTab] = useState<OrderStatus>("PENDING");
  const { data = [], isLoading, isError, error } = useAllOrders();
  const updateStatus = useUpdateOrderStatus();

  const filteredOrders = data.filter((o) => o.status === tab);

  function handleDeliver(id: string) {
    updateStatus.mutate({ id, status: "DELIVERED" });
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center gap-2">
        <ConciergeBell className="h-6 w-6 text-indigo-600" />
        <h1 className="text-xl font-semibold">Đơn dịch vụ phòng</h1>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b pb-2">
        {TABS.map((t) => {
          const count = data.filter((o) => o.status === t.key).length;
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`rounded-lg px-4 py-1.5 text-sm font-medium transition-all ${
                tab === t.key
                  ? t.color
                  : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50"
              }`}
            >
              {t.label} ({count})
            </button>
          );
        })}
      </div>

      {/* Main Content */}
      {isLoading ? (
        <LoadingBlock label="Đang tải danh sách đơn dịch vụ..." />
      ) : isError ? (
        <ErrorBlock message={errorMessage(error)} />
      ) : filteredOrders.length === 0 ? (
        <EmptyBlock message={`Không có đơn dịch vụ nào ở trạng thái ${tab}.`} />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredOrders.map((order) => (
            <Card key={order.id} className="hover:shadow-md transition-shadow">
              <CardBody className="space-y-3">
                {/* Header of Card */}
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-semibold text-slate-800 text-base">{order.amenityName}</h3>
                    <p className="text-xs text-slate-400 font-mono mt-0.5">ID: {order.id.slice(0, 8)}...</p>
                  </div>
                  <Badge
                    className={
                      order.status === "PENDING"
                        ? "bg-amber-100 text-amber-800"
                        : order.status === "DELIVERED"
                        ? "bg-green-100 text-green-800"
                        : order.status === "CANCELLED"
                        ? "bg-rose-100 text-rose-800"
                        : "bg-slate-100 text-slate-800"
                    }
                  >
                    {order.status}
                  </Badge>
                </div>

                {/* Details */}
                <div className="space-y-1.5 text-sm text-slate-600 border-t pt-2.5">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Phòng (Room ID)</span>
                    <span className="font-mono text-xs text-slate-700 bg-slate-100 px-1.5 py-0.5 rounded">
                      {order.roomId.slice(0, 8)}...
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Mã đặt phòng (Booking ID)</span>
                    <span className="font-mono text-xs text-slate-700 bg-slate-100 px-1.5 py-0.5 rounded">
                      {order.bookingId.slice(0, 8)}...
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Đơn giá</span>
                    <span>{formatCurrency(order.amenityPrice)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Số lượng</span>
                    <span className="font-medium text-slate-800">x{order.quantity}</span>
                  </div>
                  <div className="flex justify-between border-t border-dashed pt-1.5 font-semibold">
                    <span className="text-slate-800">Tổng tiền</span>
                    <span className="text-indigo-600">{formatCurrency(order.totalPrice)}</span>
                  </div>
                </div>

                {/* Action buttons */}
                {order.status === "PENDING" && (
                  <div className="flex gap-2 mt-2">
                    <Button
                      variant="danger"
                      className="flex-1"
                      loading={
                        updateStatus.isPending &&
                        updateStatus.variables?.id === order.id &&
                        updateStatus.variables?.status === "CANCELLED"
                      }
                      onClick={() => updateStatus.mutate({ id: order.id, status: "CANCELLED" })}
                    >
                      Hủy đơn
                    </Button>
                    <Button
                      variant="success"
                      className="flex-1"
                      loading={
                        updateStatus.isPending &&
                        updateStatus.variables?.id === order.id &&
                        updateStatus.variables?.status === "DELIVERED"
                      }
                      onClick={() => handleDeliver(order.id)}
                    >
                      <Check className="h-4 w-4" /> Đã giao
                    </Button>
                  </div>
                )}
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
