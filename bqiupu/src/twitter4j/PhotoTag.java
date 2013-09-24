package twitter4j;


public class PhotoTag implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2805566259259781698L;
	public int top;
	public int left;
	public int frame_width;
	public int frame_height;
	public String user_id;
	public String tag_text;
	public long photo_id;

	public PhotoTag clone() {
		PhotoTag album = new PhotoTag();
		album.photo_id = photo_id;
		album.top = top;
		album.left = left;
		album.frame_width = frame_width;
		album.frame_height = frame_height;
		album.user_id = user_id;
		album.tag_text = tag_text;

		return album;
	}
	
	public void despose() {
		user_id = null;
		tag_text = null;
    }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PhotoTag)) {
			return false;
		}
		PhotoTag uc = (PhotoTag) obj;
		return (uc.photo_id == photo_id)&&(uc.top == left)&&(uc.user_id == user_id);
	}
	
	@Override
	public String toString() {
		return " photo_id           = "+photo_id +
		       " top           = "+top +
	           " user_id          = " +user_id+
	           " left         = "+left+
	           " frame_width           = "+frame_width+
	           " frame_height         = "+frame_height+
	           " user_id         = "+user_id+
	           " tag_text         = "+tag_text;
	}
}
