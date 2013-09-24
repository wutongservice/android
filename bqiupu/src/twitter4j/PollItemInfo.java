package twitter4j;

import java.util.ArrayList;

public class PollItemInfo implements java.io.Serializable {

    private static final long serialVersionUID = 2407248522207023287L;

    public String item_id;
    public String message;
    public int count;
    public ArrayList<QiupuSimpleUser> userList = new ArrayList<QiupuSimpleUser>(); 
    public boolean viewer_voted;
    public long voted_time;// don't care
    public boolean selected;

    public PollItemInfo() {
        
    }

    public void despose() {
        message = null;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (!(o instanceof PollItemInfo)) {
            return false;
        }
    	PollItemInfo ap = (PollItemInfo) o;
        return (ap.item_id.equals(item_id));
    }

}
