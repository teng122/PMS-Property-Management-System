import { ReactNode } from "react";

export function PageHeading({
  title,
  description,
  action
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="topbar">
      <div>
        <h1 className="page-title">{title}</h1>
        {description ? <p className="muted" style={{ margin: "8px 0 0" }}>{description}</p> : null}
      </div>
      {action}
    </div>
  );
}
