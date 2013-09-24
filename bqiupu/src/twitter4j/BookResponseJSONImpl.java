package twitter4j;

import twitter4j.internal.org.json.JSONObject;

public class BookResponseJSONImpl extends BookResponse {

	private static final long serialVersionUID = -8114967106361087698L;

	public BookResponseJSONImpl(JSONObject obj) throws TwitterException {
		try {
            this.id = obj.getString(KEY_ID);
            this.name = obj.getString(KEY_NAME);
            this.summary = obj.getString(KEY_SUMMARY);
            this.coverurl = obj.getString(KEY_COVER_URL);
            this.writer = obj.getString(KEY_writer);
            this.size = obj.getString(KEY_size);

			try{
//                this.bookformat = obj.getString("format");
//                this.bookstar = obj.getInt("star");
//                this.bookdownloadedcount = obj.getInt("downloadedcount");
//                this.bookcommentcount = obj.getInt("commentcount");
//                this.booksize = obj.getInt("size");
//                this.bookmd5 = obj.getString("md5");
//                this.bookwriter = obj.getString("writer");
//                this.bookscore = obj.getInt("score");
//                this.booklanguage = obj.getString("language");
//                this.booksharedcount = obj.getInt("sharedcount");
//                this.bookownerid = obj.getString("ownerid");
//                this.bookurl  = obj.getString("url");
			}catch(Exception ex){}

		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}
	}

//	public static BookResponse createBookResponse(HttpResponse response) throws TwitterException {
//		JSONObject obj;
//		try {
//			obj = response.asJSONObject();
//		    return new BookResponseJSONImpl(obj);
//		} catch (TwitterException e) {
//			throw e;
//		}
//	}
}
