package com.smarthotel.housekeeping_service.service;

import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.housekeeping_service.dto.response.RoomResponse;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<RoomResponse> getAvailableRooms();
    RoomResponse updateRoomStatus(UUID id, RoomStatusUpdateRequest request);
}