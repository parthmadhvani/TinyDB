package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class UserTest {

    private User user;
    private String userID;
    private String password;
    private List<String> securityQuestions;
    private List<String> securityAnswers;

    @BeforeEach
    void setUp() {
        userID = "test@example.com";
        password = "hashedPassword";
        securityQuestions = Arrays.asList("What is your pet's name?", "What city were you born in?", "What is your favorite movie?");
        securityAnswers = Arrays.asList("Buddy", "New York", "Inception");
        user = new User(userID, password, securityQuestions, securityAnswers);
    }

    @Test
    void testUser() {
        assertNotNull(user);
        assertEquals(userID, user.getUserID());
        assertEquals(password, user.getPassword());
        assertEquals(securityQuestions, user.getSecurityQuestions());
        assertEquals(securityAnswers, user.getSecurityAnswers());
    }

    @Test
    void testGetUserID() {
        assertEquals(userID, user.getUserID());
    }

    @Test
    void testGetPassword() {
        assertEquals(password, user.getPassword());
    }

    @Test
    void testGetSecurityQuestions() {
        assertEquals(securityQuestions, user.getSecurityQuestions());
    }

    @Test
    void testGetSecurityAnswers() {
        assertEquals(securityAnswers, user.getSecurityAnswers());
    }

    @Test
    void testToFormattedString() {
        String expected = "test@example.com###hashedPassword###What is your pet's name?;What city were you born in?;What is your favorite movie?###Buddy;New York;Inception";
        assertEquals(expected, user.toString());
    }

    @Test
    void testFromFormattedString() {
        String formattedString = "test@example.com###hashedPassword###What is your pet's name?;What city were you born in?;What is your favorite movie?###Buddy;New York;Inception";
        User parsedUser = User.fromString(formattedString);

        assertEquals(userID, parsedUser.getUserID());
        assertEquals(password, parsedUser.getPassword());
        assertEquals(securityQuestions, parsedUser.getSecurityQuestions());
        assertEquals(securityAnswers, parsedUser.getSecurityAnswers());
    }
}

