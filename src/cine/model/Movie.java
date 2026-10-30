package cine.model;

import java.util.Objects;

/**
 * A movie in the catalog. Stored in an {@code ArrayList<Movie>}.
 */
public class Movie {

    private final String id;
    private final String title;
    private final String genre;
    private final int durationMins;
    private final double price;

    public Movie(String id, String title, String genre, int durationMins, double price) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.durationMins = durationMins;
        this.price = price;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDurationMins() { return durationMins; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return String.format("%s  |  %-28s | %-10s | %3d min | Rs.%.0f",
                id, title, genre, durationMins, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        return id.equals(((Movie) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
