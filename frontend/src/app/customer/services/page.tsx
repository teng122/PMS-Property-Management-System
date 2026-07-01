"use client";

import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { ConciergeBell, Plus, Check } from "lucide-react";
import { useAmenities, useCreateOrder } from "@/hooks/useAmenities";
import { Button, Card, CardBody, Input, Label, LoadingBlock, ErrorBlock, EmptyBlock } from "@/components/ui";
import { formatCurrency, errorMessage } from "@/lib/utils";
import type { AmenityType } from "@/types";

const TABS: AmenityType[] = ["FOOD", "LAUNDRY", "SPA"];

export default function ServicesPage() {
  const params = useSearchParams();
  const [roomId, setRoomId] = useState(params.get("roomId") ?? "");
  const [bookingId, setBookingId] = useState(params.get("bookingId") ?? "");
  const [tab, setTab] = useState<AmenityType>("FOOD");
  const [lastOrdered, setLastOrdered] = useState<string | null>(null);

  const { data, isLoading, isError, error } = useAmenities();
  const createOrder = useCreateOrder();

  const items = (data ?? []).filter((a) => a.type === tab);

  function order(amenityId: string) {
    if (!roomId || !bookingId) return;
    createOrder.mutate(
      { roomId, bookingId, amenityId, quantity: 1 },
      { onSuccess: () => setLastOrdered(amenityId) }
    );
  }

  const ready = roomId && bookingId;

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <ConciergeBell className="h-6 w-6 text-blue-600" />
        <h1 className="text-xl font-semibold">Gọi dịch vụ phòng</h1>
      </div>

      <Card>
        <CardBody className="grid gap-3 sm:grid-cols-2">
          <div>
            <Label>Mã phòng (roomId)</Label>
            <Input value={roomId} onChange={(e) => setRoomId(e.target.value)} placeholder="UUID phòng (từ QR)" />
          </div>
          <div>
            <Label>Mã đặt phòng (bookingId)</Label>
            <Input value={bookingId} onChange={(e) => setBookingId(e.target.value)} placeholder="UUID booking (từ QR)" />
          </div>
        </CardBody>
      </Card>

      {!ready && <EmptyBlock message="Nhập mã phòng và mã đặt phòng để bắt đầu gọi dịch vụ." />}

      {ready && (
        <>
          <div className="flex gap-2">
            {TABS.map((t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`rounded-lg px-4 py-1.5 text-sm font-medium ${
                  tab === t ? "bg-blue-600 text-white" : "bg-white text-slate-600 border"
                }`}
              >
                {t}
              </button>
            ))}
          </div>

          {isLoading ? (
            <LoadingBlock />
          ) : isError ? (
            <ErrorBlock message={errorMessage(error)} />
          ) : items.length === 0 ? (
            <EmptyBlock message={`Chưa có dịch vụ loại ${tab}.`} />
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {items.map((a) => (
                <Card key={a.id}>
                  <CardBody className="space-y-2">
                    <div className="font-medium">{a.name}</div>
                    <div className="text-blue-600 font-semibold">{formatCurrency(a.price)}</div>
                    <Button
                      variant={lastOrdered === a.id ? "success" : "primary"}
                      className="w-full"
                      loading={createOrder.isPending && createOrder.variables?.amenityId === a.id}
                      onClick={() => order(a.id)}
                    >
                      {lastOrdered === a.id ? (
                        <>
                          <Check className="h-4 w-4" /> Đã gọi
                        </>
                      ) : (
                        <>
                          <Plus className="h-4 w-4" /> Gọi món
                        </>
                      )}
                    </Button>
                  </CardBody>
                </Card>
              ))}
            </div>
          )}

          {createOrder.isError && <ErrorBlock message={errorMessage(createOrder.error)} />}
        </>
      )}
    </div>
  );
}
