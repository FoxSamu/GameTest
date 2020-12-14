package net.shadew.gametest.framework.api.exception;

import net.shadew.gametest.framework.GameTestInstance;
import net.shadew.gametest.framework.api.ITestInstance;

public class TestException extends RuntimeException {
    public TestException(String message) {
        super(message);
    }

    public TestException(Throwable cause) {
        super(cause);
    }

    public void displayErrorInGame(ITestInstance instance) {

    }
}
