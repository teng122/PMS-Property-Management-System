"use client";

import { authClient, apiDelete, apiGet, apiPost, apiPut } from "@/lib/api/client";
import type {
  AmenityCreateRequest,
  AmenityOrderCreateRequest,
  AmenityOrderResponse,
  AmenityOrderStatus,
  AmenityResponse,
  BookingEntityRequest,
  BookingRequest,
  BookingResponse,
  CleaningTaskResponse,
  DirtyRoomResponse,
  InvoiceResponse,
  LoginRequest,
  PaymentInitResponse,
  PreCheckoutSummaryResponse,
  RegisterRequest,
  RevenueStatsResponse,
  RoomCreateRequest,
  RoomResponse,
  RoomSearchResult,
  RoomStatus,
  SessionUser,
  UserResponse,
  UUID
} from "@/types/api";

export const authApi = {
  async login(body: LoginRequest) {
    const { data } = await authClient.post<{ user: SessionUser | null; username: string; role: string }>(
      "/login",
      body
    );
    return data;
  },
  async register(body: RegisterRequest) {
    const { data } = await authClient.post<UserResponse>("/register", body);
    return data;
  },
  async logout() {
    const { data } = await authClient.post<{ ok: boolean }>("/logout");
    return data;
  },
  async session() {
    const { data } = await authClient.get<{ user: SessionUser | null }>("/session");
    return data.user;
  }
};

export const identityApi = {
  getMe: (id: UUID) => apiGet<UserResponse>(`/identity-service/api/auth/users/${id}`),
  getUsers: () => apiGet<UserResponse[]>("/identity-service/api/users"),
  createUser: (body: RegisterRequest) => apiPost<UserResponse, RegisterRequest>("/identity-service/api/users", body),
  updateRole: (id: UUID, role: string) =>
    apiPut<UserResponse>(`/identity-service/api/users/${id}/role`, undefined, { params: { role } }),
  toggleBlock: (id: UUID) => apiPut<UserResponse>(`/identity-service/api/users/${id}/block`)
};

export const roomApi = {
  getAvailable: () => apiGet<RoomResponse[]>("/room-service/api/rooms"),
  getAll: () => apiGet<RoomResponse[]>("/room-service/api/rooms/all"),
  getById: (id: UUID) => apiGet<RoomResponse>(`/room-service/api/rooms/${id}`),
  create: (body: RoomCreateRequest) => apiPost<RoomResponse, RoomCreateRequest>("/room-service/api/rooms", body),
  update: (id: UUID, body: RoomCreateRequest) =>
    apiPut<RoomResponse, RoomCreateRequest>(`/room-service/api/rooms/${id}`, body),
  updateStatus: (id: UUID, status: RoomStatus) =>
    apiPut<RoomResponse, { status: RoomStatus }>(`/room-service/api/rooms/${id}/status`, { status }),
  remove: (id: UUID) => apiDelete<string>(`/room-service/api/rooms/${id}`)
};

export const bookingApi = {
  searchAvailableRooms: (checkIn: string, checkOut: string) =>
    apiGet<RoomSearchResult[]>("/booking-service/api/bookings/search-available-rooms", {
      params: { checkIn, checkOut }
    }),
  createOnline: (body: BookingRequest) =>
    apiPost<BookingResponse, BookingRequest>("/booking-service/api/bookings", body),
  payDeposit: (id: UUID, amount: number) =>
    apiPost<BookingResponse>(`/booking-service/api/bookings/${id}/pay-deposit`, undefined, {
      params: { amount }
    }),
  walkIn: (body: BookingEntityRequest) =>
    apiPost<BookingResponse, BookingEntityRequest>("/booking-service/api/bookings/walk-in", body),
  checkIn: (id: UUID) => apiPost<BookingResponse>(`/booking-service/api/bookings/${id}/check-in`),
  noShow: (id: UUID) => apiPost<BookingResponse>(`/booking-service/api/bookings/${id}/no-show`),
  preCheckout: (id: UUID) =>
    apiGet<PreCheckoutSummaryResponse>(`/booking-service/api/bookings/${id}/pre-checkout-summary`),
  checkOut: (id: UUID) => apiPost<BookingResponse>(`/booking-service/api/bookings/${id}/check-out`),
  myBookings: () => apiGet<BookingResponse[]>("/booking-service/api/bookings/my-bookings"),
  getAll: () => apiGet<BookingResponse[]>("/booking-service/api/bookings"),
  getById: (id: UUID) => apiGet<BookingResponse>(`/booking-service/api/bookings/${id}`),
  update: (id: UUID, body: BookingEntityRequest) =>
    apiPut<BookingResponse, BookingEntityRequest>(`/booking-service/api/bookings/${id}`, body),
  remove: (id: UUID) => apiDelete<string>(`/booking-service/api/bookings/${id}`),
  activeByRoom: (roomId: UUID) => apiGet<BookingResponse>(`/booking-service/api/bookings/active/room/${roomId}`)
};

export const amenityApi = {
  getAll: () => apiGet<AmenityResponse[]>("/amenities-service/api/amenities"),
  getById: (id: UUID) => apiGet<AmenityResponse>(`/amenities-service/api/amenities/${id}`),
  create: (body: AmenityCreateRequest) =>
    apiPost<AmenityResponse, AmenityCreateRequest>("/amenities-service/api/amenities", body),
  order: (body: AmenityOrderCreateRequest) =>
    apiPost<AmenityOrderResponse, AmenityOrderCreateRequest>("/amenities-service/api/amenities/order", body),
  orders: (status?: AmenityOrderStatus | string) =>
    apiGet<AmenityOrderResponse[]>("/amenities-service/api/amenities/orders", {
      params: status ? { status } : undefined
    }),
  updateOrderStatus: (id: UUID, status: AmenityOrderStatus) =>
    apiPut<AmenityOrderResponse>(`/amenities-service/api/amenities/orders/${id}/status`, undefined, {
      params: { status }
    }),
  unpaidByRoom: (roomId: UUID) =>
    apiGet<AmenityOrderResponse[]>(`/amenities-service/api/amenities/room/${roomId}/unpaid`),
  unpaidByBooking: (bookingId: UUID) =>
    apiGet<AmenityOrderResponse[]>(`/amenities-service/api/amenities/booking/${bookingId}/unpaid`),
  unpaidCharge: (bookingId: UUID) =>
    apiGet<number>(`/amenities-service/api/amenities/orders/booking/${bookingId}/unpaid-charge`)
};

export const housekeepingApi = {
  dirtyRooms: () => apiGet<DirtyRoomResponse[]>("/housekeeping-service/api/housekeeping/dirty-rooms"),
  tasks: (params?: { status?: string; staffId?: UUID }) =>
    apiGet<CleaningTaskResponse[]>("/housekeeping-service/api/housekeeping/tasks", { params }),
  start: (id: UUID) => apiPost<CleaningTaskResponse>(`/housekeeping-service/api/housekeeping/tasks/${id}/start`),
  complete: (id: UUID) => apiPost<CleaningTaskResponse>(`/housekeeping-service/api/housekeeping/tasks/${id}/complete`)
};

export const billingApi = {
  getAll: () => apiGet<InvoiceResponse[]>("/billing-service/api/invoices"),
  getById: (id: UUID) => apiGet<InvoiceResponse>(`/billing-service/api/invoices/${id}`),
  getByBooking: (bookingId: UUID) => apiGet<InvoiceResponse>(`/billing-service/api/invoices/booking/${bookingId}`),
  pay: (id: UUID) => apiPost<PaymentInitResponse>(`/billing-service/api/invoices/${id}/pay`),
  confirm: (id: UUID) => apiPost<InvoiceResponse>(`/billing-service/api/invoices/${id}/confirm-payment`),
  stats: () => apiGet<RevenueStatsResponse>("/billing-service/api/invoices/stats")
};
