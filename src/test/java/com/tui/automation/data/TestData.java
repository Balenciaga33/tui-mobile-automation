package com.tui.automation.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public final class TestData {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode USERS = load("testdata/users.json");
    private static final JsonNode OFFERS = load("testdata/offers.json");

    private TestData() {
    }

    public static String username() {
        return USERS.path("validUser").path("username").asText();
    }

    public static String password() {
        return USERS.path("validUser").path("password").asText();
    }

    public static String dateOfBirth() {
        return USERS.path("validUser").path("dateOfBirth").asText();
    }

    public static String firstHotelName() {
        return OFFERS.path("firstHotel").path("name").asText();
    }

    public static String firstHotelBoard() {
        return OFFERS.path("firstHotel").path("board").asText();
    }

    public static String firstHolidayName() {
        return OFFERS.path("firstHoliday").path("name").asText();
    }

    private static JsonNode load(String classpathLocation) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + classpathLocation);
            }
            return MAPPER.readTree(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + classpathLocation, e);
        }
    }
}
