Feature: Login
  The login form is the only gate to search results.
  The app does not authenticate against an account store; it requires
  a non-empty username, password, and a date of birth from the calendar.

  @login
  Scenario: Login without date of birth shows only DOB required
    Given the login screen is displayed
    When the user enters a username and password without a date of birth
    And the user submits the login form
    Then the date of birth field shows the error "Required"
    And the username field has no error
    And the password field has no error
    And the login screen is still displayed

  @login @smoke
  Scenario: Completed login form opens search results
    Given the login screen is displayed
    When the user fills a valid login form
    Then the date of birth field shows the selected date
    When the user submits the login form
    Then the search results screen is displayed
