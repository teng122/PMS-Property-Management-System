package com.smarthotel.billing_service.repository;

import com.smarthotel.billing_service.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByBookingId(UUID bookingId);

    default Invoice findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay hoa don"));
    }
}


