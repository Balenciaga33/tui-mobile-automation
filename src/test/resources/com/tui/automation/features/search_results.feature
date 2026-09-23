Feature: Search results
  After login the app shows bundled hotel and holiday offers
  behind All / Hotels / Holidays tabs. This is the main post-login product surface.
  Expected hotel / holiday anchors come from testdata/offers.json.

  Background:
    Given the user is logged in

  @offers @smoke
  Scenario: All tab shows both a hotel and a holiday offer
    Then the "All" tab is selected
    And a card for the first hotel is visible
    And a card for the first holiday is visible

  @offers
  Scenario: Hotels tab shows hotel search results
    When the user opens the "Hotels" tab
    Then a card for the first hotel is visible
    And the first card shows the expected board type
    And no holiday card is visible

  @offers
  Scenario: Holidays tab shows a holiday offer with a price
    When the user opens the "Holidays" tab
    Then a card for the first holiday is visible
    And the first card shows a price
    And no hotel card is visible
