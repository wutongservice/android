package twitter4j;

import java.util.ArrayList;



public class PageInfo implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	public long id;
	public long  page_id;
	public String name;
	public String name_en;
	public String address;
	public String address_en;
	public String description;
	public String description_en;
	public String email;
	public String website;
	public String tel;
	public String fax;
	public String zip_code;
	public String small_logo_url;
	public String logo_url;
	public String large_logo_url;
	public String small_cover_url;
	public String cover_url;
	public String large_cover_url;
	public long associated_id;
	public String free_circle_ids;
	public long created_time;
    public long updated_time;
    public int followers_count;
    public boolean followed;
    public boolean viewer_can_update;
    public long creatorId;	
    public UserCircle associatedCircle;
    public ArrayList<UserCircle> freeCircles;
    public boolean in_associated_circle;
    public ArrayList<UserImage>  fansList;
//    public String email_domain1;
//    public String email_domain2;
//    public String email_domain3;
//    public String email_domain4;
    
    public static final String PAGE_ID = "page_id";
    public static final String PAGE_INFO = "page_info";
    
    
    @Override
    public String toString() {
    	return " id           = "+id +
		"page_id     = "+ page_id +
       " name          = " +name+
       " name_en         = "+name_en+
       " address  = "+address +
       " address_en  = "+address_en +
       " description  = "+description +
       " description_en  = "+description_en +
       " email  = "+email +
       " website  = "+website +
       " tel  = "+tel +
       " fax  = "+fax +
       " zip_code  = "+zip_code +
       " small_logo_url  = "+small_logo_url +
       " logo_url  = "+logo_url +
       " large_logo_url  = "+large_logo_url +
       " small_cover_url  = "+small_cover_url +
       " cover_url  = "+cover_url +
       " large_cover_url  = "+large_cover_url +
       " associated_id  = "+associated_id +
       " created_time  = "+created_time +
       " updated_time  = "+updated_time + 
       " creatorId      = " + creatorId + 
       " free_circle_ids  = " + free_circle_ids;
    }
    
    public PageInfo clone() {
    	PageInfo info = new PageInfo();
    	info.id = id;
    	info.page_id = page_id;
    	info.name = name;
    	info.name_en = name_en;
    	info.address = address;
    	info.address_en = address_en;
    	info.description = description;
    	info.description_en = description_en;
    	info.email = email;
    	info.website = website;
    	info.tel = tel;
    	info.fax = fax;
    	info.zip_code = zip_code;
    	info.small_logo_url = small_logo_url;
    	info.logo_url = logo_url;
    	info.large_logo_url = large_logo_url;
    	info.small_cover_url = small_cover_url;
    	info.cover_url = cover_url;
    	info.large_cover_url = large_cover_url;
    	info.associated_id = associated_id;
    	info.created_time = created_time;
    	info.updated_time = updated_time;
    	info.followers_count = followers_count;
    	info.viewer_can_update = viewer_can_update;
    	info.creatorId = creatorId;
    	info.free_circle_ids = free_circle_ids;
    	info.in_associated_circle = in_associated_circle;
    	if(fansList != null) {
            info.fansList = new ArrayList<UserImage>();
            for(int i=0;i<fansList.size();i++) {
            	info.fansList.add(fansList.get(i).clone());
            }
        }
		return info;
	}    
}
