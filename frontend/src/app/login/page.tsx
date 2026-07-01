"use client";

import { useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Hotel, User, Lock, CheckCircle2 } from "lucide-react";
import { useLogin } from "@/hooks/useAuth";
import { Button, Input, Label, ErrorBlock } from "@/components/ui";
import { AuthLayout } from "@/components/AuthLayout";
import { errorMessage } from "@/lib/utils";

export default function LoginPage() {
  const params = useSearchParams();
  const login = useLogin();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function submit(e: React.FormEvent) {
    e.preventDefault();
    login.mutate({ username, password });
  }

  return (
    <AuthLayout>
      {/* mobile brand mark */}
      <div className="mb-8 flex items-center gap-2.5 lg:hidden">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand-600 to-violet-600 text-white shadow-brand-glow">
          <Hotel className="h-5 w-5" />
        </div>
        <span className="text-lg font-semibold tracking-tight text-slate-900">Smart Hotel</span>
      </div>

      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Chào mừng trở lại</h1>
        <p className="mt-1 text-sm text-slate-500">Đăng nhập để tiếp tục vào hệ thống.</p>
      </div>

      {params.get("registered") && (
        <div className="mb-4 flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-3.5 py-2.5 text-sm text-emerald-700">
          <CheckCircle2 className="h-4 w-4 shrink-0" />
          Đăng ký thành công! Hãy đăng nhập.
        </div>
      )}

      <form onSubmit={submit} className="space-y-4">
        <div>
          <Label>Tên đăng nhập</Label>
          <div className="relative">
            <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <Input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="admin"
              className="pl-9"
              required
            />
          </div>
        </div>
        <div>
          <Label>Mật khẩu</Label>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <Input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="pl-9"
              required
            />
          </div>
        </div>

        {login.isError && <ErrorBlock message={errorMessage(login.error, "Sai tài khoản hoặc mật khẩu")} />}

        <Button type="submit" variant="brand" size="lg" className="w-full" loading={login.isPending}>
          Đăng nhập
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Chưa có tài khoản?{" "}
        <Link href="/register" className="font-semibold text-brand-600 hover:text-brand-700 hover:underline">
          Đăng ký ngay
        </Link>
      </p>
    </AuthLayout>
  );
}
