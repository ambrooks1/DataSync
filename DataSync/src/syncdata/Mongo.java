package syncdata;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class Mongo{
	
	//MongoDB data

	public static final int MONGO_SUCCESS = 0;
	public static final int MONGO_FAIL = 1;
		
	private  DBCollection coll = null;
	
	public  Mongo()
	{
		coll = getCollection("events");
		if (coll==null) {
			System.out.println("Problem creating or getting events collection");
			System.exit(1);
		}
	}
   private   DBCollection getCollection(String collectionName){
	   DBCollection coll =null;
      try{   
		 // To connect to mongodb server
         MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
         // Now connect to your databases
         DB db = mongoClient.getDB( "test" );
		 System.out.println("Connect to database successfully");
        // boolean auth = db.authenticate(myUserName, myPassword);
		// System.out.println("Authentication: "+auth);
		
		 boolean collectionExists = db.collectionExists(collectionName);
		 if (collectionExists == false) {
		        db.createCollection(collectionName, null);
		 }
		    
		 coll = db.getCollection(collectionName);
		 System.out.println(coll.getFullName());
        
      }catch(Exception e){
	     System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	  }
	return coll;
   }
   
	//Edit a mongo document in the events collection
	public  int  editDoc(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject();

		doc.put("event_id",    workRecord. getEvent().getId());
		doc.put("event_name",  workRecord. getEvent().getName());
		doc.put("event_date",  workRecord. getEvent().getDatetime());
		doc.put("event_venue", workRecord. getEvent().getVenue());

		BasicDBObject searchQuery = new BasicDBObject().append("event_id", workRecord. getEvent().getId());

		WriteResult result =coll.update(searchQuery, doc);
		return getResult(result, "update");
	}
	
	//inserts into the events collection a new document
	public  int insertDoc(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject("event_id", workRecord. getEvent().getId()).
				append("event_name", workRecord.  getEvent().getName()).
				append("event_date", workRecord.  getEvent().getDatetime()).
				append("event_venue", workRecord. getEvent().getVenue());

		WriteResult result = coll.insert(doc);
		return getResult(result, "insert");
	}

	//Deletes document from the collection
	public  int deleteDoc(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject();
		doc.append("event_id", workRecord. getEvent().getId());

		WriteResult result = coll.remove(doc);
		return getResult(result, "delete");
		
	}
	
	public  List<Event> getAllEvents() {
		//get all documents in the Events collection, and return them as Event objects
		DBCursor cursor = coll.find();
		
		List<Event> eventlist = new ArrayList<Event>();
		
		try {
		   while(cursor.hasNext()) {
		      DBObject doc = cursor.next();
		      int event_id = (Integer) doc.get("event_id");
		      String event_name = (String)doc.get("event_name");
		      String event_venue = (String)doc.get("event_venue");
		      long event_date = (Long)doc.get("event_date");
		      Event event= new Event(event_id, event_name, event_venue, event_date);
		      eventlist.add(event);
		   }
		} finally {
		   cursor.close();
		}
		return eventlist;
		
	}
	//*****************************************************************************

	public static int getResult(WriteResult result, String operation) {
		if (result.getError() == null) {
			System.out.println("Document " + operation + " sucess in MongoDB");
		}
		else {
			System.out.println("Document " + operation + " fail in MongoDB " + result.getError());
		}
		return result.getError() == null ? Mongo.MONGO_SUCCESS : Mongo.MONGO_FAIL ;
	}
}
