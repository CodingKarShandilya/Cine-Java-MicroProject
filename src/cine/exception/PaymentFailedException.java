package cine.exception;

/**
 * Thrown when the payment step of a booking fails.
 * Any booked seats are released by the caller before this reaches the user.
 */
public class PaymentFailedException extends Exception {

    public PaymentFailedException(String message) {
        super(message);
    }
}
