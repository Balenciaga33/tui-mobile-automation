package com.tui.automation.driver;

import com.tui.automation.config.FrameworkConfig;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class AppiumServerManager {

    private static AppiumDriverLocalService service;

    private AppiumServerManager() {
    }

    public static synchronized void startIfNeeded() {
        FrameworkConfig config = FrameworkConfig.get();
        if (!config.startLocalServer()) {
            return;
        }
        if (service != null && service.isRunning()) {
            return;
        }
        Path projectDir = Path.of("").toAbsolutePath();
        Path appiumJs = projectDir.resolve("node_modules/appium/build/lib/main.js");
        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress(config.appiumHost())
                .usingPort(config.appiumPort())
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(GeneralServerFlag.LOG_LEVEL, "error")
                .withTimeout(Duration.ofSeconds(40));
        if (Files.exists(appiumJs)) {
            builder.withAppiumJS(appiumJs.toFile());
        }
        String node = firstExisting(
                System.getenv("NODE_BINARY"),
                which("node"),
                "/usr/local/bin/node",
                "/opt/homebrew/bin/node");
        if (node != null) {
            builder.usingDriverExecutable(new File(node));
        }
        String androidHome = firstNonBlank(
                System.getenv("ANDROID_HOME"),
                System.getenv("ANDROID_SDK_ROOT"),
                System.getProperty("user.home") + "/Library/Android/sdk");
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("ANDROID_HOME", androidHome);
        env.put("ANDROID_SDK_ROOT", androidHome);
        env.put("JAVA_HOME", firstNonBlank(System.getenv("JAVA_HOME"), System.getProperty("java.home")));
        builder.withEnvironment(env);
        service = AppiumDriverLocalService.buildService(builder);
        service.start();
    }

    public static synchronized void stop() {
        if (service != null && service.isRunning()) {
            service.stop();
        }
        service = null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String firstExisting(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank() && Files.isExecutable(Path.of(path))) {
                return path;
            }
        }
        return null;
    }

    private static String which(String binary) {
        try {
            Process process = new ProcessBuilder("bash", "-lc", "command -v " + binary)
                    .redirectErrorStream(true)
                    .start();
            String out = new String(process.getInputStream().readAllBytes()).trim();
            process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            return out.isBlank() ? null : out;
        } catch (Exception e) {
            return null;
        }
    }
}
