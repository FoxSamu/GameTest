package net.shadew.gametest.framework.output;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.framework.api.output.ITestOutput;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;
import net.shadew.gametest.framework.api.output.ITestOutputType;
import net.shadew.gametest.framework.config.GameTestConfigException;
import net.shadew.gametest.framework.output.log.LogOutput;
import net.shadew.gametest.framework.output.log.LogOutputType;

public final class TestOutputManager {
    private static final Map<ResourceLocation, ITestOutputType> OUTPUT_TYPES = new HashMap<>();

    private static final List<ITestOutput> OUTPUTS = new ArrayList<>();

    public static final ITestOutput DEFAULT_OUTPUT = new LogOutput(false, true, true, false, GameTestMod.LOGGER);

    static {
        registerBuiltinType("log", new LogOutputType());
    }

    private static void registerBuiltinType(String id, ITestOutputType type) {
        registerOutputType(new ResourceLocation(id), type);
        registerOutputType(new ResourceLocation("gametest", id), type);
    }

    public static void registerOutputType(ResourceLocation id, ITestOutputType type) {
        OUTPUT_TYPES.put(id, type);
    }

    public static void loadOutput(JsonObject json) {
        if(!json.has("type")) {
            throw new GameTestConfigException("No output 'type' specified");
        }

        if(!JSONUtils.isString(json.get("type"))) {
            throw new GameTestConfigException("Output 'type' is not a string");
        }

        String typeName = json.get("type").getAsString();
        ResourceLocation typeId = ResourceLocation.tryCreate("typeName");
        ITestOutputType type = OUTPUT_TYPES.get(typeId);
        if(type == null) {
            throw new GameTestConfigException("Unknown output 'type': '" + typeName + "'");
        }

        JsonObject config = new JsonObject();
        if(json.has("config")) {
            if(!json.get("config").isJsonObject()) {
                throw new GameTestConfigException("Output 'config' for type '" + typeName + "' is present but not an object");
            }

            config = json.getAsJsonObject("config");
        }

        addOutput(type.readConfig(config));
    }

    public static void addOutput(ITestOutput out) {
        OUTPUTS.add(out);
    }

    public static void dumpDebug() {
        GameTestMod.LOGGER.info("Registered output types:");
        for(ResourceLocation k : OUTPUT_TYPES.keySet()) {
            GameTestMod.LOGGER.info("- " + k);
        }
    }

    public static ITestOutputInstance openInstance() {
        if(OUTPUTS.isEmpty()) {
            return DEFAULT_OUTPUT.open();
        }
        CollectiveOutputInstance instance = new CollectiveOutputInstance();
        OUTPUTS.stream().map(ITestOutput::open).forEach(instance::add);
        return instance;
    }
}
