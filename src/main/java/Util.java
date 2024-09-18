import utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Util {
    public static File isTablePresent(String tableName , String dbname) {

        // Verify if the table exists
        String currentDir = System.getProperty("user.dir");
        String directoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName;
        String dataFileName = "data.txt";

        File dataFile = new File(directoryPath + File.separator + dataFileName);
        if (!dataFile.exists())
        {
            Logger.logGeneral("Table does not exist: " + tableName);
            System.out.println("Table does not exist");
            return null;
        }
        return dataFile;
    }

    public static boolean isReferencedColumnPresent(String tableName, String columnName, String dbname)
    {
        String currentDir = System.getProperty("user.dir");
        String dataDirectoryPath = currentDir + File.separator + "databases" + File.separator + dbname + File.separator + tableName + File.separator + "data.txt";

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(dataDirectoryPath));

            String[] columns = br.readLine().split(" ### ");

            for (String column : columns)
            {
                if (columnName.equals(column.trim()))
                {
                    Logger.logQuery("Referenced column found: " + columnName + " in table: " + tableName);
                    return true;
                }
            }

        }
        catch (Exception e)
        {
            Logger.logGeneral("Error reading data file for table: " + tableName);
            Logger.logGeneral("Exception: " + e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }

        Logger.logGeneral("Referenced column not found: " + columnName + " in table: " + tableName);
        return false;
    }

    public static boolean isDataBasePresent(String name )
    {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");

        // Create a File object for the "databases" directory within the current directory
        File directory = new File(currentDir + File.separator + "databases");

        // List all files and directories in the "databases" directory
        File[] files = directory.listFiles();

        // Check if the directory is not empty
        if (files != null && files.length > 0)
        {
            // Iterate over each file in the directory
            for (File file : files)
            {
                // Check if the file is a directory and its name matches the given name
                if (file.isDirectory() && file.getName().equals(name))
                {

                    // Set the current database to the new Database object
                    Logger.logGeneral("Database found: " + name);
                    // Return true indicating the database is present
                    return true;
                }
            }
        }
        else
        {
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

    public static boolean deleteDirectory(File directoryToBeDeleted)
    {
        // Get all contents (files and directories) of the directory to be deleted
        File[] allContents = directoryToBeDeleted.listFiles();

        // If the directory is not empty, recursively delete its contents
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);  // Recursively delete each file or directory
            }
        }

        // Finally, delete the directory itself
        return directoryToBeDeleted.delete();  // Return true if deletion was successful, false otherwise
    }

}
