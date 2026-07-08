package com.smarthotel.booking_service.messaging;

import com.smarthotel.common_shared.event.BookingCreatedEvent;
import com.smarthotel.common_shared.event.CheckoutStartedEvent;

public interface BookingEventPublisher {
    void publishBookingCreated(BookingCreatedEvent event);
    void publishCheckoutStarted(CheckoutStartedEvent event);
}