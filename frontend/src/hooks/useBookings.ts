"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bookingApi } from "@/lib/api/bookings";
import { roomApi } from "@/lib/api/rooms";
import type { Booking, BookingRequest } from "@/types";

export function useBookings() {
  return useQuery({
    queryKey: ["bookings"],
    queryFn: () => bookingApi.getAll().then((r) => r.data),
    refetchInterval: 30_000,
  });
}

export function useBooking(id: string | undefined) {
  return useQuery({
    queryKey: ["bookings", id],
    queryFn: () => bookingApi.getById(id as string).then((r) => r.data),
    enabled: !!id,
  });
}

export function useReserveBooking() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BookingRequest) => bookingApi.reserve(data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["bookings"] }),
  });
}

/**
 * ORCHESTRATION — Check-in.
 * Backend không có endpoint check-in, không tự đổi trạng thái phòng.
 * FE phải: PUT booking = CHECKED_IN  +  PUT room = OCCUPIED.
 */
export function useCheckIn() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (booking: Booking) => {
      await bookingApi.updateStatus(booking, "CHECKED_IN");
      await roomApi.updateStatus(booking.roomId, "OCCUPIED");
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bookings"] });
      qc.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}
