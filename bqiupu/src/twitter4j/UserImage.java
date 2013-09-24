package twitter4j;


public class UserImage implements java.io.Serializable {
	private static final long serialVersionUID = -7470213211289758998L;
	
	public long user_id;
	public String image_url;
	public String userName;

	public UserImage clone() {
		UserImage u = new UserImage();
		u.user_id = user_id;
		u.image_url = image_url;
		u.userName = userName;
		return u;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserImage)) {
			return false;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return " user_id           = "+user_id +
	           " image_url          = " +image_url + 
	           "userName            = " + userName;
	}
}
