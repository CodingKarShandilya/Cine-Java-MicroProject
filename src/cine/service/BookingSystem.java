package cine.service;

import cine.exception.AuthenticationException;
import cine.exception.InvalidSeatException;
import cine.exception.PaymentFailedException;
import cine.exception.SeatUnavailableException;
import cine.model.Movie;
import cine.model.Show;
import cine.model.Theater;
import cine.model.Ticket;
import cine.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The business core of the application.
 *
 * Data structures used (per the project report):
 *   - ArrayList<Movie> / ArrayList<Show> : ordered catalogs with fast iteration
 *   - HashMap<String, User>              : O(1) username lookup for login
 *   - HashMap<String, Boolean>           : seat map inside each Show
 */
public class BookingSystem {

    private final List<Movie> movies = new ArrayList<>();
    private final List<Show> shows = new ArrayList<>();
    private final Map<String, User> users = new HashMap<>();
    private final List<Ticket> ticketRegistry = new ArrayList<>();
    private final PaymentGateway paymentGateway;

    private User currentUser;

    public BookingSystem(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
        seedMockData();
    }

    /** Mock data so the demo works out of the box. */
    private void seedMockData() {
        Movie m1 = new Movie("M1", "Inception",       "Sci-Fi",    148, 250);
        Movie m2 = new Movie("M2", "The Dark Knight", "Action",    152, 220);
        Movie m3 = new Movie("M3", "3 Idiots",        "Comedy",    170, 180);
        movies.add(m1);
        movies.add(m2);
        movies.add(m3);

        Theater t1 = new Theater("T1", "PVR Phoenix",   "Mumbai");
        Theater t2 = new Theater("T2", "INOX City Hub", "Pune");

        shows.add(new Show("S1", m1, t1, "Today 10:30 AM"));
        shows.add(new Show("S2", m1, t1, "Today  6:45 PM"));
        shows.add(new Show("S3", m2, t1, "Today  1:15 PM"));
        shows.add(new Show("S4", m2, t2, "Today  9:30 PM"));
        shows.add(new Show("S5", m3, t2, "Today 11:00 AM"));

        // a few pre-booked seats so the layout demo is meaningful
        try {
            User systemUser = new User("admin", "admin123", "Front Desk");
            shows.get(0).bookSeat("A1", systemUser);
            shows.get(0).bookSeat("A2", systemUser);
            shows.get(0).bookSeat("B3", systemUser);
            shows.get(2).bookSeat("C4", systemUser);
        } catch (InvalidSeatException | SeatUnavailableException ignored) {
            // mock data, cannot happen
        }

        // a ready-made demo account
        users.put("demo", new User("demo", "demo123", "Demo Student"));
    }

    // ---------------- users ----------------

    public void register(String username, String password, String fullName) throws AuthenticationException {
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) {
            throw new AuthenticationException("Username '" + username + "' is already taken.");
        }
        users.put(key, new User(key, password, fullName));
    }

    public User login(String username, String password) throws AuthenticationException {
        User user = users.get(username.trim().toLowerCase());
        if (user == null || !user.checkPassword(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }
        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // ---------------- catalog ----------------

    public List<Movie> getMovies() {
        return movies;
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<Show> getShowsFor(Movie movie) {
        List<Show> result = new ArrayList<>();
        for (Show show : shows) {
            if (show.getMovie().equals(movie)) {
                result.add(show);
            }
        }
        return result;
    }

    public Show findShow(String showId) {
        for (Show show : shows) {
            if (show.getId().equalsIgnoreCase(showId.trim())) {
                return show;
            }
        }
        return null;
    }

    // ---------------- booking ----------------

    /**
     * Full booking transaction: seats -> payment -> tickets.
     * If payment fails, the seats are released before the exception
     * reaches the caller (no orphaned bookings).
     */
    public List<Ticket> book(String showId, List<String> seatIds, String paymentMode)
            throws AuthenticationException, InvalidSeatException, SeatUnavailableException,
            PaymentFailedException {
        if (currentUser == null) {
            throw new AuthenticationException("Please log in before booking.");
        }
        Show show = findShow(showId);
        if (show == null) {
            throw new InvalidSeatException("show " + showId);
        }
        List<Ticket> tickets = show.bookSeats(seatIds, currentUser);
        try {
            for (Ticket ticket : tickets) {
                String txn = paymentGateway.pay(ticket, paymentMode);
                ticket.setPaymentTxnId(txn);
            }
        } catch (PaymentFailedException e) {
            for (Ticket ticket : tickets) {          // rollback the seats
                show.releaseSeat(ticket.getSeatId());
            }
            throw new PaymentFailedException(
                    "Payment failed, your seats were released. (" + e.getMessage() + ")");
        }
        ticketRegistry.addAll(tickets);
        return tickets;
    }

    public List<Ticket> getTicketsFor(User user) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : ticketRegistry) {
            if (ticket.getUser().equals(user)) {
                result.add(ticket);
            }
        }
        return result;
    }
}
