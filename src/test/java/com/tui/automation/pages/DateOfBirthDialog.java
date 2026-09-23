package com.tui.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Material date picker on the login form.
 * Text-input mode uses a {@code MM/DD/YYYY} masked field — type digits only
 * (e.g. {@code 10211990} → {@code 10/21/1990}), dismiss the keyboard, then Confirm.
 */
public class DateOfBirthDialog extends BasePage {

    private static final By DIALOG = testId("date_of_birth_dialog");
    private static final By CONFIRM = testId("date_of_birth_dialog_confirm_button");
    private static final By CONFIRM_LABEL = AppiumBy.androidUIAutomator("new UiSelector().text(\"Confirm\")");
    private static final By TEXT_INPUT_MODE = AppiumBy.accessibilityId("Switch to text input mode");
    private static final By DATE_EDIT = AppiumBy.className("android.widget.EditText");
    private static final DateTimeFormatter DIGITS_MM_DD_YYYY = DateTimeFormatter.ofPattern("MMddyyyy");

    public void selectDateAndConfirm(LocalDate date) {
        waitVisible(DIALOG);
        enterDateViaTextInput(date);
        dismissKeyboard();
        // Confirm only after the IME is gone — otherwise the tap lands on the keyboard.
        waitClickable(CONFIRM_LABEL).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
    }

    private void enterDateViaTextInput(LocalDate date) {
        waitClickable(TEXT_INPUT_MODE).click();
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(DATE_EDIT));
        field.click();
        clearEditText();

        // Slashes break Compose/IME input; the mask inserts MM/DD/YYYY separators.
        String digits = date.format(DIGITS_MM_DD_YYYY);
        field.sendKeys(digits);

        wait.until(driver -> {
            String text = driver.findElement(DATE_EDIT).getText();
            return text != null && text.replaceAll("\\D", "").equals(digits);
        });
    }

    private void clearEditText() {
        if (!(driver instanceof AndroidDriver androidDriver)) {
            return;
        }
        androidDriver.pressKey(new KeyEvent(AndroidKey.MOVE_END));
        for (int i = 0; i < 12; i++) {
            androidDriver.pressKey(new KeyEvent(AndroidKey.DEL));
        }
    }

    private void dismissKeyboard() {
        if (!(driver instanceof AndroidDriver androidDriver)) {
            return;
        }
        try {
            if (androidDriver.isKeyboardShown()) {
                androidDriver.hideKeyboard();
            }
        } catch (RuntimeException ignored) {
            // fall through to BACK
        }
        try {
            if (androidDriver.isKeyboardShown()) {
                androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
            }
        } catch (RuntimeException ignored) {
            // already closed
        }
        wait.until(d -> {
            try {
                return !((AndroidDriver) d).isKeyboardShown();
            } catch (RuntimeException e) {
                return true;
            }
        });
        // Dialog re-lays out after the IME closes; wait for Confirm to be tappable again.
        waitClickable(CONFIRM);
    }
}
