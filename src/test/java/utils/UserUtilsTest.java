package utils;

import static org.junit.jupiter.api.Assertions.*;

import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class UserUtilsTest {

    private static final String TEST_DATA_FILE = "src/main/resources/test_data.txt";

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize test users
        user1 = new User("test1@example.com", "hashedPassword1",
                Arrays.asList("What is your pet's name?", "What city were you born in?", "What is your favorite movie?"),
                Arrays.asList("Buddy1", "New York1", "Inception1"));

        user2 = new User("test2@example.com", "hashedPassword2",
                Arrays.asList("What is your pet's name?", "What city were you born in?", "What is your favorite movie?"),
                Arrays.asList("Buddy2", "New York2", "Inception2"));

        // Ensure test data file is clean
        Files.deleteIfExists(Paths.get(TEST_DATA_FILE));
        Files.createFile(Paths.get(TEST_DATA_FILE));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test data file after each test
        Files.deleteIfExists(Paths.get(TEST_DATA_FILE));
    }

    @Test
    void testSaveUser() throws IOException {
        // Save users
        UserUtils.saveUser(user1, TEST_DATA_FILE);
        UserUtils.saveUser(user2, TEST_DATA_FILE);

        // Read the file content
        List<String> lines = Files.readAllLines(Paths.get(TEST_DATA_FILE));

        // Verify the content
        assertEquals(2, lines.size());
        assertEquals(user1.toString(), lines.get(0));
        assertEquals(user2.toString(), lines.get(1));
    }

    @Test
    void testLoadUsers() throws IOException {
        // Write test data directly to the file
        Files.write(Paths.get(TEST_DATA_FILE), Arrays.asList(
                user1.toString(),
                user2.toString()
        ));

        // Load users
        LinkedList<User> users = UserUtils.loadUsers(TEST_DATA_FILE);

        // Verify the loaded users
        assertEquals(2, users.size());
        assertEquals(user1.getUserID(), users.get(0).getUserID());
        assertEquals(user1.getPassword(), users.get(0).getPassword());
        assertEquals(user1.getSecurityQuestions(), users.get(0).getSecurityQuestions());
        assertEquals(user1.getSecurityAnswers(), users.get(0).getSecurityAnswers());

        assertEquals(user2.getUserID(), users.get(1).getUserID());
        assertEquals(user2.getPassword(), users.get(1).getPassword());
        assertEquals(user2.getSecurityQuestions(), users.get(1).getSecurityQuestions());
        assertEquals(user2.getSecurityAnswers(), users.get(1).getSecurityAnswers());
    }
}
