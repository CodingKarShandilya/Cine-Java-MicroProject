package cine.model;

/**
 * A registered customer of the ticketing system.
 */
public class User {

    private final String username;
    private String password;
    private String fullName;

    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }

    /** Encapsulation: the password is never handed out, only verified. */
    public boolean checkPassword(String candidate) {
        return password != null && password.equals(candidate);
    }

    @Override
    public String toString() {
        return fullName + " (@" + username + ")";
    }
}
