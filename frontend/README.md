# Smart Hotel PMS — Frontend (Next.js)

Frontend cho hệ thống microservices Smart Hotel PMS. Xem kế hoạch chi tiết ở `../../FRONTEND_PLAN.md`.

## Công nghệ
Next.js 14 (App Router) · TypeScript · Tailwind CSS · TanStack Query · Zustand · Axios · Recharts.

## Chạy dự án

```bash
npm install
npm run dev      # http://localhost:3000
npm run build    # build production
```

Backend cần chạy trước (qua Docker: `docker-compose up` ở thư mục gốc repo). Frontend gọi
toàn bộ qua **API Gateway** `http://localhost:8080`.

## Biến môi trường (`.env.local`)

```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_HOUSEKEEPING_URL=http://localhost:8084   # tạm, vì housekeeping chưa có route gateway
```

## Cấu trúc & vai trò
- `/login`, `/register` — xác thực JWT (identity-service).
- `/customer` — Khách hàng: tìm phòng, đặt phòng, thanh toán QR (đếm ngược 15'), gọi dịch vụ.
- `/staff` — Nhân viên (Lễ tân + Lao công): sơ đồ phòng, check-in/out, checkout+hóa đơn, dọn phòng.
- `/admin` — Quản lý: dashboard, quản lý dịch vụ, tra cứu hóa đơn.

Roles backend: `CUSTOMER · STAFF · ADMIN`. Route được bảo vệ bằng `AuthGuard` theo role.

## Ghi chú quan trọng
Backend là các CRUD service rời rạc — frontend **tự điều phối** các luồng đa-API
(xem `src/hooks/useBookings.ts`, `src/hooks/useInvoices.ts`):
- Thanh toán → tự `PUT booking = CONFIRMED` (nếu không, scheduler hủy sau 15').
- Check-in → `PUT booking CHECKED_IN` + `PUT room OCCUPIED`.
- Check-out → confirm payment + `PUT booking CHECKED_OUT` + `PUT room DIRTY`.

Các GAP backend đang chặn một phần chức năng (housekeeping task tự tạo, list-all rooms,
list invoices, create/price room) được nêu ở `FRONTEND_PLAN.md §10`.
