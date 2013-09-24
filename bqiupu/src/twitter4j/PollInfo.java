package twitter4j;

import java.util.ArrayList;
import java.util.Comparator;

public class PollInfo implements java.io.Serializable {

    private static final long serialVersionUID = -106029402600968891L;
    public static final int   TYPE_PUBLIC = 0; //公共投票
    public static final int   TYPE_INVITED_ME = 1;//邀请我的投票
    public static final int   TYPE_I_CREATED = 2;//我创建的投票
            
    public long               uid;
    public String             poll_id;
    public String             title;
    public String             description;
    public String             user_name;
    public String             image_url;
    public int                restrict;
    public int                type;  //用于标记存入数据库中数据的类型
    public String             target_id;
    public int                multi;
    public int                limit;
    public int                privacy;
    public long               created_time;
    public long               end_time;
    public long               updated_time;
    public long               destroyed_time;
    public long attend_status;
    public long attend_count;
    public long left_time;
    public boolean viewer_can_vote;
    public int mode;
    public boolean has_voted;
    public int viewer_left;
    public QiupuSimpleUser sponsor;
    public int comment_count;
    public boolean can_add_items;

    public ArrayList<PollItemInfo> pollItemList = new ArrayList<PollItemInfo>();

    public static final int MODE_ONECE = 0;
    public static final int MODE_ADD = 1;
    public static final int MODE_CHNAEGE = 2;

    public static final Comparator<PollInfo> COMPARATOR = new Comparator<PollInfo>() {
        public final int compare(PollInfo a, PollInfo b) {
            if (a.attend_status > b.attend_status) {
                return 1;
            } else if (a.attend_status < b.attend_status) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    public PollInfo() {
        
    }

    public void despose() {
        poll_id = null;
        title = null;
        description = null;
        user_name = null;
        image_url = null;
    }
}
