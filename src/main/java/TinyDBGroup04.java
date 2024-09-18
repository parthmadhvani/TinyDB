import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import model.User;
import utils.HashUtils;
import utils.Logger;
import utils.UserUtils;

/**
 * Main class for the TinyDB application, providing functionality for user registration, login, and database operations.
 */
public class TinyDBGroup04
{
    // Scanner instance for reading user input
    private static final Scanner scanner = new Scanner(System.in);

    // Path to the user data file
    private static final String DATA_FILE = "src/main/resources/users.txt";

    // Regular expression for validating email addresses
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    // DBMS instance for database operations
    private static final DBMS dbms = new DBMS();

    // ERD instance for creating Entity-Relationship Diagrams
    private static final ERD erd = new ERD();
    //transaction manager initiation
    private static TransactionManager transactionManager = new TransactionManager();

    /**
     * Main method to start the TinyDB application.
     * Displays the main menu and handles user choices.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args)
    {
        // Log that the application has started
        Logger.logGeneral("Application started.");

        // Ensure the data directory and files exist
        ensureDataDirectoryExists();
        ensureFilesExist();

        // Main menu loop
        while (true)
        {
            System.out.println("Main Menu");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            // Handle user choices
            switch (choice)
            {
                case "1":
                    registerUser();
                    break;
                case "2":
                    loginUser();
                    break;
                case "3":
                    // Log and exit the application
                    Logger.logGeneral("Exiting program.");
                    System.out.println("Exiting program.");
                    scanner.close();
                    return;
                default:
                    // Log invalid choice
                    Logger.logGeneral("Invalid choice in main menu: " + choice);
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Ensures that the data directory exists. Creates it if it does not.
     */
    private static void ensureDataDirectoryExists()
    {
        try
        {
            Files.createDirectories(Paths.get("src/main/resources"));
            Logger.logGeneral("Data directory verified/created successfully.");
        }
        catch (IOException e)
        {
            // Log and exit if directory creation fails
            Logger.logGeneral("Error creating data directory: " + e.getMessage());
            System.err.println("Error creating data directory: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Ensures that the data file exists and is properly initialized.
     * Creates the file if it does not exist, and writes metadata if it is empty.
     */
    private static void ensureFilesExist()
    {
        try
        {
            if (!Files.exists(Paths.get(DATA_FILE)))
            {
                Files.createFile(Paths.get(DATA_FILE));
                Logger.logGeneral("Data file created: " + DATA_FILE);
            }

            if (Files.size(Paths.get(DATA_FILE)) == 0)
            {
                String metadata = "userID###password###securityQuestions###securityAnswers\n";
                Files.write(Paths.get(DATA_FILE), metadata.getBytes(), StandardOpenOption.WRITE);
                Logger.logGeneral("Metadata written to data file: " + DATA_FILE);
            }
        }
        catch (IOException e)
        {
            // Log and exit if file creation fails
            Logger.logGeneral("Error creating data files: " + e.getMessage());
            System.err.println("Error creating data files: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Handles user registration by prompting for user details and storing them.
     */
    static void registerUser()
    {
        // Log the start of user registration
        Logger.logEvent("User registration started.");

        System.out.println("\n--- User Registration ---");

        // Email validation since user ID is email
        String userID;
        while (true)
        {
            System.out.print("Enter UserID (email): ");
            userID = scanner.nextLine();
            if (isValidEmail(userID))
            {
                break;
            }
            else
            {
                // Log and prompt for valid email format
                Logger.logEvent("Invalid email format attempted: " + userID);
                System.out.println("Invalid email format. Please try again.");
            }
        }

        // Check if the userID already exists
        LinkedList<User> users = UserUtils.loadUsers(DATA_FILE);
        for (User user : users)
        {
            if (user.getUserID().equals(userID))
            {
                // Log and notify userID already exists
                Logger.logEvent("UserID already exists: " + userID);
                System.out.println("UserID already exists. Please try again.");
                return;
            }
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        String hashedPassword = HashUtils.hashMD5(password);

        List<String> securityQuestions = new ArrayList<>();
        List<String> securityAnswers = new ArrayList<>();

        securityQuestions.add("What is your pet's name?");
        securityQuestions.add("What city were you born in?");
        securityQuestions.add("What is your favorite movie?");
        for (String question : securityQuestions)
        {
            System.out.print(question + ": ");
            String answer = scanner.nextLine();
            securityAnswers.add(answer);
        }

        User newUser = new User(userID, hashedPassword, securityQuestions, securityAnswers);
        UserUtils.saveUser(newUser, DATA_FILE);

        // Log and notify successful registration
        Logger.logEvent("User registered successfully: " + userID);
        System.out.println("Registration successful.\n");
    }

    /**
     * Handles user login by verifying credentials and security questions.
     */
    static void loginUser()
    {
        // Log the start of user login
        Logger.logEvent("User login started.");

        System.out.println("\n--- User Login ---");
        System.out.print("Enter UserID: ");
        String userID = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        String hashedPassword = HashUtils.hashMD5(password);

        LinkedList<User> users = UserUtils.loadUsers(DATA_FILE);

        User currentUser = null;

        for (User user : users)
        {
            if (user.getUserID().equals(userID) && user.getPassword().equals(hashedPassword))
            {
                currentUser = user;
                break;
            }
        }

        if (currentUser != null)
        {
            if (verifySecurityQuestions(currentUser))
            {
                // Log and notify successful login
                Logger.logEvent("Login successful: " + userID);
                System.out.println("Login successful.\n");
                userMenu(currentUser);
            }
            else
            {
                // Log and notify failed login due to incorrect security answers
                Logger.logEvent("Login failed due to incorrect security answers: " + userID);
                System.out.println("Login failed. Incorrect answers to security questions.\n");
            }
        }
        else
        {
            // Log and notify failed login due to invalid credentials
            Logger.logEvent("Login failed due to invalid credentials: " + userID);
            System.out.println("Login failed. Invalid UserID or Password.\n");
        }
    }

    /**
     * Verifies security questions for the provided user.
     *
     * @param user The User object for which to verify security questions.
     * @return True if all answers are correct, false otherwise.
     */
    private static boolean verifySecurityQuestions(User user)
    {
        List<String> securityQuestions = user.getSecurityQuestions();
        List<String> securityAnswers = user.getSecurityAnswers();

        for (int i = 0; i < securityQuestions.size(); i++)
        {
            System.out.print(securityQuestions.get(i) + ": ");
            String answer = scanner.nextLine();
            if (!answer.equals(securityAnswers.get(i)))
            {
                return false; // Return false if any answer is incorrect
            }
        }
        return true; // Return true if all answers are correct
    }

    /**
     * Displays the user menu and handles user choices related to database operations.
     *
     * @param user The User object representing the logged-in user.
     */
    private static void userMenu(User user)
    {
        // Log user menu access
        Logger.logGeneral("User menu accessed by: " + user.getUserID());

        while (true)
        {
            System.out.println("User Menu");
            System.out.println("1. Write Queries");
            System.out.println("2. Export Data and Structure");
            System.out.println("3. ERD");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            String userChoice = scanner.nextLine();

            switch (userChoice)
            {
                case "1":
                    // Log and execute query functionality
                    Logger.logQuery("User " + user.getUserID() + " writing query to execute");
                    writeAndExecuteQuery();
                    break;
                case "2":
                    // Log and display export data functionality
                    Logger.logEvent("User exporting data and structure: " + user.getUserID());
                    System.out.println("Export Data and Structure");
                    break;
                case "3":
                    // Log and create ERD functionality
                    Logger.logEvent("User accessing ERD functionality: " + user.getUserID());
                    System.out.println("ERD or reverse engineer");
                    System.out.println("Enter database name : ");
                    String tableName = scanner.next();
                    erd.createERD(tableName);
                    break;
                case "4":
                    // Log and exit user session
                    Logger.logGeneral("User logged out: " + user.getUserID());
                    System.out.println("Logging out.\n");
                    return;
                default:
                    // Log and notify invalid choice
                    Logger.logGeneral("Invalid choice in user menu by: " + user.getUserID());
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prompts the user to enter a query and executes it.
     */
    private static void writeAndExecuteQuery()
    {
    	System.out.print("Enter your query: ");
        String query = scanner.nextLine().trim().toUpperCase();
        System.out.println("Executing query: " + query);

        if (query.equals("BEGIN")) {
        	Database db = dbms.getDatabase();
            transactionManager.beginTransaction(db);
            Logger.logQuery("Transaction started.");
            System.out.println("Transaction started.");
        } else if (query.equals("COMMIT")) {
            transactionManager.commitTransaction();
            Logger.logQuery("Transaction committed.");
            System.out.println("Transaction committed.");
        } else if (query.equals("ROLLBACK")) {
            transactionManager.rollbackTransaction();
            Logger.logQuery("Transaction rolled back.");
            System.out.println("Transaction rolled back.");
        } else {
            try {
                if (transactionManager.isInTransaction()) {
                    transactionManager.logOperation(query);
                } else {
                    boolean success = dbms.executeQuery(query);
                    if (success) {
                        Logger.logQuery("Query executed successfully: " + query);
                        System.out.println("Query executed successfully.");
                    } else {
                        Logger.logQuery("Failed to execute query: " + query);
                        System.out.println("Failed to execute query.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error executing query: " + query);
                e.printStackTrace();
                Logger.logQuery("Exception while executing query: " + query + ". Exception: " + e.getMessage());
                if (transactionManager.isInTransaction()) {
                    transactionManager.rollbackTransaction();
                    System.out.println("Transaction rolled back due to error.");
                }
            }
        }
    }


    /**
     * Validates the email format using a regular expression.
     *
     * @param email The email address to validate.
     * @return True if the email address is valid, false otherwise.
     */
    private static boolean isValidEmail(String email)
    {
        return Pattern.matches(EMAIL_REGEX, email);
    }
}

