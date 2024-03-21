package com.vickey.lakeSidehotel.service;

import com.vickey.lakeSidehotel.model.BookedRoom;

import java.util.List;

public interface IBookedRoomService {
    List<BookedRoom> getAllBookingsByRoomId(Long roomId);

    List<BookedRoom> getAllBookings();

    BookedRoom findByBookingConfirmationCode(String confirmationCode);

    String saveBooking(Long roomId, BookedRoom bookingRequest);

    List<BookedRoom> getBookingsByUserEmail(String email);

    void cancelBooking(Long bookingId);
}
