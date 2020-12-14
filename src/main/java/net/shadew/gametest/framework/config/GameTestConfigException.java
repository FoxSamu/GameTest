package net.shadew.gametest.framework.config;

public class GameTestConfigException extends RuntimeException {
    public GameTestConfigException() {
    }

    public GameTestConfigException(String message) {
        super(message);
    }

    public GameTestConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameTestConfigException(Throwable cause) {
        super(cause);
    }
}
