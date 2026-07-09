"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { authApi, bookingApi, roomApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { dateTime, money } from "@/lib/format";
import type { BookingEntityRequest, RoomResponse } from "@/types/api";

const schema = z
  .object({
    customerId: z.string().trim().optional().default(""),
    username: z.string().trim().optional().default(""),
    password: z.string().trim().optional().default(""),
    fullName: z.string().trim().optional().default(""),
    email: z.string().trim().optional().default(""),
    roomId: z.string().uuid("Chọn phòng"),
    checkOutDate: z.string().min(1, "Chọn ngày trả phòng")
  })
  .superRefine((value, ctx) => {
    const todayStr = new Date().toISOString().slice(0, 10);
    if (value.checkOutDate <= todayStr) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["checkOutDate"], message: "Ngày trả phòng phải sau ngày hôm nay" });
    }

    if (value.customerId) {
      const parsed = z.string().uuid().safeParse(value.customerId);
      if (!parsed.success) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["customerId"], message: "Mã khách hàng không hợp lệ" });
      }
      return;
    }

    if (!value.fullName) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["fullName"], message: "Nhập họ tên khách mới" });
    }
    if (!value.username) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["username"], message: "Nhập username để tạo khách mới" });
    }
    if (!value.password || value.password.length < 6) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["password"], message: "Mật khẩu tạm tối thiểu 6 ký tự" });
    }
    if (value.email && !z.string().email().safeParse(value.email).success) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["email"], message: "Email không hợp lệ" });
    }
  });

type WalkInForm = z.infer<typeof schema>;

function localDateTimeNow() {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60_000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 19);
}

export default function WalkInPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST"]}>
      <WalkInContent />
    </ProtectedPage>
  );
}

function WalkInContent() {
  const queryClient = useQueryClient();
  const [created, setCreated] = useState<string | null>(null);
  const rooms = useQuery({ queryKey: ["rooms", "available"], queryFn: roomApi.getAvailable });
  const availableRooms = useMemo(() => rooms.data || [], [rooms.data]);

  const {
    register,
    handleSubmit,
    control,
    setValue,
    formState: { errors }
  } = useForm<WalkInForm>({
    resolver: zodResolver(schema),
    defaultValues: {
      roomId: "",
      customerId: "",
      username: "",
      password: "guest123",
      fullName: "",
      email: "",
      checkOutDate: new Date(Date.now() + 86400000).toISOString().slice(0, 10)
    }
  });

  const mutation = useMutation({
    mutationFn: async (values: WalkInForm) => {
      let customerId = values.customerId?.trim();
      let createdCustomerName = "";

      if (!customerId) {
        const createdCustomer = await authApi.register({
          username: values.username.trim(),
          password: values.password,
          fullName: values.fullName.trim(),
          email: values.email?.trim() || undefined
        });
        customerId = createdCustomer.id;
        createdCustomerName = createdCustomer.fullName || createdCustomer.username;
      }

      const body: BookingEntityRequest = {
        customerId,
        roomId: values.roomId,
        checkInDate: localDateTimeNow(),
        checkOutDate: `${values.checkOutDate}T12:00:00`,
        status: "CHECKED_IN",
        totalAmount: 0,
        depositAmount: 0,
        isDepositPaid: true
      };
      const booking = await bookingApi.walkIn(body);
      return { booking, createdCustomerName };
    },
    onSuccess: async ({ booking, createdCustomerName }) => {
      setCreated(
        `Đã nhận khách${createdCustomerName ? ` cho ${createdCustomerName}` : ""}. Mã đặt phòng ${booking.id.slice(0, 8)}, tổng ${money(booking.totalAmount)}.`
      );
      await queryClient.invalidateQueries({ queryKey: ["bookings"] });
      await queryClient.invalidateQueries({ queryKey: ["rooms"] });
    }
  });

  const selectedRoomId = useWatch({ control, name: "roomId" });

  return (
    <>
      <PageHeading
        title="Nhận khách tại quầy"
        description="Chọn phòng trống, chọn khách có sẵn hoặc tạo nhanh hồ sơ khách mới rồi nhận phòng."
        action={<Link className="button secondary" href="/bookings">Về đặt phòng</Link>}
      />

      <div className="grid cols-2">
        <Card>
          <CardHeader
            title="Thông tin nhận khách"
            description="Nếu khách chưa có hồ sơ, hãy tạo nhanh thông tin khách rồi nhận phòng."
          />
          <form className="grid" onSubmit={handleSubmit((values) => mutation.mutate(values))}>
            <div className="field">
              <label>Mã khách hàng có sẵn (tuỳ chọn)</label>
              <input className="input" placeholder="Để trống nếu muốn tạo khách mới" {...register("customerId")} />
              {errors.customerId ? <span className="field-error">{errors.customerId.message}</span> : null}
            </div>

            <div className="state" style={{ textAlign: "left", padding: 14 }}>
              <strong>Tạo khách mới nhanh</strong>
              <p className="muted" style={{ margin: "6px 0 0" }}>
                Dùng khi khách chưa có hồ sơ trong hệ thống.
              </p>
            </div>

            <div className="grid cols-2">
              <div className="field">
                <label>Họ tên khách</label>
                <input className="input" placeholder="Nguyễn Văn A" {...register("fullName")} />
                {errors.fullName ? <span className="field-error">{errors.fullName.message}</span> : null}
              </div>
              <div className="field">
                <label>Email</label>
                <input className="input" type="email" placeholder="guest@example.com" {...register("email")} />
                {errors.email ? <span className="field-error">{errors.email.message}</span> : null}
              </div>
              <div className="field">
                <label>Tên đăng nhập</label>
                <input className="input" placeholder="guest_001" {...register("username")} />
                {errors.username ? <span className="field-error">{errors.username.message}</span> : null}
              </div>
              <div className="field">
                <label>Mật khẩu tạm</label>
                <input className="input" type="password" {...register("password")} />
                {errors.password ? <span className="field-error">{errors.password.message}</span> : null}
              </div>
            </div>

            <div className="field">
              <label>Phòng</label>
              <select className="select" {...register("roomId")}>
                <option value="">Chọn phòng trống</option>
                {availableRooms.map((room) => (
                  <option key={room.id} value={room.id}>
                    {room.roomNumber} — {room.type} — {money(room.price)}
                  </option>
                ))}
              </select>
              {errors.roomId ? <span className="field-error">{errors.roomId.message}</span> : null}
            </div>

            <div className="field">
              <label>Ngày trả phòng</label>
              <input className="input" type="date" {...register("checkOutDate")} />
              {errors.checkOutDate ? <span className="field-error">{errors.checkOutDate.message}</span> : null}
            </div>

            {mutation.isError ? <ErrorState message={extractErrorMessage(mutation.error)} /> : null}
            {created ? <div className="toast">{created}</div> : null}
            <Button type="submit" disabled={mutation.isPending || rooms.isLoading}>
              {mutation.isPending ? "Đang nhận khách..." : "Nhận khách"}
            </Button>
          </form>
        </Card>

        <Card>
          <CardHeader title="Phòng đang trống hiện tại" description="Danh sách phòng sẵn sàng nhận khách." />
          {rooms.isLoading ? <LoadingState /> : null}
          {rooms.isError ? <ErrorState message={extractErrorMessage(rooms.error)} /> : null}
          <DataTable
            rows={availableRooms}
            getRowKey={(room) => room.id}
            searchableText={(room) => `${room.roomNumber} ${room.type}`}
            columns={[
              { key: "room", header: "Phòng", render: (room: RoomResponse) => <strong>{room.roomNumber}</strong> },
              { key: "type", header: "Loại", render: (room) => room.type },
              { key: "price", header: "Giá", render: (room) => money(room.price) },
              { key: "status", header: "Trạng thái", render: (room) => <StatusBadge status={room.status} /> },
              {
                key: "action",
                header: "Chọn",
                render: (room) => (
                  <Button
                    type="button"
                    variant={selectedRoomId === room.id ? "success" : "secondary"}
                    onClick={() => setValue("roomId", room.id, { shouldValidate: true })}
                  >
                    {selectedRoomId === room.id ? "Đã chọn" : "Chọn"}
                  </Button>
                )
              }
            ]}
          />
        </Card>
      </div>

      {created ? (
        <Card style={{ marginTop: 18 }}>
          <CardHeader
            title="Bước tiếp theo"
            description={`Mã đặt phòng được tạo lúc ${dateTime(new Date().toISOString())}. Khi trả phòng, hoá đơn sẽ được chuẩn bị để thanh toán.`}
          />
        </Card>
      ) : null}
    </>
  );
}
