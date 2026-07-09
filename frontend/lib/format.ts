export function money(value?: number | null) {
  const amount = Number(value || 0);
  return moneyFromVnd(amount * 1000);
}

export function moneyFromVnd(value?: number | null) {
  return `${new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 0
  }).format(Number(value || 0))} VND`;
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
