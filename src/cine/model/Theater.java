package cine.model;

/**
 * A cinema hall. Composed into Shows — each show runs in one theater.
 */
public class Theater {

    private final String id;
    private final String name;
    private final String city;

    public Theater(String id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }

    @Override
    public String toString() {
        return name + ", " + city;
    }
}
