package com.tui.automation.pages;

import com.tui.automation.config.FrameworkConfig;
import com.tui.automation.driver.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(FrameworkConfig.get().timeoutSeconds()));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void tap(By locator) {
        waitClickable(locator).click();
    }

    protected void type(By locator, String value) {
        WebElement field = waitVisible(locator);
        field.click();
        field.clear();
        field.sendKeys(value);
    }

    protected String textOf(By locator) {
        return waitVisible(locator).getText();
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected List<WebElement> all(By locator) {
        return driver.findElements(locator);
    }

    protected static By testId(String id) {
        return AppiumBy.id(id);
    }
}
