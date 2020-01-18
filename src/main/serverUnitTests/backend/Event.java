package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class Event {
	long id = 0;
    String userId = "";

    String title = "";
    String description = "";

    long startMillis = 0;
    long durationMillis = 0;
    boolean isAllDay = false;
    int repeatType = 0;
    int repeatCount = 0;
    long endMillis = 0;

    long lastEdit = 0;

    int color = (((0xff & 0xff) << 24) |
                 (( 252 & 0xff) << 16) |
                 (( 186 & 0xff) <<  8) |
                 ((   3 & 0xff) <<  0));

    long notifMinutes = -1;

    String location = "";

    boolean deleted = false;

    Event() {}

    Event(ResultSet set) throws SQLException {
		id             = set.getLong("Id");
		userId         = set.getString("UserId");
		title          = set.getString("Title");
		description    = set.getString("Description");
		startMillis    = set.getLong("StartMillis");
		durationMillis = set.getLong("DurationMillis");
		isAllDay       = set.getBoolean("IsAllDay");
		repeatType     = set.getInt("RepeatType");
		repeatCount    = set.getInt("RepeatCount");
		endMillis      = set.getLong("EndMillis");
		lastEdit       = set.getLong("LastEdit");
		color          = set.getInt("Color");
		notifMinutes   = set.getLong("NotifMinutes");
		location       = set.getString("Location");
		deleted        = set.getBoolean("Deleted");
    }

    PreparedStatement prepareStatement(Connection dbConn) throws SQLException {
        PreparedStatement statement;

        lastEdit = Calendar.getInstance().getTimeInMillis();

        if(id == 0) {
        	statement = dbConn.prepareStatement("INSERT INTO events (" +
                                                "Title," +
                                                "Description," +
                                                "StartMillis," +
                                                "DurationMillis," +
                                                "IsAllDay," +
                                                "RepeatType," +
                                                "RepeatCount," +
                                                "EndMillis," +
                                                "LastEdit," +
                                                "Color," +
                                                "NotifMinutes," +
                                                "Location," +
                                                "UserId" +
                                                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING",
                                                Statement.RETURN_GENERATED_KEYS);
            statement.setString(13, userId);
        }
        else {
        	statement = dbConn.prepareStatement("UPDATE events SET (" +
                                                "Title," +
                                                "Description," +
                                                "StartMillis," +
                                                "DurationMillis," +
                                                "IsAllDay," +
                                                "RepeatType," +
                                                "RepeatCount," +
                                                "EndMillis," +
                                                "LastEdit," +
                                                "Color," +
                                                "NotifMinutes," +
                                                "Location," +
                                                "Deleted" +
                                                ") = (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                                                "WHERE Id = ? AND UserId = ?", Statement.RETURN_GENERATED_KEYS);
            statement.setBoolean(13, deleted);
            statement.setLong(14, id);
            statement.setString(15, userId);
        }

        statement.setString(1, title);
        statement.setString(2, description);
        statement.setLong(3, startMillis);
        statement.setLong(4, durationMillis);
        statement.setBoolean(5, isAllDay);
        statement.setInt(6, repeatType);
        statement.setInt(7, repeatCount);
        statement.setLong(8, endMillis);
        statement.setLong(9, lastEdit);
        statement.setInt(10, color);
        statement.setLong(11, notifMinutes);
        statement.setString(12, location);

		return statement;
    }
}
