import { cookies } from "next/headers";
import type { NextResponse } from "next/server";
import type { ApiRole } from "@/types/api";
import { sessionFromToken } from "@/lib/auth/jwt";

export const ACCESS_COOKIE = "pms_access_token";
export const REFRESH_COOKIE = "pms_refresh_token";
export const ROLE_COOKIE = "pms_role";

const secure = process.env.NODE_ENV === "production";

export function setAuthCookies(response: NextResponse, accessToken: string, refreshToken: string) {
  const session = sessionFromToken(accessToken);
  const role: ApiRole = session?.apiRole || "ROLE_CUSTOMER";

  response.cookies.set(ACCESS_COOKIE, accessToken, {
    httpOnly: true,
    sameSite: "lax",
    secure,
    path: "/",
    maxAge: 60 * 15
  });

  response.cookies.set(REFRESH_COOKIE, refreshToken, {
    httpOnly: true,
    sameSite: "lax",
    secure,
    path: "/",
    maxAge: 60 * 60 * 24 * 7
  });

  response.cookies.set(ROLE_COOKIE, role, {
    httpOnly: true,
    sameSite: "lax",
    secure,
    path: "/",
    maxAge: 60 * 60 * 24 * 7
  });
}

export function clearAuthCookies(response: NextResponse) {
  for (const name of [ACCESS_COOKIE, REFRESH_COOKIE, ROLE_COOKIE]) {
    response.cookies.set(name, "", {
      httpOnly: true,
      sameSite: "lax",
      secure,
      path: "/",
      maxAge: 0
    });
  }
}

export async function getAccessToken() {
  return (await cookies()).get(ACCESS_COOKIE)?.value;
}

export async function getRefreshToken() {
  return (await cookies()).get(REFRESH_COOKIE)?.value;
}

export async function getSessionFromCookies() {
  const accessToken = await getAccessToken();
  return accessToken ? sessionFromToken(accessToken) : null;
}
