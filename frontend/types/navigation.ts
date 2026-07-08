import type { Role } from "@/types/api";

export interface NavItem {
  label: string;
  href: string;
  roles: Role[];
}
