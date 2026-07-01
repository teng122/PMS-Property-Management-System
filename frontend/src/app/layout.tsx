import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";

const inter = Inter({
  subsets: ["latin", "vietnamese"],
  display: "swap",
  variable: "--font-sans",
});

export const metadata: Metadata = {
  title: "Smart Hotel PMS — Hệ thống quản lý khách sạn",
  description: "Hệ thống quản lý khách sạn thông minh: đặt phòng, lễ tân, buồng phòng và thanh toán.",
};

// App dashboard client-side (auth + gọi gateway runtime) → không cần static prerender.
// Tránh lỗi "useSearchParams should be wrapped in suspense" khi build.
export const dynamic = "force-dynamic";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="vi" className={inter.variable}>
      <body className="font-sans antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
