package syncdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCHelper {
	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	public static final String DB_URL = "jdbc:mysql://localhost/crowd-surge";

	//  Database credentials
	public static final String USER = "root";
	public static final String PASS = "";


	//*****************************************************************************

	public static WorkRecord createWorkRecord(ResultSet rs) throws SQLException {
		//Retrieve by column name
		int id  = rs.getInt("id");
		String changetype = rs.getString("changetype");
		long createtime = rs.getLong("createtime");

		Boolean completed = rs.getBoolean("completed");
		int ev_id = rs.getInt("ev_id");
		String ev_name = rs.getString("ev_name");
		String ev_venue = rs.getString("ev_venue");
		long ev_datetime = rs.getLong("ev_datetime");

		WorkRecord workRecord = new WorkRecord(id, changetype, createtime, completed,
				new Event( ev_id, ev_name, ev_venue, ev_datetime));

		return workRecord;
	}

	public static Event createEvent(ResultSet rs) throws SQLException {
		//Retrieve by column name

		int ev_id = rs.getInt("event_id");
		String ev_name = rs.getString("event_name");
		String ev_venue = rs.getString("event_venue");
		long ev_datetime = rs.getLong("event_datetime");

		Event event = 
				new Event( ev_id, ev_name, ev_venue, ev_datetime);

		return event;
	}
	//*****************************************************************************


	public static List<Event> getEventList(Statement stmt) throws SQLException {

		String sql = "SELECT  * FROM events";
		ResultSet rs = stmt.executeQuery(sql);

		List<Event> eventlist = new ArrayList<Event>();

		while(rs.next()){
			Event event = createEvent(rs);
			eventlist.add(event);
		}
		rs.close();
		return eventlist;
	}

	public static int getCountIncompleteWorkRecords(Statement stmt) throws SQLException {
		int count=-1;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM work where completed = 0");
		while (rs.next()){
			count = rs.getInt(1);
		}
		rs.close();
		return count;
	}
	public static List<WorkRecord> getWorkList(Statement stmt)
			throws SQLException {
		String sql = "SELECT id, changetype, createtime, completed, ev_id, ev_name, ev_venue, ev_datetime "
				+ "FROM work WHERE completed=FALSE ORDER BY createtime";
		ResultSet rs = stmt.executeQuery(sql);

		List<WorkRecord> worklist = new ArrayList<WorkRecord>();

		while(rs.next()){
			WorkRecord workRecord = JDBCHelper.createWorkRecord(rs);
			worklist.add(workRecord);
		}
		rs.close();
		return worklist;
	}
	//*****************************************************************************

	public static void updateWorkRecord( int workId, Statement stmt)
			throws SQLException {
		System.out.println("Performing updateWorkRecords");

		final String updateStr="UPDATE work SET completed = TRUE WHERE id=" + workId;

		stmt.executeUpdate(updateStr);
		System.out.println("Executed sql: " + updateStr);
	}

	//*****************************************************************************
}
