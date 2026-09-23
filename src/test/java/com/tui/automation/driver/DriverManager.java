package com.tui.automation.driver;

import com.tui.automation.config.FrameworkConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;

public final class DriverManager {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static AppiumDriver getDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Appium driver is not started");
        }
        return driver;
    }

    public static void start() {
        FrameworkConfig config = FrameworkConfig.get();
        AppiumDriver driver = switch (config.platform()) {
            case "android" -> {
                AndroidDriver android = new AndroidDriver(serverUrl(config), androidOptions(config));
                // Compose keeps the a11y tree "busy" on slow CI GPUs; idle waits then miss testTags.
                android.setSetting("waitForIdleTimeout", 0);
                android.setSetting("waitForSelectorTimeout", 0);
                android.setSetting("disableIdLocatorAutocompletion", true);
                ensureAndroidAppForeground(android, config);
                yield android;
            }
            case "ios" -> new IOSDriver(serverUrl(config), iosOptions(config));
            default -> throw new IllegalArgumentException("Unsupported platform: " + config.platform());
        };
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        DRIVER.set(driver);
    }

    private static void ensureAndroidAppForeground(AndroidDriver android, FrameworkConfig config) {
        String pkg = config.androidAppPackage();
        try {
            String current = android.getCurrentPackage();
            if (current == null || !pkg.equals(current)) {
                android.activateApp(pkg);
            }
        } catch (Exception e) {
            android.activateApp(pkg);
        }
    }

    public static void stop() {
        AppiumDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }

    private static URL serverUrl(FrameworkConfig config) {
        String raw;
        if ("cloud".equals(config.execution())) {
            raw = config.cloudUrl();
            if (raw.isBlank()) {
                throw new IllegalStateException(
                        "Cloud execution needs CLOUD_URL or BROWSERSTACK_USERNAME + BROWSERSTACK_ACCESS_KEY");
            }
        } else {
            raw = "http://%s:%d/".formatted(config.appiumHost(), config.appiumPort());
        }
        try {
            return URI.create(raw).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Appium server URL: " + raw, e);
        }
    }

    private static UiAutomator2Options androidOptions(FrameworkConfig config) {
        if (!Files.exists(config.androidApp()) && !"cloud".equals(config.execution())) {
            throw new IllegalStateException("Android APK not found at " + config.androidApp());
        }
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setAppPackage(config.androidAppPackage())
                .setAppActivity(config.androidAppActivity())
                .setAppWaitActivity(config.androidAppActivity())
                .setAppWaitDuration(Duration.ofSeconds(60))
                .setNoReset(false)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(120));
        if (!"cloud".equals(config.execution())) {
            options.setApp(config.androidApp().toString());
            String udid = System.getenv("ANDROID_UDID");
            if (udid != null && !udid.isBlank()) {
                options.setUdid(udid);
            } else {
                options.setDeviceName(config.androidAvd());
            }
        }
        options.setCapability("appium:enforceAppInstall", true);
        options.setCapability("appium:disableIdLocatorAutocompletion", true);
        options.setCapability("appium:ignoreHiddenApiPolicyError", true);
        options.setCapability("appium:adbExecTimeout", 60_000);
        return options;
    }

    private static XCUITestOptions iosOptions(FrameworkConfig config) {
        if (!Files.exists(config.iosApp())) {
            throw new IllegalStateException(
                    "iOS app was not provided (expected " + config.iosApp()
                            + "). Android scenarios are implemented; iOS capabilities are ready for an .ipa/.app.");
        }
        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(config.iosDeviceName())
                .setBundleId(config.iosBundleId())
                .setApp(config.iosApp().toString())
                .setNoReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(120));
        String udid = System.getenv("IOS_UDID");
        if (udid != null && !udid.isBlank()) {
            options.setUdid(udid);
        }
        return options;
    }
}
