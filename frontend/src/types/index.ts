// ------- Auth (identity-service) -------
// 4 vai trò: Quản lý / Lễ tân / Lao công / Khách hàng.
export type Role = "ADMIN" | "RECEPTIONIST" | "HOUSEKEEPER" | "CUSTOMER";

export const ROLE_LABEL: Record<Role, string> = {
  ADMIN: "Quản lý",
  RECEPTIONIST: "Lễ tân",
  HOUSEKEEPER: "Lao công",
  CUSTOMER: "Khách hàng",
};

export interface AuthResponse {
  token: string;
  refreshToken: string;
  username: string;
  role: Role;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  email: string;
  role: Role;
}

export interface AuthUser {
  username: string;
  role: Role;
}

// ------- Room (room-service) -------
export type RoomStatus = "AVAILABLE" | "OCCUPIED" | "DIRTY" | "CLEANING";
export type RoomType = "SINGLE" | "DOUBLE" | "VIP";

export interface Room {
  id: string;
  roomNumber: string;
  type: RoomType;
  price: number;
  status: RoomStatus;
}

// ------- Booking (booking-service) -------
export type BookingStatus =
  | "PENDING_PAYMENT"
  | "CONFIRMED"
  | "CHECKED_IN"
  | "CHECKED_OUT"
  | "CANCELLED";

export interface Booking {
  id: string;
  roomId: string;
  customerName: string;
  checkInDate: string; // ISO date yyyy-MM-dd
  checkOutDate: string;
  status: BookingStatus;
  createdAt?: string;
}

export interface BookingRequest {
  roomId: string;
  customerName: string;
  checkInDate: string;
  checkOutDate: string;
}

// ------- Amenities (amenities-service) -------
export type AmenityType = "FOOD" | "LAUNDRY" | "SPA";
export type OrderStatus = "PENDING" | "DELIVERED" | "BILLED" | "CANCELLED";

export interface Amenity {
  id: string;
  name: string;
  price: number;
  type: AmenityType;
}

export interface AmenityCreateRequest {
  name: string;
  price: number;
  type: AmenityType;
}

export interface AmenityOrderCreateRequest {
  roomId: string;
  bookingId: string;
  amenityId: string;
  quantity: number;
}

export interface AmenityOrder {
  id: string;
  roomId: string;
  bookingId: string;
  amenityName: string;
  amenityPrice: number;
  quantity: number;
  totalPrice: number;
  status: OrderStatus;
}

// ------- Housekeeping (housekeeping-service) -------
export type CleaningTaskStatus = "ASSIGNED" | "IN_PROGRESS" | "COMPLETED";

export interface DirtyRoom {
  id: string; // task id
  roomId: string;
  staffId: string;
  status: CleaningTaskStatus;
}

export interface CleaningTask {
  id: string;
  roomId: string;
  staffId: string;
  status: CleaningTaskStatus;
  updatedAt: string;
}

// ------- Billing (finance-billing-service) -------
export type InvoiceStatus = "UNPAID" | "PAID";

export interface Invoice {
  id: string;
  bookingId: string;
  roomCharge: number;
  serviceCharge: number;
  tax: number;
  totalAmount: number;
  status: InvoiceStatus;
  createdAt?: string;
  paidAt?: string;
}

export interface PaymentInitResponse {
  qrImageUrl: string;
  amount: number;
  state: string;
}
