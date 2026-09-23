package com.tui.automation.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public final class FrameworkConfig {

    private static final FrameworkConfig INSTANCE = new FrameworkConfig();

    private final Map<String, Object> root;

    @SuppressWarnings("unchecked")
    private FrameworkConfig() {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("config/config.yaml")) {
            if (in == null) {
                throw new IllegalStateException("Missing config/config.yaml on the test classpath");
            }
            this.root = new Yaml().load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load config.yaml", e);
        }
    }

    public static FrameworkConfig get() {
        return INSTANCE;
    }

    public String platform() {
        return firstNonBlank(System.getProperty("platform"), text("platform")).toLowerCase(Locale.ROOT);
    }

    public String execution() {
        return firstNonBlank(System.getProperty("execution"), text("execution")).toLowerCase(Locale.ROOT);
    }

    public int timeoutSeconds() {
        Object value = root.get("timeoutSeconds");
        return value instanceof Number number ? number.intValue() : 20;
    }

    public boolean startLocalServer() {
        return Boolean.TRUE.equals(nested("appium").get("startLocalServer"))
                && !"cloud".equals(execution());
    }

    public String appiumHost() {
        return String.valueOf(nested("appium").get("host"));
    }

    public int appiumPort() {
        Object value = nested("appium").get("port");
        return value instanceof Number number ? number.intValue() : 4723;
    }

    public String androidAvd() {
        return String.valueOf(nested("android").get("avd"));
    }

    public Path androidApp() {
        return Path.of(String.valueOf(nested("android").get("app"))).toAbsolutePath();
    }

    public String androidAppPackage() {
        return String.valueOf(nested("android").get("appPackage"));
    }

    public String androidAppActivity() {
        return String.valueOf(nested("android").get("appActivity"));
    }

    public Path iosApp() {
        return Path.of(String.valueOf(nested("ios").get("app"))).toAbsolutePath();
    }

    public String iosBundleId() {
        return String.valueOf(nested("ios").get("bundleId"));
    }

    public String iosDeviceName() {
        return String.valueOf(nested("ios").get("deviceName"));
    }

    public String cloudUrl() {
        String fromEnv = firstNonBlank(System.getenv("CLOUD_URL"), System.getenv("BROWSERSTACK_URL"));
        if (fromEnv != null) {
            return fromEnv;
        }
        String user = System.getenv("BROWSERSTACK_USERNAME");
        String key = System.getenv("BROWSERSTACK_ACCESS_KEY");
        if (user != null && key != null) {
            return "https://%s:%s@hub.browserstack.com/wd/hub".formatted(user, key);
        }
        Object configured = nested("cloud").get("serverUrl");
        return configured == null ? "" : String.valueOf(configured);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nested(String key) {
        Object value = root.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalStateException("Missing config section: " + key);
        }
        return (Map<String, Object>) map;
    }

    private String text(String key) {
        Object value = root.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank() && !"null".equals(value)) {
                return value;
            }
        }
        return "";
    }
}
