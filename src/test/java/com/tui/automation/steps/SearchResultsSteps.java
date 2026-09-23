package com.tui.automation.steps;

import com.tui.automation.data.TestData;
import com.tui.automation.pages.SearchResultsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchResultsSteps {

    private SearchResultsPage results() {
        return new SearchResultsPage();
    }

    @When("the user opens the {string} tab")
    public void theUserOpensTheTab(String tabName) {
        results().openTab(tabName);
    }

    @Then("the {string} tab is selected")
    public void theTabIsSelected(String tabName) {
        assertTrue(results().isDisplayed(), "Search results screen was not displayed");
        assertTrue(results().isTabSelected(tabName),
                "Expected tab \"" + tabName + "\" to be selected");
    }

    @Then("a card for the first hotel is visible")
    public void aCardForTheFirstHotelIsVisible() {
        String hotel = TestData.firstHotelName();
        assertTrue(results().hasCardContaining(hotel),
                "Expected hotel card \"" + hotel + "\" but saw: " + results().firstCardName());
    }

    @Then("a card for the first holiday is visible")
    public void aCardForTheFirstHolidayIsVisible() {
        String holiday = TestData.firstHolidayName();
        assertTrue(results().hasCardContaining(holiday),
                "Expected holiday card \"" + holiday + "\" but saw: " + results().firstCardName());
    }

    @Then("the first card shows the expected board type")
    public void theFirstCardShowsTheExpectedBoardType() {
        String board = TestData.firstHotelBoard();
        assertTrue(results().firstCardBoard().contains(board),
                "Expected board \"" + board + "\" but was " + results().firstCardBoard());
    }

    @Then("no holiday card is visible")
    public void noHolidayCardIsVisible() {
        String holiday = TestData.firstHolidayName();
        assertFalse(results().hasCardContaining(holiday),
                "Hotels tab still showed holiday offer \"" + holiday
                        + "\"; first visible card: " + results().firstCardName());
    }

    @Then("no hotel card is visible")
    public void noHotelCardIsVisible() {
        String hotel = TestData.firstHotelName();
        assertFalse(results().hasCardContaining(hotel),
                "Holidays tab still showed hotel offer \"" + hotel
                        + "\"; first visible card: " + results().firstCardName());
    }

    @Then("the first card shows a price")
    public void theFirstCardShowsAPrice() {
        String price = results().firstCardPrice();
        assertTrue(price != null && !price.isBlank(), "Price CTA was empty");
        assertTrue(price.matches(".*\\d.*"), "Price CTA had no digits: " + price);
    }
}
