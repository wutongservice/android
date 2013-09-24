package twitter4j;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

public class BookBasicInfo implements java.io.Serializable {
	private static final long serialVersionUID = -7346869214278084692L;

    /**
     *  the scheme string to launch Book detail activity, defined by Book component.
     */
    public static final String DETAIL_ACTIVITY_SCHEME = "brook://borqs.com/bookinfo";

    public static final String MUSIC_ACTIVITY_SCHEME = "borqs://music/details";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_COVER_URL = "coverurl";
    public static final String KEY_writer = "writer";
    public static final String KEY_size   = "size";
    
    
    public String id = null;
	public String name = null;
    public String summary = null;    
    public String coverurl = null;
    public String writer = null;
    public String size = null;

//    public String bookwriter = null;
//    public String bookformat = null;
//    public int booksize = 0;
//    public String booklanguage = null;
//    public String bookmd5 = null;
//    public String bookownerid = null;
//    public int bookdownloadedcount = 0;
//    public int booksharedcount = 0;
//    public int bookcommentcount = 0;
//    public int bookscore = 0;
//    public int bookstar = 0;
//    public String bookurl = null;

    public Stream.Comments comments = new Stream.Comments();
	public Likes    likes    = new Likes();
	
	@Override
	public String toString() {
        return "BookResponse id:" + id
                + ", name" + name
                + ", summary" + summary
                + ", coverurl" + coverurl
                ;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void despose() {
		id = null;
		name = null;
		summary = null;
		coverurl = null;
		writer = null;
		size = null;
		
		comments.despose();
		comments = null;
		
		likes.despose();
		likes = null;
	}
	
	public BookBasicInfo clone() {
		BookResponse book = new BookResponse();

        book.id = this.id;
        book.name = this.name;
        book.coverurl = this.coverurl;
        book.summary = this.summary;
        book.writer = this.writer;
        book.size = this.size;

//        book.bookwriter = this.bookwriter;
//        book.bookformat = this.bookformat;
//        book.booksize = this.booksize;
//        book.booklanguage = this.booklanguage;
//        book.bookmd5 = this.bookmd5;
//        book.bookownerid = this.bookownerid;
//        book.bookdownloadedcount = this.bookdownloadedcount;
//        book.booksharedcount = this.booksharedcount;
//        book.bookcommentcount = this.bookcommentcount;
//        book.bookdownloaders = null;
//        book.booksharers = null;
//        book.bookcommenters = null;
//        book.bookscore = this.bookscore;
//        book.bookstar = this.bookstar;
//        book.bookurl = this.bookurl;

        // TODO: could such comment and count keep identical as Apk structure
		//apk.comments = new Comments();
		for(int i=0;i<comments.stream_posts.size();i++)
		{
			book.comments.stream_posts.add(comments.stream_posts.get(i).clone());
		}
		book.comments.count = book.comments.stream_posts.size();

		for(int i=0;i<likes.friends.size();i++)
		{
			book.likes.friends.add(likes.friends.get(i).clone());
		}

		return book;
	}
	
	public static class Likes implements java.io.Serializable 
	{
		private static final long serialVersionUID = 1L;
		
		public int count;		
		public List<QiupuSimpleUser> friends;//or likes		
		public Likes()
    	{
			friends = new ArrayList<QiupuSimpleUser>();			
    	}
		
		public void despose()
		{
			for(QiupuSimpleUser user: friends)
			{
				user.despose();
				user = null;
			}
			friends.clear();
			friends = null;
		}
	}

	@Override public boolean equals(Object obj)
	{
		if(!(obj instanceof BookBasicInfo))
		{
			return false;
		}
        return true;
//		BookBasicInfo ap = (BookBasicInfo)obj;
//		return (ap.apk_server_id.equals(apk_server_id) && ap.packagename.equals(packagename));
//		return (ap.apk_server_id == apk_server_id && ap.packagename.equals(packagename));
	}	
}
