"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Suspense, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { authApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { defaultRouteForRole } from "@/lib/auth/permissions";
import { useAuthStore } from "@/stores/auth-store";

const schema = z.object({
  username: z.string().min(1, "Vui lòng nhập tài khoản"),
  password: z.string().min(1, "Vui lòng nhập mật khẩu")
});

type LoginForm = z.infer<typeof schema>;

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="state">Đang tải trang đăng nhập...</div>}>
      <LoginView />
    </Suspense>
  );
}

function LoginView() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setUser = useAuthStore((state) => state.setUser);
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<LoginForm>({ resolver: zodResolver(schema) });

  async function onSubmit(values: LoginForm) {
    setError(null);
    try {
      const result = await authApi.login(values);
      setUser(result.user);
      const next = searchParams.get("next");
      router.replace(next || defaultRouteForRole(result.user?.role));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <div className="brand">
          <span className="brand-mark">H</span>
          <span>Smart Hotel PMS</span>
        </div>
        <h1>Quản lý khách sạn gọn gàng, rõ trạng thái, ít đoán mò.</h1>
        <p style={{ maxWidth: 560, color: "rgba(255,255,255,0.78)" }}>
          Dashboard cho lễ tân, quản trị, vận hành và khách hàng trong một hệ thống thống nhất.
        </p>
      </section>
      <main className="auth-card-wrap">
        <Card>
          <CardHeader title="Đăng nhập" description="Nhập tài khoản được cấp để vào hệ thống." />
          <form className="grid" onSubmit={handleSubmit(onSubmit)}>
            <div className="field">
              <label htmlFor="username">Tài khoản</label>
              <input id="username" className="input" autoComplete="username" {...register("username")} />
              {errors.username ? <span className="field-error">{errors.username.message}</span> : null}
            </div>
            <div className="field">
              <label htmlFor="password">Mật khẩu</label>
              <input id="password" className="input" type="password" autoComplete="current-password" {...register("password")} />
              {errors.password ? <span className="field-error">{errors.password.message}</span> : null}
            </div>
            {error ? <div className="toast" style={{ borderColor: "#fecaca", color: "#991b1b", background: "#fef2f2" }}>{error}</div> : null}
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
            </Button>
            <div className="muted">
              Chưa có tài khoản? <Link href="/register" style={{ color: "var(--primary)", fontWeight: 800 }}>Đăng ký khách hàng</Link>
            </div>
            <div className="muted">
              Muốn xem phòng trước? <Link href="/search" style={{ color: "var(--primary)", fontWeight: 800 }}>Tìm phòng trống</Link>
            </div>
          </form>
        </Card>
      </main>
    </div>
  );
}
