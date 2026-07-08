import { NextRequest, NextResponse } from "next/server";
import { ACCESS_COOKIE, ROLE_COOKIE } from "@/lib/auth/cookies";
import type { ApiRole, Role } from "@/types/api";

const publicRoutes = ["/login", "/register", "/search"];

const roleRules: Array<{ prefix: string; roles: Role[] }> = [
  { prefix: "/users", roles: ["ADMIN"] },
  { prefix: "/walk-in", roles: ["ADMIN", "RECEPTIONIST"] },
  { prefix: "/invoices", roles: ["ADMIN", "RECEPTIONIST"] },
  { prefix: "/amenities/orders", roles: ["ADMIN", "RECEPTIONIST", "STAFF"] },
  { prefix: "/housekeeping", roles: ["ADMIN", "RECEPTIONIST", "STAFF"] },
  { prefix: "/rooms", roles: ["ADMIN", "RECEPTIONIST"] },
  { prefix: "/bookings", roles: ["ADMIN", "RECEPTIONIST", "CUSTOMER"] },
  { prefix: "/dashboard", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] },
  { prefix: "/profile", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] },
  { prefix: "/amenities", roles: ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"] }
];

function stripRole(role?: string): Role {
  return ((role || "ROLE_CUSTOMER").replace(/^ROLE_/, "") || "CUSTOMER") as Role;
}

function isPublic(pathname: string) {
  return publicRoutes.some((route) => pathname === route || pathname.startsWith(`${route}/`));
}

function matchingRule(pathname: string) {
  return roleRules.find((rule) => pathname === rule.prefix || pathname.startsWith(`${rule.prefix}/`));
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (pathname.startsWith("/api") || pathname.startsWith("/_next") || pathname.includes(".")) {
    return NextResponse.next();
  }

  const accessToken = request.cookies.get(ACCESS_COOKIE)?.value;
  const apiRole = request.cookies.get(ROLE_COOKIE)?.value as ApiRole | undefined;
  const role = stripRole(apiRole);

  if (pathname === "/") {
    return NextResponse.redirect(new URL(accessToken ? "/dashboard" : "/login", request.url));
  }

  if (isPublic(pathname)) {
    if (accessToken && (pathname === "/login" || pathname === "/register")) {
      return NextResponse.redirect(new URL("/dashboard", request.url));
    }
    return NextResponse.next();
  }

  if (!accessToken) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", pathname);
    return NextResponse.redirect(loginUrl);
  }

  const rule = matchingRule(pathname);
  if (rule && !rule.roles.includes(role)) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"]
};
