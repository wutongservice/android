package twitter4j;


public class FileBasicInfo implements java.io.Serializable {

    private static final long serialVersionUID = -8950688045442104461L;

    public String file_id;
    public String title;
    public String summary;
    public long file_size;
    public String user_id;
    public String exp_name;
    public String html_url;
    public String content_type;
    public String new_file_name;
    public long created_time;
    public long updated_time;
    public long destroyed_time;
    public String file_url;
    public String description;
    public String thumbnail_url;

    public void despose() {
        file_id = null;
        title = null;
        summary = null;
        file_size = 0;
        user_id = null;
        exp_name = null;
        html_url = null;
        content_type = null;
        new_file_name = null;
        created_time = 0;
        updated_time = 0;
        destroyed_time = 0;
        file_url = null;
        description = null;
        thumbnail_url = null;
    }

    @Override
    public String toString() {
        return "file_id = " + file_id +
               "\ntitle = " + title +
               "\nsummary = " + summary +
               "\nfile_size = " + file_size +
               "\nuser_id = " + user_id +
               "\nexp_name = " + exp_name +
               "\nhtml_url = " + html_url +
               "\ncontent_type = " + content_type + 
               "\nnew_file_name = " + new_file_name +
               "\ncreated_time = " + created_time +
               "\nupdated_time = " + updated_time +
               "\ndestroyed_time = " + destroyed_time +
               "\nfile_utl = " + file_url +
               "\ndescription = " + description +
               "\nthumbnail_url = " + thumbnail_url;
    }

}