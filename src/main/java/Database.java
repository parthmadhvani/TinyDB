import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a database containing multiple tables.
 */
public class Database {

    /** The name of the database. */
    private String name;

    /** The list of tables in the database. */
    private ArrayList<Table> tables = new ArrayList<>();

    /**
     * Constructs a Database with the specified name.
     *
     * @param name the name of the database
     */
    public Database(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the database.
     *
     * @return the name of the database
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the database.
     *
     * @param name the new name of the database
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves a table by its name.
     *
     * @param name the name of the table to retrieve
     * @return the table with the specified name, or null if no such table exists
     */
    public Table getTable(String name) {
        // Iterate through the list of tables
        for (Table table : tables) {
            // Check if the table's name matches the specified name
            if (Objects.equals(table.getName(), name)) {
                return table; // Return the matching table
            }
        }
        return null; // Return null if no matching table is found
    }

    /**
     * Sets the list of tables in the database.
     *
     * @param tables the new list of tables
     */
    public void setTables(ArrayList<Table> tables) {
        this.tables = tables;
    }

    /**
     * Adds a table to the database.
     *
     * @param table the table to add
     */
    public void addTable(Table table) {
        this.tables.add(table);
    }
}
