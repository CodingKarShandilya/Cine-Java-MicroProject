package cine.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A confirmed booking: links a User, a Show and one seat.
 * Issued only after seat validation and payment succeed.
 */
public class Ticket {

    private static final AtomicInteger COUNTER = new AtomicInteger(1000);

    private final String ticketId;
    private final User user;
    private final Show show;
    private final String seatId;
    private final double price;
    private final LocalDateTime issuedAt;
    private String paymentTxnId;

    public Ticket(User user, Show show, String seatId) {
        this.ticketId = "TKT-" + COUNTER.incrementAndGet();
        this.user = user;
        this.show = show;
        this.seatId = seatId;
        this.price = show.getMovie().getPrice();
        this.issuedAt = LocalDateTime.now();
    }

    public String getTicketId() { return ticketId; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public String getSeatId() { return seatId; }
    public double getPrice() { return price; }
    public String getPaymentTxnId() { return paymentTxnId; }
    public void setPaymentTxnId(String txnId) { this.paymentTxnId = txnId; }

    /** Pretty multi-line ticket used for the confirmation printout. */
    public String asPrintable() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return "\n+---------------------------------------------+\n"
             + "                CINE JAVA TICKET                \n"
             + "+---------------------------------------------+\n"
             + String.format("  Ticket   : %s%n", ticketId)
             + String.format("  Movie    : %s%n", show.getMovie().getTitle())
             + String.format("  Theater  : %s%n", show.getTheater())
             + String.format("  Show     : %s%n", show.getShowTime())
             + String.format("  Seat     : %s%n", seatId)
             + String.format("  Customer : %s%n", user)
             + String.format("  Amount   : Rs.%.0f  (paid: %s)%n", price, paymentTxnId)
             + String.format("  Issued   : %s%n", issuedAt.format(fmt))
             + "+---------------------------------------------+";
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | seat %s | Rs.%.0f",
                ticketId, show.getMovie().getTitle(), show.getShowTime(), seatId, price);
    }
}
