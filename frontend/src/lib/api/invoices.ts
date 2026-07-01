import { apiClient } from "./client";
import type { Invoice, PaymentInitResponse } from "@/types";

const BASE = "/finance-billing-service/api/invoices";

export const invoiceApi = {
  generate: (bookingId: string) => apiClient.post<Invoice>(`${BASE}/generate`, { bookingId }),
  getById: (id: string) => apiClient.get<Invoice>(`${BASE}/${id}`),
  initPayment: (id: string) => apiClient.post<PaymentInitResponse>(`${BASE}/${id}/pay`),
  confirmPayment: (id: string) => apiClient.post<Invoice>(`${BASE}/${id}/confirm-payment`),
};
