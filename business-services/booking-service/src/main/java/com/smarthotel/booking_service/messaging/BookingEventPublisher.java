package com.smarthotel.booking_service.messaging;

import com.smarthotel.common_shared.event.BookingCreatedEvent;
import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.common_shared.event.AmenityOrderValidatedEvent;

public interface BookingEventPublisher {
    void publishBookingCreated(BookingCreatedEvent event);
    void publishCheckoutStarted(CheckoutStartedEvent event);
    void publishAmenityOrderValidated(AmenityOrderValidatedEvent event);
}