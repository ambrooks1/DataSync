package syncdata;

import static org.junit.Assert.*;

import org.junit.Test;




public class SyncTest {

	
	
	@Test
	public void test() {
		fail("Not yet implemented");
		
		// FIRST, we want to insert, update and delete a whole bunch of 
		// records into the mySQL events table
		// using java JDBC
		
		//SECOND, TableSync should do its work
		
		//after TableSync has done its' work, there should be
		//no pending records in the work table
		
		// Then, we want to make sure that the Mongo events collection is synced to these
		// mySQL database changes

		// To do these we will create a list of Event objects from BOTH
		// the sql database AND the mongoDB database.


		// We then make sure both lists have the same record count.
		
		// Finally, we find the corresponding object pairs in both lists
		// by event_id, and we test them for equality.
	}

}
