import type { ApiErrorBody } from "@/types/api";

export class ApiClientError extends Error {
  status: number;
  body?: ApiErrorBody | unknown;

  constructor(message: string, status: number, body?: ApiErrorBody | unknown) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.body = body;
  }
}

export function extractErrorMessage(error: unknown) {
  if (error instanceof ApiClientError) {
    const body = error.body as ApiErrorBody | undefined;
    return body?.message || body?.error || error.message;
  }

  if (error instanceof Error) return error.message;

  return "Đã có lỗi xảy ra. Vui lòng thử lại.";
}
