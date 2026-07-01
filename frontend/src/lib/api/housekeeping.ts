import { housekeepingClient } from "./client";
import type { CleaningTask, DirtyRoom } from "@/types";

const BASE = "/api/housekeeping";

/**
 * Dùng housekeepingClient (gọi trực tiếp :8084) vì service này CHƯA có route Gateway.
 * Xem FRONTEND_PLAN.md §10.1.
 */
export const housekeepingApi = {
  getDirtyRooms: () => housekeepingClient.get<DirtyRoom[]>(`${BASE}/dirty-rooms`),
  startTask: (id: string) => housekeepingClient.post<CleaningTask>(`${BASE}/tasks/${id}/start`),
  completeTask: (id: string) =>
    housekeepingClient.post<CleaningTask>(`${BASE}/tasks/${id}/complete`),
};
