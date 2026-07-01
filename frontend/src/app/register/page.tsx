"use client";

import { useState } from "react";
import Link from "next/link";
import { Hotel } from "lucide-react";
import { useRegister } from "@/hooks/useAuth";
import { Button, Input, Label, Select, ErrorBlock } from "@/components/ui";
import { AuthLayout } from "@/components/AuthLayout";
import { errorMessage } from "@/lib/utils";
import type { Role } from "@/types";

export default function RegisterPage() {
  const register = useRegister();
  const [form, setForm] = useState({
    username: "",
    password: "",
    fullName: "",
    email: "",
    role: "CUSTOMER" as Role,
  });

  function set<K extends keyof typeof form>(key: K, value: (typeof form)[K]) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  function submit(e: React.FormEvent) {
    e.preventDefault();
    register.mutate(form);
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
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Tạo tài khoản</h1>
        <p className="mt-1 text-sm text-slate-500">Đăng ký để bắt đầu sử dụng hệ thống.</p>
      </div>

      <form onSubmit={submit} className="space-y-3.5">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <Label>Tên đăng nhập</Label>
            <Input value={form.username} onChange={(e) => set("username", e.target.value)} required />
          </div>
          <div>
            <Label>Họ và tên</Label>
            <Input value={form.fullName} onChange={(e) => set("fullName", e.target.value)} required />
          </div>
        </div>
        <div>
          <Label>Email</Label>
          <Input type="email" value={form.email} onChange={(e) => set("email", e.target.value)} required />
        </div>
        <div>
          <Label>Mật khẩu</Label>
          <Input type="password" value={form.password} onChange={(e) => set("password", e.target.value)} placeholder="••••••••" required />
        </div>
        <div>
          <Label>Vai trò</Label>
          <Select value={form.role} onChange={(e) => set("role", e.target.value as Role)}>
            <option value="CUSTOMER">Khách hàng (CUSTOMER)</option>
            <option value="RECEPTIONIST">Lễ tân (RECEPTIONIST)</option>
            <option value="HOUSEKEEPER">Lao công (HOUSEKEEPER)</option>
            <option value="ADMIN">Quản lý (ADMIN)</option>
          </Select>
        </div>

        {register.isError && <ErrorBlock message={errorMessage(register.error)} />}

        <Button type="submit" variant="brand" size="lg" className="mt-1 w-full" loading={register.isPending}>
          Đăng ký
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Đã có tài khoản?{" "}
        <Link href="/login" className="font-semibold text-brand-600 hover:text-brand-700 hover:underline">
          Đăng nhập
        </Link>
      </p>
    </AuthLayout>
  );
}
