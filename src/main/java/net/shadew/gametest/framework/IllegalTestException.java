package net.shadew.gametest.framework;

public class IllegalTestException extends RuntimeException {
    public IllegalTestException() {
    }

    public IllegalTestException(String message) {
        super(message);
    }

    public IllegalTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTestException(Throwable cause) {
        super(cause);
    }
}
