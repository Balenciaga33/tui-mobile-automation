package com.tui.automation.hooks;

import com.tui.automation.driver.AndroidDeviceGuard;
import com.tui.automation.driver.AppiumServerManager;
import com.tui.automation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;

import java.nio.file.Files;
import java.nio.file.Path;

public class Hooks {

    @BeforeAll
    public static void beforeAll() {
        AndroidDeviceGuard.ensureDevice();
        AppiumServerManager.startIfNeeded();
    }

    @Before
    public void startSession() {
        DriverManager.start();
    }

    @After
    public void stopSession(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                var driver = DriverManager.getDriver();
                byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName());
                Path dir = Path.of("target", "screenshots");
                Files.createDirectories(dir);
                String base = safeName(scenario.getName());
                Files.write(dir.resolve(base + ".png"), screenshot);
                try {
                    Files.writeString(dir.resolve(base + ".page-source.xml"), driver.getPageSource());
                    scenario.attach(driver.getPageSource(), "text/xml", "page-source");
                } catch (Exception ignored) {
                    // screenshot alone is still useful
                }
            }
        } catch (Exception ignored) {
            // keep the original test failure
        } finally {
            DriverManager.stop();
        }
    }

    @AfterAll
    public static void afterAll() {
        AppiumServerManager.stop();
    }

    private static String safeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
