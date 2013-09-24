package twitter4j;


public class WorkExperience implements java.io.Serializable {
	private static final long serialVersionUID = -2406027434411629477L;

	public long id;
//	public long we_id;
	public String from;
	public String to;
	public long uid;
	public String  company;
	public String department;
	public String  office_address;
	public String job_title;
	public String job_description;

	public WorkExperience clone() {
		WorkExperience we = new WorkExperience();
		we.id = id;
//		we.we_id = we_id;
		we.from = from;
		we.to = to;
		we.uid = uid;
		we.company = company;
		we.department = department;
		we.office_address = office_address;
		we.job_title = job_title;
		we.job_description = job_description;

		return we;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WorkExperience)) {
			return false;
		}
		WorkExperience uc = (WorkExperience) obj;
		return (uc.id == id);
	}
	
	@Override
	public String toString() {
		return " id           = "+id +
	           " uid          = " +uid+
//	           " we_id          = " +we_id+
	           " from         = "+from+
	           " to           = "+to+
	           " company         = "+company+
	           " department         = "+department+
	           " office_address         = "+office_address+
	           " job_title  = "+job_title +
	           " job_description           = "+job_description;
	}
}
