# Cẩm Nang Nghiệp Vụ & Logic Chi Tiết Hệ Thống (Smart Hotel PMS)

Tài liệu này phân tích chi tiết tất cả các thao tác, logic nghiệp vụ, cơ chế lưu trữ dữ liệu, và cách thức hoạt động của hệ thống quản lý khách sạn **Smart Hotel PMS**.

---

## 1. Bản Đồ Cơ Sở Dữ Liệu (Entities & Tables)

Hệ thống sử dụng cơ sở dữ liệu phân tán (mỗi Microservice quản lý DB riêng của mình) thông qua các thực thể JPA (JPA Entities):

### 1.1. Identity Service (`identity-service-db`)
* **User (`User`):** Lưu trữ thông tin tài khoản bao gồm `username`, `password` (đã mã hóa BCrypt), `fullName`, `email`, `role` (vai trò), `status` (trạng thái hoạt động: `ACTIVE`, `BLOCKED`), và `refreshToken` phục vụ cấp lại token.
* **Role (`Role`):** Bảng phụ trợ chứa thông tin phân quyền nếu cần (thực tế vai trò đang được lưu trực tiếp dưới dạng chuỗi chuẩn hóa như `ROLE_ADMIN`, `ROLE_RECEPTIONIST`, `ROLE_STAFF`, `ROLE_CUSTOMER` trong trường `role` của bảng User).

### 1.2. Room Service (`room-service-db`)
* **Room (`Room`):** Lưu trữ thông tin các phòng vật lý trong khách sạn: `roomNumber` (số phòng), `roomType` (loại phòng: STANDARD, DELUXE, VIP...), `basePrice` (giá cơ bản mỗi đêm), `status` (trạng thái phòng: `AVAILABLE`, `OCCUPIED`, `CLEANING`, `DIRTY`, `MAINTENANCE`), và `reservedBookingId` (ID đơn đặt phòng đang giữ phòng này để nhận phòng).

### 1.3. Booking Service (`booking-service-db`)
* **Booking (`Booking`):** Quản lý vòng đời đặt phòng: `customerId` (mã khách hàng), `roomId` (mã phòng), `checkInDate` (ngày nhận phòng), `checkOutDate` (ngày trả phòng), `status` (trạng thái đặt phòng: `PENDING`, `AWAITING_DEPOSIT`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`), `totalAmount` (tổng tiền phòng dự kiến), `depositAmount` (tiền đặt cọc cần thanh toán - mặc định 50%), và `isDepositPaid` (đã thanh toán cọc chưa).

### 1.4. Amenities Service (`amenities-service-db`)
* **Amenity (`Amenity`):** Danh mục dịch vụ tiện ích: `name` (tên dịch vụ: giặt là, ăn sáng...), `price` (đơn giá), và `isReturnable` (dịch vụ có thể hoàn trả/hủy khi đang chuẩn bị hay không).
* **AmenityOrder (`AmenityOrder`):** Đơn đặt dịch vụ phòng: `roomId` (số phòng gọi), `bookingId` (mã đơn đặt phòng tương ứng), `status` (trạng thái đơn: `PENDING`, `PREPARING`, `DELIVERED`, `REJECTED`, `BILLED`), và `totalPrice` (tổng tiền đơn hàng).
* **AmenityOrderDetail (`AmenityOrderDetail`):** Chi tiết đơn dịch vụ: `amenityId` (mã dịch vụ), `quantity` (số lượng), và `price` (đơn giá tại thời điểm gọi).

### 1.5. Housekeeping Service (`housekeeping-service-db`)
* **CleaningTask (`CleaningTask`):** Công việc dọn dẹp buồng phòng: `roomId` (phòng cần dọn), `staffId` (nhân viên được phân công/nhận việc), `status` (trạng thái công việc: `PENDING`, `IN_PROGRESS`, `COMPLETED`), `assignedAt` (thời điểm nhận việc), và `completedAt` (thời điểm hoàn thành).

### 1.6. Billing Service (`billing-service-db`)
* **Invoice (`Invoice`):** Hóa đơn thanh toán khi checkout: `bookingId` (mã đặt phòng), `roomCharge` (tiền phòng thực tế), `serviceCharge` (tiền dịch vụ phòng), `tax` (thuế VAT - 10%), `depositAmount` (tiền cọc đã khấu trừ), `totalAmount` (tổng tiền thực tế khách cần trả thêm), `status` (trạng thái hóa đơn: `UNPAID`, `PAID`), và `paidAt` (thời điểm thanh toán).

---

## 2. Các Luồng Nghiệp Vụ Phối Hợp & Saga Workflow

Hệ thống áp dụng kiến trúc hướng sự kiện (Event-Driven Architecture) thông qua **Apache Kafka** để điều phối các giao dịch phân tán (Saga Pattern).

### 2.1. Luồng Đặt phòng Trực tuyến (Online Booking Flow)
Luồng này thực hiện giữ phòng tạm thời và yêu cầu khách hàng đặt cọc trước khi xác nhận đơn.

```mermaid
autonumber
Client ->> Booking Service: POST /api/bookings (Đặt phòng trực tuyến)
Booking Service ->> Room Service (Feign): Lấy thông tin phòng vật lý (basePrice)
Booking Service ->> Booking Service: Tính tổng tiền phòng, tiền cọc (50%), tạo đơn đặt phòng ở trạng thái PENDING
Booking Service -->> Kafka (booking-events): Phát sự kiện BookingCreatedEvent
Room Service ->> Kafka (booking-events): Nhận BookingCreatedEvent
Room Service ->> Room Service: Kiểm tra nếu phòng đang bảo trì (MAINTENANCE) -> Báo lỗi
Room Service ->> Booking Service (Feign): checkAvailability() để kiểm tra trùng lịch
alt Không trùng lịch & Không bảo trì (Thành công)
    Room Service -->> Kafka (room-events): Phát sự kiện RoomReservedEvent
    Booking Service ->> Kafka (room-events): Nhận RoomReservedEvent
    Booking Service ->> Booking Service: Cập nhật Booking -> AWAITING_DEPOSIT
else Trùng lịch hoặc Đang bảo trì (Thất bại)
    Room Service -->> Kafka (room-events): Phát sự kiện RoomReservationFailedEvent
    Booking Service ->> Kafka (room-events): Nhận RoomReservationFailedEvent
    Booking Service ->> Booking Service: Cập nhật Booking -> CANCELLED (Giao dịch bù trừ)
end
```

### 2.2. Luồng Khách vãng lai nhận phòng (Walk-in Check-in)
Dành cho khách đến trực tiếp tại quầy mà không đặt trước.
1. **Lễ tân** gửi yêu cầu `POST /api/bookings/walk-in`.
2. **Booking Service** gọi đồng bộ sang **Room Service** lấy thông tin phòng vật lý. Phòng bắt buộc phải ở trạng thái `AVAILABLE`.
3. **Booking Service** tính tiền phòng thực tế, lưu đơn đặt phòng mới ở trạng thái `CHECKED_IN`, đặt tiền cọc bằng `0`, và xác nhận đã trả cọc (`isDepositPaid = true`).
4. **Booking Service** gọi đồng bộ Feign sang **Room Service** cập nhật trạng thái phòng vật lý thành `OCCUPIED`.

### 2.3. Luồng Nhận phòng đã đặt trước (Pre-booked Check-in)
Khách đã đặt phòng online và đến làm thủ tục nhận phòng tại quầy.
1. **Lễ tân** gửi yêu cầu `POST /api/bookings/{id}/check-in`.
2. **Booking Service** kiểm tra:
   * Trạng thái đơn đặt phòng phải là `CONFIRMED` (đã đặt cọc thành công).
   * Gọi **Identity Service** xác thực sự tồn tại của khách hàng.
   * Gọi **Room Service** lấy trạng thái phòng vật lý: Phòng phải đang ở trạng thái `AVAILABLE`, HOẶC đang `OCCUPIED` nhưng có `reservedBookingId` khớp với ID đơn đặt phòng này.
3. **Booking Service** thực hiện cập nhật cục bộ trạng thái đơn sang `CHECKED_IN` và cập nhật ngày check-in thực tế thành thời gian hiện tại.
4. **Booking Service** gọi đồng bộ sang **Room Service** đổi trạng thái phòng vật lý thành `OCCUPIED`.
5. *Cơ chế an toàn (Revert):* Nếu cuộc gọi Feign sang Room Service thất bại, hệ thống sẽ thực hiện khôi phục (revert) trạng thái đơn đặt phòng về lại `CONFIRMED` để đảm bảo tính nhất quán dữ liệu.
### 2.4. Luồng Gọi Dịch vụ phòng (Amenity Order Flow)
Khách tại phòng đã check-in gọi dịch vụ (đồ ăn, giặt là, spa...)
```mermaid
autonumber
Client ->> Amenities Service: POST /api/amenities/order
Amenities Service ->> Booking Service (Feign): GET /api/bookings/active/room/{roomId}
Booking Service ->> Booking Service: Kiểm tra phòng có booking ở trạng thái CHECKED_IN không
alt Phòng đã CHECKED_IN
    Booking Service -->> Amenities Service: Trả về thông tin Active Booking
    Amenities Service ->> Amenities Service: Gán bookingId tự động, lưu đơn trạng thái PREPARING
    Amenities Service -->> Client: Trả về 201 Created (Đơn dịch vụ phòng thành công)
else Phòng chưa CHECKED_IN (Chưa nhận phòng)
    Booking Service -->> Amenities Service: Trả về 404/Không có Active Booking
    Amenities Service -->> Client: Trả về lỗi 400 Bad Request (Từ chối gọi dịch vụ)
end
```

### 2.5. Luồng Trả phòng & Thanh toán (Checkout & Billing Flow)
Khi khách làm thủ tục trả phòng, giải phóng phòng và tự động gộp hóa đơn tổng hợp.
```mermaid
autonumber
Client ->> Booking Service: POST /api/bookings/{id}/check-out
Booking Service ->> Booking Service: Kiểm tra đơn phải đang CHECKED_IN
Booking Service ->> Room Service (Feign): Lấy thông tin phòng vật lý (basePrice)
Booking Service ->> Booking Service: Tính số đêm thực tế, tính tiền phòng thực tế (roomCharge)
Booking Service ->> Booking Service: Cập nhật cục bộ Booking -> CHECKED_OUT, ghi nhận ngày checkout thực tế
Booking Service -->> Kafka (checkout-events): Phát sự kiện CheckoutStartedEvent (chứa roomCharge, depositAmount)

par Xử lý tại Room Service
    Room Service ->> Kafka (checkout-events): Nhận CheckoutStartedEvent
    Room Service ->> Room Service: Cập nhật trạng thái phòng vật lý -> DIRTY
and Xử lý tại Housekeeping Service
    Housekeeping Service ->> Kafka (checkout-events): Nhận CheckoutStartedEvent
    Housekeeping Service ->> Housekeeping Service: Tạo tác vụ dọn dẹp CleaningTask (PENDING) cho phòng
and Xử lý tại Amenities Service
    Amenities Service ->> Kafka (checkout-events): Nhận CheckoutStartedEvent
    Amenities Service ->> Amenities Service: Lấy các đơn dịch vụ chưa thanh toán của bookingId
    Amenities Service ->> Amenities Service: Tính tổng chi phí dịch vụ (serviceCharge)
    Amenities Service ->> Amenities Service: Cập nhật tất cả các đơn dịch vụ đó sang BILLED
    Amenities Service -->> Kafka (amenity-calculated-events): Phát sự kiện AmenityChargesCalculatedEvent (chứa roomCharge, serviceCharge, depositAmount)
end

Billing Service ->> Kafka (amenity-calculated-events): Nhận AmenityChargesCalculatedEvent
Billing Service ->> Billing Service: Tính tổng phụ = roomCharge + serviceCharge
Billing Service ->> Billing Service: Tính tổng hóa đơn = tổng phụ + 10% VAT - depositAmount
Billing Service ->> Billing Service: Lưu hóa đơn mới ở trạng thái UNPAID
```
Sau khi tạo hóa đơn `UNPAID` thành công:
1. **Lễ tân** gọi `POST /api/invoices/{id}/pay` để khởi tạo cổng thanh toán. **Billing Service** sẽ trả về một liên kết/QR code thanh toán VietQR chứa mã hóa đơn và số tiền cần thanh toán.
2. Khi khách hàng chuyển khoản thành công, hệ thống (hoặc lễ tân xác nhận thủ công qua API) gọi `POST /api/invoices/{id}/confirm-payment`, **Billing Service** chuyển trạng thái hóa đơn sang `PAID` và ghi nhận thời gian trả tiền thực tế.

### 2.6. Luồng Dọn phòng (Housekeeping Lifecycle)
Sau khi khách checkout, phòng chuyển sang `DIRTY` và một `CleaningTask` được tạo ra:
1. **Nhân viên buồng phòng (STAFF)** xem danh sách phòng bẩn và nhận việc qua `POST /api/housekeeping/tasks/{id}/start`.
2. **Housekeeping Service** chuyển trạng thái công việc sang `IN_PROGRESS`, ghi nhận ID nhân viên dọn phòng, và gọi Feign đồng bộ sang **Room Service** đổi trạng thái phòng vật lý thành `CLEANING`.
3. Khi dọn xong, nhân viên gửi yêu cầu `POST /api/housekeeping/tasks/{id}/complete`.
4. **Housekeeping Service** đổi trạng thái công việc thành `COMPLETED`, ghi nhận thời gian hoàn thành, và phát sự kiện `RoomCleanedEvent` lên Kafka topic `housekeeping-events`.
5. **Room Service** nhận sự kiện `RoomCleanedEvent` từ Kafka và cập nhật trạng thái phòng vật lý thành `AVAILABLE` (sẵn sàng đón khách mới).

---

## 3. Các Logic Ràng Buộc & Tính Toán Nghiệp Vụ Cụ Thể (Algorithm & Rules)

### 3.1. Phép tính số ngày lưu trú (Nights Calculation)
Số ngày lưu trú thực tế được tính bằng hiệu số giữa ngày nhận phòng (check-in) và ngày trả phòng (check-out) của khách hàng:
$$\text{Nights} = \max(1, \text{DaysBetween}(\text{CheckInDate}, \text{CheckOutDate}))$$
* *Quy tắc tối thiểu:* Nếu khách check-in và check-out trong cùng một ngày, hệ thống vẫn tính tròn là **1 đêm** để đảm bảo doanh thu tối thiểu cho khách sạn.

### 3.2. Tính toán Hóa đơn tại Billing Service
Các thành phần hóa đơn được tính toán như sau:
* **Tiền phòng (`roomCharge`):**
  $$\text{roomCharge} = \text{basePrice} \times \text{Nights}$$
* **Tiền dịch vụ (`serviceCharge`):** Tổng tiền của toàn bộ các đơn dịch vụ phòng chưa trả (`PENDING`, `PREPARING` hoặc `DELIVERED`):
  $$\text{serviceCharge} = \sum (\text{Quantity} \times \text{AmenityPrice})$$
* **Thuế GTGT (`tax`):** Cố định bằng 10% tổng số tiền phòng và dịch vụ:
  $$\text{tax} = (\text{roomCharge} + \text{serviceCharge}) \times 10\%$$
* **Tổng tiền cuối cùng khách cần trả (`totalAmount`):**
  $$\text{totalAmount} = \max(0, \text{roomCharge} + \text{serviceCharge} + \text{tax} - \text{depositAmount})$$
  *(Nếu tiền cọc lớn hơn tổng hóa đơn, tổng số tiền khách cần trả thêm sẽ bằng 0).*

### 3.3. Ràng buộc Hủy đơn dịch vụ phòng (Amenities Order Cancellation Rules)
Hành động chuyển trạng thái đơn dịch vụ sang `REJECTED` (hủy đơn) được kiểm soát bởi các logic nghiệp vụ rất chặt chẽ:
1. **Phân quyền người thực hiện:** Chỉ nhân viên có vai trò `ROLE_RECEPTIONIST` hoặc `ROLE_ADMIN` mới được phép hủy đơn đặt dịch vụ phòng. Khách hàng thông thường không được phép tự hủy đơn.
2. **Kiểm tra trạng thái hóa đơn:** Nếu đơn tiện ích đã được gom hóa đơn (trạng thái `BILLED`) hoặc đơn đã bị hủy từ trước (`REJECTED`), hệ thống sẽ chặn không cho phép hủy đơn.
3. **Kiểm tra tính chất hoàn trả (Returnable):**
   * Nếu đơn dịch vụ đang trong trạng thái chế biến (`PREPARING`) hoặc đã giao tới phòng (`DELIVERED`):
   * Hệ thống sẽ truy vấn danh mục gốc để kiểm tra thuộc tính `isReturnable` (ví dụ: món ăn sáng đã nấu xong thì không thể trả lại, nhưng dịch vụ giặt là chưa thực hiện có thể xem xét).
   * Nếu `isReturnable == false`, hệ thống sẽ ném ra lỗi chặn thao tác hủy.

### 3.4. Kiểm tra xung đột lịch đặt phòng (Overbooking Prevention)
Hệ thống kiểm tra trùng lịch đặt phòng thông qua câu lệnh SQL tối ưu hóa trong `BookingRepository`:
Một phòng có ID `roomId` được coi là trống lịch trong khoảng thời gian $[Start, End]$ khi và chỉ khi không tồn tại bất kỳ đơn đặt phòng nào khác thỏa mãn đồng thời:
1. Khớp `roomId`.
2. Không phải là chính đơn hàng hiện tại đang kiểm tra sửa đổi (`id != bookingId`).
3. Đơn đặt phòng đó đang ở trạng thái hoạt động: `AWAITING_DEPOSIT`, `CONFIRMED`, hoặc `CHECKED_IN`.
4. Có khoảng thời gian giao thoa với $[Start, End]$:
   $$\text{CheckOutDate} > Start \quad \text{AND} \quad \text{CheckInDate} < End$$

### 3.5. Nhận diện tấn công tái sử dụng Refresh Token (Reuse Detection)
Để tăng cường bảo mật cho `identity-service`, khi khách hàng yêu cầu cấp lại Access Token mới bằng Refresh Token:
1. **validateToken:** Hệ thống kiểm tra xem token gửi lên có hợp lệ về chữ ký và thời gian hết hạn hay không.
2. **Kiểm tra trạng thái khóa:** User sở hữu token phải đang hoạt động (`ACTIVE`).
3. **Phát hiện tái sử dụng:**
   * Hệ thống so sánh Refresh Token gửi lên với trường `refreshToken` đang được lưu trong cơ sở dữ liệu của User đó.
   * Nếu Refresh Token gửi lên hợp lệ nhưng **không trùng khớp** với token trong DB:
     * Điều này chứng tỏ Refresh Token này là một token cũ đã được xoay vòng trước đó và đã bị lộ lọt (kẻ tấn công hoặc client cũ đang gửi lại).
     * Ngay lập tức, hệ thống sẽ thực hiện **hủy bỏ phiên làm việc** bằng cách xóa sạch Refresh Token hiện tại trong DB của người dùng về `null`, bắt buộc người dùng thực tế phải đăng nhập lại từ đầu để ngăn ngừa rủi ro.

---

## 4. Tổng Kết Luồng Giao Tiếp Nội Bộ (Inter-service Map)

Hệ thống PMS kết hợp hài hòa giữa giao tiếp đồng bộ và không đồng bộ:

* **Đồng bộ (Feign):**
  * `booking-service` $\rightarrow$ `room-service`: Tra cứu chi tiết phòng, cập nhật trạng thái phòng vật lý khi check-in, check-out và check availability.
  * `booking-service` $\rightarrow$ `identity-service`: Tra cứu thông tin người dùng.
  * `billing-service` $\rightarrow$ `amenities-service`: Lấy các dịch vụ chưa trả tiền của phòng/booking để gộp hóa đơn và thực hiện đóng đơn dịch vụ sang `BILLED`.
  * `billing-service` $\rightarrow$ `booking-service`: Lấy thông tin cọc của booking.
  * `housekeeping-service` $\rightarrow$ `room-service`: Cập nhật trạng thái phòng vật lý thành `CLEANING` khi nhân viên nhận việc.

* **Không đồng bộ (Kafka):**
  * `booking-events` $\rightarrow$ `room-service`: Xử lý giữ chỗ phòng tạm thời khi tạo đơn mới.
  * `room-events` $\rightarrow$ `booking-service`: Xác nhận giữ chỗ thành công hoặc thất bại để đổi trạng thái đơn đặt phòng.
  * `amenity-order-events` $\rightarrow$ `booking-service`: Kiểm tra tính hợp lệ của đơn gọi dịch vụ phòng (khách phải đang ở phòng đó).
  * `booking-validation-events` $\rightarrow$ `amenities-service`: Đồng ý/Từ chối phục vụ đơn dịch vụ dựa trên kết quả xác thực.
  * `checkout-events` $\rightarrow$ `room-service` & `billing-service` & `housekeeping-service`: Kích hoạt quy trình đổi trạng thái phòng sang `DIRTY`, tạo hóa đơn và tạo tác vụ dọn dẹp phòng mới.
  * `housekeeping-events` $\rightarrow$ `room-service`: Cập nhật lại phòng vật lý thành `AVAILABLE` sau khi dọn dẹp hoàn tất.
