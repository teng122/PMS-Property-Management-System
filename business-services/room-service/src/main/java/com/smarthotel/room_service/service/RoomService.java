package com.smarthotel.room_service.service;

import com.smarthotel.room_service.client.BookingClient;
import com.smarthotel.room_service.dto.request.RoomCreateRequest;
import com.smarthotel.room_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.room_service.dto.response.RoomResponse;
import com.smarthotel.room_service.entity.Room;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.room_service.exception.RoomNotFoundException;
import com.smarthotel.room_service.exception.InvalidRoomStatusException;
import com.smarthotel.room_service.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý thông tin phòng vật lý và tìm phòng trống.
 * Các phương thức được sắp xếp theo trình tự: Tìm kiếm phòng -> Cập nhật trạng thái phòng -> Tra cứu phòng.
 */
@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookingClient bookingClient;

    // ==========================================
    // 1. TÌM KIẾM PHÒNG (ROOM SEARCH)
    // ==========================================

    /**
     * Tìm phòng trống dựa trên khoảng thời gian nhận phòng và trả phòng.
     * Liên lạc Feign với Booking Service để lọc bỏ các phòng đang bận.
     */
    public List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        // 1. Gọi sang booking-service để lấy danh sách Room ID bận trong thời gian này
        List<UUID> activeRoomIds = bookingClient.getActiveRoomIds(checkIn, checkOut);

        // 2. Lấy toàn bộ phòng vật lý trong database
        List<Room> allRooms = roomRepository.findAll();

        // 3. Lọc bỏ các phòng nằm trong danh sách bận
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !activeRoomIds.contains(room.getId()))
                .collect(Collectors.toList());

        return availableRooms.stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }

    // ==========================================
    // 2. CẬP NHẬT TRẠNG THÁI PHÒNG (ROOM OPERATIONS)
    // ==========================================

    /**
     * Cập nhật trạng thái vật lý của một phòng (ví dụ: chuyển từ DIRTY sang AVAILABLE sau khi dọn xong).
     */
    @Transactional
    public RoomResponse updateRoomStatus(UUID id, RoomStatusUpdateRequest request) {
        Room room = roomRepository.findByIdOrThrow(id);

        try {
            room.setStatus(RoomStatus.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidRoomStatusException("Trạng thái phòng không hợp lệ: " + request.getStatus());
        }
        Room updatedRoom = roomRepository.save(room);

        RoomResponse response = new RoomResponse();
        response.setId(updatedRoom.getId());
        response.setRoomNumber(updatedRoom.getRoomNumber());
        response.setType(updatedRoom.getRoomType());
        response.setPrice(updatedRoom.getBasePrice());
        response.setStatus(updatedRoom.getStatus().name());

        return response;
    }

    /**
     * Admin tạo mới một phòng vật lý.
     */
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        RoomStatus initialStatus;
        try {
            initialStatus = request.getStatus() != null 
                ? RoomStatus.valueOf(request.getStatus().toUpperCase()) 
                : RoomStatus.AVAILABLE;
        } catch (IllegalArgumentException e) {
            initialStatus = RoomStatus.AVAILABLE;
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(request.getType() != null ? request.getType().toUpperCase() : "SINGLE")
                .status(initialStatus)
                .basePrice(request.getPrice())
                .floor(1)
                .build();

        Room saved = roomRepository.save(room);
        return modelMapper.map(saved, RoomResponse.class);
    }

    /**
     * Admin cập nhật thông tin một phòng vật lý (số phòng, loại, giá, trạng thái).
     */
    @Transactional
    public RoomResponse updateRoom(UUID id, RoomCreateRequest request) {
        Room room = roomRepository.findByIdOrThrow(id);

        if (request.getRoomNumber() != null) {
            room.setRoomNumber(request.getRoomNumber());
        }
        if (request.getType() != null) {
            room.setRoomType(request.getType().toUpperCase());
        }
        if (request.getPrice() != null) {
            room.setBasePrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            try {
                room.setStatus(RoomStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidRoomStatusException("Trạng thái phòng không hợp lệ: " + request.getStatus());
            }
        }

        Room updated = roomRepository.save(room);
        return modelMapper.map(updated, RoomResponse.class);
    }

    /**
     * Admin xóa một phòng vật lý khỏi hệ thống.
     */
    @Transactional
    public void deleteRoom(UUID id) {
        Room room = roomRepository.findByIdOrThrow(id);
        roomRepository.delete(room);
    }

    // ==========================================
    // 3. TRA CỨU THÔNG TIN PHÒNG (ROOM QUERIES)
    // ==========================================

    /**
     * Lấy toàn bộ danh sách phòng vật lý (bao gồm cả phòng đang bận) cho trang quản lý kho phòng.
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Truy vấn thông tin chi tiết một phòng bằng ID phòng.
     */
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findByIdOrThrow(id);

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setType(room.getRoomType());
        response.setPrice(room.getBasePrice());
        response.setStatus(room.getStatus().name());

        return response;
    }

    /**
     * Lấy toàn bộ danh sách phòng có trạng thái hiện tại là AVAILABLE (trống).
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms() {
        List<Room> rooms = roomRepository.findByStatus(RoomStatus.AVAILABLE);
        return rooms.stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }
}