package syncdata;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SyncTest {

	static Connection conn = null;
	static Statement  stmt = null;
	
	static  Mongo mongo = null;
	
	//*****************************************************************************
	private static void getSQLConnection() {
		try{
			
			Class.forName(JDBCHelper.JDBC_DRIVER);
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(JDBCHelper.DB_URL,  JDBCHelper.USER,  JDBCHelper.PASS);

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
	//*****************************************************************************
	
	@BeforeClass
	public static void setUp() throws Exception 
	{
		//TableSync.execute();
		
		getSQLConnection();
		
		mongo= new Mongo();
	}
	//*****************************************************************************
	@AfterClass
	public static void oneTimeTearDown() {
	        // one-time cleanup code
	    System.out.println("@AfterClass - oneTimeTearDown");
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
	        // As a precondition:   1) The Table Sync demon must be running
			//                      2) we check that the two databases are in sync
	
	       //  We perform some SQL operations
	       //  we wait a little bit for Table Sync to do its work
	       //   we retest the two databases to see if they are still in sync
	
	@Test
	public void IntegrationTest() {
		
		//compares all records in sql events and mongo events - they should match
		try {
			comparison();
			performSomeSQLOperations();
			
			//after TableSync has done its' work, there should be
			//no pending records in the work table
			comparison();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}

	private void comparison() throws SQLException {
		System.out.println("performing comparison of SQL and noSQL dbs");
		
		int count = JDBCHelper.getCountIncompleteWorkRecords(stmt);
		assertEquals(0, count);
		 
		List<Event> eventlistSQL = JDBCHelper.getEventList(stmt);
		List<Event> eventlistMongo = mongo.getAllEvents();
		
		//we sort based on id, so each successive event from SQL should correspond to the next mongo event
		
		assertEquals(eventlistSQL.size(), eventlistMongo.size());
		
		Collections.sort(eventlistSQL);
		Collections.sort(eventlistMongo);
		
		for (int i=0; i < eventlistSQL.size(); i++) {
			Event eventSQL =     eventlistSQL.get(i);
			Event eventMongo =   eventlistMongo.get(i);
			
			assertEquals(eventSQL, eventMongo);
		}
	}
	
	public void performSomeSQLOperations() {
		 System.out.println("performing some SQL operations");
		// FIRST, we want to insert, update and delete a whole bunch of 
		// records into the mySQL events table
		// using java JDBC
		
		  String sql[] = {
				 /* "INSERT INTO events (event_id, event_name, event_datetime, event_venue)" +
					" VALUES (111, 'Michael Buble World Tour',1234565, 'Madison Square Garden')",
					
					"INSERT INTO events (event_id, event_name, event_datetime, event_venue)" +
					"  VALUES (222, 'Joe Shmoe European Tour',288288, 'Vienna Garden') ",
					
					"UPDATE events SET event_venue = 'The Toilet Bowl' WHERE event_id=111",
					
					"UPDATE events SET event_venue = 'The Super Bowl' WHERE event_id=222",*/
					
					//"DELETE from events WHERE event_id=222"
		  };
		  
		  for (int i=0; i < sql.length; i++) {
			  
			  try {
				stmt.executeUpdate(sql[i]);
			  } catch (SQLException e) {
				
				e.printStackTrace();
			  }
		  }
		
		  //give TableSync some time to do its work
	    try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
		
		
	}

}
