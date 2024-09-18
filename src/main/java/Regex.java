import java.util.regex.Pattern;

/**
 * A class that defines various regular expressions for SQL-like queries.
 * These regular expressions are used to match and extract components from SQL commands.
 */
public class Regex {

    // Regular expression pattern for creating a database
    // Matches: CREATE DATABASE <database_name>;
    Pattern createDatabase = Pattern.compile("CREATE DATABASE (\\S+);?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for creating a table
    // Matches: CREATE TABLE <table_name> (<column_definitions>);
    Pattern createTable = Pattern.compile("CREATE TABLE (\\w+) \\(\\s*([^;]+)\\s*\\);?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for inserting data into a table
    // Matches: INSERT INTO <table_name> (<column_names>) VALUES (<values>);
    Pattern insert = Pattern.compile("INSERT INTO (\\w+) \\(([^)]+)\\) VALUES \\(([^)]+)\\);?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for updating data in a table
    // Matches: UPDATE <table_name> SET <column=value, ...> WHERE <field>=<value>;
    Pattern update = Pattern.compile("UPDATE\\s+(\\S+)\\s+SET\\s+([\\S+\\s*=\\s*'\\S+'\\s*,]*\\S+\\s*=\\s*'\\S+')\\s*\\s+WHERE\\s+(\\S+)\\s*=\\s*'(\\S+)';?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for deleting data from a table
    // Matches: DELETE FROM <table_name> WHERE <field>='<value>';
    Pattern delete = Pattern.compile("DELETE\\s+FROM\\s+(\\S+)\\s+WHERE\\s+(\\S+)\\s*=\\s*'(\\S+)';?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for dropping a table
    // Matches: DROP TABLE <table_name>;
    Pattern drop = Pattern.compile("DROP\\s+TABLE\\s+(\\S+);?", Pattern.CASE_INSENSITIVE);

    // Regular expression pattern for using a specific database
    // Matches: USE <database_name>;
    Pattern useDatabase = Pattern.compile("USE\\s+(\\S+);?", Pattern.CASE_INSENSITIVE);

     // Regular expression pattern for selecting specific columns with or without a WHERE clause
    //Matches: SELECT * FROM <table_name>;
    //Matches: SELECT <Column_name> FROM <table_name> WHERE <CONDITION>;
    Pattern selectPattern = Pattern.compile("SELECT\\s+([\\w\\*,\\s]+)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(\\w+)\\s*=\\s*(?:'([^']*)'|([^\\s;]+)))?;?", Pattern.CASE_INSENSITIVE);

}
