package twitter4j;


public class RecommendHeadViewItemInfo implements java.io.Serializable{
	private static final long serialVersionUID = -8688978046463819616L;
	public long sub_id;
    public String sub_name;
    public CategoryIcon categoryIcon;
    public boolean isSuggest;
    public boolean isFromSerialize;
    
    public static class CategoryIcon implements java.io.Serializable 
	{
		private static final long serialVersionUID = 1L;
		
		public String mdpi;		
		public String hdpi;		
		public CategoryIcon()
    	{}
		
		public void despose()
		{
			mdpi = null;
			hdpi = null;
		}
	}
    
}
