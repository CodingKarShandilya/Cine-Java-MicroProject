# Cine Java — Automated Movie Ticketing

A console-based movie ticket booking application built with **plain Java (JDK 8+)** —
no build tools, no external libraries. Everything is in `src/cine/`.

## How to run

You need Java installed. Check with `java -version` (any JDK 8 or newer works;
to install: [Adoptium](https://adoptium.net) on Windows/macOS, or
`sudo apt install default-jdk` on Ubuntu).

### Option 1 — one command

- **Windows:** double-click `run.bat` (or run it from cmd)
- **Linux / macOS:** `bash run.sh`

### Option 2 — manual commands

```bash
# from the cine-java folder
javac -d out $(find src -name "*.java")      # Windows: use the run.bat, or list files with: dir /s /b src\*.java > sources.txt && javac -d out @sources.txt
java -cp out cine.Main
```

## Demo login

    username: demo
    password: demo123

(you can also register your own user from menu option 2)

## What to show in the demo

1. **Login** (option 1) with `demo / demo123`
2. **View movies & showtimes** — 3 movies, 5 shows
3. **Book a ticket** — pick a show, see the seat layout, enter e.g. `C5,C6`,
   pay by UPI → a printable ticket is generated
4. **Book an already-taken seat** (e.g. `A1` on show S1) → custom
   `SeatUnavailableException` message, no crash
5. **Enter an invalid seat** (e.g. `Z9`) → custom `InvalidSeatException` message
6. **Thread-safety demo** (menu option 4) → 5 threads race for the same seat,
   exactly one wins — proves the `synchronized` double-booking guard

## Code map (what maps to which OOP concept)

| File | Concept it demonstrates |
|---|---|
| `model/Movie.java`, `model/Theater.java`, `model/User.java` | Encapsulation (private fields, getters) |
| `model/Show.java` | `HashMap<String,Boolean>` seat map + `synchronized` booking |
| `model/Ticket.java` | Immutable-ish booking record, printable ticket |
| `exception/*.java` | Custom exceptions: `InvalidSeatException`, `SeatUnavailableException`, `AuthenticationException`, `PaymentFailedException` |
| `service/PaymentGateway.java` | Abstraction / polymorphism (interface) |
| `service/MockPaymentGateway.java` | Interface implementation |
| `service/BookingSystem.java` | `ArrayList` catalogs + `HashMap` users, business logic |
| `Main.java` | Console menu, input validation, try-catch handling |
