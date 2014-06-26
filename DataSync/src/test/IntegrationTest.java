package test;

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

import syncdata.Event;
import syncdata.SQL;
import syncdata.Mongo;

public class IntegrationTest {

	static  Mongo mongo = null;
	static SQL sql = null;
	
	//*****************************************************************************
	@BeforeClass
	public static void setUp() throws Exception 
	{
		sql = new SQL();
		mongo= new Mongo("events");
	}
	//*****************************************************************************
	@AfterClass
	public static void oneTimeTearDown() {
	        // one-time cleanup code
	    System.out.println("@AfterClass - oneTimeTearDown");
	    sql.closeResources();
	}
	        // As a precondition:   1) The Table Sync demon must be running
			//                      2) we check that the two databases are in sync
	
	       //  We perform some SQL operations
	       //  we wait a little bit for Table Sync to do its work
	       //   we retest the two databases to see if they are still in sync
	
	@Test
	public void runIntegrationTest() {
		
		  String sqlStatements[] = {
				  
		  "DELETE from events" ,
		  
		  "INSERT INTO events (event_id, event_name, event_datetime, event_venue)" +
			" VALUES (111, 'Michael Buble World Tour',1234565, 'Madison Square Garden')",
			
			"INSERT INTO events (event_id, event_name, event_datetime, event_venue)" +
			"  VALUES (222, 'Joe Shmoe European Tour',288288, 'Vienna Garden') ",
			
			"UPDATE events SET event_venue = 'The Rose Bowl' WHERE event_id=111",
			
			"UPDATE events SET event_venue = 'The Super Bowl' WHERE event_id=222",
			
			"DELETE from events WHERE event_id=222"
 };
		
		//compares all records in sql events and mongo events - they should match
		try {
			comparison();
			sql.performSomeSQLOperations(sqlStatements);
			
			  //give TableSync some time to do its work
		    try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			//after TableSync has done its' work, there should be
			//no pending records in the work table
			comparison();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}

	//Are the noSQL and SQL tables in sync?  Do they have the same number of records?
	// If so, are corresponding events in both tables equal?
	
	private void comparison() throws SQLException {
		System.out.println("performing comparison of SQL and noSQL dbs");
		
		int count = sql.getCountIncompleteWorkRecords();
		assertEquals(0, count);
		 
		List<Event> eventlistSQL = sql.getEventList();
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
	
	

}
