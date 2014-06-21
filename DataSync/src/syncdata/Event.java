package syncdata;

public class Event implements Comparable<Event>{
	
	 private int id;
	 private String name ;
	 private String venue ;
	 private long datetime ;
	 
	 public Event(int ev_id, String ev_name, String ev_venue, long ev_datetime) {
		super();
		this.id = ev_id;
		this.name = ev_name;
		this.venue = ev_venue;
		this.datetime = ev_datetime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (datetime ^ (datetime >>> 32));
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((venue == null) ? 0 : venue.hashCode());
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
		Event other = (Event) obj;
		if (datetime != other.datetime)
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (venue == null) {
			if (other.venue != null)
				return false;
		} else if (!venue.equals(other.venue))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Event [id=" + id + ", name=" + name + ", venue="
				+ venue + ", datetime=" + datetime + "]";
	}
	
	 public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVenue() {
		return venue;
	}
	public void setVenue(String venue) {
		this.venue = venue;
	}
	public long getDatetime() {
		return datetime;
	}
	public void setDatetime(long datetime) {
		this.datetime = datetime;
	}
	
	@Override
	public int compareTo(Event other) {
		
		Integer idd = new Integer(id);
		Integer iddd = new Integer(other.id);
		
		return idd.compareTo(iddd);
	}
	
	 

	
}
