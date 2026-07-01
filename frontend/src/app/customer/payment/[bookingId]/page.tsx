"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { CheckCircle2, XCircle, Clock } from "lucide-react";
import { useBooking } from "@/hooks/useBookings";
import { useGenerateInvoice, useInitPayment, useConfirmBookingPayment } from "@/hooks/useInvoices";
import { Button, Card, CardBody, CardHeader, LoadingBlock, ErrorBlock } from "@/components/ui";
import { formatCurrency, errorMessage } from "@/lib/utils";
import type { Invoice, PaymentInitResponse } from "@/types";

const PAYMENT_WINDOW_MS = 15 * 60 * 1000;

export default function PaymentPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const { data: booking, isLoading, isError, error, refetch } = useBooking(bookingId);

  const generate = useGenerateInvoice();
  const initPayment = useInitPayment();
  const confirm = useConfirmBookingPayment();

  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [payment, setPayment] = useState<PaymentInitResponse | null>(null);
  const [paid, setPaid] = useState(false);
  const started = useRef(false);

  // Sinh hóa đơn + init QR khi có booking (chạy 1 lần)
  useEffect(() => {
    if (!booking || started.current) return;
    started.current = true;
    generate.mutate(bookingId, {
      onSuccess: (inv) => {
        setInvoice(inv);
        initPayment.mutate(inv.id, { onSuccess: setPayment });
      },
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [booking, bookingId]);

  // Đếm ngược dựa trên thời điểm tạo booking
  const deadline = useMemo(() => {
    const created = booking?.createdAt ? new Date(booking.createdAt).getTime() : Date.now();
    return created + PAYMENT_WINDOW_MS;
  }, [booking?.createdAt]);

  const [remaining, setRemaining] = useState(PAYMENT_WINDOW_MS);
  useEffect(() => {
    const t = setInterval(() => setRemaining(Math.max(0, deadline - Date.now())), 1000);
    return () => clearInterval(t);
  }, [deadline]);

  if (isLoading) return <LoadingBlock />;
  if (isError || !booking) return <ErrorBlock message={errorMessage(error, "Không tìm thấy đơn")} />;

  const expired = booking.status === "CANCELLED" || (remaining <= 0 && !paid && booking.status === "PENDING_PAYMENT");
  const confirmed = paid || booking.status === "CONFIRMED";
  const mm = Math.floor(remaining / 60000);
  const ss = Math.floor((remaining % 60000) / 1000);

  function handlePay() {
    if (!invoice || !booking) return;
    confirm.mutate({ invoiceId: invoice.id, booking }, { onSuccess: () => { setPaid(true); refetch(); } });
  }

  if (confirmed) {
    return (
      <div className="mx-auto max-w-md">
        <Card>
          <CardBody className="flex flex-col items-center gap-3 py-10 text-center">
            <CheckCircle2 className="h-16 w-16 text-green-500" />
            <h1 className="text-xl font-semibold">Thanh toán thành công!</h1>
            <p className="text-slate-500">Đơn đặt phòng đã được xác nhận (CONFIRMED).</p>
            <Link href="/customer"><Button variant="outline">Về trang chủ</Button></Link>
          </CardBody>
        </Card>
      </div>
    );
  }

  if (expired) {
    return (
      <div className="mx-auto max-w-md">
        <Card>
          <CardBody className="flex flex-col items-center gap-3 py-10 text-center">
            <XCircle className="h-16 w-16 text-red-500" />
            <h1 className="text-xl font-semibold">Đơn đã hết hạn</h1>
            <p className="text-slate-500">
              Quá 15 phút chưa thanh toán — hệ thống tự động hủy và giải phóng phòng.
            </p>
            <Link href="/customer"><Button variant="outline">Tìm phòng khác</Button></Link>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md space-y-4">
      <div className="flex items-center justify-center gap-2 rounded-lg bg-amber-50 py-3 text-amber-700">
        <Clock className="h-5 w-5" />
        <span className="font-mono text-lg font-bold">
          {String(mm).padStart(2, "0")}:{String(ss).padStart(2, "0")}
        </span>
        <span className="text-sm">còn lại để thanh toán</span>
      </div>

      <Card>
        <CardHeader className="text-center font-semibold">Quét mã VietQR để thanh toán</CardHeader>
        <CardBody className="flex flex-col items-center gap-4">
          {generate.isPending || initPayment.isPending || !payment ? (
            <LoadingBlock label="Đang tạo hóa đơn & mã QR..." />
          ) : (
            <>
              <Image
                src={payment.qrImageUrl}
                alt="VietQR"
                width={240}
                height={240}
                unoptimized
                className="rounded-lg border"
              />
              <div className="text-center">
                <div className="text-sm text-slate-500">Số tiền</div>
                <div className="text-2xl font-bold text-blue-600">{formatCurrency(payment.amount)}</div>
              </div>
              {invoice && (
                <div className="w-full space-y-1 rounded-lg bg-slate-50 p-3 text-sm">
                  <Row label="Tiền phòng" value={invoice.roomCharge} />
                  <Row label="Dịch vụ" value={invoice.serviceCharge} />
                  <Row label="VAT (10%)" value={invoice.tax} />
                  <div className="mt-1 flex justify-between border-t pt-1 font-semibold">
                    <span>Tổng</span>
                    <span>{formatCurrency(invoice.totalAmount)}</span>
                  </div>
                </div>
              )}

              {confirm.isError && <ErrorBlock message={errorMessage(confirm.error)} />}

              <Button variant="success" className="w-full" loading={confirm.isPending} onClick={handlePay}>
                Giả lập ngân hàng xác nhận đã trả
              </Button>
            </>
          )}
        </CardBody>
      </Card>
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
