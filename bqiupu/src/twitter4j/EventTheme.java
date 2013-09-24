package twitter4j;


public class EventTheme  implements java.io.Serializable, Comparable<EventTheme>{

	private static final long serialVersionUID = 7744204736022144280L;
	public long id;
	public long creator;  //creator id
	public long updated_time; 
	public String name;
    public String image_url;
    
	@Override
	public int compareTo(EventTheme another) {
		return 0;
	}
}
