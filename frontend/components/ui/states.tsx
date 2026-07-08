import { ReactNode } from "react";

export function LoadingState({ label = "Đang tải dữ liệu..." }: { label?: string }) {
  return <div className="state">{label}</div>;
}

export function EmptyState({ title = "Chưa có dữ liệu", children }: { title?: string; children?: ReactNode }) {
  return (
    <div className="state">
      <strong>{title}</strong>
      {children ? <div style={{ marginTop: 8 }}>{children}</div> : null}
    </div>
  );
}

export function ErrorState({ message }: { message: string }) {
  return (
    <div className="state" style={{ color: "var(--danger)" }}>
      {message}
    </div>
  );
}
