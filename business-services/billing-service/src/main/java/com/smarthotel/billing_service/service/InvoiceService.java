package com.smarthotel.billing_service.service;

import com.smarthotel.billing_service.dto.BookingInfoDTO;
import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.PaymentInitResponse;
import com.smarthotel.billing_service.dto.RoomInfoDTO;
import com.smarthotel.billing_service.dto.UnpaidAmenityDTO;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.client.AmenityClient;
import com.smarthotel.billing_service.client.BookingClient;
import com.smarthotel.billing_service.client.RoomClient;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service xử lý các nghiệp vụ liên quan đến hóa đơn và thanh toán (Billing & Payment).
 * Các hàm được tổ chức theo quy trình thực tế: Tạo hóa đơn -> Thanh toán -> Tra cứu.
 */
@Service
public class InvoiceService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final InvoiceRepository repo;
    private final BookingClient bookingClient;
    private final RoomClient roomClient;
    private final AmenityClient amenityClient;

    public InvoiceService(InvoiceRepository repo, BookingClient bookingClient, RoomClient roomClient, AmenityClient amenityClient) {
        this.repo = repo;
        this.bookingClient = bookingClient;
        this.roomClient = roomClient;
        this.amenityClient = amenityClient;
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private InvoiceService self;

    // ==========================================
    // 1. TẠO HÓA ĐƠN (INVOICE GENERATION)
    // ==========================================

    /**
     * Tạo hóa đơn tổng hợp cho một đơn đặt phòng.
     * Thu thập dữ liệu phòng và dịch vụ chưa trả tiền qua Feign Client ngoài transaction để tránh nghẽn Connection Pool.
     */
    public InvoiceResponse generate(UUID bookingId) {
        // Kiểm tra xem hóa đơn cho Booking này đã tồn tại chưa
        repo.findByBookingId(bookingId).ifPresent(i -> {
            throw new IllegalStateException("Hoa don cho booking nay da ton tai");
        });

        // 1. Lấy thông tin đặt phòng qua Feign Client
        BookingInfoDTO booking = bookingClient.getBooking(bookingId);

        // 2. Lấy thông tin phòng vật lý và tính tiền phòng dựa trên số đêm thực tế
        RoomInfoDTO room = roomClient.getRoom(booking.roomId());
        long nights = ChronoUnit.DAYS.between(booking.checkInDate(), booking.checkOutDate());
        if (nights < 1) {
            nights = 1; // Tính tối thiểu 1 đêm
        }
        BigDecimal roomCharge = room.price().multiply(BigDecimal.valueOf(nights));

        // 3. Lấy thông tin toàn bộ dịch vụ phòng chưa thanh toán
        List<UnpaidAmenityDTO> unpaidAmenities = amenityClient.getUnpaid(booking.roomId());
        BigDecimal serviceCharge = unpaidAmenities.stream()
                .map(UnpaidAmenityDTO::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Tính toán thuế giá trị gia tăng VAT và tổng tiền cần thanh toán
        BigDecimal subtotal = roomCharge.add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal depositAmount = booking.depositAmount() != null ? booking.depositAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(tax).subtract(depositAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 5. Lưu hóa đơn vào Database qua proxy bọc Transaction ngắn
        Invoice inv = self.executeSaveInvoice(bookingId, roomCharge, serviceCharge, tax, depositAmount, totalAmount);

        // 6. Đóng các order dịch vụ phòng qua Feign Client ngoài transaction
        List<UUID> billedOrderIds = unpaidAmenities.stream()
                .map(UnpaidAmenityDTO::id)
                .toList();
        for (UUID orderId : billedOrderIds) {
            amenityClient.updateOrderStatus(orderId, "BILLED");
        }

        return InvoiceResponse.from(inv);
    }

    /**
     * Phương thức phụ trợ được bọc Transaction ngắn để ghi nhận hóa đơn xuống Database.
     */
    @Transactional
    public Invoice executeSaveInvoice(UUID bookingId, BigDecimal roomCharge, BigDecimal serviceCharge, BigDecimal tax, BigDecimal depositAmount, BigDecimal totalAmount) {
        repo.findByBookingId(bookingId).ifPresent(i -> {
            throw new IllegalStateException("Hoa don cho booking nay da ton tai");
        });

        Invoice inv = new Invoice();
        inv.setBookingId(bookingId);
        inv.setRoomCharge(roomCharge);
        inv.setServiceCharge(serviceCharge);
        inv.setTax(tax);
        inv.setDepositAmount(depositAmount);
        inv.setTotalAmount(totalAmount);
        return repo.save(inv);
    }

    // ==========================================
    // 2. QUY TRÌNH THANH TOÁN (PAYMENT FLOW)
    // ==========================================

    /**
     * Khởi tạo quá trình thanh toán, sinh mã VietQR dựa trên số tiền hóa đơn.
     */
    public PaymentInitResponse initPayment(UUID id) {
        Invoice inv = findById(id);
        String qrUrl = "https://img.vietqr.io/image/970415-113366668888-compact2.png"
                + "?amount=" + inv.getTotalAmount().toBigInteger()
                + "&addInfo=INV" + inv.getId();
        return new PaymentInitResponse(qrUrl, inv.getTotalAmount(), "WAITING_BANK");
    }

    /**
     * Xác nhận thanh toán hóa đơn thành công và chuyển trạng thái hóa đơn sang PAID.
     */
    @Transactional
    public InvoiceResponse markPaid(UUID id) {
        Invoice inv = findById(id);
        if (inv.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Hoa don da thanh toan");
        }
        inv.setStatus(InvoiceStatus.PAID);
        inv.setPaidAt(Instant.now());
        return InvoiceResponse.from(repo.save(inv));
    }

    // ==========================================
    // 3. TRA CỨU HÓA ĐƠN (INVOICE QUERIES)
    // ==========================================

    /**
     * Tra cứu hóa đơn bằng ID hóa đơn.
     */
    public Invoice findById(UUID id) {
        return repo.findByIdOrThrow(id);
    }

    /**
     * Tra cứu hóa đơn bằng ID đơn đặt phòng (bookingId).
     */
    public Invoice findByBookingId(UUID bookingId) {
        return repo.findByBookingId(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn cho booking ID: " + bookingId));
    }
}


