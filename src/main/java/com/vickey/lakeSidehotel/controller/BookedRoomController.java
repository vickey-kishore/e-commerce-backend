package com.vickey.lakeSidehotel.controller;

import com.vickey.lakeSidehotel.exception.InvalidBookingRequestException;
import com.vickey.lakeSidehotel.exception.ResourceNotFoundException;
import com.vickey.lakeSidehotel.model.BookedRoom;
import com.vickey.lakeSidehotel.model.Room;
import com.vickey.lakeSidehotel.response.BookingResponse;
import com.vickey.lakeSidehotel.response.RoomResponse;
import com.vickey.lakeSidehotel.service.IBookedRoomService;
import com.vickey.lakeSidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vickey_Kishore
 */
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bookings")
public class BookedRoomController {

    private final IBookedRoomService bookingService;
    private final IRoomService roomService;

    /**
     * Get all the bookings in the hotel
     * @return
     */
    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings){
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }

    /**
     * Get booking details by confirmation code
     * @param confirmationCode
     * @return
     */
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){
        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    /**
     * Save the bookings to the database
     * @param roomId
     * @param bookingRequest
     * @return
     */
    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId,
                                         @RequestBody BookedRoom bookingRequest){
        try{
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(
                    "Room booked successfully, Your booking confirmation code is :"+confirmationCode);

        }catch (InvalidBookingRequestException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     *  Get booking details by user email
     * @param email
     * @return
     */
    @GetMapping("/user/{email}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
        List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }

    /**
     *  Cancel booking by booking ID
     * @param bookingId
     */
    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());
        return new BookingResponse(
                booking.getBookingId(), booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), room);
    }

}
