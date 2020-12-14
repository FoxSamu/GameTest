package net.shadew.gametest.framework.api.output;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface ITestOutputType {
    ITestOutput readConfig(JsonObject config);
}
