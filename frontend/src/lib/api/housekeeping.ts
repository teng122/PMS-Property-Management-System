import { apiClient } from "./client";
import type { CleaningTask, DirtyRoom } from "@/types";

const BASE = "/housekeeping-service/api/housekeeping";

export const housekeepingApi = {
  getDirtyRooms: () => apiClient.get<DirtyRoom[]>(`${BASE}/dirty-rooms`),
  startTask: (id: string) => apiClient.post<CleaningTask>(`${BASE}/tasks/${id}/start`),
  completeTask: (id: string) => apiClient.post<CleaningTask>(`${BASE}/tasks/${id}/complete`),
};
