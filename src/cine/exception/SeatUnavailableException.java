package cine.exception;

/**
 * Thrown when a user tries to book a seat that is already booked
 * (a double-booking attempt).
 */
public class SeatUnavailableException extends Exception {

    private final String seatId;

    public SeatUnavailableException(String seatId) {
        super("Seat '" + seatId + "' is already booked. Please pick another seat.");
        this.seatId = seatId;
    }

    public String getSeatId() {
        return seatId;
    }
}
