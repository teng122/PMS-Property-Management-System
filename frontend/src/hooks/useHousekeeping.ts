"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { housekeepingApi } from "@/lib/api/housekeeping";

export function useDirtyRooms() {
  return useQuery({
    queryKey: ["housekeeping", "dirty-rooms"],
    queryFn: () => housekeepingApi.getDirtyRooms().then((r) => r.data),
    refetchInterval: 20_000,
    retry: 0, // service có thể chưa bật / chưa route qua gateway
  });
}

export function useStartCleaning() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (taskId: string) => housekeepingApi.startTask(taskId).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["housekeeping"] });
      qc.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}

export function useCompleteCleaning() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (taskId: string) => housekeepingApi.completeTask(taskId).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["housekeeping"] });
      qc.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}
