package net.shadew.gametest.framework.output.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.shadew.gametest.framework.GameTestInstance;
import net.shadew.gametest.framework.api.output.ITestOutput;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;

public class LogOutput implements ITestOutput, ITestOutputInstance {
    private final boolean passed;
    private final boolean optional;
    private final boolean required;
    private final boolean withStacktrace;
    private final Logger logger;

    public LogOutput(boolean passed, boolean optional, boolean required, boolean withStacktrace, String loggerName) {
        this(passed, optional, required, withStacktrace, LogManager.getLogger(loggerName));
    }

    public LogOutput(boolean passed, boolean optional, boolean required, boolean withStacktrace, Logger logger) {
        this.passed = passed;
        this.optional = optional;
        this.required = required;
        this.withStacktrace = withStacktrace;
        this.logger = logger;
    }

    @Override
    public void logFailed(GameTestInstance instance, boolean opt) {
        if(opt) {
            if(optional) {
                logger.warn(instance.getName() + " failed, but is optional");
                if(withStacktrace)
                    logger.warn(instance.getFailure());
                else
                    logger.warn(instance.getFailure().toString());
            }
        } else {
            if(required) {
                logger.error(instance.getName() + " failed");
                if(withStacktrace)
                    logger.error(instance.getFailure());
                else
                    logger.error(instance.getFailure().toString());
            }
        }
    }

    @Override
    public void logPassed(GameTestInstance instance) {
        if (passed)
            logger.info(instance.getName() + " passed");
    }

    @Override
    public ITestOutputInstance open() {
        return this;
    }
}
