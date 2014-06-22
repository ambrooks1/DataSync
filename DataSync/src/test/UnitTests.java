package test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import syncdata.Event;
import syncdata.Mongo;
import syncdata.WorkRecord;

public class UnitTests {

	static  Mongo mongo = null;
	
	@BeforeClass
	public static void setUp() throws Exception 
	{
		
		mongo= new Mongo("unitTest");
		mongo.emptyCollection();
	}
	
	@Test
	public void SQLTest() {
		
	}
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
