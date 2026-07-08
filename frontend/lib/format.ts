export function money(value?: number | null) {
  return `$${Number(value || 0).toFixed(2)}`;
}

export function dateTime(value?: string | null) {
  if (!value) return "N/A";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

export function dateOnly(value?: string | null) {
  if (!value) return "N/A";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium"
  }).format(new Date(value));
}
