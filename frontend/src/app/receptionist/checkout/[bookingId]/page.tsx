"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { AlertTriangle, CheckCircle2 } from "lucide-react";
import { useBooking } from "@/hooks/useBookings";
import { useUnpaidOrders } from "@/hooks/useAmenities";
import { useGenerateInvoice, useCompleteCheckout } from "@/hooks/useInvoices";
import { Button, Card, CardBody, CardHeader, LoadingBlock, ErrorBlock } from "@/components/ui";
import { formatCurrency, errorMessage } from "@/lib/utils";
import type { Invoice } from "@/types";

export default function CheckoutPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const { data: booking, isLoading, isError, error } = useBooking(bookingId);
  const unpaid = useUnpaidOrders(booking?.roomId);
  const generate = useGenerateInvoice();
  const complete = useCompleteCheckout();

  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [done, setDone] = useState(false);
  const started = useRef(false);

  useEffect(() => {
    if (!booking || started.current) return;
    started.current = true;
    generate.mutate(bookingId, { onSuccess: setInvoice });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [booking, bookingId]);

  if (isLoading) return <LoadingBlock />;
  if (isError || !booking) return <ErrorBlock message={errorMessage(error)} />;

  // Chặn thất thu: còn order chưa BILLED thì cảnh báo (Scenario 2)
  const pendingOrders = (unpaid.data ?? []).filter((o) => o.status !== "BILLED");
  const hasUnbilled = pendingOrders.length > 0;

  if (done) {
    return (
      <div className="mx-auto max-w-md">
        <Card>
          <CardBody className="flex flex-col items-center gap-3 py-10 text-center">
            <CheckCircle2 className="h-16 w-16 text-green-500" />
            <h1 className="text-xl font-semibold">Check-out hoàn tất</h1>
            <p className="text-slate-500">
              Đã thanh toán, booking chuyển CHECKED_OUT, phòng chuyển <b>DIRTY</b> (khóa cho tới khi dọn xong).
            </p>
            <Link href="/receptionist"><Button variant="outline">Về sơ đồ phòng</Button></Link>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold">Check-out · {booking.customerName}</h1>

      <Card>
        <CardHeader className="font-medium">Hóa đơn tổng</CardHeader>
        <CardBody>
          {generate.isPending || !invoice ? (
            <LoadingBlock label="Đang tổng hợp hóa đơn..." />
          ) : (
            <div className="space-y-2 text-sm">
              <Row label="Tiền phòng" value={invoice.roomCharge} />
              <Row label="Dịch vụ đã dùng" value={invoice.serviceCharge} />
              <Row label="VAT (10%)" value={invoice.tax} />
              <div className="flex justify-between border-t pt-2 text-base font-semibold">
                <span>Tổng cộng</span>
                <span className="text-indigo-600">{formatCurrency(invoice.totalAmount)}</span>
              </div>
            </div>
          )}
        </CardBody>
      </Card>

      {/* Danh sách dịch vụ (minh chứng Scenario 2) */}
      {unpaid.data && unpaid.data.length > 0 && (
        <Card>
          <CardHeader className="font-medium">Dịch vụ trong kỳ</CardHeader>
          <CardBody className="space-y-1 text-sm">
            {unpaid.data.map((o) => (
              <div key={o.id} className="flex justify-between">
                <span>
                  {o.amenityName} × {o.quantity}
                </span>
                <span>{formatCurrency(o.totalPrice)}</span>
              </div>
            ))}
          </CardBody>
        </Card>
      )}

      {hasUnbilled && (
        <div className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0" />
          <span>
            Còn <b>{pendingOrders.length}</b> dịch vụ chưa được gộp vào hóa đơn. Hãy tạo lại hóa đơn
            trước khi đóng để tránh thất thu (Late-Charge Prevention).
          </span>
        </div>
      )}

      {complete.isError && <ErrorBlock message={errorMessage(complete.error)} />}

      <Button
        variant="success"
        className="w-full"
        disabled={!invoice}
        loading={complete.isPending}
        onClick={() =>
          invoice &&
          complete.mutate({ invoiceId: invoice.id, booking }, { onSuccess: () => setDone(true) })
        }
      >
        Xác nhận thanh toán & Trả phòng
      </Button>
    </div>
  );
}

function Row({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex justify-between text-slate-600">
      <span>{label}</span>
      <span>{formatCurrency(value)}</span>
    </div>
  );
}
