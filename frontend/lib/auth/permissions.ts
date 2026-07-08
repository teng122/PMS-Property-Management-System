"use client";

import type { Role } from "@/types/api";

export function hasAnyRole(current: Role | undefined, allowed: Role[]) {
  return !!current && allowed.includes(current);
}

export function roleLabel(role?: Role) {
  switch (role) {
    case "ADMIN":
      return "Quản trị";
    case "RECEPTIONIST":
      return "Lễ tân";
    case "STAFF":
      return "Nhân viên";
    case "CUSTOMER":
      return "Khách hàng";
    default:
      return "Chưa đăng nhập";
  }
}

export function defaultRouteForRole(role?: Role) {
  switch (role) {
    case "ADMIN":
      return "/dashboard";
    case "RECEPTIONIST":
      return "/bookings";
    case "STAFF":
      return "/amenities/orders";
    case "CUSTOMER":
      return "/bookings";
    default:
      return "/login";
  }
}
