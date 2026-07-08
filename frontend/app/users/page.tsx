"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { Modal } from "@/components/ui/modal";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { identityApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import type { Role } from "@/types/api";

const roles: Role[] = ["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"];

const schema = z.object({
  username: z.string().min(3),
  password: z.string().min(6),
  fullName: z.string().min(2),
  email: z.string().email().optional().or(z.literal("")),
  role: z.enum(["ADMIN", "RECEPTIONIST", "STAFF", "CUSTOMER"])
});

type UserForm = z.infer<typeof schema>;

export default function UsersPage() {
  return (
    <ProtectedPage roles={["ADMIN"]}>
      <UsersContent />
    </ProtectedPage>
  );
}

function UsersContent() {
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const users = useQuery({ queryKey: ["users"], queryFn: identityApi.getUsers });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string; role: Role }) => identityApi.updateRole(id, role),
    onSuccess: async () => {
      setMessage("Đã đổi role.");
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    }
  });

  const blockMutation = useMutation({
    mutationFn: identityApi.toggleBlock,
    onSuccess: async () => {
      setMessage("Đã cập nhật trạng thái tài khoản.");
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    }
  });

  return (
    <>
      <PageHeading title="Quản trị tài khoản" description="Chỉ ADMIN có quyền tạo user, đổi role, khoá/mở khoá." action={<Button onClick={() => setOpen(true)}>Tạo user</Button>} />
      <Card>
        <CardHeader title="Danh sách user" />
        {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
        {users.isLoading ? <LoadingState /> : null}
        {users.isError ? <ErrorState message={extractErrorMessage(users.error)} /> : null}
        {roleMutation.isError ? <ErrorState message={extractErrorMessage(roleMutation.error)} /> : null}
        {blockMutation.isError ? <ErrorState message={extractErrorMessage(blockMutation.error)} /> : null}
        <DataTable
          rows={users.data || []}
          getRowKey={(user) => user.id}
          searchableText={(user) => `${user.username} ${user.fullName} ${user.email} ${user.role} ${user.status}`}
          columns={[
            { key: "username", header: "Tài khoản", render: (user) => <strong>{user.username}</strong> },
            { key: "name", header: "Họ tên", render: (user) => user.fullName || "N/A" },
            { key: "email", header: "Email", render: (user) => user.email || "N/A" },
            {
              key: "role",
              header: "Role",
              render: (user) => (
                <select
                  className="select"
                  value={user.role.replace(/^ROLE_/, "")}
                  onChange={(event) => roleMutation.mutate({ id: user.id, role: event.target.value as Role })}
                >
                  {roles.map((role) => <option key={role} value={role}>{role}</option>)}
                </select>
              )
            },
            { key: "status", header: "Trạng thái", render: (user) => <StatusBadge status={user.status} /> },
            {
              key: "actions",
              header: "Thao tác",
              render: (user) => (
                <Button variant="secondary" onClick={() => blockMutation.mutate(user.id)}>
                  {user.status === "BLOCKED" ? "Mở khoá" : "Khoá"}
                </Button>
              )
            }
          ]}
        />
      </Card>
      <CreateUserModal open={open} onClose={() => setOpen(false)} onSaved={async () => {
        setOpen(false);
        setMessage("Đã tạo user.");
        await queryClient.invalidateQueries({ queryKey: ["users"] });
      }} />
    </>
  );
}

function CreateUserModal({ open, onClose, onSaved }: { open: boolean; onClose: () => void; onSaved: () => Promise<void> }) {
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<UserForm>({
    resolver: zodResolver(schema),
    defaultValues: { role: "CUSTOMER" }
  });

  async function onSubmit(values: UserForm) {
    setError(null);
    try {
      await identityApi.createUser({ ...values, email: values.email || undefined });
      await onSaved();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  return (
    <Modal open={open} title="Tạo user" onClose={onClose}>
      <form className="grid cols-2" onSubmit={handleSubmit(onSubmit)}>
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
        <div className="field">
          <label>Role</label>
          <select className="select" {...register("role")}>{roles.map((role) => <option key={role} value={role}>{role}</option>)}</select>
        </div>
        {error ? <ErrorState message={error} /> : null}
        <div className="actions" style={{ gridColumn: "1 / -1" }}>
          <Button disabled={isSubmitting}>Tạo</Button>
          <Button type="button" variant="ghost" onClick={onClose}>Huỷ</Button>
        </div>
      </form>
    </Modal>
  );
}
