import type { ApiRole, Role, SessionUser } from "@/types/api";

interface JwtPayload {
  sub?: string;
  role?: ApiRole | string;
  userId?: string;
  exp?: number;
  iat?: number;
  jti?: string;
}

function base64UrlDecode(value: string) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");

  if (typeof window === "undefined") {
    return Buffer.from(padded, "base64").toString("utf8");
  }

  return decodeURIComponent(
    Array.from(atob(padded))
      .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, "0")}`)
      .join("")
  );
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split(".");
    if (!payload) return null;
    return JSON.parse(base64UrlDecode(payload)) as JwtPayload;
  } catch {
    return null;
  }
}

export function stripRole(role?: string | null): Role {
  const normalized = (role || "ROLE_CUSTOMER").replace(/^ROLE_/, "") as Role;
  if (["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"].includes(normalized)) {
    return normalized;
  }
  return "CUSTOMER";
}

export function toApiRole(role: Role): ApiRole {
  return `ROLE_${role}`;
}

export function sessionFromToken(token: string): SessionUser | null {
  const payload = decodeJwt(token);
  if (!payload?.sub || !payload.userId) return null;

  const role = stripRole(payload.role);
  return {
    id: payload.userId,
    username: payload.sub,
    role,
    apiRole: toApiRole(role)
  };
}

export function isTokenExpired(token: string, skewSeconds = 15) {
  const payload = decodeJwt(token);
  if (!payload?.exp) return true;
  return payload.exp * 1000 <= Date.now() + skewSeconds * 1000;
}
