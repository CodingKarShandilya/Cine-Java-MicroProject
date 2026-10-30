package cine.exception;

/**
 * Thrown when a seat id does not exist in the show's seat map
 * (e.g. "Z9" on a 5x8 layout).
 */
public class InvalidSeatException extends Exception {

    private final String seatId;

    public InvalidSeatException(String seatId) {
        super("Invalid seat '" + seatId + "'. Please choose a seat from the layout (e.g. C5).");
        this.seatId = seatId;
    }

    public String getSeatId() {
        return seatId;
    }
}
