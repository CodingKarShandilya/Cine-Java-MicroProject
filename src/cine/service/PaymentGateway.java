package cine.service;

import cine.exception.PaymentFailedException;
import cine.model.Ticket;

/**
 * Abstraction for payment processing (Polymorphism).
 * The booking flow depends on this interface only — swap in a
 * real gateway later without touching the booking code.
 */
public interface PaymentGateway {

    /**
     * Charges the ticket amount using the given payment mode
     * ("UPI", "CARD", "CASH").
     *
     * @return a transaction id on success
     * @throws PaymentFailedException if the payment cannot be completed
     */
    String pay(Ticket ticket, String mode) throws PaymentFailedException;
}
