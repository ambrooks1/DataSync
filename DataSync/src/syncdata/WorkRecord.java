package syncdata;

public class WorkRecord {

	 private int id  ;
	 private String changetype;
	 private long  createTime;
	 
	 public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	private Boolean completed ;
	 private int ev_id;
	 private String ev_name ;
	 private String ev_venue ;
	 private long ev_datetime ;
	
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
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	public int getEv_id() {
		return ev_id;
	}
	public void setEv_id(int ev_id) {
		this.ev_id = ev_id;
	}
	public String getEv_name() {
		return ev_name;
	}
	public void setEv_name(String ev_name) {
		this.ev_name = ev_name;
	}
	public String getEv_venue() {
		return ev_venue;
	}
	public void setEv_venue(String ev_venue) {
		this.ev_venue = ev_venue;
	}
	public long getEv_datetime() {
		return ev_datetime;
	}
	public void setEv_datetime(int ev_datetime) {
		this.ev_datetime = ev_datetime;
	}
	 public WorkRecord(int id, String changetype, long createTime, Boolean completed, int ev_id,
				String ev_name, String ev_venue, long ev_datetime) {
			super();
			this.id = id;
			this.changetype = changetype;
			this.createTime=createTime;
			this.completed = completed;
			this.ev_id = ev_id;
			this.ev_name = ev_name;
			this.ev_venue = ev_venue;
			this.ev_datetime = ev_datetime;
		}
	@Override
	public String toString() {
		return "WorkRecord [id=" + id + ", changetype=" + changetype
				+ ", createTime=" + createTime + ", completed=" + completed
				+ ", ev_id=" + ev_id + ", ev_name=" + ev_name + ", ev_venue="
				+ ev_venue + ", ev_datetime=" + ev_datetime + "]";
	}
	

	
}
