package utils;

import model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for handling operations related to User data.
 * Provides methods to save user data to a file and load users from a file.
 */
public class UserUtils {

    /**
     * Saves a user object to the specified data file.
     *
     * @param user The user object to be saved.
     * @param dataFile The path to the file where user data will be saved.
     */
    public static void saveUser(User user, String dataFile) {
        try {
            // Convert user object to a string and append to the data file
            String userData = user.toString() + System.lineSeparator();
            Files.write(Paths.get(dataFile), userData.getBytes(), StandardOpenOption.APPEND);
            Logger.logEvent("User saved successfully: " + user.getUserID());
        } catch (IOException e) {
            // Log and print an error message if saving fails
            Logger.logEvent("Error saving user: " + user.getUserID() + " - " + e.getMessage());
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    /**
     * Loads all users from the specified data file.
     *
     * @param dataFile The path to the file from which users will be loaded.
     * @return A LinkedList containing all users loaded from the file.
     */
    public static LinkedList<User> loadUsers(String dataFile) {
        LinkedList<User> users = new LinkedList<>();
        try {
            // Read all lines from the data file and convert each line to a User object
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            for (String line : lines) {
                users.add(User.fromString(line));
            }
            Logger.logEvent("Users loaded successfully from file: " + dataFile);
        } catch (IOException e) {
            // Log and print an error message if loading fails
            Logger.logEvent("Error loading users from file: " + dataFile + " - " + e.getMessage());
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }
}
