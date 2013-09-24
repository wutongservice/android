package twitter4j;


public class SharedPhotos implements java.io.Serializable {
	private static final long serialVersionUID = -7470213211289758998L;
	
	public long post_id;
	public String photo_img_middle;

	public SharedPhotos clone() {
		SharedPhotos u = new SharedPhotos();
		u.post_id = post_id;
		u.photo_img_middle = photo_img_middle;
		
		return u;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SharedPhotos)) {
			return false;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return " post_id           = "+post_id +
	           " photo_img_middle          = " +photo_img_middle;
	}
}
