package cine;

import cine.exception.AuthenticationException;
import cine.exception.InvalidSeatException;
import cine.exception.PaymentFailedException;
import cine.exception.SeatUnavailableException;
import cine.model.Movie;
import cine.model.Show;
import cine.model.Ticket;
import cine.model.User;
import cine.service.BookingSystem;
import cine.service.MockPaymentGateway;
import cine.service.PaymentGateway;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Console front-end (Presentation layer) for Cine Java.
 * Run:  java -cp out cine.Main
 */
public class Main {

    private static final Scanner IN = new Scanner(System.in);
    private static BookingSystem system;

    public static void main(String[] args) {
        PaymentGateway gateway = new MockPaymentGateway();
        system = new BookingSystem(gateway);

        System.out.println("===============================================");
        System.out.println("    CINE JAVA — Automated Movie Ticketing");
        System.out.println("    (Collections + Exceptions + Threads)");
        System.out.println("===============================================");
        System.out.println("  Demo login ->  username: demo   password: demo123");

        boolean running = true;
        while (running) {
            if (system.getCurrentUser() == null) {
                running = showGuestMenu();
            } else {
                running = showMemberMenu();
            }
        }
        System.out.println("\nThank you for using Cine Java. Goodbye!");
    }

    // ---------------- guest menu ----------------

    private static boolean showGuestMenu() {
        System.out.println("\n---- MAIN MENU (guest) ----");
        System.out.println(" 1. Login");
        System.out.println(" 2. Register");
        System.out.println(" 3. View movies & showtimes");
        System.out.println(" 4. Thread-safety demo (concurrent booking)");
        System.out.println(" 0. Exit");
        int choice = readInt("Choose an option: ");
        switch (choice) {
            case 1: doLogin();      return true;
            case 2: doRegister();   return true;
            case 3: doBrowseMovies(); return true;
            case 4: doConcurrencyDemo(); return true;
            case 0: return false;
            default:
                System.out.println("  ! Unknown option. Try again.");
                return true;
        }
    }

    // ---------------- member menu ----------------

    private static boolean showMemberMenu() {
        System.out.println("\n---- MAIN MENU (logged in as "
                + system.getCurrentUser().getFullName() + ") ----");
        System.out.println(" 1. View movies & showtimes");
        System.out.println(" 2. Book a ticket");
        System.out.println(" 3. My bookings");
        System.out.println(" 4. Thread-safety demo (concurrent booking)");
        System.out.println(" 0. Logout");
        int choice = readInt("Choose an option: ");
        switch (choice) {
            case 1: doBrowseMovies(); return true;
            case 2: doBookTicket();   return true;
            case 3: doMyBookings();   return true;
            case 4: doConcurrencyDemo(); return true;
            case 0:
                system.logout();
                System.out.println("  Logged out.");
                return true;
            default:
                System.out.println("  ! Unknown option. Try again.");
                return true;
        }
    }

    // ---------------- actions ----------------

    private static void doLogin() {
        try {
            String username = readLine("Username: ");
            String password = readLine("Password: ");
            User user = system.login(username, password);
            System.out.println("  Welcome back, " + user.getFullName() + "!");
        } catch (AuthenticationException e) {
            System.out.println("  ! Login failed: " + e.getMessage());
        }
    }

    private static void doRegister() {
        try {
            String username = readLine("Choose a username: ");
            String password = readLine("Choose a password: ");
            String fullName = readLine("Your full name: ");
            system.register(username, password, fullName);
            System.out.println("  Registered! You can now log in.");
        } catch (AuthenticationException e) {
            System.out.println("  ! Registration failed: " + e.getMessage());
        }
    }

    private static void doBrowseMovies() {
        List<Movie> movies = system.getMovies();
        System.out.println("\n================ MOVIE CATALOG ================");
        System.out.printf("  %-4s %-28s %-10s %-8s %-8s%n",
                "ID", "TITLE", "GENRE", "LENGTH", "PRICE");
        for (Movie movie : movies) {
            System.out.printf("  %-4s %-28s %-10s %-8s Rs.%.0f%n",
                    movie.getId(), movie.getTitle(), movie.getGenre(),
                    movie.getDurationMins() + "m", movie.getPrice());
        }
        for (Movie movie : movies) {
            System.out.println("\n  Showtimes for " + movie.getTitle() + ":");
            for (Show show : system.getShowsFor(movie)) {
                System.out.printf("    %s | %s | %s | %d seats free%n",
                        show.getId(), show.getTheater(), show.getShowTime(),
                        show.availableCount());
            }
        }
    }

    private static void doBookTicket() {
        try {
            Show show = pickShow();
            if (show == null) return;

            System.out.println(show.seatLayout());
            String seatInput = readLine("Enter seat(s) to book (comma separated, e.g. C5,C6): ");
            List<String> seats = new ArrayList<>();
            for (String seat : seatInput.split(",")) {
                if (!seat.trim().isEmpty()) seats.add(seat);
            }
            if (seats.isEmpty()) {
                System.out.println("  ! No seats entered.");
                return;
            }

            System.out.println("  Payment mode: 1 = UPI,  2 = CARD,  3 = CASH");
            int modeChoice = readInt("  Select mode: ");
            String mode;
            switch (modeChoice) {
                case 1:  mode = "UPI";  break;
                case 2:  mode = "CARD"; break;
                case 3:  mode = "CASH"; break;
                default: System.out.println("  ! Unknown payment mode."); return;
            }

            double total = show.getMovie().getPrice() * seats.size();
            System.out.printf("  Total payable: Rs.%.0f for %d seat(s)%n", total, seats.size());
            String confirm = readLine("  Confirm booking? (y/n): ");
            if (!confirm.trim().equalsIgnoreCase("y")) {
                System.out.println("  Booking cancelled.");
                return;
            }

            List<Ticket> tickets = system.book(show.getId(), seats, mode);
            System.out.println("\n  BOOKING SUCCESSFUL — " + tickets.size() + " ticket(s)");
            for (Ticket ticket : tickets) {
                System.out.println(ticket.asPrintable());
            }
        } catch (InvalidSeatException | SeatUnavailableException e) {
            System.out.println("  ! Booking failed: " + e.getMessage());
        } catch (AuthenticationException e) {
            System.out.println("  ! " + e.getMessage());
        } catch (PaymentFailedException e) {
            System.out.println("  ! Payment error: " + e.getMessage());
        }
    }

    private static void doMyBookings() {
        List<Ticket> tickets = system.getTicketsFor(system.getCurrentUser());
        if (tickets.isEmpty()) {
            System.out.println("  You have no bookings yet. Choose 'Book a ticket'.");
            return;
        }
        System.out.println("\n---- MY BOOKINGS ----");
        for (Ticket ticket : tickets) {
            System.out.println("  " + ticket);
        }
    }

    private static Show pickShow() {
        List<Show> shows = system.getShows();
        System.out.println("\n---- AVAILABLE SHOWS ----");
        for (int i = 0; i < shows.size(); i++) {
            System.out.printf("  %d. %s | %d seats free%n",
                    i + 1, shows.get(i), shows.get(i).availableCount());
        }
        int pick = readInt("  Pick a show (1-" + shows.size() + ", 0 to cancel): ");
        if (pick < 1 || pick > shows.size()) {
            System.out.println("  Cancelled.");
            return null;
        }
        return shows.get(pick - 1);
    }

    /**
     * Demonstrates the double-booking guard: several threads race to book
     * the SAME seat. Exactly one thread wins; the rest get
     * SeatUnavailableException. Thanks to the synchronized bookSeat method
     * the seat map is never corrupted.
     */
    private static void doConcurrencyDemo() {
        Show show = system.getShows().get(0);
        String seat = "E5";
        show.releaseSeat(seat); // make sure the race starts from a free seat

        System.out.println("\n---- THREAD-SAFETY DEMO ----");
        System.out.println("  5 threads will now try to book seat " + seat
                + " on show " + show.getId() + " simultaneously...");

        final int THREADS = 5;
        Thread[] racers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int racerId = i + 1;
            racers[i] = new Thread(() -> {
                User racer = new User("racer" + racerId, "x", "Racer #" + racerId);
                try {
                    Thread.sleep(50); // let all threads line up at the door
                    Ticket ticket = show.bookSeat(seat, racer);
                    System.out.println("  [" + Thread.currentThread().getName()
                            + "] SUCCESS -> " + ticket.getTicketId());
                } catch (SeatUnavailableException e) {
                    System.out.println("  [" + Thread.currentThread().getName()
                            + "] rejected -> " + e.getMessage());
                } catch (InvalidSeatException e) {
                    System.out.println("  [" + Thread.currentThread().getName()
                            + "] error -> " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Booker-" + racerId);
            racers[i].start();
        }
        for (Thread racer : racers) {
            try {
                racer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("  Seat " + seat + " booked = " + show.isBooked(seat)
                + "  (exactly one winner — no double booking)");
    }

    // ---------------- input helpers (graceful, never crash) ----------------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = IN.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a number.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return IN.nextLine();
    }
}
