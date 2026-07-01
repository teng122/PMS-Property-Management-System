"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Search, BedDouble, Star, ShieldCheck } from "lucide-react";
import { Button, Input, Label, Select } from "@/components/ui";
import type { RoomType } from "@/types";

export default function CustomerSearchPage() {
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);
  const [checkIn, setCheckIn] = useState(today);
  const [checkOut, setCheckOut] = useState(today);
  const [type, setType] = useState<RoomType | "">("");

  function submit(e: React.FormEvent) {
    e.preventDefault();
    const q = new URLSearchParams({ checkIn, checkOut });
    if (type) q.set("type", type);
    router.push(`/customer/rooms?${q.toString()}`);
  }

  return (
    <div className="space-y-6">
      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-brand-700 via-brand-600 to-violet-700 px-8 py-12 text-white shadow-elevated">
        <div className="pointer-events-none absolute -right-16 -top-20 h-72 w-72 rounded-full bg-white/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-24 left-1/3 h-72 w-72 rounded-full bg-violet-400/20 blur-3xl" />
        <div className="relative max-w-xl">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-medium backdrop-blur">
            <Star className="h-3.5 w-3.5" /> Trải nghiệm đặt phòng 5 sao
          </span>
          <h1 className="mt-4 text-balance text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
            Tìm phòng nghỉ hoàn hảo cho chuyến đi của bạn
          </h1>
          <p className="mt-2 max-w-md text-brand-100">
            Đặt phòng nhanh chóng, xác nhận tức thì và thanh toán an toàn qua mã QR.
          </p>
          <div className="mt-5 flex flex-wrap gap-4 text-sm text-white/80">
            <span className="inline-flex items-center gap-1.5">
              <ShieldCheck className="h-4 w-4" /> Thanh toán bảo mật
            </span>
            <span className="inline-flex items-center gap-1.5">
              <BedDouble className="h-4 w-4" /> Đa dạng loại phòng
            </span>
          </div>
        </div>
      </div>

      {/* Search card — floating, overlapping hero */}
      <div className="-mt-12 px-1 sm:px-4">
        <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-floating">
          <form onSubmit={submit} className="grid gap-4 sm:grid-cols-4">
            <div>
              <Label>Nhận phòng</Label>
              <Input type="date" value={checkIn} min={today} onChange={(e) => setCheckIn(e.target.value)} />
            </div>
            <div>
              <Label>Trả phòng</Label>
              <Input type="date" value={checkOut} min={checkIn} onChange={(e) => setCheckOut(e.target.value)} />
            </div>
            <div>
              <Label>Loại phòng</Label>
              <Select value={type} onChange={(e) => setType(e.target.value as RoomType | "")}>
                <option value="">Tất cả</option>
                <option value="SINGLE">SINGLE</option>
                <option value="DOUBLE">DOUBLE</option>
                <option value="VIP">VIP</option>
              </Select>
            </div>
            <div className="flex items-end">
              <Button type="submit" variant="brand" size="lg" className="w-full">
                <Search className="h-4 w-4" /> Tìm phòng
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
