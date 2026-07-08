import type { Metadata } from "next";
import { Providers } from "@/components/providers";
import { APP_NAME } from "@/lib/env";
import "./globals.css";

export const metadata: Metadata = {
  title: APP_NAME,
  description: "Hotel management dashboard"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="vi">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
