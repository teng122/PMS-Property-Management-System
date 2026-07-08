"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge, statusLabel } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { amenityApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { money } from "@/lib/format";
import { useAuthStore } from "@/stores/auth-store";
import type { AmenityOrderStatus } from "@/types/api";

const filters: Array<AmenityOrderStatus | ""> = ["PREPARING", "DELIVERED", "BILLED", "REJECTED", ""];

export default function AmenityOrdersPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "STAFF"]}>
      <AmenityOrdersContent />
    </ProtectedPage>
  );
}

function AmenityOrdersContent() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<AmenityOrderStatus | "">("PREPARING");
  const [message, setMessage] = useState<string | null>(null);

  const orders = useQuery({
    queryKey: ["amenity-orders", status],
    queryFn: () => amenityApi.orders(status || undefined)
  });

  const update = useMutation({
    mutationFn: ({ id, next }: { id: string; next: AmenityOrderStatus }) => amenityApi.updateOrderStatus(id, next),
    onSuccess: async (_data, variables) => {
      setMessage(`Đã chuyển đơn sang ${statusLabel(variables.next)}.`);
      await queryClient.invalidateQueries({ queryKey: ["amenity-orders"] });
    }
  });

  return (
    <>
      <PageHeading title="Đơn dịch vụ" description="Theo dõi yêu cầu dịch vụ, xác nhận đã giao hoặc huỷ khi cần." />
      <Card>
        <CardHeader title="Bộ lọc" />
        <div className="toolbar">
          <select className="select" style={{ maxWidth: 260 }} value={status} onChange={(event) => setStatus(event.target.value as AmenityOrderStatus | "")}>
            {filters.map((item) => <option key={item || "ALL"} value={item}>{item ? statusLabel(item) : "Tất cả"}</option>)}
          </select>
        </div>
        {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
        {orders.isLoading ? <LoadingState /> : null}
        {orders.isError ? <ErrorState message={extractErrorMessage(orders.error)} /> : null}
        {update.isError ? <ErrorState message={extractErrorMessage(update.error)} /> : null}
        <DataTable
          rows={orders.data || []}
          getRowKey={(order) => order.id}
          searchableText={(order) => `${order.id} ${order.roomId} ${order.amenityName} ${order.status}`}
          columns={[
            { key: "id", header: "Mã đơn", render: (order) => <code>{order.id.slice(0, 8)}</code> },
            { key: "room", header: "Phòng", render: (order) => <code>{order.roomId.slice(0, 8)}</code> },
            { key: "amenity", header: "Dịch vụ", render: (order) => order.amenityName || "Chưa có" },
            { key: "qty", header: "SL", render: (order) => order.quantity || 0 },
            { key: "total", header: "Tổng", render: (order) => money(order.totalPrice) },
            { key: "status", header: "Trạng thái", render: (order) => <StatusBadge status={order.status} /> },
            {
              key: "actions",
              header: "Thao tác",
              render: (order) => (
                <div className="actions">
                  {order.status === "PREPARING" ? (
                    <Button variant="success" disabled={update.isPending} onClick={() => update.mutate({ id: order.id, next: "DELIVERED" })}>
                      Đã giao
                    </Button>
                  ) : null}
                  {(user?.role === "ADMIN" || user?.role === "RECEPTIONIST") && order.status !== "BILLED" && order.status !== "REJECTED" ? (
                    <Button variant="danger" disabled={update.isPending} onClick={() => update.mutate({ id: order.id, next: "REJECTED" })}>
                      Huỷ
                    </Button>
                  ) : null}
                </div>
              )
            }
          ]}
        />
      </Card>
    </>
  );
}
