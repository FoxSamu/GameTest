package net.shadew.gametest.framework.api.exception;

public class AssertException extends TestException {
    public AssertException(String message) {
        super(message);
    }

    public AssertException(Throwable cause) {
        super(cause);
    }
}
