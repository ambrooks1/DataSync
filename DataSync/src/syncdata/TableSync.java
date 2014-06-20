package syncdata;
//STEP 1. Import required packages


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

/*
 *  When modifications occur on the mysql events table, triggers are fired
 *  which create work records to be processed, corresponding to the 
 *  modification that occurred.
 *  
 *  This java program periodically scans the work table looking for not-yet-processed work records
 *  to process in order of creation time.  The work to be done is insert, update or delete
 *  of documents in the mongodb events collection.
 * 
 * 
 */
public class TableSync extends TimerTask
{
	//Timer data
	private static final int SLEEP_SECONDS = 5;  // how long does the main thread sleep

	private static final int secondsOfDelay = 1 ;  //time between task executions, where task is processing work records
	private static final int startDelay = 5;   //initially in millisecs

	//MongoDB data

	public static final int MONGO_SUCCESS = 0;
	public static final int MONGO_FAIL = 1;

	static DBCollection coll = null;

	//SQL data

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/crowd-surge";

	//  Database credentials
	static final String USER = "root";
	static final String PASS = "";


	static Connection conn = null;
	static Statement  stmt = null;

	//*********************************************************************************

	public TableSync() {

		//get connections to mongoDB and mySQL

		coll = MongoDBJDBC.getCollection("events");
		if (coll==null) {
			System.out.println("Problem creating or getting events collection");
			System.exit(1);
		}

		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			System.out.println("Creating statement...");
			stmt = conn.createStatement();

		}
		catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}
		catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}
	}
	//*****************************************************************************
	public static void main(String[] args) {

		TimerTask timerTask = new TableSync();

		Timer timer = new Timer(true);

		/*
		 * public void scheduleAtFixedRate(TimerTask task,long delay,long period)
		 * task--This is the task to be scheduled.
           delay--This is the delay in milliseconds before task is to be executed.
           period--This is the time in milliseconds between successive task executions.
		 */

		timer.scheduleAtFixedRate(timerTask, startDelay, secondsOfDelay * 1000);

		System.out.println("TimerTask begins! :" + new Date());

		//this program is supposed to run forever  (:-)
		while (true) {
			try {
				Thread.sleep(SLEEP_SECONDS * 1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}//end main

	//*****************************************************************************

	//get all the work records from the mysql database, and
	//use them to update mongodb

	private static void processAllWorkRecords()
			throws SQLException {
		
		//get the work records which have not yet been processed in order of creation

		//System.out.println("Process all work records");

		List<WorkRecord> worklist = getWorkList();
		
		for (int i=0; i < worklist.size(); i++) {
		
			WorkRecord workRecord = worklist.get(i);
			
			System.out.println(workRecord.toString());

			if (workRecord.getChangetype().equals("NEW")) {
				processNEW( workRecord);
			}
			else
				if (workRecord.getChangetype().equals("EDIT")) {
					processEDIT(workRecord);
				}
				else
					if (workRecord.getChangetype().equals("DELETE")) {
						processDELETE(workRecord);
					}
		}
	
	}
	//*****************************************************************************
	private static List<WorkRecord> getWorkList()
			throws SQLException {
		String sql = "SELECT id, changetype, createtime, completed, ev_id, ev_name, ev_venue, ev_datetime "
				+ "FROM work WHERE completed=FALSE ORDER BY createtime";
		ResultSet rs = stmt.executeQuery(sql);

		List<WorkRecord> worklist = new ArrayList<WorkRecord>();
		
		while(rs.next()){
			WorkRecord workRecord = createWorkRecordObj(rs);
			worklist.add(workRecord);
		}
		rs.close();
		return worklist;
	}
	//*****************************************************************************
	//we have a NEW type work record, we want to insert a new mongodb doc
	private static void processNEW( WorkRecord workRecord) {
		int result = insertNewMongoDocument(workRecord);
		try {
			updateWorkRecordOnSuccess( workRecord, result, "insert");
		} catch (SQLException e) {
			//here we should really roll back the mongo operation
			e.printStackTrace();
		}
	}

	//we have an EDIT type work record, we want to edit a pre-existing mongodb doc
	private static void processEDIT( WorkRecord workRecord) {
		int result = editMongoDocument(workRecord);

		try {
			updateWorkRecordOnSuccess( workRecord, result, "edit");
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	//we have a DELETE type work record, we want to delete the corresponding mongodb doc
	private static void processDELETE( WorkRecord workRecord) {
		int result = deleteMongoDocument(workRecord);
		try {
			updateWorkRecordOnSuccess( workRecord, result, "delete");
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	
	//*****************************************************************************
	//when the work record has been synced with mongo, we can mark it as being processed
	// so that we do not reprocess it

	private static void updateWorkRecordOnSuccess(
			WorkRecord workRecord, int result, String operation) throws SQLException {
		
		System.out.println("MongoDB result=" + result);
		if (result==MONGO_SUCCESS) {
			System.out.println("MongoDB " + operation + " success; id=" + workRecord.getId());
			updateWorkRecord( workRecord.getId());
		}
		else {
			System.out.println("MongoDB " + operation + " fail; id=" + workRecord.getId());
		}
	}

	private static void updateWorkRecord( int workId)
			throws SQLException {
		System.out.println("Performing updateWorkRecords");

		final String updateStr="UPDATE work SET completed = TRUE WHERE id=" + workId;

		stmt.executeUpdate(updateStr);
		System.out.println("Executed sql: " + updateStr);

	}

	//*****************************************************************************
	//Edit a mongo document in the events collection
	private static int  editMongoDocument(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject();

		doc.put("event_id", workRecord.getEv_id());
		doc.put("event_name", workRecord.getEv_name());
		doc.put("event_date", workRecord.getEv_datetime());
		doc.put("event_venue", workRecord.getEv_venue());

		BasicDBObject searchQuery = new BasicDBObject().append("event_id", workRecord.getEv_id());

		WriteResult result =coll.update(searchQuery, doc);

		printResultOfMongoOperation(result, "update");
		return result.getError() == null ? MONGO_SUCCESS : MONGO_FAIL ;
	}
	//inserts into the events collection a new document
	private static int insertNewMongoDocument(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject("event_id", workRecord.getEv_id()).
				append("event_name", workRecord.getEv_name()).
				append("event_date", workRecord.getEv_datetime()).
				append("event_venue", workRecord.getEv_venue());

		WriteResult result = coll.insert(doc);
		printResultOfMongoOperation(result, "insert");
		return result.getError() == null ? MONGO_SUCCESS : MONGO_FAIL ;
	}

	//inserts into the events collection a new document
	private static int deleteMongoDocument(WorkRecord workRecord) {

		BasicDBObject doc = new BasicDBObject();
		doc.append("event_id", workRecord.getEv_id());

		WriteResult result = coll.remove(doc);
		printResultOfMongoOperation(result, "delete");
		return result.getError() == null ? MONGO_SUCCESS : MONGO_FAIL ;
	}
	//*****************************************************************************
	
	private static void printResultOfMongoOperation(WriteResult result, String operation) {
		if (result.getError() == null) {
			System.out.println("Document " + operation + " sucess in MongoDB");
		}
		else {
			System.out.println("Document " + operation + " fail in MongoDB " + result.getError());
		}

	}
	private static WorkRecord createWorkRecordObj(ResultSet rs) throws SQLException {
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
				ev_id, ev_name, ev_venue, ev_datetime);

		return workRecord;
	}

	//*****************************************************************************
	@Override
	public void run() {
		try{
			processAllWorkRecords();

		}
		catch(SQLException se)
		{
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
	}
	//*****************************************************************************
}
