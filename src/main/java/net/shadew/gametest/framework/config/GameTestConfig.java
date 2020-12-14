package net.shadew.gametest.framework.config;

import com.google.gson.*;
import net.minecraft.util.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.shadew.gametest.framework.GameTestRegistry;
import net.shadew.gametest.framework.output.TestOutputManager;
import net.shadew.gametest.framework.run.ITestRun;
import net.shadew.gametest.framework.run.TestRunManager;
import net.shadew.gametest.util.Utils;

public final class GameTestConfig {
    private static final Logger LOGGER = LogManager.getLogger("GameTest Config");
    private static final File DEFAULT_TEMPLATE_PATH = new File(Utils.TEST_STRUCTURE_DIR).getAbsoluteFile();
    private static final File DEFAULT_CONFIG_PATH = new File("gametest_defaults.json").getAbsoluteFile();

    private static int defaultTestsPerRow = 8;
    private static int defaultSimultaneousTests = 100;
    private static int defaultRunThisRadius = 200;
    private static int defaultTimeoutTicks = 100;
    private static int defaultPredelayTicks = 0;
    private static File templatePath = DEFAULT_TEMPLATE_PATH;

    public static int getDefaultTestsPerRow() {
        return defaultTestsPerRow;
    }

    public static int getDefaultSimultaneousTests() {
        return defaultSimultaneousTests;
    }

    public static int getDefaultRunThisRadius() {
        return defaultRunThisRadius;
    }

    public static int getDefaultTimeoutTicks() {
        return defaultTimeoutTicks;
    }

    public static int getDefaultPredelayTicks() {
        return defaultPredelayTicks;
    }

    public static File getTemplatePath() {
        return templatePath;
    }


    private static void load(String name, JsonElement json, boolean modResource, File base) {
        new GameTestConfig(name, json, modResource, base).load();
    }

    private static void load(String name, Reader jsonReader, boolean modResource, File base) {
        load(name, new JsonParser().parse(jsonReader), modResource, base);
    }

    private static void loadExternal(File file) throws IOException {
        file = file.getAbsoluteFile();
        try (FileReader fr = new FileReader(file)) {
            load(file.toString(), fr, false, file.getParentFile());
        }
    }

    private static void loadInternal(URL url) throws IOException {
        String path = url.getPath();
        try (InputStreamReader isr = new InputStreamReader(url.openStream())) {
            load(path, isr, true, new File("."));
        }
    }

    private static void loadAllInternal() throws IOException {
        Enumeration<URL> resources = GameTestConfig.class.getClassLoader().getResources("META-INF/gametest.json");
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            try {
                loadInternal(res);
            } catch (Exception e) {
                LOGGER.error("Failed loading a 'META-INF/gametest.json' file at '" + res + "'", e);
            }
        }
    }

    public static void loadAll() {
        LOGGER.info("Loading GameTest configs...");
        if (DEFAULT_CONFIG_PATH.exists()) {
            try {
                loadExternal(DEFAULT_CONFIG_PATH);
            } catch (Exception e) {
                LOGGER.error("Failed loading default config at '" + DEFAULT_CONFIG_PATH + "'", e);
            }
        } else {
            LOGGER.info("No default config exists, creating one for the next time");
            JsonObject defaultConfig = new JsonObject();
            defaultConfig.addProperty("run", "all");
            defaultConfig.add("config", new JsonObject());

            try (FileWriter writer = new FileWriter(DEFAULT_CONFIG_PATH)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig, writer);
                load(DEFAULT_CONFIG_PATH.toString(), defaultConfig, false, new File("."));
            } catch (Exception e) {
                LOGGER.warn("Failed initializing a default config at '" + DEFAULT_CONFIG_PATH + "'", e);
            }
        }

        String configPath = System.getProperty("gametest.config");
        if (configPath != null) {
            String[] paths = configPath.split(",");

            for (String path : paths) {
                try {
                    loadExternal(new File(path));
                } catch (Exception e) {
                    LOGGER.error("Failed loading config at '" + path + "'", e);
                }
            }
        }

        try {
            loadAllInternal();
        } catch (Exception e) {
            LOGGER.error("Failed loading internal configs", e);
        }
    }


    private final String name;
    private final JsonElement json;
    private final boolean modResource;
    private final File base;

    private GameTestConfig(String name, JsonElement json, boolean modResource, File base) {
        this.name = name;
        this.json = json;
        this.modResource = modResource;
        this.base = base;
    }

    private void load() {
        LOGGER.info("Loading config file: " + name);
        if (!json.isJsonObject()) {
            error("Config root is not an object");
            return;
        }

        JsonObject obj = json.getAsJsonObject();

        try {
            if (modResource && obj.has("classes")) {
                loadClassesSection(obj.get("classes"));
            }

            if (!modResource && obj.has("config")) {
                loadConfigSection(obj.get("config"));
            }

            if (!modResource && obj.has("run")) {
                loadRunSection(obj.get("run"));
            }

            if (obj.has("output")) {
                loadOutputSection(obj.get("output"));
            }
        } catch (Exception e) {
            error("Exception aborted config loading");
            LOGGER.error("Exception: ", e);
        }
    }

    private void loadRunSection(JsonElement runEl) {
        try {
            TestRunManager.set(loadRuns(runEl));
        } catch (GameTestConfigException e) {
            error(e.getLocalizedMessage());
        }
    }

    private ITestRun loadRuns(JsonElement json) {
        if (json.isJsonArray()) {
            List<ITestRun> runs = new ArrayList<>();
            for (JsonElement e : json.getAsJsonArray()) {
                try {
                    runs.add(loadRuns(e));
                } catch (GameTestConfigException exc) {
                    error(exc.getLocalizedMessage());
                }
            }

            return ITestRun.collect(runs);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.size() == 0) {
                return ITestRun.none();
            }
            ITestRun run = null;
            if (obj.has("function")) {
                JsonElement function = obj.get("function");
                if (!JSONUtils.isString(function)) {
                    throw new GameTestConfigException("Run config 'function' is not a string");
                }
                run = ITestRun.function(function.getAsString());
            }
            if (obj.has("batch")) {
                if (run != null) {
                    throw new GameTestConfigException("Run config has multiple selectors");
                } else {
                    JsonElement batch = obj.get("batch");
                    if (!JSONUtils.isString(batch)) {
                        throw new GameTestConfigException("Run config 'batch' is not a string");
                    }
                    run = ITestRun.batch(batch.getAsString());
                }
            }
            if (obj.has("class")) {
                if (run != null) {
                    throw new GameTestConfigException("Run config has multiple selectors");
                } else {
                    JsonElement cls = obj.get("class");
                    if (!JSONUtils.isString(cls)) {
                        throw new GameTestConfigException("Run config 'class' is not a string");
                    }
                    run = ITestRun.testClass(cls.getAsString());
                }
            }
            if (obj.has("namespace")) {
                if (run != null) {
                    throw new GameTestConfigException("Run config has multiple selectors");
                } else {
                    JsonElement ns = obj.get("namespace");
                    if (!JSONUtils.isString(ns)) {
                        throw new GameTestConfigException("Run config 'namespace' is not a string");
                    }
                    run = ITestRun.namespace(ns.getAsString());
                }
            }
            if (run == null) run = ITestRun.none();

            if (obj.has("include")) {
                try {
                    run = run.include(loadRuns(obj.get("include")));
                } catch (GameTestConfigException e) {
                    error("Run config 'include': " + e.getLocalizedMessage());
                }
            }
            if (obj.has("exclude")) {
                try {
                    run = run.exclude(loadRuns(obj.get("exclude")));
                } catch (GameTestConfigException e) {
                    error("Run config 'include': " + e.getLocalizedMessage());
                }
            }
            if (obj.has("only")) {
                if (!JSONUtils.isString(obj.get("only"))) {
                    error("Run config 'only' is not a string");
                } else {
                    String only = obj.get("only").getAsString();
                    if (only.equals("required")) {
                        run = run.requiredOnly();
                    } else if (only.equals("optional")) {
                        run = run.optionalOnly();
                    } else {
                        error("Illegal 'only' selector: '" + only + "'");
                    }
                }
            }
            if (obj.has("if") && obj.has("if_not")) {
                error("Run config with both 'if' and 'if_not'");
            } else {
                if (obj.has("if")) {
                    boolean ifValue = evaluateIf(obj.get("if"), true);
                    if (!ifValue) {
                        run = ITestRun.none();
                    }
                }
                if (obj.has("if_not")) {
                    boolean ifValue = !evaluateIf(obj.get("if_not"), false);
                    if (ifValue) {
                        run = ITestRun.none();
                    }
                }
            }

            return run;
        } else if (JSONUtils.isString(json)) {
            String selector = json.getAsString().trim();
            if (selector.equals("all")) {
                return ITestRun.all();
            } else if (selector.equals("none")) {
                return ITestRun.none();
            } else if (selector.startsWith("function ")) {
                return ITestRun.function(selector.substring("function ".length()).trim());
            } else if (selector.startsWith("batch ")) {
                return ITestRun.batch(selector.substring("batch ".length()).trim());
            } else if (selector.startsWith("class ")) {
                return ITestRun.testClass(selector.substring("class ".length()).trim());
            } else if (selector.startsWith("namespace ")) {
                return ITestRun.namespace(selector.substring("namespace ".length()).trim());
            } else {
                return ITestRun.function(selector);
            }
        } else {
            throw new GameTestConfigException("Run config is not a string, object or array");
        }
    }

    private boolean evaluateIf(JsonElement ifEl, boolean def) {
        if (ifEl.isJsonObject()) {
            JsonObject o = ifEl.getAsJsonObject();
            if (o.has("and") || o.has("or")) {
                boolean or = o.has("or");

                JsonElement entries = o.get(or ? "or" : "and");
                if (!entries.isJsonArray()) {
                    throw new GameTestConfigException("Run config 'if': '" + (or ? "or" : "and") + "' is not an array");
                }
                JsonArray entriesArr = entries.getAsJsonArray();
                if (entriesArr.size() == 0) {
                    warning("Run config 'if': '" + (or ? "or" : "and") + "' is empty");
                }
                if (entriesArr.size() == 1) {
                    warning("Run config 'if': '" + (or ? "or" : "and") + "' is useless");
                }
                boolean out = false;
                for (JsonElement e : entriesArr) {
                    boolean b = evaluateIf(e, false);
                    out = or ? out || b : out && b;
                }
            } else if (o.has("not")) {
                return !evaluateIf(o.get("not"), !def);
            }
        }
        return getBoolean(ifEl, def);
    }

    private void loadOutputSection(JsonElement outputEl) {
        if (!outputEl.isJsonArray()) {
            error("'output' section is not an array");
            return;
        }
        JsonArray output = outputEl.getAsJsonArray();

        for (JsonElement element : output) {
            if (!element.isJsonObject()) {
                error("'output' entry is not an object");
                return;
            }

            try {
                TestOutputManager.loadOutput(element.getAsJsonObject());
            } catch (GameTestConfigException e) {
                error(e.getLocalizedMessage());
            }
        }
    }

    private void loadConfigSection(JsonElement configEl) {
        if (!configEl.isJsonObject()) {
            throw new GameTestConfigException("'config' section is not an object");
        }
        JsonObject config = configEl.getAsJsonObject();

        if (config.has("row_size")) {
            defaultTestsPerRow = getInt(config.get("row_size"), 1, Integer.MAX_VALUE, defaultTestsPerRow);
        }
        if (config.has("parallel_tests")) {
            defaultSimultaneousTests = getInt(config.get("parallel_tests"), 1, Integer.MAX_VALUE, defaultSimultaneousTests);
        }
        if (config.has("runthis_radius")) {
            defaultRunThisRadius = getInt(config.get("runthis_radius"), 1, 401, defaultRunThisRadius);
        }
        if (config.has("default_timeout")) {
            defaultTimeoutTicks = getInt(config.get("default_timeout"), 0, Integer.MAX_VALUE, defaultTimeoutTicks);
        }
        if (config.has("default_predelay")) {
            defaultPredelayTicks = getInt(config.get("default_predelay"), 0, Integer.MAX_VALUE, defaultPredelayTicks);
        }
        if (config.has("template_path")) {
            templatePath = getFile(config.get("template_path"), true, false, templatePath);
        }
    }

    private int getInt(JsonElement element, int min, int max, Integer def) {
        return getSystem(
            element,
            p -> assertRange(p.getAsInt(), min, max),
            p -> assertRange(Integer.parseInt(p), min, max),
            () -> def == null || def < min || def >= max ? min : def
        );
    }

    private int assertRange(int v, int min, int max) {
        if (v < min || v >= max) {
            throw new NumberFormatException(v + " is out of range [" + min + ".." + max + "]");
        }
        return v;
    }

    private String getString(JsonElement element, String defaultString) {
        return getSystem(
            element,
            JsonPrimitive::getAsString,
            Function.identity(),
            () -> defaultString == null ? "" : defaultString
        );
    }

    private boolean getBoolean(JsonElement element, boolean def) {
        return getSystem(
            element,
            JsonPrimitive::getAsBoolean,
            p -> {
                if (p.equals("true")) return true;
                if (p.equals("false")) return false;
                throw new GameTestConfigException("Not a boolean: '" + p + "'");
            },
            () -> def
        );
    }

    private File getFile(JsonElement element, Boolean directory, boolean checkExists, File defaultFile) {
        return getSystem(
            element,
            p -> assertExists(p.getAsString(), directory, checkExists, defaultFile),
            p -> assertExists(p, directory, checkExists, defaultFile),
            () -> defaultFile
        );
    }

    private File assertExists(String path, Boolean directory, boolean checkExists, File def) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(base, path).getAbsoluteFile();
        }
        if (checkExists && !file.exists()) {
            error("File '" + path + "' does not exist (looked at '" + file + "')");
            return def;
        }
        if (directory == null || file.isDirectory() == directory) {
            return file;
        }
        error("File '" + path + "' is" + (directory ? " not " : " ") + "a directory");
        return def;
    }

    private <T> T getSystem(JsonElement element, Function<JsonPrimitive, T> fromJson, Function<String, T> fromSystem, Supplier<T> defValue) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() < 1) {
                error("System property array does not have elements");
                return defValue.get();
            }
            if (array.size() > 2) {
                error("System property array has too much elements");
                return defValue.get();
            }
            JsonElement sysVar = array.get(0);
            JsonElement def = null;
            if (!JSONUtils.isString(sysVar)) {
                error("System property is not a string");
                return defValue.get();
            }
            if (array.size() == 2) {
                def = array.get(1);
                if (!def.isJsonPrimitive()) {
                    error("System property default is not a primitive");
                    return defValue.get();
                }
            }
            Properties props = System.getProperties();
            return getSystemVar(props::containsKey, props::getProperty, sysVar, def, fromJson, fromSystem, defValue);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("select")) {
                JsonElement select = object.get("select");
                String selectValue = getString(select, "default");
                JsonElement value;
                if (!object.has(selectValue)) {
                    if (object.has("default")) {
                        value = object.get("default");
                    } else {
                        error("Unknown 'select' value '" + selectValue + "', and no 'default' was specified");
                        return defValue.get();
                    }
                } else {
                    value = object.get(selectValue);
                }
                return getSystem(value, fromJson, fromSystem, defValue);
            } else {
                boolean env = false;
                JsonElement sysVar;

                if (object.has("env")) {
                    sysVar = object.get("env");
                    env = true;
                } else if (object.has("prop")) {
                    sysVar = object.get("prop");
                } else {
                    error("System variable object must have 'select', 'prop' or 'env'");
                    return defValue.get();
                }
                if (!JSONUtils.isString(sysVar)) {
                    error("System variable name is not a string");
                    return defValue.get();
                }

                JsonElement def = null;
                if (object.has("default")) {
                    def = object.get("default");
                    if (!def.isJsonPrimitive()) {
                        error("System variable default is not a primitive");
                        return defValue.get();
                    }
                }

                Predicate<String> contains = env ? System.getenv()::containsKey : System.getProperties()::containsKey;
                Function<String, String> envmap = env ? System.getenv()::get : System.getProperties()::getProperty;
                return getSystemVar(contains, envmap, sysVar, def, fromJson, fromSystem, defValue);
            }
        } else if (element.isJsonPrimitive()) {
            try {
                return fromJson.apply(element.getAsJsonPrimitive());
            } catch (Throwable e) {
                error("Invalid config value: " + e.getLocalizedMessage());
                return defValue.get();
            }
        } else {
            error("Null config value");
            return defValue.get();
        }
    }

    private <T> T getSystemVar(Predicate<String> contains, Function<String, String> env, JsonElement sysVar, JsonElement def, Function<JsonPrimitive, T> fromJson, Function<String, T> fromSystem, Supplier<T> defValue) {
        if (contains.test(sysVar.getAsString())) {
            try {
                return fromSystem.apply(env.apply(sysVar.getAsString()));
            } catch (Throwable e) {
                if (def == null) {
                    error("System property '" + sysVar.getAsString() + "' has malformed value: " + e.getLocalizedMessage());
                    return defValue.get();
                } else {
                    try {
                        return fromJson.apply(def.getAsJsonPrimitive());
                    } catch (Throwable e2) {
                        error("System property '" + sysVar.getAsString() + "' and specified default value have both malformed values: " + e2.getLocalizedMessage() + " & " + e.getLocalizedMessage());
                        return defValue.get();
                    }
                }
            }
        } else {
            try {
                return fromJson.apply(def.getAsJsonPrimitive());
            } catch (Throwable e) {
                error("System property '" + sysVar.getAsString() + "' is not available and specified default value has malformed value: " + e.getLocalizedMessage());
                return defValue.get();
            }
        }
    }

    private void loadClassesSection(JsonElement classesEl) {
        if (!classesEl.isJsonArray()) {
            error("'classes' section is not an array");
            return;
        }
        JsonArray classes = classesEl.getAsJsonArray();

        for (JsonElement element : classes) {
            if (!JSONUtils.isString(element)) {
                error("'classes' section contains non-string");
                return;
            }

            String className = element.getAsString();
            try {
                Class<?> cls = Class.forName(className);
                GameTestRegistry.registerClass(cls);
            } catch (ClassNotFoundException e) {
                warning("Could not find test class '" + className + "': " + e);
            } catch (IllegalArgumentException e) {
                warning("Failed loading test class '" + className + "': " + e);
            }
        }
    }

    private void error(String problem, Object... args) {
        LOGGER.error("Config file '{}':", name);
        LOGGER.error("  " + (args.length == 0 ? problem : String.format(problem, args)));
    }

    private void warning(String problem, Object... args) {
        LOGGER.warn("Config file '{}':", name);
        LOGGER.warn("  " + (args.length == 0 ? problem : String.format(problem, args)));
    }
}
