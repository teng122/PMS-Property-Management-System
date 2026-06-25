package com.smarthotel.room_service.service.impl;
import org.springframework.transaction.annotation.Transactional;
import com.smarthotel.room_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.room_service.dto.response.RoomResponse;
import com.smarthotel.room_service.entity.Room;
import com.smarthotel.room_service.exception.ResourceNotFoundException;
import com.smarthotel.room_service.repository.RoomRepository;
import com.smarthotel.room_service.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<RoomResponse> getAvailableRooms() {
        List<Room> rooms = roomRepository.findByStatus("AVAILABLE");
        return rooms.stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomResponse updateRoomStatus(UUID id, RoomStatusUpdateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        room.setStatus(request.getStatus().toUpperCase());
        Room updatedRoom = roomRepository.save(room);

        RoomResponse response = new RoomResponse();
        response.setId(updatedRoom.getId());
        response.setRoomNumber(updatedRoom.getRoomNumber());
        response.setType(updatedRoom.getType());
        response.setPrice(updatedRoom.getPrice());
        response.setStatus(updatedRoom.getStatus());

        return response;
    }
}