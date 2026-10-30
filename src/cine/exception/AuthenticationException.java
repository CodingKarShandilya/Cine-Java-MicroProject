package cine.exception;

/**
 * Thrown on failed login (unknown username or wrong password)
 * and on duplicate registration.
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
