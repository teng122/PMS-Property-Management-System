package com.smarthotel.billing_service.repository;

import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByBookingId(UUID bookingId);

    List<Invoice> findByStatus(InvoiceStatus status);

    default Invoice findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay hoa don"));
    }
}


