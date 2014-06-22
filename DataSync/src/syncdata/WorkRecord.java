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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((changetype == null) ? 0 : changetype.hashCode());
		result = prime * result
				+ ((completed == null) ? 0 : completed.hashCode());
		result = prime * result + (int) (createTime ^ (createTime >>> 32));
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkRecord other = (WorkRecord) obj;
		if (changetype == null) {
			if (other.changetype != null)
				return false;
		} else if (!changetype.equals(other.changetype))
			return false;
		if (completed == null) {
			if (other.completed != null)
				return false;
		} else if (!completed.equals(other.completed))
			return false;
		if (createTime != other.createTime)
			return false;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	public WorkRecord(String changetype, 
			Boolean completed, Event event) {
		super();
		this.changetype = changetype;
		this.createTime = 0L;
		this.completed = completed;
		this.event = event;
	}
	
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
