import axios from "axios";
import { useAuthStore } from "@/store/useAuthStore";
import type { TokenRefreshResponse } from "@/types";

/** Gateway là điểm vào duy nhất. Xem FRONTEND_PLAN.md §1 (path-prefix routing). */
export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

/**
 * Housekeeping-service CHƯA có route ở Gateway → tạm gọi trực tiếp :8084.
 * (Xem FRONTEND_PLAN.md §10.1.) Khi backend thêm route thì trỏ về apiClient.
 */
export const housekeepingClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_HOUSEKEEPING_URL ?? "http://localhost:8084",
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

// Gắn Bearer token cho mọi request qua gateway
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Hàng đợi cho các request bị lỗi 401 trong lúc đang refresh token
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 401 → tự động refresh token ngầm, nếu thất bại thì đăng xuất
apiClient.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;

    // Tránh lặp vô hạn nếu chính request refresh bị lỗi 401
    if (originalRequest.url?.includes("/identity-service/api/auth/refresh")) {
      useAuthStore.getState().logout();
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
      return Promise.reject(err);
    }

    const isAuthUrl =
      originalRequest.url?.includes("/identity-service/api/auth/login") ||
      originalRequest.url?.includes("/identity-service/api/auth/register");

    if (err?.response?.status === 401 && !originalRequest._retry && !isAuthUrl) {
      if (isRefreshing) {
        // Đã có tiến trình refresh đang chạy, đẩy request này vào hàng đợi
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((error) => {
            return Promise.reject(error);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        // Gọi API refresh token ngầm (sử dụng instance axios thô để tránh trigger interceptor)
        const response = await axios.post<TokenRefreshResponse>(
          `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/identity-service/api/auth/refresh`,
          { refreshToken }
        );

        const { accessToken, refreshToken: newRefreshToken } = response.data;

        // Lưu tokens mới vào store
        useAuthStore.getState().setTokens(accessToken, newRefreshToken);

        // Chạy lại request lỗi ban đầu với token mới
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        processQueue(null, accessToken);

        return apiClient(originalRequest);
      } catch (refreshError) {
        // Nếu refresh thất bại (hết hạn, bị thu hồi) -> Đăng xuất toàn bộ
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(err);
  }
);
