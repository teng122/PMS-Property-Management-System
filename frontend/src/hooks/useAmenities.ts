"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { amenityApi } from "@/lib/api/amenities";
import type { AmenityCreateRequest, AmenityOrderCreateRequest, OrderStatus } from "@/types";

export function useAmenities() {
  return useQuery({
    queryKey: ["amenities"],
    queryFn: () => amenityApi.getAll().then((r) => r.data),
  });
}

export function useUnpaidOrders(roomId: string | undefined) {
  return useQuery({
    queryKey: ["amenity-orders", "unpaid", roomId],
    queryFn: () => amenityApi.getUnpaidByRoom(roomId as string).then((r) => r.data),
    enabled: !!roomId,
  });
}

export function useCreateAmenity() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: AmenityCreateRequest) => amenityApi.create(data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["amenities"] }),
  });
}

export function useCreateOrder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: AmenityOrderCreateRequest) =>
      amenityApi.createOrder(data).then((r) => r.data),
    onSuccess: (_d, vars) => {
      qc.invalidateQueries({ queryKey: ["amenity-orders", "unpaid", vars.roomId] });
      qc.invalidateQueries({ queryKey: ["amenity-orders", "all"] });
    },
  });
}

export function useAllOrders() {
  return useQuery({
    queryKey: ["amenity-orders", "all"],
    queryFn: () => amenityApi.getAllOrders().then((r) => r.data),
  });
}

export function useUpdateOrderStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: OrderStatus }) =>
      amenityApi.updateOrderStatus(id, status).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["amenity-orders"] });
    },
  });
}
