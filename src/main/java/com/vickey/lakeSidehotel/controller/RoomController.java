package com.vickey.lakeSidehotel.controller;

import com.vickey.lakeSidehotel.exception.PhotoRetrievalException;
import com.vickey.lakeSidehotel.exception.ResourceNotFoundException;
import com.vickey.lakeSidehotel.model.BookedRoom;
import com.vickey.lakeSidehotel.model.Room;
import com.vickey.lakeSidehotel.response.BookingResponse;
import com.vickey.lakeSidehotel.response.RoomResponse;
import com.vickey.lakeSidehotel.service.IBookedRoomService;
import com.vickey.lakeSidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Vickey_Kishore
 */

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;
    private final IBookedRoomService bookedRoomService;

    /**
     * Add new Room
     *
     * @param photo
     * @param roomType
     * @param roomPrice
     * @return
     */
    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(),
                savedRoom.getRoomType(), savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }

    /**
     * get distinct room types
     *
     * @return
     */
    @GetMapping("/room-types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }


    /**
     * Get all rooms
     *
     * @return
     * @throws SQLException
     */
    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    /**
     * Delete a room by ID
     *
     * @param roomId
     * @return
     */
    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Update a room by RoomID
     * @param roomId
     * @param roomType
     * @param roomPrice
     * @param photo
     * @return
     * @throws SQLException
     * @throws IOException
     */
    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException {

        byte[] photoBytes = photo != null && !photo.isEmpty() ?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }

    /**
     *  Get a Room by RoomID
     * @param roomId
     * @return
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId){
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);
            return  ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmationCode())
                )
                .toList();
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error Retrieving Photo");
            }
        }
        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes,
                bookingInfo
        );
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookedRoomService.getAllBookingsByRoomId(roomId);
    }
}
