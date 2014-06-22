package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import syncdata.Event;
import syncdata.Mongo;
import syncdata.SQL;
import syncdata.WorkRecord;

public class UnitTests {

	static  private Mongo mongo = null;
	static private SQL sql=null;
	
	@BeforeClass
	public static void setUp() throws Exception 
	{
		
		mongo= new Mongo("unitTest");
		mongo.emptyCollection();
		
		sql= new SQL();
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
	        // one-time cleanup code
	    System.out.println("@AfterClass - oneTimeTearDown");
	    sql.closeResources();
	}
	
	//We want to test method "getEventlist" by adding a new Event
	// Also that this causes a new work record to be generated, with incomplete status
	@Test
	public void SQLEventTest() {
		
		List<Event> eventlist;
		try {
			//As a precondition, we want to start with no incomplete work records
			assertEquals(0, sql.getCountIncompleteWorkRecords());
			
			eventlist = sql.getEventList();
			int firstEVListCount = eventlist.size();
			
			int myEventId=16711;
			
			String sqlStatement[] = { "INSERT INTO events (event_id, event_name, event_datetime, event_venue)" +
					" VALUES (" + myEventId +  ", 'Michael Buble World Tour',1234565, 'Madison Square Garden')" };
			
			sql.performSomeSQLOperations(sqlStatement);
			
			//Adding a work record should increase the count of the event list by one
			eventlist = sql.getEventList();
			int secondEVListCount = eventlist.size();
			
			assertEquals(firstEVListCount + 1, secondEVListCount);
			
			//at this point, there should be one incomplete record in the table
			assertEquals(1, sql.getCountIncompleteWorkRecords());
			
			// The worklist should have one record in it, an incomplete record
			List<WorkRecord> worklist = sql.getWorkList();
			assertEquals(1, worklist.size());
			
			WorkRecord wr = worklist.get(0);
			//status of the workrecord should be incomplete
			assertFalse(wr.getCompleted());
			
			//cleanup the created event and work record
			String sqlStatement2[] = { "DELETE from events WHERE event_id =" + myEventId};
			sql.performSomeSQLOperations(sqlStatement2);
			
			//the eventlist count should be back the way it was
			eventlist = sql.getEventList();
			int thirdEVListCount = eventlist.size();
			assertEquals(firstEVListCount, thirdEVListCount);
			
			String sqlStatement3[] = { "DELETE from work WHERE ev_id = " + myEventId};
			sql.performSomeSQLOperations(sqlStatement3);
			
			// The worklist should now be empty again
			worklist = sql.getWorkList();
			assertEquals(0, worklist.size());
						
			//As a postcondition, there should again be no incomplete work records
			assertEquals(0, sql.getCountIncompleteWorkRecords());
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	//We are going to test the insert and updating of work records methods in SQL to complete status
	// and also the retrieval of the worklist (ie; incomplete work records) 
	@Test
	public void SQLWorkListTest() {
		
		//let's create a brand new work record with a new event
		Event event = new Event(1, "Larry's big concert", "Fred's concert venue", new Date().getTime());
		
		boolean completed=false;
		String changeType="NEW";
		
		WorkRecord workRecord = new WorkRecord(changeType, completed,  event);
		
		try {
			//As a precondition, we want to start with no incomplete work records
			assertEquals(0, sql.getCountIncompleteWorkRecords());
		
			sql.insertWorkRecord(workRecord);
			
			//at this point, there should be one incomplete record in the table
			assertEquals(1, sql.getCountIncompleteWorkRecords());
			
			// The worklist should have one record in it
			List<WorkRecord> worklist = sql.getWorkList();
			assertEquals(1, worklist.size());
			
			//Now let's get the workRecord back from the work table
			//so we can get the actual id of the workrecord
			WorkRecord workRecord2 = sql.getWorkRecord(event);
			
			int workId = workRecord2.getId();
			
			//Let's set the workRecord in the table as completed
			sql.updateWorkRecordAsComplete(workId);
			
			//Let's get the workRecord from the table again, and verify that it is actally set as completed
			workRecord2 = sql.getWorkRecord(event);
			
			assertEquals(true, workRecord2.getCompleted());
			
			//Now we should have no incomplete work records, again
			assertEquals(0, sql.getCountIncompleteWorkRecords());
			
			//cleanup the work record we created
			sql.deleteWorkRecord(workId);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	
	//Here we are going to test that the insert, edit and delete of documents
	// in the mongodDB collection do what they are supposed to do
	
	@Test
	public void mongoTest() {
		
		//we should start with empty collection, since we just did "mongo.emptyCollection"
		
		List<Event> eventlist = mongo.getAllEvents();
        assertEquals(0, eventlist.size());

        //Create a workRecord, add it to Mongo.
        // Get the event back, they should match.
        
		Event event = new Event(1, "Joe's big concert", "martha's barn", new Date().getTime());
		WorkRecord workRecord = new WorkRecord(123, "NEW", 123456,
				true,  event);
		
		mongo.insertDoc(workRecord);
		
		compareTheEvents(event);
		
		//Now change the event data in the workRecord, then edit the mongo doc, and
		//test to see if the returned mongo event still matches
		
		event.setName("Joe's stupid concert");
		event.setVenue("Martha's closet");
		workRecord.setEvent(event);
		
		mongo.editDoc(workRecord);
		
		compareTheEvents(event);
		
		//Finally delete
		
		mongo.deleteDoc(workRecord);
		
		eventlist = mongo.getAllEvents();
		assertEquals(0, eventlist.size());
		
	}

	//This is called when mongoDB has exactly one event ( as a document in the collection )
	private void compareTheEvents(Event event) {
		List<Event> eventlist = mongo.getAllEvents();
		assertEquals(1, eventlist.size());
		
		Event event2 = eventlist.get(0);
		assertEquals(event, event2);
	}

}
