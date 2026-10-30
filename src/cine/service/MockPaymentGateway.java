package cine.service;

import cine.exception.PaymentFailedException;
import cine.model.Ticket;

import java.util.Random;

/**
 * Simulated gateway used by the console demo.
 * Generates a fake transaction id and always succeeds
 * (a real implementation would call a bank/payments API).
 */
public class MockPaymentGateway implements PaymentGateway {

    private static final Random RANDOM = new Random();

    @Override
    public String pay(Ticket ticket, String mode) throws PaymentFailedException {
        if (mode == null || mode.trim().isEmpty()) {
            throw new PaymentFailedException("No payment mode selected.");
        }
        if ("CASH".equalsIgnoreCase(mode.trim())) {
            return "CASH-AT-COUNTER";
        }
        // simulate a short processing delay, then succeed
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentFailedException("Payment interrupted.");
        }
        return String.format("TXN-%06d", RANDOM.nextInt(1_000_000));
    }
}
