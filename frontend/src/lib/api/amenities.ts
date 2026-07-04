import { apiClient } from "./client";
import type {
  Amenity,
  AmenityCreateRequest,
  AmenityOrder,
  AmenityOrderCreateRequest,
  OrderStatus,
} from "@/types";

const BASE = "/amenities-service/api/amenities";

export const amenityApi = {
  getAll: () => apiClient.get<Amenity[]>(BASE),
  getById: (id: string) => apiClient.get<Amenity>(`${BASE}/${id}`),
  create: (data: AmenityCreateRequest) => apiClient.post<Amenity>(BASE, data),
  createOrder: (data: AmenityOrderCreateRequest) =>
    apiClient.post<AmenityOrder>(`${BASE}/order`, data),
  getUnpaidByRoom: (roomId: string) =>
    apiClient.get<AmenityOrder[]>(`${BASE}/room/${roomId}/unpaid`),
  updateOrderStatus: (id: string, status: OrderStatus) =>
    apiClient.put<AmenityOrder>(`${BASE}/orders/${id}/status`, null, { params: { status } }),
  getAllOrders: () => apiClient.get<AmenityOrder[]>(`${BASE}/orders`),
};
