 -- create Mysql tables events, work

  CREATE TABLE events
  (
  	 event_id 			bigint,
  	 event_name 		varchar(255),
  	 event_datetime  	bigint,
  	 event_venue 		varchar(255),
  	 PRIMARY KEY (event_id)
  )

 CREATE TABLE work
  (
  	 id mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
     changetype enum('NEW','EDIT','DELETE') NOT NULL,
     createtime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     
     completed  boolean DEFAULT false,    -- when work record is written to noSQL, set this to true
     
     ev_id 			bigint,
  	 ev_name 		varchar(255),
  	 ev_datetime  	bigint,
  	 ev_venue 		varchar(255),
  	 PRIMARY KEY (id)
  
  )
  
 
  
               -- create new records in work table using a trigger
   DELIMITER $$
   CREATE TRIGGER events_trigger_ins 
    AFTER INSERT ON events
    FOR EACH ROW 
    
    BEGIN
    	INSERT INTO work ( changetype, completed, ev_id, ev_name, ev_datetime, ev_venue) 
    	 VALUES ("NEW", FALSE,
    	   NEW.event_id,
    	   NEW.event_name, 
    	   NEW.event_datetime,
    	   NEW.event_venue     );
   END$$
   DELIMITER ;
                
   DELIMITER $$
   CREATE TRIGGER events_trigger_upd 
    AFTER UPDATE ON events
    FOR EACH ROW 
    
    BEGIN
    	INSERT INTO work ( changetype, completed, ev_id, ev_name, ev_datetime, ev_venue) 
    	 VALUES ("EDIT", FALSE,
    	   NEW.event_id,
    	   NEW.event_name, 
    	   NEW.event_datetime,
    	   NEW.event_venue     );
   END$$
   DELIMITER ;
  
   DELIMITER $$
   CREATE TRIGGER events_trigger_del
    AFTER DELETE ON events
    FOR EACH ROW 
    
    BEGIN
    	INSERT INTO work ( changetype, completed, ev_id, ev_name, ev_datetime, ev_venue) 
    	 VALUES ("DELETE", FALSE,
    	   OLD.event_id,
    	   OLD.event_name, 
    	   OLD.event_datetime,
    	   OLD.event_venue     );
   END$$
   DELIMITER ;
   	
   -- sample data to insert into events table
   
  --INSERT INTO events (event_id, event_name, event_datetime, event_venue)
   --VALUES (1, 'Michael Buble World Tour',1234565, 'Madison Square Garden')  
  
   --INSERT INTO events (event_id, event_name, event_datetime, event_venue)
   --VALUES (2, 'Joe Jackson Asian Tour',288288, 'Boston Garden') 
   
   --UPDATE events SET event_venue = 'The Rose Bowl' WHERE event_id=1
   --UPDATE events SET event_venue = 'The Super Bowl' WHERE event_id=2
   
   --DELETE from events WHERE event_id=1