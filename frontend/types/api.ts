export type UUID = string;

export type Role = "ADMIN" | "RECEPTIONIST" | "STAFF" | "CUSTOMER";
export type ApiRole = `ROLE_${Role}`;

export type UserStatus = "ACTIVE" | "BLOCKED";

export type BookingStatus =
  | "PENDING"
  | "AWAITING_DEPOSIT"
  | "CONFIRMED"
  | "CHECKED_IN"
  | "CHECKED_OUT"
  | "CANCELLED"
  | "NO_SHOW";

export type RoomStatus = "AVAILABLE" | "OCCUPIED" | "DIRTY" | "CLEANING" | "MAINTENANCE";
export type RoomType = "SINGLE" | "DOUBLE" | "SUITE" | string;
export type AmenityType = "FOOD" | "LAUNDRY" | "SPA" | string;
export type AmenityOrderStatus = "PENDING" | "PREPARING" | "DELIVERED" | "BILLED" | "REJECTED";
export type CleaningTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";
export type InvoiceStatus = "UNPAID" | "PAID";

export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: ApiRole;
  refreshToken: string;
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  email?: string;
  role?: Role | ApiRole;
}

export interface UserResponse {
  id: UUID;
  username: string;
  fullName?: string;
  email?: string;
  role: ApiRole;
  status?: UserStatus;
}

export interface SessionUser {
  id: UUID;
  username: string;
  role: Role;
  apiRole: ApiRole;
}

export interface RoomResponse {
  id: UUID;
  roomNumber: string;
  type: RoomType;
  price: number;
  status: RoomStatus;
}

export interface RoomCreateRequest {
  roomNumber: string;
  type: RoomType;
  price: number;
  status?: RoomStatus;
  hotelId?: UUID;
}

export interface RoomStatusUpdateRequest {
  status: RoomStatus;
}

export interface RoomSearchResult {
  id: UUID;
  roomNumber: string;
  roomType: RoomType;
  status: RoomStatus;
  price: number;
  reservedBookingId?: UUID | null;
}

export interface BookingRequest {
  roomId: UUID;
  customerName?: string;
  checkInDate: string;
  checkOutDate: string;
}

export interface BookingEntityRequest {
  id?: UUID;
  customerId: UUID;
  roomId: UUID;
  checkInDate: string;
  checkOutDate: string;
  status: BookingStatus;
  totalAmount: number;
  depositAmount: number;
  isDepositPaid: boolean;
  version?: number;
}

export interface BookingResponse {
  id: UUID;
  customerId: UUID;
  roomId: UUID;
  checkInDate: string;
  checkOutDate: string;
  status: BookingStatus;
  totalAmount: number;
  depositAmount: number;
  isDepositPaid: boolean;
}

export interface InvoiceResponse {
  id: UUID;
  bookingId: UUID;
  roomCharge: number;
  serviceCharge: number;
  tax: number;
  depositAmount: number;
  totalAmount: number;
  status: InvoiceStatus;
}

export interface PreCheckoutSummaryResponse {
  bookingId: UUID;
  customerName: string;
  roomNumber: string;
  invoice: InvoiceResponse | null;
  paymentStatus: string;
}

export interface AmenityCreateRequest {
  name: string;
  price: number;
  type: AmenityType;
  isReturnable?: boolean;
}

export interface AmenityResponse {
  id: UUID;
  name: string;
  price: number;
  type: AmenityType;
  isReturnable: boolean;
}

export interface AmenityOrderCreateRequest {
  roomId: UUID;
  amenityId: UUID;
  quantity: number;
}

export interface AmenityOrderResponse {
  id: UUID;
  roomId: UUID;
  bookingId: UUID;
  amenityName?: string;
  amenityPrice?: number;
  quantity?: number;
  totalPrice: number;
  status: AmenityOrderStatus;
}

export interface DirtyRoomResponse {
  id: UUID;
  roomId: UUID;
  roomNumber?: string | null;
  staffId?: UUID | null;
}

export interface CleaningTaskResponse {
  id: UUID;
  roomId: UUID;
  roomNumber?: string | null;
  staffId?: UUID | null;
  status: CleaningTaskStatus;
  updatedAt?: string | null;
}

export interface PaymentInitResponse {
  qrImageUrl: string;
  amount: number;
  state: string;
}

export interface RevenueStatsResponse {
  totalInvoices: number;
  paidInvoices: number;
  unpaidInvoices: number;
  totalRoomRevenue: number;
  totalServiceRevenue: number;
  totalTax: number;
  totalRevenue: number;
}

export type PaginatedClientState = {
  page: number;
  pageSize: number;
  search: string;
};
