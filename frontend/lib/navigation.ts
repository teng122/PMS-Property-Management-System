import type { NavItem } from "@/types/navigation";

export const navItems: NavItem[] = [
  { label: "Tổng quan", href: "/dashboard", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] },
  { label: "Tìm phòng", href: "/search", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] },
  { label: "Kho phòng", href: "/rooms", roles: ["ADMIN", "RECEPTIONIST"] },
  { label: "Đặt phòng", href: "/bookings", roles: ["ADMIN", "RECEPTIONIST", "CUSTOMER"] },
  { label: "Nhận khách", href: "/walk-in", roles: ["ADMIN", "RECEPTIONIST"] },
  { label: "Hoá đơn", href: "/invoices", roles: ["ADMIN", "RECEPTIONIST"] },
  { label: "Dịch vụ", href: "/amenities", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] },
  { label: "Đơn dịch vụ", href: "/amenities/orders", roles: ["ADMIN", "RECEPTIONIST", "STAFF"] },
  { label: "Dọn phòng", href: "/housekeeping", roles: ["ADMIN", "RECEPTIONIST", "STAFF"] },
  { label: "Tài khoản", href: "/users", roles: ["ADMIN"] },
  { label: "Hồ sơ", href: "/profile", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] }
];
