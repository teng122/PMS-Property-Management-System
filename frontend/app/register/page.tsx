"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { authApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";

const schema = z.object({
  username: z.string().min(3, "Tài khoản tối thiểu 3 ký tự"),
  password: z.string().min(6, "Mật khẩu tối thiểu 6 ký tự"),
  fullName: z.string().min(2, "Vui lòng nhập họ tên"),
  email: z.string().email("Email không hợp lệ").optional().or(z.literal(""))
});

type RegisterForm = z.infer<typeof schema>;

export default function RegisterPage() {
  const router = useRouter();
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<RegisterForm>({ resolver: zodResolver(schema) });

  async function onSubmit(values: RegisterForm) {
    setError(null);
    setMessage(null);
    try {
      await authApi.register({ ...values, email: values.email || undefined });
      setMessage("Đăng ký thành công. Bạn có thể đăng nhập ngay.");
      setTimeout(() => router.push("/login"), 900);
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
        <h1>Tạo tài khoản khách hàng để đặt phòng và theo dõi lưu trú.</h1>
        <p style={{ maxWidth: 560, color: "rgba(255,255,255,0.78)" }}>
          Tạo tài khoản khách hàng để đặt phòng và sử dụng dịch vụ.
        </p>
      </section>
      <main className="auth-card-wrap">
        <Card>
          <CardHeader title="Đăng ký" description="Tạo tài khoản khách hàng mới." />
          <form className="grid" onSubmit={handleSubmit(onSubmit)}>
            <div className="field">
              <label>Tài khoản</label>
              <input className="input" {...register("username")} />
              {errors.username ? <span className="field-error">{errors.username.message}</span> : null}
            </div>
            <div className="field">
              <label>Mật khẩu</label>
              <input className="input" type="password" {...register("password")} />
              {errors.password ? <span className="field-error">{errors.password.message}</span> : null}
            </div>
            <div className="field">
              <label>Họ tên</label>
              <input className="input" {...register("fullName")} />
              {errors.fullName ? <span className="field-error">{errors.fullName.message}</span> : null}
            </div>
            <div className="field">
              <label>Email</label>
              <input className="input" type="email" {...register("email")} />
              {errors.email ? <span className="field-error">{errors.email.message}</span> : null}
            </div>
            {error ? <div className="toast" style={{ borderColor: "#fecaca", color: "#991b1b", background: "#fef2f2" }}>{error}</div> : null}
            {message ? <div className="toast" style={{ borderColor: "#bbf7d0", color: "#166534", background: "#f0fdf4" }}>{message}</div> : null}
            <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Đang tạo..." : "Tạo tài khoản"}</Button>
            <div className="muted">
              Đã có tài khoản? <Link href="/login" style={{ color: "var(--primary)", fontWeight: 800 }}>Đăng nhập</Link>
            </div>
          </form>
        </Card>
      </main>
    </div>
  );
}
