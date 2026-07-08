"use client";

import axios, { AxiosError, AxiosRequestConfig } from "axios";
import { ApiClientError } from "@/lib/api/error";
import type { ApiErrorBody } from "@/types/api";

export const apiClient = axios.create({
  baseURL: "/api/proxy",
  withCredentials: true,
  timeout: 20_000
});

export const authClient = axios.create({
  baseURL: "/api/auth",
  withCredentials: true,
  timeout: 20_000
});

function normalizeAxiosError(error: AxiosError<ApiErrorBody>) {
  const status = error.response?.status ?? 0;
  const body = error.response?.data;
  const message =
    body?.message ||
    body?.error ||
    error.message ||
    "Không thể kết nối tới hệ thống. Vui lòng thử lại.";

  return new ApiClientError(message, status, body);
}

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => Promise.reject(normalizeAxiosError(error))
);

authClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => Promise.reject(normalizeAxiosError(error))
);

export async function apiGet<T>(url: string, config?: AxiosRequestConfig) {
  const { data } = await apiClient.get<T>(url, config);
  return data;
}

export async function apiPost<T, B = unknown>(url: string, body?: B, config?: AxiosRequestConfig) {
  const { data } = await apiClient.post<T>(url, body, config);
  return data;
}

export async function apiPut<T, B = unknown>(url: string, body?: B, config?: AxiosRequestConfig) {
  const { data } = await apiClient.put<T>(url, body, config);
  return data;
}

export async function apiDelete<T = unknown>(url: string, config?: AxiosRequestConfig) {
  const { data } = await apiClient.delete<T>(url, config);
  return data;
}
