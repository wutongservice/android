package twitter4j;


public class QiupuAlbum implements java.io.Serializable,Comparable<QiupuAlbum> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3094579370231636453L;

	public long album_id;
	public long user_id;
	public int album_type;
	public String title;
	public String summary;
	public boolean  privacy;
	public long created_time;
	public long updated_time;
	public int  photo_count;
	public boolean  have_expired = true;
	public String album_cover_photo_middle;

	public QiupuAlbum clone() {
		QiupuAlbum album = new QiupuAlbum();
		album.album_id = album_id;
		album.user_id = user_id;
		album.album_type = album_type;
		album.title = title;
		album.summary = summary;
		album.privacy = privacy;
		album.created_time = created_time;
		album.have_expired = have_expired;
		album.updated_time = updated_time;
		album.photo_count = photo_count;
		album.album_cover_photo_middle = album_cover_photo_middle;

		return album;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QiupuAlbum)) {
			return false;
		}
		QiupuAlbum uc = (QiupuAlbum) obj;
		return (uc.album_id == album_id);
	}
	
	
	@Override
	public String toString() {
		return " album_id           = "+album_id +
	           " user_id          = " +user_id+
	           " album_type         = "+album_type+
	           " title           = "+title+
	           " summary         = "+summary+
	           " privacy         = "+privacy+
	           " updated_time         = "+updated_time+
	           " have_expired         = "+have_expired+
	           " created_time         = "+created_time+
	           " photo_count         = "+photo_count+
	           " album_cover_photo_middle  = "+album_cover_photo_middle;
	}

    @Override
    public int compareTo(QiupuAlbum another) {
        if(QiupuAlbum.class.isInstance(another)) {
            if(updated_time > another.updated_time) {
                return 1;
            }else {
                return -1;
            }
        }
        return 0;
    }
}
