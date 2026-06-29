package com.smarthotel.billing.gateway;

import com.smarthotel.billing.dto.UnpaidAmenityDTO;

import java.util.List;
import java.util.UUID;

public interface AmenityGateway {

    /** Lay danh sach order dich vu chua thanh toan cua mot phong (S3). */
    List<UnpaidAmenityDTO> getUnpaid(UUID roomId);

    /**
     * Danh dau cac order da duoc gop vao hoa don -> chuyen status sang BILLED,
     * chong tinh trung. S3 chi ho tro cap nhat theo tung order id.
     */
    void markBilled(List<UUID> orderIds);
}
