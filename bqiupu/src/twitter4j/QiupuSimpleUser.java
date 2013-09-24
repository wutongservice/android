package twitter4j;


public class QiupuSimpleUser implements java.io.Serializable {		
	private static final long serialVersionUID = -2406027434411629477L;
	
	public long   id;
	public long   uid;
	public String name;	
	public String nick_name;
	public String name_pinyin;
	public String location;	
	
	public String profile_image_url;
	public String profile_simage_url;
	public String profile_limage_url;
    public boolean reset_image_url;
    public String distance;
    // for recommend show
    public String contact_method;
    public String work_background;
    public String education_background;
	
//	public int    relationship;	
	
//	public String domain;

	//it is for select user
	public boolean selected;
	
	public QiupuSimpleUser clone()
	{
		QiupuSimpleUser user = new QiupuSimpleUser();
		user.id = id;
		user.uid = uid;
		user.name = name;
		user.nick_name = nick_name;
		user.name_pinyin = name_pinyin;
		user.location  = location;
		user.profile_image_url = profile_image_url;
		user.profile_limage_url = profile_limage_url;
		user.profile_simage_url = profile_simage_url;
        user.reset_image_url = reset_image_url;
        user.distance = distance;
        // for recommend show
        user.contact_method = contact_method;
        user.work_background = work_background;
        user.education_background = education_background;
		
		return user;
	}
	
	@Override public boolean equals(Object obj)
	{
		if(!(obj instanceof QiupuSimpleUser))
		{
			return false;
		}
		QiupuSimpleUser ap = (QiupuSimpleUser)obj;
		return (ap.uid == uid);		
	}

	public void despose() {
		
		name      = null;	
		nick_name = null;
		name_pinyin = null;
		location  = null; 	
		
		profile_image_url = null;
		profile_simage_url = null;
		profile_limage_url = null;
		distance = null;
		contact_method = null;
		work_background = null;
		education_background = null;
	}
}
