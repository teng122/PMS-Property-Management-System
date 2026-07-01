"use client";

import { useState } from "react";
import { Search } from "lucide-react";
import { invoiceApi } from "@/lib/api/invoices";
import { Button, Card, CardBody, CardHeader, Input, Label, ErrorBlock } from "@/components/ui";
import { Badge } from "@/components/ui";
import { formatCurrency, formatDate, errorMessage } from "@/lib/utils";
import type { Invoice } from "@/types";

export default function AdminInvoicesPage() {
  const [id, setId] = useState("");
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function lookup(e: React.FormEvent) {
    e.preventDefault();
    if (!id) return;
    setLoading(true);
    setErr(null);
    setInvoice(null);
    try {
      const res = await invoiceApi.getById(id.trim());
      setInvoice(res.data);
    } catch (e) {
      setErr(errorMessage(e, "Không tìm thấy hóa đơn"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold">Tra cứu hóa đơn</h1>
      <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700">
        Backend chưa có API liệt kê hóa đơn → tra cứu theo <b>mã hóa đơn (invoiceId)</b>.
      </div>

      <form onSubmit={lookup} className="flex gap-2">
        <div className="flex-1">
          <Label className="sr-only">Mã hóa đơn</Label>
          <Input value={id} onChange={(e) => setId(e.target.value)} placeholder="Nhập invoiceId (UUID)" />
        </div>
        <Button type="submit" loading={loading}>
          <Search className="h-4 w-4" /> Tra cứu
        </Button>
      </form>

      {err && <ErrorBlock message={err} />}

      {invoice && (
        <Card>
          <CardHeader className="flex items-center justify-between">
            <span className="font-medium">Hóa đơn</span>
            <Badge
              className={
                invoice.status === "PAID"
                  ? "bg-green-100 text-green-800 border-green-300"
                  : "bg-yellow-100 text-yellow-800 border-yellow-300"
              }
            >
              {invoice.status}
            </Badge>
          </CardHeader>
          <CardBody className="space-y-2 text-sm">
            <Row label="Mã booking" value={invoice.bookingId} />
            <Row label="Tiền phòng" value={formatCurrency(invoice.roomCharge)} />
            <Row label="Dịch vụ" value={formatCurrency(invoice.serviceCharge)} />
            <Row label="VAT" value={formatCurrency(invoice.tax)} />
            <div className="flex justify-between border-t pt-2 font-semibold">
              <span>Tổng cộng</span>
              <span className="text-purple-600">{formatCurrency(invoice.totalAmount)}</span>
            </div>
            {invoice.paidAt && <Row label="Thanh toán lúc" value={formatDate(invoice.paidAt)} />}
          </CardBody>
        </Card>
      )}
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <span className="text-slate-500">{label}</span>
      <span className="text-right font-medium">{value}</span>
    </div>
  );
}
