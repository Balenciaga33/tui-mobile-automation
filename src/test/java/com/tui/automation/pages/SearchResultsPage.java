package com.tui.automation.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchResultsPage extends BasePage {

    private static final By ROOT = testId("search_result_screen_root");
    private static final By FIRST_NAME = testId("content_card_hotel_name_0");
    private static final By FIRST_BOARD = testId("content_card_board_type_0");
    private static final By FIRST_PRICE = testId("content_card_price_button_0");
    private static final By FIRST_PRICE_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"content_card_price_button_0\")"
                    + ".childSelector(new UiSelector().className(\"android.widget.TextView\"))");

    public SearchResultsPage waitUntilVisible() {
        waitVisible(ROOT);
        return this;
    }

    public boolean isDisplayed() {
        return isPresent(ROOT);
    }

    public void openTab(String tabName) {
        tap(tabId(tabName));
        waitVisible(FIRST_NAME);
    }

    public boolean isTabDisplayed(String tabName) {
        return isPresent(tabId(tabName));
    }

    public boolean isTabSelected(String tabName) {
        String selected = waitVisible(tabId(tabName)).getAttribute("selected");
        return "true".equalsIgnoreCase(selected);
    }

    public String firstCardName() {
        return textOf(FIRST_NAME);
    }

    public String firstCardBoard() {
        return textOf(FIRST_BOARD);
    }

    public String firstCardPrice() {
        waitVisible(FIRST_PRICE);
        return textOf(FIRST_PRICE_LABEL);
    }

    public boolean hasCardContaining(String name) {
        waitVisible(FIRST_NAME);
        return visibleCardNames().stream().anyMatch(text -> text.contains(name));
    }

    private List<String> visibleCardNames() {
        List<String> names = new ArrayList<>();
        for (int index = 0; ; index++) {
            By locator = testId("content_card_hotel_name_" + index);
            if (!isPresent(locator)) {
                break;
            }
            String text = driver.findElement(locator).getText();
            if (text != null && !text.isBlank()) {
                names.add(text);
            }
        }
        return names;
    }

    private static By tabId(String tabName) {
        return testId("top_app_bar_" + tabName.toLowerCase(Locale.ROOT) + "_tab");
    }
}
