import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
/**
 * TransactionManager is responsible for handling transactions, logging operations,
 * and applying changes to the database.
 */
public class TransactionManager 
{
	// Indicates if a transaction is currently active
    private boolean inTransaction = false;
    // Temporary log file to store operations during a transaction
    private File tempLogFile;
   // Regex for matching SQL operations
    private Regex regex = new Regex();
    private QueryExecuter queryExecuter = new QueryExecuter();
    // The name of the current database set to null
    private String database = "";

    /**
     * Begins a new transaction and initializes the temporary log file.
     * 
     * @param db2 The database object to begin the transaction on.
     */
    public void beginTransaction(Database db2)
    {
        inTransaction = true;
        tempLogFile = new File("temp_operation.log");
        database = db2.getName();
    }
    
    /**
     * Commits the current transaction by applying all logged operations and
     * logging them to the permanent operation log file.
     */
    public void commitTransaction() 
    {
        if (inTransaction) 
        {
            try (BufferedReader logReader = new BufferedReader(new FileReader(tempLogFile))) 
            {
                String log;
                while ((log = logReader.readLine()) != null) 
                {
                    applyLog(log);
                    logToFile(log, new File("operation.log"));
                }
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
            finally 
            {
                inTransaction = false;
                deleteTempFiles();
            }
        }
    }
    /**
     * Rolls back the current transaction and deletes any temporary log files.
     */
    public void rollbackTransaction() 
    {
    	if (inTransaction) 
    	{
            try 
            {
                
            }
            finally 
            {
                inTransaction = false;
                deleteTempFiles();
            }
        }
    }
    /**
     * Logs an operation. If a transaction is active, the operation is logged to
     * the temporary log file. Otherwise, it is applied and logged immediately.
     * 
     * @param operation The SQL operation to log.
     */

    public void logOperation(String operation)
    {
        if (inTransaction) 
        {
            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(tempLogFile, true))) 
            {
                logWriter.write(operation);
                logWriter.newLine();
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        } 
        else 
        {
            try 
            {
                applyLog(operation);
                logToFile(operation, new File("operation.log"));
            } 
            catch (Exception e) 
            {
                System.err.println("Error applying log operation: " + operation);
                e.printStackTrace();
            }
        }
    }
    /**
     * Applies a logged operation to the database.
     * 
     * @param operation The SQL operation to apply.
     * @throws IOException If an I/O error occurs.
     */

    private void applyLog(String operation) throws IOException 
    {
        Matcher insertMatcher = regex.insert.matcher(operation);
        Matcher updateMatcher = regex.update.matcher(operation);
        Matcher deleteMatcher = regex.delete.matcher(operation);

        if (insertMatcher.matches()) 
        {
        	queryExecuter.insertData(operation, database);
        } 
        else if (updateMatcher.matches())
        {
            queryExecuter.updateQuery(operation, regex, database);
        } 
        else if (deleteMatcher.matches()) 
        {
            queryExecuter.deleteQuery(operation, regex, database);
        } 
        else 
        {
            System.err.println("Unknown operation: " + operation);
        }
    }
    /**
     * Logs an operation to the specified file.
     * 
     * @param operation The SQL operation to log.
     * @param file      The file to log the operation to.
     */
    private void logToFile(String operation, File file)
    {
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(file, true))) 
        {
            logWriter.write(operation);
            logWriter.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Deletes the temporary log file if it exists.
     */
    private void deleteTempFiles() 
    {
        if (tempLogFile != null && tempLogFile.exists())
        {
            tempLogFile.delete();
        }
    }
    /**
     * Checks if a transaction is currently active.
     * 
     * @return true if a transaction is active, false otherwise.
     */
    public boolean isInTransaction()
    {
        return inTransaction;
    }
}
