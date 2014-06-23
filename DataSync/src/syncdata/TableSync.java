package syncdata;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	private static final int SLEEP_SECONDS = 2;  // how long does the main thread sleep

	private static final int secondsOfDelay = 1 ;  //time between task executions, where task is processing work records
	private static final int startDelay = 5;   //initially in millisecs

	
	private static Mongo mongo=null;
	private static SQL sql=null;
	
	//*********************************************************************************

	public TableSync() {
		//get connections to mongoDB using the events collection
		// and mySQL
		
		mongo = new Mongo("events");
		sql = new SQL();
	}
	
	//*****************************************************************************
	public static void main(String[] args) {
		execute();
	}
	//*****************************************************************************
	public static void execute() {
		TimerTask timerTask = new TableSync();

		startTimer(timerTask);

		//this program is supposed to run forever  (:-)
		runForever();
	}
	//*****************************************************************************
	private static void runForever() {
		while (true) {
			try {
				Thread.sleep(SLEEP_SECONDS * 1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}
	//*****************************************************************************
	private static void startTimer(TimerTask timerTask) {
		Timer timer = new Timer(true);

		/*
		 * public void scheduleAtFixedRate(TimerTask task,long delay,long period)
		 * task--This is the task to be scheduled.
           delay--This is the delay in milliseconds before task is to be executed.
           period--This is the time in milliseconds between successive task executions.
		 */

		timer.scheduleAtFixedRate(timerTask, startDelay, secondsOfDelay * 1000);

		System.out.println("TimerTask begins! :" + new Date());
	}

	//*****************************************************************************

	//get all the work records from the mysql database, and
	//use them to update mongodb

	private static void processAllWorkRecords()
			throws SQLException {
		
		//get the work records which have not yet been processed in order of creation
		//System.out.println("Process all work records");

		List<WorkRecord> worklist = sql.getWorkList();
		
		for (int i=0; i < worklist.size(); i++) {
		
			WorkRecord workRecord = worklist.get(i);
			
			System.out.println(workRecord.toString());
			String type= workRecord.getChangetype();
			
			if (type.equals("NEW")) {
				processNEW( workRecord);
			}
			else
				if (type.equals("EDIT")) {
					processEDIT(workRecord);
				}
				else
					if (type.equals("DELETE")) {
						processDELETE(workRecord);
					}
		}
	
	}
	//*****************************************************************************
	
	//we have a NEW type work record, we want to insert a new mongodb doc
	private static void processNEW( WorkRecord workRecord) {
		int result = mongo.insertDoc(workRecord);
		try {
			updateWorkRecordOnSuccess( workRecord, result, "insert");
		} catch (SQLException e) {
			//here we should really roll back the mongo operation
			e.printStackTrace();
		}
	}

	//we have an EDIT type work record, we want to edit a pre-existing mongodb doc
	private static void processEDIT( WorkRecord workRecord) {
		int result = mongo.editDoc(workRecord);

		try {
			updateWorkRecordOnSuccess( workRecord, result, "edit");
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	//we have a DELETE type work record, we want to delete the corresponding mongodb doc
	private static void processDELETE( WorkRecord workRecord) {
		int result = mongo.deleteDoc(workRecord);
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
		if (result==Mongo.MONGO_SUCCESS) {
			System.out.println("MongoDB " + operation + " success; id=" + workRecord.getId());
			sql.updateWorkRecordAsComplete( workRecord.getId());
		}
		else {
			System.out.println("MongoDB " + operation + " fail; id=" + workRecord.getId());
		}
	}

	@Override
	public void run() {
		try{
			processAllWorkRecords();
		}
		catch(SQLException se){
		 sql.handleSQLerrors(se);
		}
	}
	//*****************************************************************************
}
