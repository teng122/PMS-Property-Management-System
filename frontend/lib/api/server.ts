import { NextRequest, NextResponse } from "next/server";
import { BACKEND_GATEWAY_URL } from "@/lib/env";
import {
  clearAuthCookies,
  getAccessToken,
  getRefreshToken,
  setAuthCookies
} from "@/lib/auth/cookies";
import { sessionFromToken } from "@/lib/auth/jwt";
import type { AuthResponse, TokenRefreshResponse } from "@/types/api";

const JSON_CONTENT_TYPES = ["application/json", "application/problem+json"];

function buildGatewayUrl(path: string, search = "") {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${BACKEND_GATEWAY_URL}${normalizedPath}${search}`;
}

async function parseGatewayResponse(response: Response) {
  const contentType = response.headers.get("content-type") || "";
  if (JSON_CONTENT_TYPES.some((type) => contentType.includes(type))) {
    return response.json();
  }
  return response.text();
}

export async function gatewayFetch(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  const accessToken = await getAccessToken();

  if (accessToken && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(buildGatewayUrl(path), {
    ...init,
    headers,
    cache: "no-store"
  });
}

export async function loginViaGateway(body: unknown) {
  const response = await fetch(buildGatewayUrl("/identity-service/api/auth/login"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
    cache: "no-store"
  });

  const data = await parseGatewayResponse(response);
  if (!response.ok) {
    return NextResponse.json(data, { status: response.status });
  }

  const auth = data as AuthResponse;
  const nextResponse = NextResponse.json({ user: sessionFromToken(auth.token), username: auth.username, role: auth.role });
  setAuthCookies(nextResponse, auth.token, auth.refreshToken);
  return nextResponse;
}

export async function registerViaGateway(body: unknown) {
  const response = await fetch(buildGatewayUrl("/identity-service/api/auth/register"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
    cache: "no-store"
  });
  const data = await parseGatewayResponse(response);
  return NextResponse.json(data, { status: response.status });
}

export async function refreshAccessToken() {
  const refreshToken = await getRefreshToken();
  if (!refreshToken) return null;

  const response = await fetch(buildGatewayUrl("/identity-service/api/auth/refresh"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
    cache: "no-store"
  });

  if (!response.ok) return null;

  const data = (await response.json()) as TokenRefreshResponse;
  return data;
}

export async function proxyToGateway(
  request: NextRequest,
  segments: string[],
  retryOnUnauthorized = true
): Promise<NextResponse> {
  const accessToken = await getAccessToken();
  const path = `/${segments.join("/")}`;
  const gatewayUrl = buildGatewayUrl(path, request.nextUrl.search);
  const headers = new Headers(request.headers);

  headers.delete("host");
  headers.delete("cookie");

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const method = request.method.toUpperCase();
  const hasBody = !["GET", "HEAD"].includes(method);
  const bodyText = hasBody ? await request.text() : undefined;
  const response = await fetch(gatewayUrl, {
    method,
    headers,
    body: bodyText,
    cache: "no-store"
  });

  if (response.status === 401 && retryOnUnauthorized) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      const retryShell = NextResponse.next();
      setAuthCookies(retryShell, refreshed.accessToken, refreshed.refreshToken);

      const retryRequest = new NextRequest(request.url, {
        method,
        headers: request.headers,
        body: bodyText
      });
      const retryResponse = await proxyToGateway(retryRequest, segments, false);
      for (const cookie of retryShell.cookies.getAll()) {
        retryResponse.cookies.set(cookie);
      }
      return retryResponse;
    }

    const unauthorized = NextResponse.json(
      { status: 401, error: "Unauthorized", message: "Phiên đăng nhập đã hết hạn." },
      { status: 401 }
    );
    clearAuthCookies(unauthorized);
    return unauthorized;
  }

  const data = await parseGatewayResponse(response);
  return NextResponse.json(data, { status: response.status });
}
