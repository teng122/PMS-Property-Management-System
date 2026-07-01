"use client";

import { Hotel, ShieldCheck, QrCode, Sparkles } from "lucide-react";

const FEATURES = [
  { icon: QrCode, title: "Thanh toán QR tức thì", desc: "Đặt phòng và thanh toán trong vài giây." },
  { icon: ShieldCheck, title: "Phân quyền theo vai trò", desc: "Lễ tân, buồng phòng, quản lý — mỗi người một không gian." },
  { icon: Sparkles, title: "Vận hành thời gian thực", desc: "Trạng thái phòng và đặt phòng cập nhật liên tục." },
];

/**
 * Bố cục xác thực split-screen: panel thương hiệu (trái) + nội dung form (phải).
 */
export function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Brand panel */}
      <div className="relative hidden overflow-hidden bg-gradient-to-br from-brand-700 via-brand-600 to-violet-700 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        {/* decorative glows */}
        <div className="pointer-events-none absolute -left-24 -top-24 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-32 -right-16 h-[26rem] w-[26rem] rounded-full bg-violet-400/20 blur-3xl" />

        <div className="relative flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/15 backdrop-blur">
            <Hotel className="h-6 w-6" />
          </div>
          <div>
            <div className="text-lg font-semibold tracking-tight">Smart Hotel</div>
            <div className="text-xs text-white/70">Property Management System</div>
          </div>
        </div>

        <div className="relative max-w-md">
          <h2 className="text-balance text-3xl font-bold leading-tight tracking-tight">
            Quản lý khách sạn thông minh, mượt mà từ đặt phòng đến thanh toán.
          </h2>
          <ul className="mt-8 space-y-4">
            {FEATURES.map((f) => (
              <li key={f.title} className="flex items-start gap-3">
                <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
                  <f.icon className="h-5 w-5" />
                </div>
                <div>
                  <div className="text-sm font-semibold">{f.title}</div>
                  <div className="text-sm text-white/70">{f.desc}</div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="relative text-xs text-white/60">
          © {new Date().getFullYear()} Smart Hotel PMS. Bảo lưu mọi quyền.
        </div>
      </div>

      {/* Form panel */}
      <div className="flex items-center justify-center px-4 py-10 sm:px-8">
        <div className="w-full max-w-sm animate-fade-up">{children}</div>
      </div>
    </div>
  );
}
