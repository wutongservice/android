package twitter4j;

import java.io.Serializable;

public class InfoCategory implements Serializable , Comparable<InfoCategory>{
	private static final long serialVersionUID = -654438052230354508L;
	public long categoryId;
    public String categoryName;
    public long creatorId;
    public long scopeId;
    public String scopeName;
    
	@Override
	public int compareTo(InfoCategory another) {
		// TODO Auto-generated method stub
		return 0;
	}
}
