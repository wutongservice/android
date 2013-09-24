package twitter4j;

import java.io.Serializable;

public class Employee implements Serializable, Comparable<Employee> {

	private static final long serialVersionUID = -1529549232554276697L;

	@Override
	public int compareTo(Employee another) {
		// TODO Auto-generated method stub
		return 0;
	}
	public String name;
	public String namePinYin;
	public String employee_id;
	public String user_id;
	public String image_url_s;
	public String image_url_m;
	public String image_url_l;
	public String email;
	public String tel;
	public String mobile_tel;
	public String department;
	public String job_title;
	public boolean is_favorite;
	public int role_in_group;
	public int status;
	
	
	public Employee clone() {
		Employee em = new Employee();
		em.name = name;
		em.namePinYin = namePinYin;
		em.employee_id = employee_id;
		em.user_id = user_id;
		em.image_url_s = image_url_s;
		em.image_url_m = image_url_m;
		em.image_url_l = image_url_l;
		em.email = email;
		em.tel = tel;
		em.mobile_tel = mobile_tel;
		em.department = department;
		em.job_title = job_title;
		em.is_favorite = is_favorite;
		em.role_in_group = role_in_group;
		em.status = status;
		
		return em;
	}
	
	public void dispose() {
		name = null;
		namePinYin = null;
		employee_id = null;
		user_id = null;
		image_url_s = null;
		image_url_m = null;
		image_url_l = null;
		email = null;
		tel = null;
		mobile_tel = null;
		department = null;
		job_title = null;
	}
	
}
