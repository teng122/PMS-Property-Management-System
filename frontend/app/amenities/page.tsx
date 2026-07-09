"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { Modal } from "@/components/ui/modal";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { amenityApi, bookingApi, roomApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { dateTime, money } from "@/lib/format";
import { useAuthStore } from "@/stores/auth-store";
import type { AmenityResponse, BookingResponse } from "@/types/api";

const createSchema = z.object({
  name: z.string().min(2, "Nhập tên dịch vụ"),
  price: z.coerce.number().positive("Giá phải lớn hơn 0"),
  type: z.string().min(1, "Chọn loại"),
  isReturnable: z.boolean().optional()
});

const orderSchema = z.object({
  roomId: z.string().uuid("Chọn phòng đang lưu trú"),
  amenityId: z.string().uuid(),
  quantity: z.coerce.number().int().positive("Số lượng phải lớn hơn 0")
});

type CreateForm = z.infer<typeof createSchema>;
type OrderForm = z.infer<typeof orderSchema>;

function amenityTypeLabel(type: string) {
  const labels: Record<string, string> = {
    FOOD: "Ăn uống",
    LAUNDRY: "Giặt là",
    SPA: "Spa"
  };
  return labels[type] || type;
}

export default function AmenitiesPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"]}>
      <AmenitiesContent />
    </ProtectedPage>
  );
}

function AmenitiesContent() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [ordering, setOrdering] = useState<AmenityResponse | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const amenities = useQuery({ queryKey: ["amenities"], queryFn: amenityApi.getAll });

  return (
    <>
      <PageHeading
        title="Danh mục dịch vụ"
        description="Gọi món, giặt là, spa và các dịch vụ trong thời gian khách lưu trú."
        action={
          <div className="actions">
            {user?.role === "ADMIN" || user?.role === "RECEPTIONIST" ? <Button onClick={() => setCreateOpen(true)}>Thêm dịch vụ</Button> : null}
            {user?.role !== "CUSTOMER" ? <Link className="button secondary" href="/amenities/orders">Hàng chờ</Link> : null}
          </div>
        }
      />
      <Card>
        <CardHeader title="Dịch vụ hiện có" description="Chọn dịch vụ để phục vụ phòng đang lưu trú." />
        {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
        {amenities.isLoading ? <LoadingState /> : null}
        {amenities.isError ? <ErrorState message={extractErrorMessage(amenities.error)} /> : null}
        <DataTable
          rows={amenities.data || []}
          getRowKey={(item) => item.id}
          searchableText={(item) => `${item.name} ${item.type}`}
          columns={[
            { key: "name", header: "Dịch vụ", render: (item) => <strong>{item.name}</strong> },
            { key: "type", header: "Loại", render: (item) => amenityTypeLabel(item.type) },
            { key: "price", header: "Giá", render: (item) => money(item.price) },
            { key: "returnable", header: "Hoàn trả", render: (item) => (item.isReturnable ? "Có" : "Không") },
            {
              key: "action",
              header: "Thao tác",
              render: (item) =>
                user?.role === "CUSTOMER" || user?.role === "RECEPTIONIST" || user?.role === "ADMIN" ? (
                  <Button variant="secondary" onClick={() => setOrdering(item)}>Gọi dịch vụ</Button>
                ) : (
                  <span className="muted">Chỉ xem</span>
                )
            }
          ]}
        />
      </Card>

      <CreateAmenityModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSaved={async () => {
          setCreateOpen(false);
          setMessage("Đã thêm dịch vụ.");
          await queryClient.invalidateQueries({ queryKey: ["amenities"] });
        }}
      />
      <OrderAmenityModal amenity={ordering} onClose={() => setOrdering(null)} onDone={(msg) => setMessage(msg)} />
    </>
  );
}

function CreateAmenityModal({ open, onClose, onSaved }: { open: boolean; onClose: () => void; onSaved: () => Promise<void> }) {
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<CreateForm>({
    resolver: zodResolver(createSchema),
    defaultValues: { type: "FOOD", isReturnable: false }
  });

  async function onSubmit(values: CreateForm) {
    setError(null);
    try {
      await amenityApi.create(values);
      await onSaved();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  return (
    <Modal open={open} title="Thêm dịch vụ" onClose={onClose}>
      <form className="grid cols-2" onSubmit={handleSubmit(onSubmit)}>
        <div className="field">
          <label>Tên</label>
          <input className="input" {...register("name")} />
          {errors.name ? <span className="field-error">{errors.name.message}</span> : null}
        </div>
        <div className="field">
          <label>Loại</label>
          <select className="select" {...register("type")}>
            <option value="FOOD">Ăn uống</option>
            <option value="LAUNDRY">Giặt là</option>
            <option value="SPA">Spa</option>
          </select>
        </div>
        <div className="field">
          <label>Giá</label>
          <input className="input" type="number" step="0.01" {...register("price")} />
          {errors.price ? <span className="field-error">{errors.price.message}</span> : null}
        </div>
        <label className="field" style={{ alignContent: "end" }}>
          <span>Cho phép hoàn trả</span>
          <input type="checkbox" {...register("isReturnable")} />
        </label>
        {error ? <ErrorState message={error} /> : null}
        <div className="actions" style={{ gridColumn: "1 / -1" }}>
          <Button disabled={isSubmitting}>Lưu</Button>
          <Button type="button" variant="ghost" onClick={onClose}>Huỷ</Button>
        </div>
      </form>
    </Modal>
  );
}

function OrderAmenityModal({
  amenity,
  onClose,
  onDone
}: {
  amenity: AmenityResponse | null;
  onClose: () => void;
  onDone: (message: string) => void;
}) {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const [error, setError] = useState<string | null>(null);

  const activeBookings = useQuery({
    queryKey: ["active-stays-for-amenity", user?.role],
    queryFn: user?.role === "CUSTOMER" ? bookingApi.myBookings : bookingApi.getAll,
    enabled: !!amenity && !!user && user.role !== "STAFF"
  });

  const roomDirectory = useQuery({
    queryKey: ["rooms", "amenity-order-directory"],
    queryFn: roomApi.getAll,
    enabled: !!amenity && (user?.role === "ADMIN" || user?.role === "RECEPTIONIST")
  });

  const roomNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const room of roomDirectory.data || []) map.set(room.id, room.roomNumber);
    return map;
  }, [roomDirectory.data]);

  const activeStays = useMemo(
    () => (activeBookings.data || []).filter((booking: BookingResponse) => booking.status === "CHECKED_IN"),
    [activeBookings.data]
  );

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<OrderForm>({
    resolver: zodResolver(orderSchema),
    values: { amenityId: amenity?.id || "", roomId: activeStays[0]?.roomId || "", quantity: 1 }
  });

  async function onSubmit(values: OrderForm) {
    setError(null);
    try {
      const order = await amenityApi.order(values);
      onDone(`Đã gửi yêu cầu dịch vụ. Mã đơn ${order.id.slice(0, 8)}.`);
      await queryClient.invalidateQueries({ queryKey: ["amenity-orders"] });
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  return (
    <Modal open={!!amenity} title={`Gọi dịch vụ ${amenity?.name || ""}`} onClose={onClose}>
      <form className="grid" onSubmit={handleSubmit(onSubmit)}>
        {activeBookings.isLoading ? <LoadingState label="Đang lấy danh sách phòng đang lưu trú..." /> : null}
        {activeBookings.isError ? <ErrorState message={extractErrorMessage(activeBookings.error)} /> : null}

        <div className="field">
          <label>Phòng đang lưu trú</label>
          <select className="select" {...register("roomId")} disabled={activeStays.length === 0}>
            {activeStays.length === 0 ? <option value="">Không có phòng đang lưu trú</option> : null}
            {activeStays.map((booking) => (
              <option key={booking.id} value={booking.roomId}>
                Phòng {roomNameById.get(booking.roomId) || booking.roomId.slice(0, 8)} — mã đặt phòng {booking.id.slice(0, 8)} — nhận phòng {dateTime(booking.checkInDate)}
              </option>
            ))}
          </select>
          {errors.roomId ? <span className="field-error">{errors.roomId.message}</span> : null}
        </div>

        <div className="field">
          <label>Số lượng</label>
          <input className="input" type="number" min={1} {...register("quantity")} />
          {errors.quantity ? <span className="field-error">{errors.quantity.message}</span> : null}
        </div>

        {activeStays.length === 0 ? <div className="toast">Chỉ có thể gọi dịch vụ cho phòng đã nhận khách.</div> : null}
        {error ? <ErrorState message={error} /> : null}
        <Button disabled={isSubmitting || activeStays.length === 0}>{isSubmitting ? "Đang gửi..." : "Gọi dịch vụ"}</Button>
      </form>
    </Modal>
  );
}
