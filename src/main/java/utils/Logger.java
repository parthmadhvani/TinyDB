package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for logging messages to different log files.
 * This class provides methods to log general messages, queries, and events.
 */
public class Logger {

    // Directory paths for different types of logs
    private static final String LOG_DIRECTORY_GENERAL = "logs/general/";
    private static final String LOG_DIRECTORY_QUERY = "logs/query/";
    private static final String LOG_DIRECTORY_EVENT = "logs/event/";

    // Date format for logging and file names
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    private static final String LOG_FILE = "_TinyDBGroup04.log";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    /**
     * Logs a message to a file in the specified directory.
     *
     * @param directoryPath The path to the directory where the log file will be created.
     * @param category The category of the log (e.g., GENERAL, QUERY, EVENT).
     * @param message The message to be logged.
     */
    private static synchronized void log(String directoryPath, LogCategory category, String message) {
        // Get today's date to organize logs by date
        String today = DATE_FORMAT.format(new Date());

        try (PrintWriter out = new PrintWriter(new FileWriter(directoryPath + today + LOG_FILE, true))) {
            // Format the log entry with a timestamp and category
            String timestamp = sdf.format(new Date());
            out.printf("[%s] ### [%s] %s%n", timestamp, category, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a log directory if it does not already exist.
     *
     * @param directoryPath The path to the directory to be created.
     */
    private static void createLogDirectory(String directoryPath) {
        File logDir = new File(directoryPath);
        if (!logDir.exists()) {
            // Create the directory and its parent directories if needed
            if (logDir.mkdirs()) {
                System.out.println("Log directory created successfully: " + directoryPath);
            } else {
                System.err.println("Failed to create log directory: " + directoryPath);
            }
        }
    }

    /**
     * Logs a general message.
     *
     * @param message The message to be logged.
     */
    public static void logGeneral(String message) {
        createLogDirectory(LOG_DIRECTORY_GENERAL);
        log(LOG_DIRECTORY_GENERAL, LogCategory.GENERAL, message);
    }

    /**
     * Logs a query message.
     *
     * @param message The message to be logged.
     */
    public static void logQuery(String message) {
        createLogDirectory(LOG_DIRECTORY_QUERY);
        log(LOG_DIRECTORY_QUERY, LogCategory.QUERY, message);
    }

    /**
     * Logs an event message.
     *
     * @param message The message to be logged.
     */
    public static void logEvent(String message) {
        createLogDirectory(LOG_DIRECTORY_EVENT);
        log(LOG_DIRECTORY_EVENT, LogCategory.EVENT, message);
    }

    /**
     * Enumeration for log categories.
     */
    public enum LogCategory {
        GENERAL, QUERY, EVENT
    }
}
