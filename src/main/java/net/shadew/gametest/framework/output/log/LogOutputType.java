package net.shadew.gametest.framework.output.log;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.framework.api.output.ITestOutput;
import net.shadew.gametest.framework.api.output.ITestOutputType;

public class LogOutputType implements ITestOutputType {
    @Override
    public ITestOutput readConfig(JsonObject config) {
        return new LogOutput(
            getOrDefault(config, "passed", false),
            getOrDefault(config, "optional", true),
            getOrDefault(config, "required", true),
            getOrDefault(config, "stacktrace", false),
            getOrDefault(config, "name", GameTestMod.LOGGER)
        );
    }

    private static boolean getOrDefault(JsonObject obj, String key, boolean def) {
        if(JSONUtils.isBoolean(obj, key))
            return obj.get(key).getAsBoolean();
        return def;
    }

    private static Logger getOrDefault(JsonObject obj, String key, Logger def) {
        if(JSONUtils.isString(obj, key))
            return LogManager.getLogger(obj.get(key).getAsString());
        return def;
    }
}
