import utils.SQLDumpUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;

import utils.SQLDumpUtility;


import utils.Logger;

/**
 * Represents a Database Management System (DBMS) that can execute various types of SQL queries
 * and manage databases and tables.
 */
public class DBMS {

    /** The current database being used. */
    private Database database;
    private final SQLDumpUtility sqlDumpUtility;


    /** The query executor used to run SQL queries. */
    private QueryExecuter queryExecuter = new QueryExecuter();


    public DBMS() {
        this.sqlDumpUtility = new SQLDumpUtility();
    }

    /**
     * Checks if a database with the given name is present in the "databases" directory.
     *
     * @param name the name of the database to check for
     * @return true if the database is present, false otherwise
     */
    public boolean isDataBasePresent(String name) {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");

        // Create a File object for the "databases" directory within the current directory
        File directory = new File(currentDir + File.separator + "databases");

        // List all files and directories in the "databases" directory
        File[] files = directory.listFiles();

        // Check if the directory is not empty
        if (files != null && files.length > 0) {
            // Iterate over each file in the directory
            for (File file : files) {
                // Check if the file is a directory and its name matches the given name
                if (file.isDirectory() && file.getName().equals(name)) {
                    // Create a new Database object with the given name
                    Database db = new Database(name);
                    // Set the current database to the new Database object
                    this.database = db;
                    Logger.logGeneral("Database found: " + name);
                    // Return true indicating the database is present
                    return true;
                }
            }
        } else {
            Logger.logGeneral("No databases present in directory: " + directory.getPath());
            // If the directory is empty, print a message indicating no databases are present
            System.out.println("No database present");
            // Return false indicating no databases are present
            return false;
        }
        Logger.logGeneral("Database not found: " + name);
        // Return false if the database with the given name is not found
        return false;
    }


    /**
     *
     * @param tableName name of the table that will come from the query
     * @param columns this will be the arrayList of string and each element will contain the name of the column and it's data type (eg. [student_id int] , [student_name varchanr] , [address varchar] )
     * We will use a RegEx , which will get all the column name and it's data type from the query and then we will make an arrayList out of it.
     * @return
     */
    private void addColumns(String tableName, List<String> columns) {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");

        // Construct the path to the data.txt file within the specified table directory
        String dataDirectoryPath = currentDir + File.separator + "databases"
                + File.separator + this.database.getName()
                + File.separator + tableName
                + File.separator + "data.txt";

        // Use BufferedWriter to append columns to the data.txt file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataDirectoryPath, true))) {
            int count = 0;
            // Iterate over the columns list
            for (String column : columns) {
                // Check if it's the last column in the list
                if (count == columns.size() - 1) {
                    writer.write(column);  // Write the column without a separator
                } else {
                    writer.write(column + " ### ");  // Write the column followed by a separator
                }
                count++;
            }
            writer.newLine();  // Write a new line after writing all columns
        } catch (IOException e) {
            // Print the exception message if an IOException occurs
            System.out.println(e.getMessage());
        }
    }

//    private Database getDatabase(String dbname) {
//        for (Database db : databases){
//            if (Objects.equals(db.getName(), dbname)){
//                return db;
//            }
//        }
//        return null;
//    }

    public boolean useDatabase(String query, Regex regex) {
        // Print the query for debugging purposes
        System.out.println("Query:" + query);

        // Match the query against the useDatabase regex pattern
        Matcher matcher = regex.useDatabase.matcher(query);

        // Check if the query matches the expected pattern
        if (!matcher.matches()) {
            // Print an error message if the query is invalid
            System.out.println("Invalid USE DATABASE query");
            return false;
        }

        // Extract the database name from the matched group
        String databaseName = matcher.group(1);

        // Check if the database with the given name is present
        boolean isDatabasePresent = isDataBasePresent(databaseName);
//  this.database = getDatabase(databaseName);  // Optionally get the Database object

        // Return true if the database is present, otherwise false
        return isDatabasePresent;
    }


    /**
     * Inserts data into the specified table.
     *
     * @param query SQL insert query (e.g., "INSERT INTO tableName (column1, column2, ...) VALUES (value1, value2, ...)").
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertData(String query) {
        // Sample query format: "INSERT INTO tableName (column1, column2, ...) VALUES (value1, value2, ...)"
        String tableName;
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Extract table name, columns, and values from the query
        try {
            // Find the index of "into" keyword and the start of the table name
            int startIndex = query.toLowerCase().indexOf("into") + 5;
            // Find the end index of the table name
            int endIndex = query.indexOf("(");
            // Extract the table name
            tableName = query.substring(startIndex, endIndex).trim();

            // Extract the columns part of the query
            String columnsString = query.substring(endIndex + 1, query.indexOf(")")).trim();
            // Extract the values part of the query
            String valuesString = query.substring(query.lastIndexOf("(") + 1, query.lastIndexOf(")")).trim();

            // Split the columns string into an array and add to columns list
            String[] columnsArray = columnsString.split(",");
            for (String col : columnsArray) {
                columns.add(col.trim());
            }

            // Split the values string into an array and add to values list
            String[] valuesArray = valuesString.split(",");
            for (String val : valuesArray) {
                values.add(val.trim());
            }
        } catch (Exception e) {
            // Print an error message if the query format is invalid
            System.out.println("Invalid insert query format.");
            return false;
        }

        File dataFile = isTablePresent(tableName);
        if (dataFile == null) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] existingValues = line.split("\\s*###\\s*");
                if (existingValues.length == values.size() && Arrays.equals(existingValues, values.toArray(new String[0]))) {
                    System.out.println("Duplicate entry detected. Skipping insertion.");
                    return false; // Exit method if duplicate found
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading data file: " + e.getMessage());
            return false;
        }

        // Write data into the table's data file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true))) {
            // Build the row to be inserted
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    row.append(" ### ");  // Append the separator between values
                }
                row.append(values.get(i));  // Append the value
            }
            writer.write(row.toString());  // Write the row to the file
            writer.newLine();  // Add a new line after writing the row
            System.out.println("Data inserted successfully into table: " + tableName);
            return true;
        } catch (IOException e) {
            // Print an error message if there is an issue writing to the file
            System.out.println("Error writing data to table: " + e.getMessage());
            return false;
        }
    }

    private File isTablePresent(String tableName) {

        //Verify if the database presents
        Database db = this.database;
        if (db == null) {
            System.out.println("No database selected");
            return null;
        }

        // Verify if the table exists
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + db.getName() + File.separator + tableName;
        String dataFileName = "data.txt";

        File dataFile = new File(directoryPath + File.separator + dataFileName);
        if (!dataFile.exists()) {
            System.out.println("Table does not exist");
            return null;
        }
        return dataFile;
    }

    /**
     * Executes SQL query against the current database.
     *
     * @param query the SQL query to execute
     * @return true if the query was executed successfully, false otherwise
     */
    public boolean executeQuery(String query) {
        // Instantiate Regex object for query matching
        Regex regex = new Regex();

        // Check the type of query and call corresponding method
        if (query.toLowerCase().startsWith("update")) {
            Logger.logEvent("Executing UPDATE query: " + query);
            return isDatabaseSelected() && queryExecuter.updateQuery(query, regex, database.getName());  // Call updateQuery method for UPDATE queries
        } else if (query.toLowerCase().startsWith("delete")) {
            Logger.logEvent("Executing DELETE query: " + query);
            return isDatabaseSelected() && queryExecuter.deleteQuery(query, regex, database.getName());  // Call deleteQuery method for DELETE queries
        } else if (query.toLowerCase().startsWith("drop")) {
            Logger.logEvent("Executing DROP query: " + query);
            return isDatabaseSelected() && queryExecuter.dropQuery(query, regex, database.getName());  // Call dropQuery method for DROP queries
        } else if (query.toLowerCase().startsWith("select")) {
            Logger.logEvent("Executing SELECT query: " + query);
            return isDatabaseSelected() && queryExecuter.selectQuery(query, regex, database.getName());  // Call selectQuery method for SELECT queries
        } else if (query.toLowerCase().startsWith("create database")) {
            Logger.logEvent("Executing CREATE DATABASE query: " + query);
            return queryExecuter.createDatabase(query, regex);  // Call createDatabase method for CREATE DATABASE queries
        } else if (query.toLowerCase().startsWith("create table")) {
            Logger.logEvent("Executing CREATE TABLE query: " + query);
            return isDatabaseSelected() && queryExecuter.createTableQuery(query, regex, database.getName());  // Call createTableQuery method for CREATE TABLE queries
        } else if (query.toLowerCase().startsWith("use")) {
            Logger.logEvent("Executing USE DATABASE query: " + query);
            this.database = queryExecuter.useDatabase(query, regex);
            if (this.database == null) {
                return false;
            }
            return true;
        } else if (query.toLowerCase().startsWith("insert")) {
            Logger.logEvent("Executing INSERT query: " + query);
            return isDatabaseSelected() && queryExecuter.insertData(query, database.getName());  // Call insertData method for INSERT queries
        } else {
            // Print an error message for unsupported query types
            Logger.logGeneral("Unsupported query type: " + query);
            System.out.println("Unsupported query type");
            return false;  // Return false for unsupported query types
        }
    }

    /**
     * Checks if a database has been selected for operations.
     *
     * @return true if a database is selected, false otherwise
     */
    private boolean isDatabaseSelected() {
        if (this.database == null) {
            Logger.logGeneral("No database selected");
            System.out.println("No database selected");
            return false;
        }
        return true;
    }
    public Database getDatabase() {
    	return this.database;
    }
    /**
     * Retrieves the index of a column in a table based on its name.
     *
     * @param tableName  the name of the table.
     * @param columnName the name of the column whose index is to be found.
     * @return the index of the column if found; -1 if not found.
     */
    private int getColumnIndex(String tableName, String columnName) {
        // Get the in-use database
        Database db = this.database;
        if (db == null) {
            Logger.logGeneral("No database selected");
            System.out.println("No database selected");
            return -1;
        }

        // Directory path for the table metadata
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + db.getName() + File.separator + tableName;
        String metadataFileName = "metadata.txt";

        // Read metadata file to get column names
        try (BufferedReader reader = new BufferedReader(new FileReader(directoryPath + File.separator + metadataFileName))) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(":");
                if (columns.length > 0 && columns[0].trim().equalsIgnoreCase(columnName.trim())) {
                    return index;
                }
                index++;
            }
        } catch (IOException e) {
            Logger.logGeneral("Error reading metadata file: " + e.getMessage());
            System.out.println("Error reading metadata file: " + e.getMessage());
        }

        // Column not found
        Logger.logGeneral("Column not found: " + columnName);
        System.out.println("Column not found: " + columnName);
        return -1;
    }

    /**
     * This method allows the user to export the data structure of a selected database.
     * It lists all available databases and prompts the user to choose one for which
     * the data structure will be exported as an SQL dump.
     *
     * @throws IOException if an I/O error occurs during the export process.
     */
    public void exportDataStructure() throws IOException {
        Scanner sc = new Scanner(System.in);
        File databaseFolder = new File("./databases");
        File[] dbs = databaseFolder.listFiles();

        if (dbs == null || dbs.length == 0) {
            System.out.println("No databases found.");
            return;
        }

        System.out.println("Choose option from below\n");
        for (int i = 0; i < dbs.length; i++) {
            System.out.println((i + 1) + ": " + dbs[i].getName());
        }
        System.out.print("Choice: ");
        int choice = sc.nextInt() - 1;
        if (choice < 0 || choice >= dbs.length) {
            System.out.println("Invalid choice");
        } else {
            generateSqlDump(dbs[choice].getAbsolutePath());
        }
    }

    /**
     * This method generates an SQL dump for the specified database.
     *
     * @param databasefullPath the full path of the database to be exported.
     * @throws IOException if an I/O error occurs during the export process.
     */
    public void generateSqlDump(String databasefullPath) throws IOException {
        sqlDumpUtility.export(databasefullPath);
    }

}

