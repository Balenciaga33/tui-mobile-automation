package com.tui.automation.pages;

import com.tui.automation.data.TestData;
import org.openqa.selenium.By;

import java.time.LocalDate;

public class LoginPage extends BasePage {

    private static final By ROOT = testId("login_form_screen_root");
    private static final By USERNAME = testId("username_input_field");
    private static final By PASSWORD = testId("password_input_field");
    private static final By DATE_OF_BIRTH = testId("date_of_birth_field");
    private static final By CALENDAR = testId("date_of_birth_field_calendar_icon");
    private static final By SUBMIT = testId("login_form_submit_button");
    private static final By USERNAME_ERROR = testId("username_input_field_error");
    private static final By PASSWORD_ERROR = testId("password_input_field_error");
    private static final By DOB_ERROR = testId("date_of_birth_field_error");

    public boolean isDisplayed() {
        return isPresent(ROOT);
    }

    public LoginPage waitUntilVisible() {
        waitVisible(ROOT);
        return this;
    }

    public LoginPage enterUsername(String username) {
        type(USERNAME, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    public LoginPage chooseDateOfBirth() {
        return chooseDateOfBirth(LocalDate.parse(TestData.dateOfBirth()));
    }

    public LoginPage chooseDateOfBirth(LocalDate date) {
        tap(CALENDAR);
        new DateOfBirthDialog().selectDateAndConfirm(date);
        waitVisible(DATE_OF_BIRTH);
        return this;
    }

    public void submit() {
        tap(SUBMIT);
    }

    public void login(String username, String password) {
        fillValidForm(username, password);
        submit();
    }

    public LoginPage fillValidForm(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        chooseDateOfBirth();
        return this;
    }

    public String dateOfBirthValue() {
        return textOf(DATE_OF_BIRTH);
    }

    public String usernameError() {
        return textOf(USERNAME_ERROR);
    }

    public String passwordError() {
        return textOf(PASSWORD_ERROR);
    }

    public String dateOfBirthError() {
        return textOf(DOB_ERROR);
    }

    public boolean hasUsernameError() {
        return isPresent(USERNAME_ERROR);
    }

    public boolean hasPasswordError() {
        return isPresent(PASSWORD_ERROR);
    }

    public boolean hasDateOfBirthError() {
        return isPresent(DOB_ERROR);
    }
}
