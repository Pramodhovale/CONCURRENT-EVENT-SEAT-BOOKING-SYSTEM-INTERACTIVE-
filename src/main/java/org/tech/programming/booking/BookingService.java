package org.tech.programming.booking;

import java.util.ArrayList;
import java.util.List;

public class BookingService
{
    private final Event event;
    private final List<Integer> availableSeats = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public BookingService(Event event) {
        this.event = event;

        for (int i=1;i<=event.getTotalSeats();i++) {
            availableSeats.add(i);
        }
    }

    private void validateSeat(int seatNumber) throws InvalidBookingException {
        if(seatNumber < 1 || seatNumber > event.getTotalSeats()) {
            throw new InvalidBookingException("Invalid seat number: "+seatNumber);
        }
    }

    public synchronized Booking bookSeat(String customerName, int seatNumber)
        throws InvalidBookingException {
        // Step 1: Validate the seat number
        validateSeat(seatNumber);

        // Step 2: Check whether the seat is available
        if (!availableSeats.contains(seatNumber)) {
            throw new InvalidBookingException(
                    "Seat " + seatNumber + " is already booked"
            );
        }
        // Step 3: Remove the seat from available seats
        availableSeats.remove(Integer.valueOf(seatNumber));

        // Step 4: Create a booking record
        Booking booking =  new Booking(customerName, seatNumber);

        // Step 5 : Store the successful booking
        bookings.add(booking);

        // Step 6: Return the booking
        return booking;
    }

    public synchronized List<Integer> getAvailableSeats() {
        return new ArrayList<>(availableSeats);
    }

    public synchronized List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

}
