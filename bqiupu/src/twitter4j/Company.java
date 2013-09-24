package twitter4j;

import java.io.Serializable;
import java.util.ArrayList;

public class Company implements Serializable, Comparable<Company> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6352331321380777950L;
	public static final String COMPANY_ID = "COMPANY_ID";
	public static final String COMPANY_INFO = "COMPANY_INFO";
	public static final String COMPANY_NAME = "COMPANY_NAME";
	
	public long id;
	public long department_id;
	public long created_time;
	public long updated_time;
	public int role;
	public int person_count;
	public int department_count;
	public String email_domain1;
	public String email_domain2;
	public String email_domain3;
	public String email_domain4;
	public String name;
	public String address;
	public String email;
	public String website;
	public String tel;
	public String fax;
	public String zip_code;
	public String small_logo_url;
	public String logo_url;
	public String large_logo_url;
	public String small_cover_url;
	public String cover_url;
	public String large_cover_url;
	public String description;
	public ArrayList<UserImage> memberList;
	public ArrayList<UserImage> depList;

	@Override
	public int compareTo(Company another) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void despose() {
		email_domain1 = null;
		email_domain2 = null;
		email_domain3 = null;
		email_domain4 = null;
		name = null;
		address = null;
		email = null;
		website = null;
		tel = null;
		fax = null;
		zip_code = null;
		small_logo_url = null;
		logo_url = null;
		large_logo_url = null;
		description  = null;
		small_cover_url  = null;
		cover_url  = null;
		large_cover_url  = null;
		
		if(memberList != null) {
			memberList.clear();
			memberList = null;
		}
		if(depList != null) {
			depList.clear();
			depList = null;
		}
	}
	
	public Company clone() {
		Company c = new Company();
		c.id = id;
		c.department_id = department_id;
		c.created_time = created_time;
		c.updated_time = updated_time;
		c.role = role;
		c.person_count = person_count;
		c.department_count = department_count;
		c.email_domain1 = email_domain1;
		c.email_domain2 = email_domain2;
		c.email_domain3 = email_domain3;
		c.email_domain4 = email_domain4;
		c.name = name;
		c.address = address;
		c.email = email;
		c.website = website;
		c.tel = tel;
		c.fax = fax;
		c.zip_code = zip_code;
		c.small_logo_url = small_logo_url;
		c.logo_url = logo_url;
		c.large_logo_url = large_logo_url;
		c.description = description;
		c.small_cover_url  = small_cover_url;
		c.cover_url  = cover_url;
		c.large_cover_url  = large_cover_url;
		
		if(memberList != null) {
			for(int i=0;i<memberList.size();i++) {
				c.memberList.add(memberList.get(i).clone());
			}
		}
		if(depList != null) {
			for(int i=0;i<depList.size();i++) {
				c.depList.add(depList.get(i).clone());
			}
		}
		return c;
	}

}
