import { apiClient } from "./client";
import type { Booking, BookingRequest, BookingStatus } from "@/types";

const BASE = "/booking-service/api/bookings";

export const bookingApi = {
  reserve: (data: BookingRequest) => apiClient.post<Booking>(`${BASE}/reserve`, data),
  getAll: () => apiClient.get<Booking[]>(BASE),
  getById: (id: string) => apiClient.get<Booking>(`${BASE}/${id}`),
  /**
   * Không có endpoint check-in/out riêng → PUT toàn bộ booking với status mới.
   * Backend đọc updatedData.getStatus(). Xem FRONTEND_PLAN.md §10.3.
   */
  updateStatus: (booking: Booking, status: BookingStatus) =>
    apiClient.put<Booking>(`${BASE}/${booking.id}`, { ...booking, status }),
  remove: (id: string) => apiClient.delete(`${BASE}/${id}`),
};
