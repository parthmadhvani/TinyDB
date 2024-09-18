import utils.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

public class QueryExecuter {
    /**
     * Creates a table with the specified name and columns in the specified database.
     *
     * @param tableName The name of the table to create.
     * @param columns   A list of columns for the table.
     * @param dbname    The name of the database in which to create the table.
     * @return true if the table was successfully created, false otherwise.
     */
    public boolean createTableHelper(String tableName, List<String> columns, String dbname) {
        // Directory path for the table
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName;
        String metadataFileName = "metadata.txt";
        String dataFileName = "data.txt";

        boolean isDirectoryCreated = false;
        boolean isMetadataFileCreated = false;
        boolean isDataFileCreated = false;

        try {
            File directory = new File(directoryPath);
            File metadataFile = new File(directoryPath + File.separator + metadataFileName);
            File dataFile = new File(directoryPath + File.separator + dataFileName);

            // Create the directory for the table
            isDirectoryCreated = directory.mkdirs();

            // Create metadata file
            isMetadataFileCreated = metadataFile.createNewFile();

            // Create the data file
            isDataFileCreated = dataFile.createNewFile();

            // Log creation success
            Logger.logGeneral("Table directory created successfully: " + directoryPath);
            Logger.logGeneral("Metadata file created successfully: " + metadataFile.getPath());
            Logger.logGeneral("Data file created successfully: " + dataFile.getPath());

        } catch (IOException e) {
            Logger.logGeneral("Failed to create table files for table: " + tableName);
            throw new RuntimeException(e);
        }

        // If everything is created, make the table object and write the columns' names into data.txt file
        if (isDataFileCreated && isDirectoryCreated && isMetadataFileCreated) {
            Table table = new Table(tableName);
            ArrayList<String> filteredColumns = new ArrayList<>();
            String metadataDirectoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName + File.separator + "metadata.txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataDirectoryPath, true))) {
                for (String column : columns) {
                    String[] splitColumn = column.split(" ");
                    if (Arrays.asList(splitColumn).contains("references")) {

                        Scanner sc = new Scanner(System.in);

                        System.out.println("Please add the relation between tables:");
                        String relationship = sc.nextLine();

                        String referencedTableName = splitColumn[3].split("\\.")[0];
                        String referencedColumnName = splitColumn[3].split("\\.")[1];

                        // Check if the referenced table exists
                        File isReferencedTablePresent = Util.isTablePresent(referencedTableName, dbname);
                        if (isReferencedTablePresent == null) {
                            System.out.println("Referenced table does not exist");
                            Logger.logGeneral("Referenced table does not exist: " + referencedTableName);
                            return false;
                        }

                        // Check if the referenced column exists
                        boolean isReferencedColumnPresent = Util.isReferencedColumnPresent(referencedTableName, referencedColumnName, dbname);
                        if (!isReferencedColumnPresent) {
                            System.out.println("Referenced column does not exist");
                            Logger.logGeneral("Referenced column does not exist: " + referencedColumnName + " in table: " + referencedTableName);
                            return false;
                        }
                        column = column+" (Relation: "+tableName+" "+relationship+" "+referencedTableName+" )";
                    }
                    writer.write(column);
                    writer.newLine();
                    filteredColumns.add(column.split(" ")[0].trim());
                }
                // Add this to write a new line after writing the columns
                writer.newLine();

            } catch (IOException e) {
                Logger.logGeneral("Error writing metadata for table: " + tableName);
                System.out.println(e.getMessage());
                return false;
            } catch (Exception e){
                System.out.println("Could not perform the operation, Please try again.");
                return false;
            }

            // Add columns to the table
            addColumns(tableName, filteredColumns, dbname);
            Logger.logGeneral("Table created successfully: " + tableName);
        }
        else {
            System.out.println("Could not create table.");
            return false;
        }

        return isDirectoryCreated;
    }

    /**
     * Adds columns to the specified table in the specified database.
     *
     * @param tableName The name of the table to which columns are to be added.
     * @param columns   A list of columns to add to the table.
     * @param dbname    The name of the database in which the table resides.
     */
    private void addColumns(String tableName, List<String> columns, String dbname) {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");

        // Construct the path to the data.txt file within the specified table directory
        String dataDirectoryPath = currentDir + File.separator + "databases"
                + File.separator + dbname
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
            Logger.logEvent("Columns added to table: " + tableName);
        } catch (IOException e) {
            Logger.logGeneral("Failed to add columns to table: " + tableName);
            // Print the exception message if an IOException occurs
            System.out.println(e.getMessage());
        }
    }

    /**
     * Selects the specified database for use.
     *
     * @param query The SQL query for selecting the database.
     * @param regex The regex object containing the pattern for matching the query.
     * @return A Database object if the database exists and is selected, null otherwise.
     */
    public Database useDatabase(String query, Regex regex) {
        // Match the query against the useDatabase regex pattern
        Matcher matcher = regex.useDatabase.matcher(query);

        // Check if the query matches the expected pattern
        if (!matcher.matches()) {
            // Print an error message if the query is invalid
            System.out.println("Invalid USE DATABASE query");
            Logger.logGeneral("Invalid USE DATABASE query: " + query);
            return null;
        }

        // Extract the database name from the matched group
        String databaseName = matcher.group(1);

        // Check if the database with the given name is present
        boolean isDatabasePresent = Util.isDataBasePresent(databaseName);

        if (isDatabasePresent) {
            return new Database(databaseName);
        }
        System.out.println("Database " + databaseName + " does not exist");
        return null;
    }

    /**
     * Creates new database with the specified name.
     *
     * @param query The SQL query for creating the database.
     * @param regex The regex object containing the pattern for matching the query.
     * @return true if the database was successfully created, false otherwise.
     */
    public boolean createDatabase(String query, Regex regex) {
        // Match the query against the createDatabase regex pattern
        Matcher matcher = regex.createDatabase.matcher(query);
        boolean isValid = matcher.matches();

        // Check if the query matches the expected pattern
        if (!isValid) {
            System.out.println("Cannot create database.");
            Logger.logGeneral("Invalid database creation query: " + query);
            return false;
        }

        // Extract the database name from the matched group
        String databaseName = matcher.group(1);

        Database db = new Database(databaseName);

        // Get current directory
        String currentDir = System.getProperty("user.dir");

        // Get path to create database folder
        String directoryPath = currentDir + File.separator + "databases" + File.separator + databaseName;

        // Create file object
        File directory = new File(directoryPath);

        // Create the directories
        boolean isDirectoryCreated = directory.mkdirs();

        if (isDirectoryCreated) {
            Logger.logGeneral("Database created successfully: " + databaseName);
        } else {
            Logger.logGeneral("Failed to create database: " + databaseName);
        }

        return isDirectoryCreated;
    }

    /**
     * Inserts data into a specified table within a given database.
     *
     * @param query  the SQL insert query.
     * @param dbname the name of the database.
     * @return true if data is successfully inserted, false otherwise.
     */
    public boolean insertData(String query, String dbname) {
        String tableName;
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Extract table name, columns, and values from the query
        try {
            int startIndex = query.toLowerCase().indexOf("into") + 5;
            int endIndex = query.indexOf("(");
            tableName = query.substring(startIndex, endIndex).trim();

            String columnsString = query.substring(endIndex + 1, query.indexOf(")")).trim();
            String valuesString = query.substring(query.lastIndexOf("(") + 1, query.lastIndexOf(")")).trim();

            String[] columnsArray = columnsString.split(",");
            for (String col : columnsArray) {
                columns.add(col.trim());
            }

            String[] valuesArray = valuesString.split(",");
            for (String val : valuesArray) {
                values.add(val.trim());
            }
        } catch (Exception e) {
            // Log and print an error message if the query format is invalid
            Logger.logGeneral("Invalid insert query format: " + query);
            System.out.println("Invalid insert query format.");
            return false;
        }

        // Check if the table exists
        File dataFile = Util.isTablePresent(tableName, dbname);
        if (dataFile == null) {
            // Log and print an error message if the table is not found
            Logger.logGeneral("Table not found for insert query: " + query);
            return false;
        }

        // Read metadata to find the primary key column
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName;
        String metadataFileName = "metadata.txt";
        File metadataFile = new File(directoryPath + File.separator + metadataFileName);
        String primaryKeyColumn = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3 && parts[2].equalsIgnoreCase("pk")) {
                    primaryKeyColumn = parts[0];
                    break;
                }
            }
        } catch (IOException e) {
            // Log and print an error message if there is an error reading the metadata file
            Logger.logGeneral("Error reading metadata for table: " + tableName + ". Error: " + e.getMessage());
            System.out.println("Error reading metadata for table: " + e.getMessage());
            return false;
        }

        if (primaryKeyColumn != null) {
            int primaryKeyIndex = columns.indexOf(primaryKeyColumn);
            if (primaryKeyIndex == -1) {
                // Log and print an error message if the primary key column is not provided in the query
                Logger.logGeneral("Primary key column not provided in insert query: " + query);
                System.out.println("Primary key column not provided in insert query.");
                return false;
            }

            String primaryKeyValue = values.get(primaryKeyIndex);

            // Check for primary key uniqueness
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(" ### ");
                    if (data.length > primaryKeyIndex && data[primaryKeyIndex].equals(primaryKeyValue)) {
                        // Log and print an error message if a duplicate primary key value is found
                        Logger.logGeneral("Duplicate primary key value found: " + primaryKeyValue);
                        System.out.println("Duplicate primary key value found: " + primaryKeyValue);
                        return false;
                    }
                }
            } catch (IOException e) {
                // Log and print an error message if there is an error reading the data file
                Logger.logGeneral("Error reading data for table: " + tableName + ". Error: " + e.getMessage());
                System.out.println("Error reading data for table: " + e.getMessage());
                return false;
            }
        }

        // Write data into the table's data file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true))) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    row.append(" ### ");
                }
                row.append(values.get(i));
            }
            writer.write(row.toString());
            writer.newLine();
            // Log and print a message indicating successful data insertion
            Logger.logEvent("Data inserted into table " + tableName + ": " + row.toString());
            System.out.println("Data inserted successfully into table: " + tableName);
            return true;
        } catch (IOException e) {
            // Log and print an error message if there is an error writing to the data file
            Logger.logGeneral("Error writing data to table: " + tableName + ". Error: " + e.getMessage());
            System.out.println("Error writing data to table: " + e.getMessage());
            return false;
        }
    }


    /**
     * Parses and executes a CREATE TABLE query.
     *
     * @param query  the CREATE TABLE SQL query.
     * @param regex  an instance of the Regex class containing the regex pattern for matching the CREATE TABLE query.
     * @param dbname the name of the database where the table will be created.
     * @return true if the table is successfully created, false otherwise.
     */
    public boolean createTableQuery(String query, Regex regex, String dbname) {
        // Match the query against the createTable regex pattern
        Matcher matcher = regex.createTable.matcher(query);

        // Check if the query matches the expected pattern
        if (!matcher.matches()) {
            // Log and print an error message if the query is invalid
            Logger.logGeneral("Invalid CREATE TABLE query: " + query);
            System.out.println("Invalid CREATE TABLE query");
            return false; // Return false indicating failure
        }

        // Extract table name and column names from the matched groups
        String tableName = matcher.group(1);
        String columnNames = matcher.group(2);

        // Split column names into an array
        String[] columnArr = columnNames.split(",");

        // Call createTableHelper method to create the table
        return createTableHelper(tableName, Arrays.asList(columnArr), dbname);

    }


    /**
     * Executes a SELECT query to retrieve data from a specified table.
     *
     * @param query  the SELECT SQL query to execute.
     * @param regex  an instance of the Regex class containing patterns for matching SELECT queries.
     * @param dbname the name of the database containing the table.
     * @return true if the query executes successfully and data is retrieved or no records match the WHERE clause, false otherwise.
     */
    public boolean selectQuery(String query, Regex regex, String dbname) {
        // Match the query against the select pattern
        Matcher matcher = regex.selectPattern.matcher(query.trim());
        if (!matcher.matches()) {
            // Log and print an error message if the query is invalid
            Logger.logGeneral("Invalid SELECT query: " + query);
            System.out.println("Invalid SELECT query");
            return false; // Return false indicating failure
        }

        // Extract columns, table name, and optional WHERE clause components from the matched groups
        String columnsString = matcher.group(1).trim();
        String tableName = matcher.group(2).trim();
        String whereField = matcher.group(3) != null ? matcher.group(3).trim() : null;
        String whereValue = matcher.group(4) != null ? matcher.group(4).trim() : (matcher.group(5) != null ? matcher.group(5).trim() : null);

        // Construct the path to the data file for the table
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName;
        String dataFileName = "data.txt";

        // Create a File object for the data file
        File dataFile = new File(directoryPath, dataFileName);
        if (!dataFile.exists()) {
            // Log and print an error message if the table does not exist
            Logger.logGeneral("Table does not exist: " + tableName);
            System.out.println("Table does not exist");
            return false; // Return false indicating failure
        }

        // Read the data file and process its contents
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            List<String[]> records = new ArrayList<>();
            String[] headers = null;

            // Read each line of the data file
            while ((line = reader.readLine()) != null) {
                // Split the line into parts, handling the custom delimiter " ### "
                String[] parts = line.split("\\s*###\\s*");

                // Remove surrounding quotes from each part if present
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].replaceAll("^'|'$", "").trim(); // Remove surrounding quotes
                }

                // Process headers from the first line
                if (headers == null) {
                    headers = parts;
                    // Trim headers if specific columns are queried
                    if (!columnsString.equals("*")) {
                        for (int i = 0; i < headers.length; i++) {
                            headers[i] = headers[i].trim();
                        }
                    }
                    continue;
                }
                records.add(parts);
            }

            // Filter records based on the WHERE clause if present
            List<String[]> filteredRecords = new ArrayList<>();
            int whereColumnIndex = -1;
            if (whereField != null) {
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].equalsIgnoreCase(whereField)) {
                        whereColumnIndex = i;
                        break;
                    }
                }
                if (whereColumnIndex == -1) {
                    // Log and print an error message if the WHERE field is not found
                    Logger.logGeneral("WHERE field '" + whereField + "' not found");
                    System.out.println("WHERE field '" + whereField + "' not found");
                    System.out.println("Available fields: " + String.join(", ", headers)); // Debug output
                    return false; // Return false indicating failure
                }

                // Filter records based on the WHERE clause value
                for (String[] record : records) {
                    if (record[whereColumnIndex].equalsIgnoreCase(whereValue)) {
                        filteredRecords.add(record);
                    }
                }
            } else {
                filteredRecords = records; // No WHERE clause, use all records
            }

            // Check if any records match the query
            if (filteredRecords.isEmpty()) {
                Logger.logEvent("No records found for SELECT query");
                System.out.println("No records found");
                return true; // Return true indicating no records matched
            }

            // Determine column indexes for output if specific columns are queried
            int[] columnIndexes = null;
            if (!columnsString.equals("*")) {
                String[] columns = columnsString.split(",");
                columnIndexes = new int[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].trim();
                    columnIndexes[i] = -1;
                    for (int j = 0; j < headers.length; j++) {
                        if (headers[j].equalsIgnoreCase(columns[i])) {
                            columnIndexes[i] = j;
                            break;
                        }
                    }
                    if (columnIndexes[i] == -1) {
                        // Log and print an error message if a column is not found
                        Logger.logGeneral("Column " + columns[i] + " not found");
                        System.out.println("Column " + columns[i] + " not found");
                        return false; // Return false indicating failure
                    }
                }
            }

            // Output the results of the SELECT query
            if (columnsString.equals("*")) {
                Logger.logEvent("Displaying all columns for SELECT query");
                System.out.println(String.join(" | ", headers));
            } else {
                System.out.println(String.join(" | ", columnsString.split(",")));
                Logger.logEvent("Displaying selected columns for SELECT query: ");
            }

            // Print each filtered record
            for (String[] record : filteredRecords) {
                if (columnsString.equals("*")) {
                    System.out.println(String.join(" | ", record));
                } else {
                    for (int i = 0; i < columnIndexes.length; i++) {
                        if (i > 0) {
                            System.out.print(" | ");
                        }
                        System.out.print(record[columnIndexes[i]]);
                    }
                    System.out.println();
                }
            }
        } catch (IOException e) {
            // Log and print an error message if an I/O error occurs
            Logger.logGeneral("Error reading data file: " + e.getMessage());
            System.out.println("Error reading data file: " + e.getMessage());
            return false; // Return false indicating failure
        }

        return true; // Return true indicating success
    }
    /**
     * Executes an UPDATE query to modify existing records in a specified table.
     *
     * @param query  the UPDATE SQL query to execute.
     * @param regex  an instance of the Regex class containing patterns for matching UPDATE queries.
     * @param dbname the name of the database containing the table.
     * @return true if the query executes successfully and data is updated, false otherwise.
     */
    public boolean updateQuery(String query, Regex regex, String dbname) {
        // Match the query against the update regex pattern
        Matcher matcher = regex.update.matcher(query);
        boolean isUpdateValid = matcher.matches();

        // Check if the query matches the update pattern
        if (!isUpdateValid) {
            Logger.logGeneral("Invalid update query");
            System.out.println("Invalid update query");
            return false; // Return false indicating failure for invalid query format
        }

        // Extract table name, SET clause, WHERE field, and WHERE value from the matched groups
        String tableName = matcher.group(1);
        String setClause = matcher.group(2);
        String whereField = matcher.group(3);
        String whereValue = matcher.group(4);

        // Split SET clause into column-value pairs
        ArrayList<String> columnPair = new ArrayList<>();
        ArrayList<String> valuePair = new ArrayList<>();
        String[] columnValuePair = setClause.split(",");

        for (String columnValue : columnValuePair) {
            columnPair.add(columnValue.split("=")[0].trim());
            String temp = columnValue.split("=")[1].trim();
            valuePair.add(temp.substring(1, temp.length() - 1)); // Remove surrounding quotes
        }

        // Construct the path for the table's data file
        String filePath = System.getProperty("user.dir") + File.separator + "databases" + File.separator +
                dbname + File.separator + tableName + File.separator + "data.txt";

        // Iterate over each value pair to update the data in the file
        for (int i = 0; i < valuePair.size(); i++) {
            String updateField = columnPair.get(i);
            String valueToUpdate = valuePair.get(i);

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                StringBuilder content = new StringBuilder();
                String line;

                int count = 0;
                int updateColumnIndex = -1;
                int whereColumnIndex = -1;

                // Read each line from the file
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" ### ");

                    // For the first line (headers), locate the update and where columns
                    if (count == 0) {
                        boolean isUpdateColumnPresent = false;
                        boolean isWhereColumnPresent = false;

                        // Check each part (column name) for update and where fields
                        for (int j = 0; j < parts.length; j++) {
                            if (Objects.equals(parts[j], whereField)) {
                                whereColumnIndex = j;
                                isWhereColumnPresent = true;
                            }

                            if (Objects.equals(parts[j], updateField)) {
                                updateColumnIndex = j;
                                isUpdateColumnPresent = true;
                            }
                        }

                        // Handle cases where update or where fields are not found
                        if (!isUpdateColumnPresent && isWhereColumnPresent) {
                            Logger.logGeneral(updateField + " is not a valid field");
                            System.out.println(updateField + " is not a valid field");
                            return false; // Return false if the update field is not valid
                        } else if (isUpdateColumnPresent && !isWhereColumnPresent) {
                            Logger.logGeneral(whereField + " is not a valid field");
                            System.out.println(whereField + " is not a valid field");
                            return false; // Return false if the where field is not valid
                        } else if (!isUpdateColumnPresent && !isWhereColumnPresent) {
                            Logger.logGeneral(updateField + " and " + whereField + " are not valid fields");
                            System.out.println(updateField + " and " + whereField + " are not valid fields");
                            return false; // Return false if neither field is valid
                        }

                        content.append(line).append(System.lineSeparator());
                    } else {
                        StringBuilder lineToWrite = new StringBuilder();

                        // Update the value in the record if where condition is met
                        if (Objects.equals(parts[whereColumnIndex], whereValue)) {
                            parts[updateColumnIndex] = valueToUpdate; // Update the value
                        }

                        // Reconstruct the line to write back to the file
                        for (int j = 0; j < parts.length; j++) {
                            if (j > 0) {
                                lineToWrite.append(" ### ");
                            }
                            lineToWrite.append(parts[j]);
                        }

                        content.append(lineToWrite).append(System.lineSeparator());
                    }
                    count++;
                }

                // Write the updated content back to the file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
                    writer.write(content.toString());
                    Logger.logEvent("Data updated successfully in table: " + tableName);
                    System.out.println("Data updated successfully in table: " + tableName);
                } catch (Exception e) {
                    Logger.logGeneral("Error writing data to table: " + e.getMessage());
                    System.out.println("Error writing data to table: " + e.getMessage());
                    return false; // Return false if an error occurs while writing to the file
                }
            } catch (FileNotFoundException e) {
                Logger.logGeneral("Table does not exist");
                // Print an error message if the table does not exist
                System.out.println("Table does not exist");
                return false; // Return false if the file does not exist
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return true; // Return true indicating successful update operation
    }


    /**
     * Executes a DELETE query to remove records from a specified table based on a condition.
     *
     * @param query  the DELETE SQL query to execute.
     * @param regex  an instance of the Regex class containing patterns for matching DELETE queries.
     * @param dbname the name of the database containing the table.
     * @return true if the query executes successfully and records are deleted, false otherwise.
     */
    boolean deleteQuery(String query, Regex regex, String dbname) {
        // Match the query against the delete regex pattern
        Matcher matcher = regex.delete.matcher(query);
        boolean isDeleteValid = matcher.matches();

        // Check if the query matches the delete pattern
        if (!isDeleteValid) {
            Logger.logGeneral("Invalid delete query");
            System.out.println("Invalid delete query");
            return false;  // Return false indicating failure for invalid query format
        }

        // Extract table name, WHERE field, and WHERE value from the matched groups
        String tableName = matcher.group(1);
        String whereField = matcher.group(2);
        String whereValue = matcher.group(3);

        // Construct the file path for the table's data file
        String filePath = System.getProperty("user.dir") + File.separator + "databases" + File.separator +
                dbname + File.separator + tableName + File.separator + "data.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;

            int count = 0;
            int whereColumnIndex = -1;

            // Read each line from the data file
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ### ");

                // For the first line (headers), find the index of the WHERE field
                if (count == 0) {
                    for (int i = 0; i < parts.length; i++) {
                        if (Objects.equals(parts[i], whereField)) {
                            whereColumnIndex = i;
                        }
                    }
                    // Append headers to content
                    content.append(line).append(System.lineSeparator());
                } else {
                    StringBuilder lineToWrite = new StringBuilder();

                    // Skip the line if it matches the WHERE condition
                    if (Objects.equals(parts[whereColumnIndex], whereValue)) {
                        Logger.logEvent("Deleted record in table: " + tableName + ", where " + whereField + " = " + whereValue);
                        continue;
                    }

                    // Reconstruct the line to keep in the updated content
                    for (int i = 0; i < parts.length; i++) {
                        if (i > 0) {
                            lineToWrite.append(" ### ");
                        }
                        lineToWrite.append(parts[i]);
                    }
                    content.append(lineToWrite).append(System.lineSeparator());
                }
                count++;
            }

            // Write the updated content back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
                writer.write(content.toString());
                Logger.logEvent("Data deleted successfully from table: " + tableName);
                System.out.println("Data deleted successfully from table: " + tableName);
            } catch (Exception e) {
                Logger.logGeneral("Error writing data to table: " + e.getMessage());
                System.out.println("Error writing data to table: " + e.getMessage());
                return false; // Return false if an error occurs while writing to the file
            }

        } catch (FileNotFoundException e) {
            // Print an error message if the table does not exist
            Logger.logGeneral("Table does not exist");
            System.out.println("Table does not exist");
            return false; // Return false if the file does not exist
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            Logger.logGeneral("Error deleting data from table: " + e.getMessage());
            System.out.println("Error deleting data from table: " + e.getMessage());
            return false; // Return false for any unexpected exceptions
        }

        return true; // Return true indicating successful deletion operation
    }


    /**
     * Executes a DROP TABLE query to delete a specified table from the database.
     *
     * @param query  the DROP TABLE SQL query to execute.
     * @param regex  an instance of the Regex class containing patterns for matching DROP queries.
     * @param dbname the name of the database from which the table will be deleted.
     * @return true if the table is successfully dropped, false otherwise.
     */
    public boolean dropQuery(String query, Regex regex, String dbname) {
        // Match the query against the drop regex pattern
        Matcher matcher = regex.drop.matcher(query);
        boolean isDropValid = matcher.matches();

        // Check if the query matches the drop pattern
        if (!isDropValid) {
            Logger.logGeneral("Invalid drop query");
            System.out.println("Invalid drop query");
            return false;  // Return false indicating failure for invalid query format
        }

        // Extract table name from the matched group
        String tableName = matcher.group(1);

        // Construct file path for the table directory to drop
        String filePath = System.getProperty("user.dir") + File.separator + "databases" + File.separator +
                dbname + File.separator + tableName;

        // Create a File object for the directory
        File file = new File(filePath);

        // Check if the table directory exists
        if (!file.exists()) {
            Logger.logGeneral("Table does not exist");
            System.out.println("Table does not exist");
            return false;  // Return false indicating failure as table does not exist
        }

        // Attempt to delete the directory and its contents recursively
        return Util.deleteDirectory(file);
    }

}
