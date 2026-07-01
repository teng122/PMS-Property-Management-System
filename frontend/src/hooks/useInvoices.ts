"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { invoiceApi } from "@/lib/api/invoices";
import { bookingApi } from "@/lib/api/bookings";
import { roomApi } from "@/lib/api/rooms";
import type { Booking } from "@/types";

export function useInvoice(id: string | undefined) {
  return useQuery({
    queryKey: ["invoices", id],
    queryFn: () => invoiceApi.getById(id as string).then((r) => r.data),
    enabled: !!id,
  });
}

/** Tạo hóa đơn từ bookingId (gộp tiền phòng + dịch vụ + VAT). */
export function useGenerateInvoice() {
  return useMutation({
    mutationFn: (bookingId: string) => invoiceApi.generate(bookingId).then((r) => r.data),
  });
}

export function useInitPayment() {
  return useMutation({
    mutationFn: (invoiceId: string) => invoiceApi.initPayment(invoiceId).then((r) => r.data),
  });
}

/**
 * ORCHESTRATION — Xác nhận thanh toán khi ĐẶT PHÒNG.
 * Backend confirm-payment CHỈ set invoice = PAID, KHÔNG cập nhật booking.
 * Nếu không PUT booking = CONFIRMED, scheduler sẽ tự HỦY sau 15'.
 * Xem FRONTEND_PLAN.md §10 (orchestration).
 */
export function useConfirmBookingPayment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ invoiceId, booking }: { invoiceId: string; booking: Booking }) => {
      await invoiceApi.confirmPayment(invoiceId);
      await bookingApi.updateStatus(booking, "CONFIRMED");
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["bookings"] }),
  });
}

/**
 * ORCHESTRATION — Check-out hoàn tất.
 * FE phải: confirm-payment hóa đơn + PUT booking = CHECKED_OUT + PUT room = DIRTY.
 * (Việc tạo cleaning_task cho lao công hiện backend chưa hỗ trợ — xem FRONTEND_PLAN.md §10.)
 */
export function useCompleteCheckout() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({
      invoiceId,
      booking,
    }: {
      invoiceId: string;
      booking: Booking;
    }) => {
      await invoiceApi.confirmPayment(invoiceId);
      await bookingApi.updateStatus(booking, "CHECKED_OUT");
      await roomApi.updateStatus(booking.roomId, "DIRTY");
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bookings"] });
      qc.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}
