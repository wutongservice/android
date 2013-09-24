package twitter4j;

import java.util.ArrayList;


public class QiupuPhoto implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2805566259259781930L;
	public long photo_id;
	public long album_id;

    public String album_name;
    public long uid;

	public String photo_url_small;
	public String photo_url_middle;
	public String photo_url_big;
	public String photo_url_original;
	public String photo_url_thumbnail;

	public String caption;
	public String location;
	public long created_time;

	public int likes_count;
	public int comments_count;

    public String tag_ids;
   	public ArrayList<PhotoTag> tags;

	public boolean iliked;
	public long from_user_id;
	public String from_nick_name;
	public String from_image_url;

	public QiupuPhoto() {
		super();
	}

	public void despose() {
        album_name           = null;
		photo_url_original = null;
		photo_url_middle = null;
		photo_url_big = null;
		photo_url_small = null;
		photo_url_thumbnail = null;
		from_nick_name = null;
		from_image_url = null;
		caption = null;
		location = null;
		tag_ids = null;
		if(tags != null) {
			tags.clear();
			tags = null;
		}
    }

	public QiupuPhoto clone() {
		QiupuPhoto photo = new QiupuPhoto();
		photo.photo_id = photo_id;
		photo.album_id = album_id;
        photo.album_name = album_name;
		photo.uid = uid;
		photo.photo_url_original = photo_url_original;
		photo.photo_url_middle = photo_url_middle;
		photo.photo_url_small = photo_url_small;
		photo.photo_url_big = photo_url_big;
		photo.photo_url_thumbnail = photo_url_thumbnail;
		photo.caption = caption;
		photo.created_time = created_time;
		photo.location = location;
		photo.tag_ids = tag_ids;
		photo.from_user_id = from_user_id;
		photo.from_nick_name = from_nick_name;
		photo.from_image_url = from_image_url;

		if(tags != null) {
			for(int i=0;i<tags.size();i++) {
				photo.tags.add(tags.get(i).clone());
			}
		}

		return photo;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QiupuPhoto)) {
			return false;
		}
		QiupuPhoto uc = (QiupuPhoto) obj;
		return (uc.photo_id == photo_id);
	}

	@Override
	public String toString() {
		return " photo_id           = "+photo_id +
		       " album_id           = "+album_id +
                " album_name        = "+album_name +
	           " user_id          = " +uid+
	           " photo_url_original         = "+photo_url_original+
	           " photo_url_middle         = "+photo_url_middle+
	           " photo_url_thumbnail         = "+photo_url_thumbnail+
	           " photo_url_small         = "+photo_url_small+
	           " photo_url_big           = "+photo_url_big+
	           " from_image_url           = "+from_image_url+
	           " from_nick_name           = "+from_nick_name+
	           " from_user_id           = "+from_user_id+
	           " tag_ids         = "+tag_ids+
	           " caption         = "+caption+
	           " created_time         = "+created_time+
	           " location  = "+location;
	}
}
