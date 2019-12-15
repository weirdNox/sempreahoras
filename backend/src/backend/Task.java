package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class Task {
	long id = 0;
    String userId = "";

    String title = "";
    String description = "";

    int color = (((0xff & 0xff) << 24) |
                 (( 244 & 0xff) << 16) |
                 ((  67 & 0xff) <<  8) |
                 ((  54 & 0xff) <<  0));

    long lastEdit = 0;

    boolean deleted = false;

    Task() {}

    Task(ResultSet set) throws SQLException {
		id             = set.getLong("Id");
		userId         = set.getString("UserId");
		title          = set.getString("Title");
		description    = set.getString("Description");
		color          = set.getInt("Color");
		lastEdit       = set.getLong("LastEdit");
		deleted        = set.getBoolean("Deleted");
    }

    PreparedStatement prepareStatement(Connection dbConn) throws SQLException {
        PreparedStatement statement;

        lastEdit = Calendar.getInstance().getTimeInMillis();

        if(id == 0) {
        	statement = dbConn.prepareStatement("INSERT OR ABORT INTO tasks (" +
                                                "Title," +
                                                "Description," +
                                                "Color," +
                                                "LastEdit," +
                                                "UserId" +
                                                ") VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(5, userId);
        }
        else {
        	statement = dbConn.prepareStatement("UPDATE tasks SET (" +
                                                "Title," +
                                                "Description," +
                                                "Color," +
                                                "LastEdit," +
                                                "Deleted" +
                                                ") = (?,?,?,?,?) " +
                                                "WHERE Id = ? AND UserId = ?", Statement.RETURN_GENERATED_KEYS);
            statement.setBoolean(5, deleted);
            statement.setLong(6, id);
            statement.setString(7, userId);
        }

        statement.setString(1, title);
        statement.setString(2, description);
        statement.setLong(3, color);
        statement.setLong(4, lastEdit);

		return statement;
    }
}
