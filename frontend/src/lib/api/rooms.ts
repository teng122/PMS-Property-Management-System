import { apiClient } from "./client";
import type { Room, RoomStatus } from "@/types";

const BASE = "/room-service/api/rooms";

export const roomApi = {
  /** Public. Chỉ trả phòng AVAILABLE (backend không nhận filter param). */
  search: () => apiClient.get<Room[]>(`${BASE}/search`),
  getById: (id: string) => apiClient.get<Room>(`${BASE}/${id}`),
  updateStatus: (id: string, status: RoomStatus) =>
    apiClient.put<Room>(`${BASE}/${id}/status`, { status }),
};
