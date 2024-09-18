package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SQLDumpUtility {

    /**
     * Export database structure and data to a SQL dump file.
     *
     * @param databasePath The path to the database directory.
     */
    public void export(String databasePath) {
        File dbFolder = new File(databasePath);
        String databaseName = dbFolder.getName();
        String dumpFilePath = databasePath + File.separator + databaseName + "_dump.sql";

        File dumpFile = new File(dumpFilePath);
        if (dumpFile.exists()) {
            dumpFile.delete();
            System.out.println("Delete existing dump");
        }

        try (FileWriter writer = new FileWriter(dumpFilePath)) {
            writer.write("CREATE DATABASE " + databaseName + ";\n");
            writer.write("USE " + databaseName + ";\n\n");

            List<File> dataFiles = findDataFiles(databasePath);
            for (File dataFile : dataFiles) {
                String tableName = dataFile.getParentFile().getName();
                String metadataFilePath = dataFile.getParent() + File.separator + "metadata.txt";

                String metadata = readMetadata(metadataFilePath);

                String sqlDump = generateTableSQLDump(tableName, dataFile.getAbsolutePath(), metadata);

                writer.write(sqlDump + "\n\n");
            }

            System.out.println("SQL dump file created successfully: " + dumpFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively find all data files (`data.txt`) in the database folder.
     *
     * @param databaseFolderPath The path to the database folder.
     * @return List of data files found in the database folder.
     */
    private static List<File> findDataFiles(String databaseFolderPath) {
        List<File> dataFiles = new ArrayList<>();
        File folder = new File(databaseFolderPath);
        findDataFilesRecursive(folder, dataFiles);
        return dataFiles;
    }

    /**
     * Recursively find data files (`data.txt`) in a folder and its subfolders.
     *
     * @param folder The current folder to search.
     * @param dataFiles List to store found data files.
     */
    private static void findDataFilesRecursive(File folder, List<File> dataFiles) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findDataFilesRecursive(file, dataFiles);
                } else if (file.isFile() && file.getName().equals("data.txt")) {
                    dataFiles.add(file);
                }
            }
        }
    }

    /**
     * Read metadata from a metadata file (`metadata.txt`) for a table.
     *
     * @param metadataFilePath The path to the metadata file.
     * @return Metadata content as a string.
     * @throws IOException If an I/O error occurs while reading the metadata file.
     */
    private static String readMetadata(String metadataFilePath) throws IOException {
        StringBuilder metadataBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                metadataBuilder.append(line.trim()).append("\n");
            }
        }
        return metadataBuilder.toString();
    }

    /**
     * Generate SQL statements to create a table and insert data from a data file.
     *
     * @param tableName The name of the table.
     * @param dataFilePath The path to the data file.
     * @param metadata The metadata defining columns of the table.
     * @return SQL statements to create the table and insert data.
     * @throws IOException If an I/O error occurs while reading the data file or generating SQL.
     */

    private static String generateTableSQLDump(String tableName, String dataFilePath, String metadata) throws IOException {
        StringBuilder sqlDumpBuilder = new StringBuilder();
        sqlDumpBuilder.append(String.format("CREATE TABLE %s (\n", tableName));

        String[] metadataLines = metadata.split("\\n");
        boolean isFirstColumn = true;
        List<String> columnNames = new ArrayList<>();

        for (String metadataLine : metadataLines) {
            String[] parts = metadataLine.trim().split("\\s+");
            if (parts.length >= 2) {
                String columnName = parts[0].trim();
                String dataType = parts[1].trim();
                String constraints = parts.length > 2 ? parts[2].trim() : "";

                if (!isFirstColumn) {
                    sqlDumpBuilder.append(",\n");
                }
                sqlDumpBuilder.append(String.format("    %s %s %s", columnName, dataType, constraints));
                columnNames.add(columnName);
                isFirstColumn = false;
            }
        }

        sqlDumpBuilder.append("\n);\n\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip the first line (assuming it's a header row)
                }
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] values = line.split("\\s*###\\s*");

                List<String> formattedValues = new ArrayList<>();
                for (String value : values) {
                    // Remove any enclosing single quotes
                    value = value.replaceAll("^'|'$", "");
                    // Re-add single quotes around non-integer values
                    if (!isInteger(value)) {
                        value = "'" + value + "'";
                    }
                    formattedValues.add(value);
                }
                sqlDumpBuilder.append(String.format("INSERT INTO %s (%s) VALUES (%s);\n", tableName, String.join(", ", columnNames), String.join(", ", formattedValues)));
            }
        }

        return sqlDumpBuilder.toString();
    }

    /**
     * Check if a given string represents an integer.
     *
     * @param value The string to check.
     * @return True if the string represents an integer, false otherwise.
     */
    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
