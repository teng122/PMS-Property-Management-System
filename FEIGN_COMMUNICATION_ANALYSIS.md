# Phân tích Các Luồng Giao Tiếp Đồng Bộ (OpenFeign) trong Smart Hotel PMS

Tài liệu này phân tích chi tiết các luồng giao tiếp đồng bộ nội bộ giữa các microservices bằng Spring Cloud OpenFeign, lý do nghiệp vụ bắt buộc phải chạy đồng bộ (Request-Response) thay vì sử dụng hàng đợi tin nhắn (Kafka), cùng cơ chế xử lý sự cố (Resilience4j Circuit Breaker) khi có dịch vụ con bị sập.

---

## 1. Danh sách các Interface `@FeignClient` trong hệ thống

Hệ thống Smart Hotel PMS định nghĩa **4 interface `@FeignClient`** như sau:

| # | Tên Feign Client | Đặt tại Dịch vụ (Source) | Gọi sang Dịch vụ (Target) | Các API/Method khai báo | Trạng thái & Ghi chú |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | [BookingClient](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/amenities-service/src/main/java/com/smarthotel/amenities_service/client/BookingClient.java) | **Amenities Service** | **Booking Service** | `GET /api/bookings/active/room/{roomId}` | **Đang hoạt động.** Dùng để lấy thông tin booking đang hoạt động của phòng trước khi cho phép đặt dịch vụ phòng. |
| **2** | [RoomClient](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/booking-service/src/main/java/com/smarthotel/booking_service/client/RoomClient.java) | **Booking Service** | **Room Service** | - `GET /api/rooms/{id}`<br/>- `PUT /api/rooms/{id}/status`<br/>- `GET /api/rooms/all` | **Đang hoạt động.** Dùng để xác thực phòng tồn tại, đồng bộ trạng thái phòng khi check-in/walk-in, và lấy danh sách phòng để lọc phòng trống. |
| **3** | [RoomServiceClient](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/housekeeping-service/src/main/java/com/smarthotel/housekeeping_service/client/RoomServiceClient.java) | **Housekeeping Service** | **Room Service** | `PUT /api/rooms/{id}/status` | **Đang hoạt động.** Dùng để cập nhật trạng thái phòng thành `CLEANING` đồng bộ khi nhân viên bắt đầu dọn phòng. |
| **4** | [AmenitiesFeignClient](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/amenities-service/src/main/java/com/smarthotel/amenities_service/client/AmenitiesFeignClient.java) | **Amenities Service** | **Amenities Service** (Gọi chính nó) | - `GET /room/{roomId}/unpaid`<br/>- `PUT /orders/{id}/status` | **Chưa sử dụng.** File này khai báo Feign Client gọi chính dịch vụ của nó, được thiết kế để các dịch vụ khác (như Billing) import sử dụng. Tuy nhiên trong mã nguồn hiện tại, Billing Service giao tiếp thông qua Kafka nên client này không có class nào sử dụng thực tế. |

---

## 2. Tại sao các luồng này BẮT BUỘC phải chạy đồng bộ?

Trong kiến trúc Microservices, giao tiếp bất đồng bộ qua hàng đợi tin nhắn (Kafka/RabbitMQ) giúp tăng tính độc lập và chịu lỗi. Tuy nhiên, đối với 3 luồng giao tiếp OpenFeign trên, việc chạy **đồng bộ (Request-Response)** là bắt buộc vì các lý do sau:

### 2.1. Xác thực cư trú của khách trước khi gọi đồ ăn (Amenities $\rightarrow$ Booking)
* **Luồng xử lý:** Khi khách hàng đặt dịch vụ tiện ích, [AmenityOrderService](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/amenities-service/src/main/java/com/smarthotel/amenities_service/service/AmenityOrderService.java#L53-L69) gọi `BookingClient` để lấy thông tin booking hiện tại của phòng.
* **Lý do bắt buộc đồng bộ:**
  1. **Kiểm tra quyền sở hữu thời gian thực (Real-time Authorization):** Hệ thống cần biết ngay lập tức khách hàng gửi request có đúng là người đang thuê phòng đó hay không (chống việc tài khoản này đặt đồ ăn/tiện ích tính tiền vào phòng của khách hàng khác). Nếu dùng Kafka, hệ thống sẽ phải tạo đơn hàng ở trạng thái chờ xác thực, gây phức tạp hóa nghiệp vụ và làm chậm trải nghiệm của khách hàng.
  2. **Tính phụ thuộc dữ liệu (Data Dependency):** Amenities Service cần lấy được `bookingId` của phòng từ Booking Service để lưu vào cơ sở dữ liệu của mình. Nếu không có `bookingId` ngay tại thời điểm tạo đơn, hệ thống sẽ không thể gộp các đơn dịch vụ phòng này vào hóa đơn tổng khi khách hàng thực hiện checkout.

### 2.2. Kiểm tra thông tin phòng & Cập nhật trạng thái (Booking $\rightarrow$ Room)
* **Luồng xử lý:** Khi lễ tân làm thủ tục Check-in hoặc Walk-in, Booking Service gọi `RoomClient` để lấy thông tin phòng vật lý và yêu cầu cập nhật trạng thái phòng sang `OCCUPIED`.
* **Lý do bắt buộc đồng bộ:**
  1. **Ngăn chặn Double Booking (Đặt trùng phòng):** Cập nhật trạng thái phòng vật lý sang `OCCUPIED` phải được thực hiện đồng bộ ngay tức thì. Nếu dùng Kafka gửi sự kiện bất đồng bộ, độ trễ xử lý (processing lag) có thể khiến một nhân viên lễ tân khác vẫn nhìn thấy phòng đó đang trống và tiếp tục gán cho một khách hàng khác.
  2. **Tính toán chi phí tức thời (Pre-checkout Summary):** Khi khách yêu cầu tính tiền tạm tính tại quầy lễ tân, Booking Service cần gọi Room Service để lấy giá phòng thực tế. Khách hàng không thể đứng chờ lễ tân đợi sự kiện Kafka phản hồi về để tính tiền.

### 2.3. Bắt đầu dọn dẹp phòng (Housekeeping $\rightarrow$ Room)
* **Luồng xử lý:** Khi nhân viên buồng phòng nhận nhiệm vụ dọn phòng bẩn, Housekeeping Service gọi `RoomServiceClient` để cập nhật trạng thái phòng sang `CLEANING`.
* **Lý do bắt buộc đồng bộ:** Tránh việc phòng đang được dọn dẹp nhưng quầy lễ tân không biết (do độ trễ tin nhắn Kafka) và làm thủ tục check-in cho khách vào phòng đó.
* **So sánh đối chiếu:** Khi nhân viên **hoàn thành** dọn phòng (`completeTask`), hệ thống lại sử dụng **Kafka** để gửi sự kiện `RoomCleanedEvent` bất đồng bộ. Lý do là vì khi phòng đã sạch, việc chuyển trạng thái phòng về `AVAILABLE` có thể xử lý trễ vài giây dưới nền mà không ảnh hưởng nghiêm trọng đến tính toàn vẹn tức thời của hệ thống.

---

## 3. Cơ chế hoạt động của Resilience4j Circuit Breaker khi có dịch vụ sập

Hệ thống có hai chốt chặn bảo vệ chống sập dây chuyền (Cascading Failures) thông qua Resilience4j:

### 3.1. Tại các OpenFeign Client (Dịch vụ con gọi nhau)
* **Cấu hình:** Được kích hoạt tập trung qua Config Server trong file [application.yml](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/infrastructure-services/config-server/src/main/resources/configfiles/application.yml):
  ```yaml
  spring:
    cloud:
      openfeign:
        circuitbreaker:
          enabled: true
        client:
          config:
            default:
              connectTimeout: 2000  # Thời gian chờ kết nối tối đa: 2 giây
              readTimeout: 5000     # Thời gian chờ đọc dữ liệu tối đa: 5 giây
  ```
* **Cách hoạt động khi dịch vụ con sập:**
  1. Giả sử **Room Service bị sập**. Khi Booking Service gọi `RoomClient.getRoomById(id)`, cuộc gọi sẽ bị lỗi (Connection Refused) hoặc bị treo quá 5 giây (Timeout).
  2. Spring Cloud Circuit Breaker sẽ đánh chặn ngoại lệ này và chuyển hướng xử lý đến lớp fallback được chỉ định là [RoomClientFallback](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/business-services/booking-service/src/main/java/com/smarthotel/booking_service/client/fallback/RoomClientFallback.java).
  3. Lớp fallback log lỗi cảnh báo hệ thống và trả về giá trị mặc định là `null` hoặc danh sách rỗng (`List.of()`).
  4. Booking Service nhận về `null` thay vì văng lỗi crash toàn hệ thống (Http 500), cho phép trả về phản hồi lỗi thân thiện cho khách hàng (ví dụ: *"Không tìm thấy thông tin phòng vật lý"*).
  5. Nếu tỷ lệ cuộc gọi lỗi vượt quá ngưỡng thiết lập (mặc định của Resilience4j là 50%), Circuit Breaker sẽ chuyển sang trạng thái **OPEN** (Mở mạch). Mọi request tiếp theo qua `RoomClient` sẽ đi thẳng vào `RoomClientFallback` mà không gửi request thực tế lên mạng nữa, giúp bảo vệ tài nguyên luồng (threads) của Booking Service không bị treo.

### 3.2. Tại API Gateway (Chốt chặn Client bên ngoài)
* **Cấu hình hiện tại:** API Gateway có import thư viện `spring-cloud-starter-circuitbreaker-resilience4j` trong `pom.xml`, nhưng trong file cấu hình [application.yml](file:///c:/Users/vuong/IdeaProjects/smart-hotel-pms/infrastructure-services/api-gateway/src/main/resources/application.yml) **chưa khai báo** filter `- CircuitBreaker` cho các route.
* **Cách hoạt động khi dịch vụ con sập:**
  * Do chưa cấu hình Gateway Filter cho Circuit Breaker, API Gateway không tự động ngắt mạch hay chuyển hướng request sang một trang fallback chung của Gateway.
  * Nếu một dịch vụ con (như `booking-service`) bị sập hoàn toàn:
    * Nếu Eureka chưa kịp hủy đăng ký dịch vụ đó: Gateway sẽ cố gắng chuyển tiếp request đến instance lỗi, dẫn đến lỗi kết nối và trả về mã lỗi **`500 Internal Server Error`** hoặc **`504 Gateway Timeout`**.
    * Nếu Eureka đã gỡ bỏ dịch vụ: Gateway trả về **`503 Service Unavailable`**.
