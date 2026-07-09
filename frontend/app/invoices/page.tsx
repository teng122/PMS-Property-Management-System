"use client";

import { Suspense, useState } from "react";
import Image from "next/image";
import { useSearchParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { Modal } from "@/components/ui/modal";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { billingApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { money, moneyFromVnd } from "@/lib/format";
import type { InvoiceResponse, PaymentInitResponse, UUID } from "@/types/api";

export default function InvoicesPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST"]}>
      <Suspense fallback={<LoadingState />}>
        <InvoicesContent />
      </Suspense>
    </ProtectedPage>
  );
}

function InvoicesContent() {
  const searchParams = useSearchParams();
  const bookingId = searchParams.get("bookingId");
  const [selected, setSelected] = useState<InvoiceResponse | null>(null);

  const invoices = useQuery({
    queryKey: ["invoices"],
    queryFn: billingApi.getAll
  });

  const invoiceByBooking = useQuery({
    queryKey: ["invoice", "booking", bookingId],
    queryFn: () => billingApi.getByBooking(bookingId as UUID),
    enabled: !!bookingId,
    retry: false,
    refetchInterval: (query) => (query.state.data ? false : 3000)
  });

  const rows = invoices.data || [];

  return (
    <>
      <PageHeading
        title="Hoá đơn & thanh toán"
        description="Theo dõi hoá đơn, tạo mã thanh toán và xác nhận thanh toán."
      />

      {bookingId ? (
        <Card style={{ marginBottom: 18 }}>
          <CardHeader title="Hoá đơn sau trả phòng" description={`Mã đặt phòng: ${bookingId.slice(0, 8)}`} />
          {invoiceByBooking.isLoading || invoiceByBooking.isFetching ? <LoadingState label="Đang chuẩn bị hoá đơn..." /> : null}
          {invoiceByBooking.isError ? (
            <div className="toast">Hoá đơn chưa sẵn sàng. Vui lòng đợi trong giây lát rồi thử lại.</div>
          ) : null}
          {invoiceByBooking.data ? (
            <div className="grid cols-4">
              <Metric title="Mã hoá đơn" value={invoiceByBooking.data.id.slice(0, 8)} />
              <Metric title="Tổng tiền" value={money(invoiceByBooking.data.totalAmount)} />
              <Metric title="Dịch vụ" value={money(invoiceByBooking.data.serviceCharge)} />
              <Metric title="Trạng thái" value={<StatusBadge status={invoiceByBooking.data.status} />} />
            </div>
          ) : null}
        </Card>
      ) : null}

      <Card>
        <CardHeader title="Danh sách hoá đơn" description="Lễ tân và quản trị có thể tạo mã QR và xác nhận thanh toán." />
        {invoices.isLoading ? <LoadingState /> : null}
        {invoices.isError ? <ErrorState message={extractErrorMessage(invoices.error)} /> : null}
        <DataTable
          rows={rows}
          getRowKey={(invoice) => invoice.id}
          searchableText={(invoice) => `${invoice.id} ${invoice.bookingId} ${invoice.status}`}
          columns={[
            { key: "id", header: "Mã hoá đơn", render: (invoice) => <code>{invoice.id.slice(0, 8)}</code> },
            { key: "booking", header: "Mã đặt phòng", render: (invoice) => <code>{invoice.bookingId.slice(0, 8)}</code> },
            { key: "room", header: "Tiền phòng", render: (invoice) => money(invoice.roomCharge) },
            { key: "service", header: "Dịch vụ", render: (invoice) => money(invoice.serviceCharge) },
            { key: "tax", header: "Thuế", render: (invoice) => money(invoice.tax) },
            { key: "deposit", header: "Cọc", render: (invoice) => money(invoice.depositAmount) },
            { key: "total", header: "Còn thu", render: (invoice) => <strong>{money(invoice.totalAmount)}</strong> },
            { key: "status", header: "Trạng thái", render: (invoice) => <StatusBadge status={invoice.status} /> },
            {
              key: "actions",
              header: "Thao tác",
              render: (invoice) => (
                <Button variant="secondary" onClick={() => setSelected(invoice)}>
                  Thanh toán
                </Button>
              )
            }
          ]}
        />
      </Card>

      <PaymentModal invoice={selected} onClose={() => setSelected(null)} />
    </>
  );
}

function PaymentModal({ invoice, onClose }: { invoice: InvoiceResponse | null; onClose: () => void }) {
  const queryClient = useQueryClient();
  const [payment, setPayment] = useState<PaymentInitResponse | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const payMutation = useMutation({
    mutationFn: (id: UUID) => billingApi.pay(id),
    onSuccess: (data) => setPayment(data)
  });

  const confirmMutation = useMutation({
    mutationFn: (id: UUID) => billingApi.confirm(id),
    onSuccess: async () => {
      setMessage("Đã xác nhận thanh toán thành công.");
      await queryClient.invalidateQueries({ queryKey: ["invoices"] });
    }
  });

  if (!invoice) return null;

  return (
    <Modal open={!!invoice} title={`Thanh toán hoá đơn ${invoice.id.slice(0, 8)}`} onClose={onClose}>
      <div className="grid">
        <div className="grid cols-3">
          <Metric title="Tổng tiền" value={money(invoice.totalAmount)} />
          <Metric title="Trạng thái" value={<StatusBadge status={invoice.status} />} />
          <Metric title="Mã đặt phòng" value={invoice.bookingId.slice(0, 8)} />
        </div>
        {payMutation.isError ? <ErrorState message={extractErrorMessage(payMutation.error)} /> : null}
        {confirmMutation.isError ? <ErrorState message={extractErrorMessage(confirmMutation.error)} /> : null}
        {message ? <div className="toast">{message}</div> : null}
        {payment ? (
          <div className="grid cols-2">
            <div>
              <Image
                src={payment.qrImageUrl}
                alt="VietQR thanh toán"
                width={320}
                height={320}
                unoptimized
                style={{ width: "100%", maxWidth: 320, height: "auto", borderRadius: 16, border: "1px solid var(--line)" }}
              />
            </div>
            <div className="state" style={{ textAlign: "left" }}>
              <strong>{payment.state}</strong>
              <p>Số tiền: {moneyFromVnd(payment.amount)}</p>
              <p className="muted">Sau khi khách chuyển khoản, lễ tân bấm xác nhận để hoàn tất thanh toán.</p>
            </div>
          </div>
        ) : null}
        <div className="actions">
          <Button disabled={payMutation.isPending || invoice.status === "PAID"} onClick={() => payMutation.mutate(invoice.id)}>
            Tạo QR
          </Button>
          <Button
            variant="success"
            disabled={confirmMutation.isPending || invoice.status === "PAID"}
            onClick={() => confirmMutation.mutate(invoice.id)}
          >
            Xác nhận đã thanh toán
          </Button>
        </div>
      </div>
    </Modal>
  );
}

function Metric({ title, value }: { title: string; value: React.ReactNode }) {
  return (
    <div className="state" style={{ textAlign: "left", padding: 14 }}>
      <div className="muted" style={{ fontSize: 12, fontWeight: 800, textTransform: "uppercase" }}>{title}</div>
      <div style={{ marginTop: 6 }}>{value}</div>
    </div>
  );
}
