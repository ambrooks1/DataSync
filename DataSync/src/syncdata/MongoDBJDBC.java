package syncdata;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoDBJDBC{
	
   public static  DBCollection getCollection(String collectionName){
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
}
