import type {
  AmenityOrderStatus,
  BookingStatus,
  CleaningTaskStatus,
  InvoiceStatus,
  RoomStatus,
  UserStatus
} from "@/types/api";

type Status = BookingStatus | RoomStatus | AmenityOrderStatus | CleaningTaskStatus | InvoiceStatus | UserStatus | string;

const statusLabels: Record<string, string> = {
  AVAILABLE: "Sẵn sàng",
  OCCUPIED: "Đang có khách",
  DIRTY: "Chờ dọn",
  CLEANING: "Đang dọn",
  MAINTENANCE: "Bảo trì",
  PENDING: "Đang xử lý",
  AWAITING_DEPOSIT: "Chờ đặt cọc",
  CONFIRMED: "Đã xác nhận",
  CHECKED_IN: "Đã nhận phòng",
  CHECKED_OUT: "Đã trả phòng",
  CANCELLED: "Đã huỷ",
  NO_SHOW: "Khách không đến",
  PREPARING: "Đang chuẩn bị",
  DELIVERED: "Đã giao",
  BILLED: "Đã lên hoá đơn",
  REJECTED: "Đã từ chối",
  IN_PROGRESS: "Đang thực hiện",
  COMPLETED: "Hoàn tất",
  UNPAID: "Chưa thanh toán",
  PAID: "Đã thanh toán",
  ACTIVE: "Hoạt động",
  BLOCKED: "Đã khoá"
};

function toneForStatus(status: Status) {
  if (["AVAILABLE", "CONFIRMED", "CHECKED_IN", "DELIVERED", "COMPLETED", "PAID", "ACTIVE"].includes(status)) {
    return "success";
  }
  if (["PENDING", "AWAITING_DEPOSIT", "PREPARING", "IN_PROGRESS", "UNPAID", "CLEANING"].includes(status)) {
    return "warning";
  }
  if (["CANCELLED", "NO_SHOW", "REJECTED", "BLOCKED"].includes(status)) {
    return "danger";
  }
  return "neutral";
}

export function statusLabel(status?: Status | null) {
  if (!status) return "Không có";
  return statusLabels[status] || status;
}

export function StatusBadge({ status }: { status?: Status | null }) {
  if (!status) return <span className="badge neutral">Không có</span>;
  return <span className={`badge ${toneForStatus(status)}`}>{statusLabel(status)}</span>;
}
