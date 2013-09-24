package twitter4j;

public class SyncResponse {
	public int versioncode; //-1 means pool does not contain the apk;
	public String versionName;
    public String apkname;
    public String iconurl; //icon url
    public String packagename;
    public String lastedapkurl; //Lasted apk url
    public long   apksize; //Lasted apk filesize
    public float  rating;
    public String apk_server_id; 
    public long last_installed_time;
    
    @Override
    public String toString() 
    {
    	return "SyncResponse packagemame:"+packagename+" apkname:"+apkname
    		+" versioncode:"+versioncode +"version :"+versionName +" iconurl:"+iconurl
    		+" lastedapkurl:"+lastedapkurl
    		+" apksize:"+apksize
    		+" rating:"+rating
    		+" apk_server_id:"+apk_server_id
    		+" last_installed_time = " + last_installed_time;
    }

	public void despose() {
		versionName = null;
	    apkname     = null;
	    iconurl     = null; //icon url
	    packagename = null;
	    lastedapkurl= null; //Lasted apk url	    
	    apk_server_id = null;
	}
}
