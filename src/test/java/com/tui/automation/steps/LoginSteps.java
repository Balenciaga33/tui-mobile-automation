package com.tui.automation.steps;

import com.tui.automation.data.TestData;
import com.tui.automation.pages.LoginPage;
import com.tui.automation.pages.SearchResultsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps {

    private LoginPage loginPage() {
        return new LoginPage();
    }

    @Given("the login screen is displayed")
    public void theLoginScreenIsDisplayed() {
        loginPage().waitUntilVisible();
    }

    @Given("the user is logged in")
    public void theUserIsLoggedIn() {
        loginPage().waitUntilVisible().login(TestData.username(), TestData.password());
        new SearchResultsPage().waitUntilVisible();
    }

    @When("the user submits the login form")
    public void theUserSubmitsTheLoginForm() {
        loginPage().submit();
    }

    @When("the user enters a username and password without a date of birth")
    public void theUserEntersUsernameAndPasswordWithoutDob() {
        loginPage()
                .enterUsername(TestData.username())
                .enterPassword(TestData.password());
    }

    @When("the user fills a valid login form")
    public void theUserFillsAValidLoginForm() {
        loginPage().fillValidForm(TestData.username(), TestData.password());
    }

    @When("the user logs in with a valid form")
    public void theUserLogsInWithAValidForm() {
        loginPage().login(TestData.username(), TestData.password());
    }

    @Then("the username field shows the error {string}")
    public void theUsernameFieldShowsTheError(String expected) {
        assertEquals(expected, loginPage().usernameError());
    }

    @Then("the password field shows the error {string}")
    public void thePasswordFieldShowsTheError(String expected) {
        assertEquals(expected, loginPage().passwordError());
    }

    @Then("the date of birth field shows the error {string}")
    public void theDateOfBirthFieldShowsTheError(String expected) {
        assertEquals(expected, loginPage().dateOfBirthError());
    }

    @Then("the username field has no error")
    public void theUsernameFieldHasNoError() {
        assertFalse(loginPage().hasUsernameError(), "Username unexpectedly showed a validation error");
    }

    @Then("the password field has no error")
    public void thePasswordFieldHasNoError() {
        assertFalse(loginPage().hasPasswordError(), "Password unexpectedly showed a validation error");
    }

    @Then("the date of birth field shows the selected date")
    public void theDateOfBirthFieldShowsTheSelectedDate() {
        assertEquals(TestData.dateOfBirth(), loginPage().dateOfBirthValue());
    }

    @Then("the login screen is still displayed")
    public void theLoginScreenIsStillDisplayed() {
        assertTrue(loginPage().isDisplayed());
    }

    @Then("the search results screen is displayed")
    public void theSearchResultsScreenIsDisplayed() {
        new SearchResultsPage().waitUntilVisible();
        assertTrue(new SearchResultsPage().isDisplayed());
    }
}
