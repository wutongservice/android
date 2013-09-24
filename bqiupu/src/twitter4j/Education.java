package twitter4j;


public class Education implements java.io.Serializable {
	private static final long serialVersionUID = -2406027434411629477L;

	public long id;
	public String from;
	public String to;
	public long uid;
	public String  school;
	public String school_class;
	public String type;
	public String  school_location;
	public String degree;
	public String major;

	public Education clone() {
		Education edu = new Education();
		edu.id = id;
		edu.from = from;
		edu.to = to;
		edu.uid = uid;
		edu.school = school;
		edu.school_class = school_class;
		edu.type = type;
		edu.school_location = school_location;
		edu.degree = degree;
		edu.major = major;

		return edu;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Education)) {
			return false;
		}
		Education uc = (Education) obj;
		return (uc.id == id);
	}
	
	@Override
	public String toString() {
		return " id           = "+id +
	           " uid          = " +uid+
	           " from         = "+from+
	           " to           = "+to+
	           " school         = "+school+
	           " school_class         = "+school_class+
	           " school_location         = "+school_location+
	           " type         = "+type+
	           " degree  = "+degree +
	           " major           = "+major;
	}
}
