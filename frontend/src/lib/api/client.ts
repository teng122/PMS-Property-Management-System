import axios from "axios";
import { useAuthStore } from "@/store/useAuthStore";

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

// 401 → đăng xuất và về trang login
apiClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      useAuthStore.getState().logout();
      if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  }
);
