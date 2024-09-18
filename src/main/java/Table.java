import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a table in a database.
 * It contains the table name and a list of columns.
 */
public class Table {
    // The name of the table
    private String name;

    // List of columns in the table
    private ArrayList<Column> columns = new ArrayList<>();

    /**
     * Constructs a Table object with the given name.
     *
     * @param name The name of the table.
     */
    public Table(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the table.
     *
     * @param name The new name of the table.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of columns in the table.
     *
     * @return An ArrayList of Column objects.
     */
    public ArrayList<Column> getColumns() {
        return columns;
    }

    /**
     * Sets the list of columns in the table.
     *
     * @param columns An ArrayList of Column objects to set.
     */
    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }

    /**
     * Adds a column to the table.
     *
     * @param column The Column object to add.
     */
    public void addColumn(Column column) {
        this.columns.add(column);
    }

    /**
     * Retrieves a column by its name.
     *
     * @param columnName The name of the column to retrieve.
     * @return The Column object if found, otherwise null.
     */
    public Column getColumn(String columnName) {
        for (Column col : columns) {
            if (col.getName().equals(columnName)) {
                return col;
            }
        }
        return null; // Return null if the column is not found
    }
}
