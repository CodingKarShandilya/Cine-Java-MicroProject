package cine.model;

import cine.exception.InvalidSeatException;
import cine.exception.SeatUnavailableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One screening of a Movie in a Theater at a given time.
 *
 * The seat map is a HashMap<String, Boolean>: seat id -> booked status.
 * That gives O(1) availability checks and direct updates instead of
 * scanning a whole grid.
 *
 * The booking methods are synchronized so two threads (two customers)
 * can never book the same seat at the same moment — the double-booking
 * guard at the heart of this project.
 */
public class Show {

    public static final char[] ROWS = {'A', 'B', 'C', 'D', 'E'};
    public static final int COLS = 8;

    private final String id;
    private final Movie movie;
    private final Theater theater;
    private final String showTime;
    private final Map<String, Boolean> seats = new HashMap<>();

    public Show(String id, Movie movie, Theater theater, String showTime) {
        this.id = id;
        this.movie = movie;
        this.theater = theater;
        this.showTime = showTime;
        for (char row : ROWS) {
            for (int col = 1; col <= COLS; col++) {
                seats.put("" + row + col, false);
            }
        }
    }

    public String getId() { return id; }
    public Movie getMovie() { return movie; }
    public Theater getTheater() { return theater; }
    public String getShowTime() { return showTime; }

    /**
     * Books a single seat. Synchronized + checked in one atomic step,
     * so a seat can never be double-booked even under concurrent access.
     */
    public synchronized Ticket bookSeat(String seatId, User user)
            throws InvalidSeatException, SeatUnavailableException {
        String seat = seatId.trim().toUpperCase();
        Boolean booked = seats.get(seat);
        if (booked == null) {
            throw new InvalidSeatException(seat);
        }
        if (booked) {
            throw new SeatUnavailableException(seat);
        }
        seats.put(seat, true);
        return new Ticket(user, this, seat);
    }

    /**
     * Books several seats atomically. If any seat is invalid or taken,
     * everything already booked in this call is released again —
     * the caller never ends up with a partial booking.
     */
    public synchronized List<Ticket> bookSeats(List<String> seatIds, User user)
            throws InvalidSeatException, SeatUnavailableException {
        List<String> bookedSoFar = new ArrayList<>();
        try {
            for (String seat : seatIds) {
                String normalized = seat.trim().toUpperCase();
                Boolean booked = seats.get(normalized);
                if (booked == null) {
                    throw new InvalidSeatException(normalized);
                }
                if (booked) {
                    throw new SeatUnavailableException(normalized);
                }
                seats.put(normalized, true);
                bookedSoFar.add(normalized);
            }
        } catch (InvalidSeatException | SeatUnavailableException e) {
            for (String seat : bookedSoFar) {   // rollback — all or nothing
                seats.put(seat, false);
            }
            throw e;
        }
        List<Ticket> tickets = new ArrayList<>();
        for (String seat : bookedSoFar) {
            tickets.add(new Ticket(user, this, seat));
        }
        return tickets;
    }

    /** Frees a seat (used for rollback when payment fails). */
    public synchronized void releaseSeat(String seatId) {
        String seat = seatId.trim().toUpperCase();
        if (seats.containsKey(seat)) {
            seats.put(seat, false);
        }
    }

    public synchronized boolean isBooked(String seatId) {
        return Boolean.TRUE.equals(seats.get(seatId.trim().toUpperCase()));
    }

    public synchronized int availableCount() {
        int count = 0;
        for (Boolean booked : seats.values()) {
            if (!booked) count++;
        }
        return count;
    }

    /**
     * Console seat layout. '.' = available, 'X' = booked.
     */
    public synchronized String seatLayout() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n            [   S C R E E N   ]\n\n     ");
        for (int c = 1; c <= COLS; c++) sb.append(c).append(" ");
        sb.append("\n");
        for (char row : ROWS) {
            sb.append("  ").append(row).append("   ");
            for (int c = 1; c <= COLS; c++) {
                String seat = "" + row + c;
                sb.append(Boolean.TRUE.equals(seats.get(seat)) ? "X " : ". ");
            }
            sb.append("\n");
        }
        sb.append("\n  ( . = available,  X = booked )\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s | %-26s | %-18s | %s",
                id, movie.getTitle(), theater.getName(), showTime);
    }
}
