package twitter4j;

import java.util.ArrayList;



public class Circletemplate implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	public String majorVersion;
	public String minorVersion;
	public String description;
	public ArrayList<TemplateInfo> template;
	public ArrayList<TemplateInfo> templateFormal;
	public ArrayList<TemplateInfo> templateFormalSchool;
	public ArrayList<TemplateInfo> templateFormalCompany;
	
	public static final String SUBTYPE_TEMPLATE = "template";
	public static final String SUBTYPE_TEMPLATEFORMAL = "template.formal";
	public static final String SUBTYPE_TEMPLATEFORMALSCHOOL = "template.formal.school";
	public static final String SUBTYPE_TEMPLATEFORMALCOMPANY = "template.formal.company";
	
	public static final String TEMPLATE_FREE_NAME = "free_circle";
	public static final String TEMPLATE_FORMAL_NAME = "formal";
	public static final String TEMPLATE_NAME_CLASS = "class";
	public static final String TEMPLATE_NAME_DEPARTMENT = "department";
	public static final String TEMPLATE_NAME_SCHOOL = "school";
	public static final String TEMPLATE_NAME_COMPANY = "company";
	public static final String TEMPLATE_NAME_PROJECT = "project";
	public static final String TEMPLATE_NAME_APPLICATION = "application";
	
	
    @Override
    public String toString() {
    	return " majorVersion           = "+majorVersion;
    }
    
    public Circletemplate clone() {
    	Circletemplate info = new Circletemplate();
    	info.majorVersion = majorVersion;
		return info;
	}    
    
    public static class TemplateInfo implements java.io.Serializable{
		private static final long serialVersionUID = 1L;
		public String name;
		public int formal;
        public String title;
        public String title_en;
        public String description;
        public String description_en;
        public String icon_url;
    }
}
