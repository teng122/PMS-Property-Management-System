import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type { BookingStatus, RoomStatus } from "@/types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(value: number | undefined | null): string {
  if (value == null) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatDate(value: string | undefined): string {
  if (!value) return "-";
  try {
    return new Date(value).toLocaleDateString("vi-VN");
  } catch {
    return value;
  }
}

/** Số đêm giữa 2 ngày (tối thiểu 1) — khớp cách tính của billing-service. */
export function nightsBetween(checkIn: string, checkOut: string): number {
  const a = new Date(checkIn).getTime();
  const b = new Date(checkOut).getTime();
  const nights = Math.round((b - a) / (1000 * 60 * 60 * 24));
  return nights < 1 ? 1 : nights;
}

type StatusStyle = { label: string; className: string; dot: string };

export const ROOM_STATUS_STYLE: Record<RoomStatus, StatusStyle> = {
  AVAILABLE: { label: "Trống", className: "bg-emerald-50 text-emerald-700 border-emerald-200", dot: "bg-emerald-500" },
  OCCUPIED: { label: "Đang ở", className: "bg-rose-50 text-rose-700 border-rose-200", dot: "bg-rose-500" },
  DIRTY: { label: "Bẩn", className: "bg-orange-50 text-orange-700 border-orange-200", dot: "bg-orange-500" },
  CLEANING: { label: "Đang dọn", className: "bg-amber-50 text-amber-700 border-amber-200", dot: "bg-amber-500" },
};

export const BOOKING_STATUS_STYLE: Record<BookingStatus, StatusStyle> = {
  PENDING_PAYMENT: { label: "Chờ thanh toán", className: "bg-amber-50 text-amber-700 border-amber-200", dot: "bg-amber-500" },
  CONFIRMED: { label: "Đã xác nhận", className: "bg-blue-50 text-blue-700 border-blue-200", dot: "bg-blue-500" },
  CHECKED_IN: { label: "Đã nhận phòng", className: "bg-indigo-50 text-indigo-700 border-indigo-200", dot: "bg-indigo-500" },
  CHECKED_OUT: { label: "Đã trả phòng", className: "bg-slate-100 text-slate-600 border-slate-200", dot: "bg-slate-400" },
  CANCELLED: { label: "Đã hủy", className: "bg-rose-50 text-rose-700 border-rose-200", dot: "bg-rose-500" },
};

/** Trích message lỗi thân thiện từ AxiosError. */
export function errorMessage(err: unknown, fallback = "Đã có lỗi xảy ra"): string {
  const anyErr = err as { response?: { data?: { message?: string } }; message?: string };
  return anyErr?.response?.data?.message ?? anyErr?.message ?? fallback;
}
