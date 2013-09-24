package twitter4j;

import java.util.HashMap;

public class CacheMap {
    public final static String bookLinkContent = "bookLinkContent";
    public final static String urllinkContent = "urllinkContent";
    public static final String postFrom = "postFrom";
    public static final String appSummary = "appSummary";
    public static final String apkurllink = "apkurllink";
    public static final String CommentsUserName = "CommentsUserName";
    public static final String CommentsMessageCahed = "CommentsMessageCahed";
    public static final String SimpleCommentsCached = "SimpleCommentsCached";
    public static final String postRecipient = "postRecipient";
    public static final String albumDescription = "albumDescription";
    public static final String CommentsReferredCache = "CommentsReferredCache";

    public HashMap<String, String> cacheMap = new HashMap<String, String>();

    public String getCache(String metaAtt) {
        return cacheMap == null ? null : cacheMap.get(metaAtt);
    }

    public boolean getBooleanCache(String metaAtt) {
        if (cacheMap.get(metaAtt) != null)
            return cacheMap.get(metaAtt).equals("1");
        else
            return false;
    }

    public void cacheMeta(String metaAtt, String content) {
        cacheMap.put(metaAtt, content);
    }

    protected void despose() {
        cacheMap.clear();
    }
}
