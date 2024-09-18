import utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Represents an Entity-Relationship Diagram (ERD) generator for a database.
 * Provides methods to check cardinality between tables and generate an ERD.
 */
public class ERD {

    /**
     * Checks the cardinality between two tables based on their metadata.
     *
     * @param tableName1 the name of the first table.
     * @param tableName2 the name of the second table.
     * @return a string representing the cardinality ("1-to-1" or "1-to-N").
     */
    public String checkCardinality(String tableName1, String tableName2) {
        // Load the second table
        Table table2 = loadTable(tableName2);

        // Variables to hold column names
        String referencedColumnIn1 = null;
        String columnWhichRefersIn2 = null;

        // File path for the metadata of the second table
        String filePathForTable1 = System.getProperty("user.dir") + File.separator + "databases" + File.separator +
                "test" + File.separator + tableName2 + "/metadata.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePathForTable1))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] content = line.split(" ");

                // Extract column names if the line contains sufficient data
                if (content.length > 2) {
                    referencedColumnIn1 = content[0];
                    columnWhichRefersIn2 = content[3].split("\\.")[1];
                }
            }
        } catch (Exception e) {
            // Log and print error if file reading fails
            Logger.logGeneral("Error while reading metadata file for table: " + tableName2 + ". Error: " + e.getMessage());
            System.out.println(e.getMessage());
        }

        // Check if the column which refers is duplicated
        boolean isColumnWhichRefersDuplicate = containsDuplicates(table2.getColumn(referencedColumnIn1).getData());

        // Return cardinality based on duplicate check
        if (isColumnWhichRefersDuplicate) {
            return "1-to-N";
        } else {
            return "1-to-1";
        }
    }

    /**
     * Checks if a list contains duplicate elements.
     *
     * @param list the list to check for duplicates.
     * @return true if duplicates are found, false otherwise.
     */
    public static boolean containsDuplicates(ArrayList<String> list) {
        HashSet<String> set = new HashSet<>();
        for (String item : list) {
            if (!set.add(item)) {
                // Log the duplicate item found
                Logger.logEvent("Duplicate found in list: " + item);
                return true; // Duplicate found
            }
        }
        return false; // No duplicates found
    }

    /**
     * Loads a table from the file system and creates a Table object.
     *
     * @param tableName the name of the table to load.
     * @return the loaded Table object.
     */
    private Table loadTable(String tableName) {
        // File path for the data of the table
        String filePathForTable1 = System.getProperty("user.dir") + File.separator + "databases" + File.separator +
                "test" + File.separator + tableName + "/data.txt";

        Table table1 = new Table(tableName);

        try (BufferedReader br = new BufferedReader(new FileReader(filePathForTable1))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(" ### ");

                // Process each line based on its position (header or data)
                for (int j = 0; j < columns.length; j++) {
                    if (i == 0) {
                        Column column = new Column(columns[j]);
                        table1.addColumn(column);
                    } else {
                        Column temp = table1.getColumns().get(j);
                        temp.getData().add(columns[j]);
                    }
                }
                i++;
            }
        } catch (Exception e) {
            // Log and print error if file reading fails
            Logger.logGeneral("Error loading table " + tableName + ": " + e.getMessage());
            System.out.println(e.getMessage());
        }

        // Log successful table loading
        Logger.logEvent("Loaded table: " + tableName);
        return table1;
    }

    /**
     * Creates an Entity-Relationship Diagram (ERD) for the specified database.
     *
     * @param databaseName the name of the database.
     * @return true if the ERD was created successfully, false otherwise.
     */
    public boolean createERD(String databaseName) {
        // Check if the database is present
        if (!Util.isDataBasePresent(databaseName)) {
            Logger.logGeneral("Database does not exist: " + databaseName);
            System.out.println("Database does not exist");
            return false;
        }

        // File path for the database
        String databasePath = System.getProperty("user.dir") + File.separator + "databases" + File.separator + databaseName;
        File databases = new File(databasePath);

        // List all files in the database directory
        File[] files = databases.listFiles();
        if (files == null) {
            Logger.logGeneral("No tables found in database: " + databaseName);
            return false;
        }

        // Log the found tables
        Logger.logGeneral("Found tables: " + Arrays.toString(files));

        // File path for the ERD output
        String ERDpath = System.getProperty("user.dir") + File.separator + "databases" + File.separator + databaseName + File.separator + "erd.txt";

        // Process each file in the database directory
        for (File file : files) {
            if (file.isDirectory()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file.getPath() + File.separator + "metadata.txt"))) {

                    List<String> tableStructure = new ArrayList<>();
                    String tableName = file.getName();
                    String referencedTable = "";

                    tableStructure.add(tableName);

                    String line;
                    boolean hasReference = false;
                    String cardinality = "";
                    while ((line = br.readLine()) != null) {
                        if (line.contains("references")) {
                            hasReference = true;
                            referencedTable = line.split(" ")[3].split("\\.")[0];
                            cardinality = checkCardinality(referencedTable, tableName);
                        }
                        tableStructure.add(line);
                    }

                    // Write the table structure and cardinality to the ERD file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ERDpath, true))) { // Open in append mode
                        for (int i = 0; i < tableStructure.size(); i++) {
                            if (i == 0) {
                                writer.write("Table : " + tableName);
                                writer.newLine();
                                if (hasReference) {
                                    writer.write("Cardinality : " + referencedTable + " " + cardinality + " " + tableName);
                                    writer.newLine();
                                }
                            } else {
                                writer.write("\t" + tableStructure.get(i));
                                writer.newLine();
                            }
                        }
                        writer.write("***************************");
                        writer.newLine();
                    }
                } catch (Exception e) {
                    // Log and print error if file processing fails
                    Logger.logGeneral("Error processing table metadata for table: " + file.getName() + " - " + e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        }
        // Log completion of ERD generation
        Logger.logGeneral("ERD generation completed for database: " + databaseName);
        return true;
    }
}