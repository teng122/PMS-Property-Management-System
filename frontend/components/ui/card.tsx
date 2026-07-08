import { HTMLAttributes, ReactNode } from "react";

export function Card({
  children,
  className = "",
  ...props
}: { children: ReactNode; className?: string } & HTMLAttributes<HTMLElement>) {
  return <section className={["card", className].filter(Boolean).join(" ")} {...props}>{children}</section>;
}

export function CardHeader({
  title,
  description,
  action
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="card-header">
      <div>
        <h2 className="card-title">{title}</h2>
        {description ? <p className="muted" style={{ margin: "6px 0 0" }}>{description}</p> : null}
      </div>
      {action}
    </div>
  );
}
