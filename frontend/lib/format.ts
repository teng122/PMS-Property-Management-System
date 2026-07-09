export function money(value?: number | null) {
  const amount = Number(value || 0);
  const amountInVnd = Math.abs(amount) < 10000 ? amount * 1000 : amount;

  return `${new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 0
  }).format(amountInVnd)} VND`;
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
