"use client";

import { ReactNode, useMemo, useState } from "react";
import { EmptyState } from "@/components/ui/states";

export interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  width?: string | number;
  align?: "left" | "center" | "right";
}

export function DataTable<T>({
  rows,
  columns,
  getRowKey,
  searchableText,
  emptyLabel = "Không có dữ liệu"
}: {
  rows: T[];
  columns: Column<T>[];
  getRowKey: (row: T) => string;
  searchableText?: (row: T) => string;
  emptyLabel?: string;
}) {
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    if (!searchableText || !search.trim()) return rows;
    const q = search.trim().toLowerCase();
    return rows.filter((row) => searchableText(row).toLowerCase().includes(q));
  }, [rows, search, searchableText]);

  return (
    <div>
      {searchableText ? (
        <div className="toolbar">
          <input
            className="input"
            style={{ maxWidth: 360 }}
            placeholder="Tìm kiếm..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
          <span className="muted">{filtered.length} kết quả</span>
        </div>
      ) : null}
      {filtered.length === 0 ? (
        <EmptyState title={emptyLabel} />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                {columns.map((column) => (
                  <th key={column.key} style={{ width: column.width, textAlign: column.align }}>{column.header}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((row) => (
                <tr key={getRowKey(row)}>
                  {columns.map((column) => (
                    <td key={column.key} style={{ width: column.width, textAlign: column.align }}>{column.render(row)}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
