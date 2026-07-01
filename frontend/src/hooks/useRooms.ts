"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { roomApi } from "@/lib/api/rooms";
import type { RoomStatus } from "@/types";

export function useAvailableRooms() {
  return useQuery({
    queryKey: ["rooms", "available"],
    queryFn: () => roomApi.search().then((r) => r.data),
    refetchInterval: 30_000, // polling cho Room Grid (thay WebSocket)
  });
}

export function useRoom(id: string | undefined) {
  return useQuery({
    queryKey: ["rooms", id],
    queryFn: () => roomApi.getById(id as string).then((r) => r.data),
    enabled: !!id,
  });
}

export function useUpdateRoomStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: RoomStatus }) =>
      roomApi.updateStatus(id, status).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["rooms"] }),
  });
}
