"use client";

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
import { StatusBadge, statusLabel } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { extractErrorMessage } from "@/lib/api/error";
import { roomApi } from "@/lib/api/services";
import { useAuthStore } from "@/stores/auth-store";
import type { RoomCreateRequest, RoomResponse, RoomStatus } from "@/types/api";

const statuses: RoomStatus[] = ["AVAILABLE", "OCCUPIED", "DIRTY", "CLEANING", "MAINTENANCE"];
const roomTypes = ["SINGLE", "DOUBLE", "SUITE"];

const roomSchema = z.object({
  roomNumber: z.string().min(1, "Nhập số phòng"),
  type: z.string().min(1, "Chọn loại phòng"),
  price: z.coerce.number().positive("Giá phải lớn hơn 0"),
  status: z.enum(["AVAILABLE", "OCCUPIED", "DIRTY", "CLEANING", "MAINTENANCE"])
});

type RoomForm = z.infer<typeof roomSchema>;

export default function RoomsPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST"]}>
      <RoomsContent />
    </ProtectedPage>
  );
}

function RoomsContent() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState<RoomResponse | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const rooms = useQuery({
    queryKey: ["rooms"],
    queryFn: roomApi.getAll
  });

  const statusCounts = useMemo(() => {
    const counts = new Map<RoomStatus, number>();
    for (const status of statuses) counts.set(status, 0);
    for (const room of rooms.data || []) counts.set(room.status, (counts.get(room.status) || 0) + 1);
    return counts;
  }, [rooms.data]);

  const deleteMutation = useMutation({
    mutationFn: roomApi.remove,
    onSuccess: async () => {
      setMessage("Đã xoá phòng.");
      await queryClient.invalidateQueries({ queryKey: ["rooms"] });
    }
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: RoomStatus }) => roomApi.updateStatus(id, status),
    onSuccess: async () => {
      setMessage("Đã cập nhật trạng thái phòng.");
      await queryClient.invalidateQueries({ queryKey: ["rooms"] });
    }
  });

  function openCreate() {
    setEditing(null);
    setModalOpen(true);
  }

  function openEdit(room: RoomResponse) {
    setEditing(room);
    setModalOpen(true);
  }

  return (
    <>
      <PageHeading
        title="Kho phòng"
        description="Quản lý phòng, giá phòng và trạng thái sẵn sàng phục vụ khách."
        action={user?.role === "ADMIN" ? <Button onClick={openCreate}>Thêm phòng</Button> : null}
      />

      <div className="grid cols-5" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))", marginBottom: 18 }}>
        {statuses.map((status) => (
          <Card key={status}>
            <StatusBadge status={status} />
            <div style={{ fontSize: 28, fontWeight: 900, marginTop: 10 }}>{statusCounts.get(status) || 0}</div>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader title="Danh sách phòng" description="Theo dõi và cập nhật tình trạng phòng trong khách sạn." />
        {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
        {rooms.isLoading ? <LoadingState /> : null}
        {rooms.isError ? <ErrorState message={extractErrorMessage(rooms.error)} /> : null}
        <DataTable
          rows={rooms.data || []}
          getRowKey={(room) => room.id}
          searchableText={(room) => `${room.roomNumber} ${room.type} ${room.status}`}
          columns={[
            { key: "room", header: "Phòng", render: (room) => <strong>{room.roomNumber}</strong> },
            { key: "type", header: "Loại", render: (room) => room.type },
            { key: "price", header: "Giá", render: (room) => `$${Number(room.price || 0).toFixed(2)}` },
            { key: "status", header: "Trạng thái", render: (room) => <StatusBadge status={room.status} /> },
            {
              key: "change",
              header: "Đổi trạng thái",
              render: (room) => (
                <select
                  className="select"
                  value={room.status}
                  disabled={statusMutation.isPending}
                  onChange={(event) => statusMutation.mutate({ id: room.id, status: event.target.value as RoomStatus })}
                >
                  {statuses.map((status) => (
                    <option key={status} value={status}>{statusLabel(status)}</option>
                  ))}
                </select>
              )
            },
            {
              key: "actions",
              header: "Thao tác",
              render: (room) => (
                <div className="actions">
                  {user?.role === "ADMIN" ? (
                    <>
                      <Button variant="secondary" onClick={() => openEdit(room)}>Sửa</Button>
                      <Button
                        variant="danger"
                        disabled={deleteMutation.isPending}
                        onClick={() => {
                          if (confirm(`Xoá phòng ${room.roomNumber}?`)) deleteMutation.mutate(room.id);
                        }}
                      >
                        Xoá
                      </Button>
                    </>
                  ) : (
                    <span className="muted">Chỉ quản trị được sửa/xoá</span>
                  )}
                </div>
              )
            }
          ]}
        />
      </Card>

      <RoomModal
        open={modalOpen}
        room={editing}
        onClose={() => setModalOpen(false)}
        onSaved={async () => {
          setModalOpen(false);
          setMessage(editing ? "Đã cập nhật phòng." : "Đã tạo phòng.");
          await queryClient.invalidateQueries({ queryKey: ["rooms"] });
        }}
      />
    </>
  );
}

function RoomModal({
  open,
  room,
  onClose,
  onSaved
}: {
  open: boolean;
  room: RoomResponse | null;
  onClose: () => void;
  onSaved: () => Promise<void>;
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm<RoomForm>({
    resolver: zodResolver(roomSchema),
    values: {
      roomNumber: room?.roomNumber || "",
      type: room?.type || "SINGLE",
      price: Number(room?.price || 100),
      status: room?.status || "AVAILABLE"
    }
  });
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(values: RoomForm) {
    setError(null);
    const body: RoomCreateRequest = values;
    try {
      if (room) {
        await roomApi.update(room.id, body);
      } else {
        await roomApi.create(body);
      }
      reset();
      await onSaved();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  return (
    <Modal open={open} title={room ? `Sửa phòng ${room.roomNumber}` : "Thêm phòng"} onClose={onClose}>
      <form className="grid cols-2" onSubmit={handleSubmit(onSubmit)}>
        <div className="field">
          <label>Số phòng</label>
          <input className="input" {...register("roomNumber")} />
          {errors.roomNumber ? <span className="field-error">{errors.roomNumber.message}</span> : null}
        </div>
        <div className="field">
          <label>Loại phòng</label>
          <select className="select" {...register("type")}>
            {roomTypes.map((type) => <option key={type} value={type}>{type}</option>)}
          </select>
          {errors.type ? <span className="field-error">{errors.type.message}</span> : null}
        </div>
        <div className="field">
          <label>Giá</label>
          <input className="input" type="number" step="0.01" {...register("price")} />
          {errors.price ? <span className="field-error">{errors.price.message}</span> : null}
        </div>
        <div className="field">
          <label>Trạng thái</label>
          <select className="select" {...register("status")}>
            {statuses.map((status) => <option key={status} value={status}>{statusLabel(status)}</option>)}
          </select>
          {errors.status ? <span className="field-error">{errors.status.message}</span> : null}
        </div>
        {error ? <div className="toast" style={{ gridColumn: "1 / -1", borderColor: "#fecaca", color: "#991b1b", background: "#fef2f2" }}>{error}</div> : null}
        <div className="actions" style={{ gridColumn: "1 / -1" }}>
          <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Đang lưu..." : "Lưu"}</Button>
          <Button type="button" variant="ghost" onClick={onClose}>Huỷ</Button>
        </div>
      </form>
    </Modal>
  );
}
