package syncdata;

//This class represents the Work table. Work records are generated in the
//SQL database by triggers linked to updates and deletes on the events table

// id : 			unique identifier
// changetype :    can be NEW, EDIT or DELETE
// createTime :    is the time that the work table record was created
// completed:      has the change been propagated to mongoDB yet?

public class WorkRecord {

	 private int id  ;
	 private String changetype;
	 private long  createTime;
	 private Boolean completed ;
	 private Event event;
	 
	public WorkRecord(int id, String changetype, long createTime,
			Boolean completed, Event event) {
		super();
		this.id = id;
		this.changetype = changetype;
		this.createTime = createTime;
		this.completed = completed;
		this.event = event;
	}
	@Override
	public String toString() {
		return "WorkRecord [id=" + id + ", changetype=" + changetype
				+ ", createTime=" + createTime + ", completed=" + completed
				+ ", event=" + event.toString() + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getChangetype() {
		return changetype;
	}
	public void setChangetype(String changetype) {
		this.changetype = changetype;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	

}
