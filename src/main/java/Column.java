import java.util.ArrayList;

import utils.Logger;

/**
 * Represents a column in a data structure, holding a name and a list of string data.
 */
public class Column {

    /** The name of the column. */
    private String name;

    /** The list of data entries for the column. */
    private ArrayList<String> data = new ArrayList<>();

    /**
     * Constructs a Column with the specified name.
     *
     * @param name the name of the column
     */
    public Column(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the column.
     *
     * @return the name of the column
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the column.
     *
     * @param name the new name of the column
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of data entries for the column.
     *
     * @return the list of data entries
     */
    public ArrayList<String> getData() {
        return data;
    }

    /**
     * Sets the list of data entries for the column.
     *
     * @param data the new list of data entries
     */
    public void setData(ArrayList<String> data) {
        this.data = data;
    }
}
