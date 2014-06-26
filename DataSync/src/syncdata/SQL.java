package syncdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQL {
	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	public static final String DB_URL = "jdbc:mysql://localhost/foobar";

	//  Database credentials
	public static final String USER = "root";
	public static final String PASS = "";


	//SQL data

		private  Connection conn = null;
		private  Statement  stmt = null;

		
	//*****************************************************************************
		//instatiate connection and statement
		
		public SQL() {
			getSQLConnection();
		}
		
		private void getSQLConnection() {
			try{
				Class.forName(SQL.JDBC_DRIVER);
				System.out.println("Connecting to database...");
				conn = DriverManager.getConnection(SQL.DB_URL,  SQL.USER,  SQL.PASS);

				System.out.println("Creating statement...");
				stmt = conn.createStatement();

			}
			catch(SQLException se){
				//Handle errors for JDBC
				se.printStackTrace();
				System.exit(1);
			}
			catch(Exception e){
				//Handle errors for Class.forName
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	public void handleSQLerrors(SQLException se) {
		//Handle errors for JDBC
		se.printStackTrace();

		try{
			if(stmt!=null)
				stmt.close();
		}
		catch(SQLException se2){
		}// nothing we can do
		try{
			if(conn!=null)
				conn.close();
		}catch(SQLException se3){
			se3.printStackTrace();
		}

		System.exit(1);
	}
	
	private  WorkRecord createWorkRecord(ResultSet rs) throws SQLException {
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

	private  Event createEvent(ResultSet rs) throws SQLException {
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


	public  List<Event> getEventList() throws SQLException {

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

	public  int getCountIncompleteWorkRecords() throws SQLException {
		int count=-1;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM work where completed = 0");
		while (rs.next()){
			count = rs.getInt(1);
		}
		rs.close();
		return count;
	}
	
	public  List<WorkRecord> getWorkList()
			throws SQLException {
		String sql = "SELECT id, changetype, createtime, completed, ev_id, ev_name, ev_venue, ev_datetime "
				+ "FROM work WHERE completed=FALSE ORDER BY createtime";
		ResultSet rs = stmt.executeQuery(sql);

		List<WorkRecord> worklist = new ArrayList<WorkRecord>();

		while(rs.next()){
			WorkRecord workRecord = createWorkRecord(rs);
			worklist.add(workRecord);
		}
		rs.close();
		return worklist;
	}
	//*****************************************************************************
	
	//wrap in single quote and convert apostrophe to double
	private String conversion(String s) {
		return "'" + s.replaceAll("'","''")    +  "'";
	}

	//used only for unit testing purposes
	public  void insertWorkRecord(WorkRecord wr) throws SQLException {
		
		Event event = wr.getEvent();
		String sql = 
		"INSERT INTO work (changetype, completed, ev_id, ev_name, ev_datetime, ev_venue) "+
		" VALUES ("  +
				"'" + wr.getChangetype() + "' ," +
				
				wr.getCompleted()  + "," +
				
				event.getId()      + "," +
				
				conversion(event.getName())    +  "," +
				
				event.getDatetime()    + "," +
				
                conversion(event.getVenue())  +
				
	         	")";
		
		System.out.println(sql);
		stmt.executeUpdate(sql);
		System.out.println("Executed sql: " + sql);
	}
	
	//used only for unit testing purposes
	// get a record from the work table that matches this event
	//we are looking to get the work record id so that we can delete it subsequently
	
	public WorkRecord getWorkRecord(Event event) throws SQLException {
		
		String sql ="SELECT  id, changetype, createtime, completed, ev_id, ev_name, ev_datetime, ev_venue" +
	                " FROM work WHERE " +
				          "ev_id= "   + event.getId() + " AND " +
				          "ev_name= " + conversion(event.getName()) + " AND " +
				          "ev_datetime= " + event.getDatetime() + " AND " +
				          "ev_venue= " + conversion(event.getVenue()) ;
		
		WorkRecord workRecord=null;
		
		ResultSet rs = stmt.executeQuery(sql);
		if(rs.next()){
			 workRecord = createWorkRecord(rs);
			
		}
		rs.close();
		return workRecord;
	}
	//used only for unit testing purposes
	public  void deleteWorkRecord(int workId) throws SQLException
	{
		final String updateStr="DELETE FROM work WHERE id=" + workId;
		stmt.executeUpdate(updateStr);
		System.out.println("Executed sql: " + updateStr);
	}
	
	public  void updateWorkRecordAsComplete( int workId)
			throws SQLException {
		System.out.println("Performing updateWorkRecords");

		final String updateStr="UPDATE work SET completed = TRUE WHERE id=" + workId;

		stmt.executeUpdate(updateStr);
		System.out.println("Executed sql: " + updateStr);
	}

	//*****************************************************************************
	
	public void closeResources() {
		 try{
	         if(stmt!=null)
	            conn.close();
	      }catch(SQLException se){
	      }// do nothing
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }
	}
	
	//used in testing
	public void performSomeSQLOperations(String sql[]) throws SQLException {
		  for (int i=0; i < sql.length; i++) {
			    System.out.println("performing SQL operation : " + sql[i]);
				stmt.executeUpdate(sql[i]);
		  }
	
	}
	
}
