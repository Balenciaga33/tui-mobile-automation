package com.tui.automation.driver;

import com.tui.automation.config.FrameworkConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class AndroidDeviceGuard {

    private AndroidDeviceGuard() {
    }

    public static void ensureDevice() {
        FrameworkConfig config = FrameworkConfig.get();
        if (!"android".equals(config.platform()) || "cloud".equals(config.execution())) {
            return;
        }
        if (hasConnectedDevice()) {
            return;
        }
        startAvd(config.androidAvd());
        waitForBoot();
        if (!hasConnectedDevice()) {
            throw new IllegalStateException("""
                    No Android device/emulator is connected.
                    Start one first, then re-run:
                      emulator -avd %s
                      adb wait-for-device
                    """.formatted(config.androidAvd()));
        }
    }

    private static boolean hasConnectedDevice() {
        String output = run(adb(), "devices");
        return output.lines()
                .skip(1)
                .anyMatch(line -> line.contains("\tdevice"));
    }

    private static void startAvd(String avd) {
        Path emulator = sdkPath("emulator", "emulator");
        if (emulator == null) {
            return;
        }
        ProcessBuilder builder = new ProcessBuilder(
                emulator.toString(), "-avd", avd, "-netdelay", "none", "-netspeed", "full", "-no-snapshot-load");
        builder.redirectErrorStream(true);
        try {
            builder.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start emulator " + avd, e);
        }
    }

    private static String adb() {
        Path path = sdkPath("platform-tools", "adb");
        return path != null ? path.toString() : "adb";
    }

    private static void waitForBoot() {
        run(adb(), "wait-for-device");
        for (int i = 0; i < 40; i++) {
            String boot = run(adb(), "shell", "getprop", "sys.boot_completed").trim();
            if ("1".equals(boot)) {
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static Path sdkPath(String... parts) {
        String home = firstNonBlank(System.getenv("ANDROID_HOME"), System.getenv("ANDROID_SDK_ROOT"),
                System.getProperty("user.home") + "/Library/Android/sdk");
        Path path = Path.of(home, parts);
        return Files.isExecutable(path) ? path : null;
    }

    private static String run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.lines().collect(Collectors.joining("\n"));
                process.waitFor(30, TimeUnit.SECONDS);
                return output;
            }
        } catch (Exception e) {
            return "";
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
