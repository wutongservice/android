package twitter4j;

import android.content.ContentValues;
import android.database.Cursor;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;

import java.util.ArrayList;
import java.util.List;

public class QiupuUser extends QiupuAccountInfo implements
        Comparable<QiupuUser>, java.io.Serializable {
    public static final int USER_SUGGEST_TYPE_NONE = 0;
    public static final int USER_SUGGEST_TYPE_RECOMMENDER = 10;
   	public static final int USER_SUGGEST_TYPE_ATTENTION = 12;
   	public static final int USER_SUGGEST_TYPE_FROM_MY_ADDRESSBOOK = 21;
   	public static final int USER_SUGGEST_TYPE_FROM_BOTH_ADDRESSBOOK = 22;
   	public static final int USER_SUGGEST_TYPE_HAVE_MY_ADDRESSBOOK = 23;
   	public static final int USER_SUGGEST_FROM_WORK_INFO = 31;
   	public static final int USER_SUGGEST_FROM_EDUCATION_INFO = 32;
   	public static final int USER_SUGGEST_TYPE_BOTH_KNOW   = 40;
   	public static final int USER_SUGGEST_FROM_SERVER = 50;
   	
   	public static final int USER_STATUS_DEFAULT = 0;
   	public static final int USER_STATUS_UPDATED = 1;

    private static final long serialVersionUID = 5407931216597307323L;

    public String phone_number;
    public String date_of_birth;
    private String about_me;
    public int province;
    public int city;

    public long created_at;
    public long last_visit_time;

    public int verify_code;
    public int verified;

    public String url;

    public String gender = "";
//	public String email;

    public long friends_count;
    public long followers_count;
    public long favorites_count;
    public long posts_count;
    public long app_count;

    public String status;
    public long status_time;

    public String circleId;
    public String circleName;
    public boolean bidi;
    public boolean profile_privacy;

    public boolean isShortCut;
    public boolean his_friend;
    public String pedding_requests;

    public int referred_count;

    public String remark;
    public ArrayList<WorkExperience> work_history_list;
    public ArrayList<Education> education_list;
    public ArrayList<UserImage> friendsImageList;
    public ArrayList<UserImage> fansImageList;
    public ArrayList<SharedPhotos> shareImageList;
    public ArrayList<PerhapsName> perhapsNames;
    public long contactId; // only used to refresh friends contact UI

    public String distance;

    public QiupuUser() {
        super();
        work_history_list = new ArrayList<WorkExperience>();
        education_list = new ArrayList<Education>();

    }

    public void despose() {
        super.despose();

        phone_number = null;
        date_of_birth = null;

        about_me = null;

        url = null;
//		email = null;

        status = null;
        circleId = null;
        circleName = null;

        pedding_requests = null;
        remark = null;
        work_history_list.clear();
        work_history_list = null;
        education_list.clear();
        education_list = null;
        distance = null;

        if (friendsImageList != null) {
            friendsImageList.clear();
            friendsImageList = null;
        }
        if (fansImageList != null) {
            fansImageList.clear();
            fansImageList = null;
        }
        if (shareImageList != null) {
            shareImageList.clear();
            shareImageList = null;
        }
        if (perhapsNames != null) {
            perhapsNames.clear();
            perhapsNames = null;
        }
    }

    // TODO why compare with nick_name not with uid.
    public int compareTo(QiupuUser another) {
        if (QiupuUser.class.isInstance(another)) {
            String tnickname = another.nick_name;
            return nick_name.compareToIgnoreCase(tnickname);
        }
        return 0;
    }

    public QiupuUser clone() {
        QiupuUser user = new QiupuUser();
        clone(user);
        return user;
    }

    protected QiupuUser clone(QiupuUser user) {
        if (user == null) {
            user = new QiupuUser();
        }

        super.clone(user);

//		user.id = id;
//		user.uid = uid;
//		user.name = name;
//		user.nick_name = nick_name;
//		user.location = location;
//		user.profile_image_url = profile_image_url;
//		user.profile_limage_url = profile_limage_url;
//		user.profile_simage_url = profile_simage_url;

        user.phone_number = phone_number;
        user.date_of_birth = date_of_birth;

        user.about_me = about_me;
//		user.company = company;
//		user.department = department;
//		user.office_address = office_address;
        user.province = province;

        user.city = city;

        user.created_at = created_at;
        user.last_visit_time = last_visit_time;

        user.verify_code = verify_code;
        user.verified = verified;

//		user.description = description;

        user.url = url;

        user.gender = gender;
//		user.email = email;
        user.friends_count = friends_count;
        user.followers_count = followers_count;
        user.favorites_count = favorites_count;
        user.posts_count = posts_count;
        user.app_count = app_count;

        user.status = status;
        user.status_time = status_time;

//		if(addressInfo != null)
//		    user.addressInfo = addressInfo.clone();

        user.circleId = circleId;
        user.circleName = circleName;
        user.bidi = bidi;

        user.profile_privacy = profile_privacy;
        user.pedding_requests = pedding_requests;

        user.isShortCut = isShortCut;
        user.his_friend = his_friend;
//		user.name_pinyin = name_pinyin;
//		user.jobtitle = jobtitle;
        user.referred_count = referred_count;
        user.remark = remark;
        user.distance = distance;

        for (int i = 0; i < work_history_list.size(); i++) {
            user.work_history_list.add(work_history_list.get(i).clone());
        }
        for (int i = 0; i < education_list.size(); i++) {
            user.education_list.add(education_list.get(i).clone());
        }
//		for(int i=0;i<phoneList.size();i++) {
//            user.phoneList.add(phoneList.get(i).clone());
//        }
//		for(int i=0;i<emailList.size();i++) {
//			user.emailList.add(emailList.get(i).clone());
//		}
        if (friendsImageList != null) {
            user.friendsImageList = new ArrayList<UserImage>();
            for (int i = 0; i < friendsImageList.size(); i++) {
                user.friendsImageList.add(friendsImageList.get(i).clone());
            }
        }
        if (fansImageList != null) {
            user.fansImageList = new ArrayList<UserImage>();
            for (int i = 0; i < fansImageList.size(); i++) {
                user.fansImageList.add(fansImageList.get(i).clone());
            }
        }
        if (shareImageList != null) {
            user.shareImageList = new ArrayList<SharedPhotos>();
            for (int i = 0; i < shareImageList.size(); i++) {
                user.shareImageList.add(shareImageList.get(i).clone());
            }
        }
        if (perhapsNames != null) {
            user.perhapsNames = new ArrayList<QiupuUser.PerhapsName>();
            for (int i = 0; i < perhapsNames.size(); i++) {
                user.perhapsNames.add(perhapsNames.get(i).clone());
            }
        }

        return user;
    }

    public static ContentValues toContentValues(QiupuUser userinfo) {
        ContentValues cv = QiupuAccountInfo.toContentValues(userinfo);
        cv.put(UsersColumns.BIDI, userinfo.bidi ? 1 : 0);
        cv.put(UsersColumns.NAME_PINGYIN, QiupuORM.getPinyin(userinfo.nick_name));

        cv.put(UsersColumns.DATA_OF_BIRTH, userinfo.date_of_birth);
        cv.put(UsersColumns.ABOUT_ME, userinfo.about_me);
        cv.put(UsersColumns.PROVINCE, userinfo.province);
        cv.put(UsersColumns.CITY, userinfo.city);
        cv.put(UsersColumns.CREATED_AT, userinfo.created_at);
        cv.put(UsersColumns.LAST_VISIT_TIME, userinfo.last_visit_time);
        cv.put(UsersColumns.VERIFY_CODE, userinfo.verify_code);
        cv.put(UsersColumns.VERIFIED, userinfo.verified);
        cv.put(UsersColumns.URL, userinfo.url);
        cv.put(UsersColumns.GENDER, userinfo.gender);
        cv.put(UsersColumns.FRIENDS_COUNT, userinfo.friends_count);
        cv.put(UsersColumns.FOLLOWERS_COUNT, userinfo.followers_count);
        cv.put(UsersColumns.FAVOURITES_COUNT, userinfo.favorites_count);
        cv.put(UsersColumns.APP_COUNT, userinfo.app_count);
        cv.put(UsersColumns.STATUS, userinfo.status);
        cv.put(UsersColumns.STATUS_TIME, userinfo.status_time);
        cv.put(UsersColumns.CIRCLE_ID, userinfo.circleId);
        cv.put(UsersColumns.CIRCLE_NAME, userinfo.circleName);
        cv.put(UsersColumns.PROFILE_PRIVACY, userinfo.profile_privacy ? 1 : 0);
        cv.put(UsersColumns.BIDI, userinfo.bidi ? 1 : 0);
        cv.put(UsersColumns.HIS_FRIENDS, userinfo.his_friend ? 1 : 0);
        //no need the following two, it is local save
        cv.put(UsersColumns.REQUESTED_ID, userinfo.pedding_requests);
        //cv.put(UsersColumns.SHORTCUT, isShortCut ? 1 : 0);
//        cv.put(UsersColumns.REFERRED_COUNT, userinfo.referred_count); // used to record referred count
        cv.put(UsersColumns.REMARK, userinfo.remark);
        return cv;
    }

    public static QiupuUser createUserInformation(QiupuUser result, Cursor cursor) {
        QiupuAccountInfo.createUserInformation(result, cursor);
        result.name_pinyin = cursor.getString(cursor.getColumnIndex(UsersColumns.NAME_PINGYIN));
        result.date_of_birth = cursor.getString(cursor.getColumnIndex(UsersColumns.DATA_OF_BIRTH));
        result.about_me = cursor.getString(cursor.getColumnIndex(UsersColumns.ABOUT_ME));
        result.province = cursor.getInt(cursor.getColumnIndex(UsersColumns.PROVINCE));
        result.city = cursor.getInt(cursor.getColumnIndex(UsersColumns.CITY));
        result.created_at = cursor.getLong(cursor.getColumnIndex(UsersColumns.CREATED_AT));
        result.last_visit_time = cursor.getLong(cursor.getColumnIndex(UsersColumns.LAST_VISIT_TIME));
        result.verify_code = cursor.getInt(cursor.getColumnIndex(UsersColumns.VERIFY_CODE));
        result.verified = cursor.getInt(cursor.getColumnIndex(UsersColumns.VERIFIED));
        result.url = cursor.getString(cursor.getColumnIndex(UsersColumns.URL));
        result.gender = cursor.getString(cursor.getColumnIndex(UsersColumns.GENDER));
        result.friends_count = cursor.getLong(cursor.getColumnIndex(UsersColumns.FRIENDS_COUNT));
        result.followers_count = cursor.getLong(cursor.getColumnIndex(UsersColumns.FOLLOWERS_COUNT));
        result.favorites_count = cursor.getLong(cursor.getColumnIndex(UsersColumns.FAVOURITES_COUNT));
        result.app_count = cursor.getLong(cursor.getColumnIndex(UsersColumns.APP_COUNT));
        result.status = cursor.getString(cursor.getColumnIndex(UsersColumns.STATUS));
        result.status_time = cursor.getLong(cursor.getColumnIndex(UsersColumns.STATUS_TIME));

        result.bidi = cursor.getInt(cursor.getColumnIndex(UsersColumns.BIDI)) == 0 ? false : true;
        result.circleId = cursor.getString(cursor.getColumnIndex(UsersColumns.CIRCLE_ID));
        result.circleName = cursor.getString(cursor.getColumnIndex(UsersColumns.CIRCLE_NAME));
        result.profile_privacy = cursor.getInt(cursor.getColumnIndex(UsersColumns.PROFILE_PRIVACY)) == 0 ? false : true;
        result.pedding_requests = cursor.getString(cursor.getColumnIndex(UsersColumns.REQUESTED_ID));
        result.isShortCut = cursor.getInt(cursor.getColumnIndex(UsersColumns.SHORTCUT)) == 0 ? false : true;
        result.his_friend = cursor.getInt(cursor.getColumnIndex(UsersColumns.HIS_FRIENDS)) == 0 ? false : true;
        result.remark = cursor.getString(cursor.getColumnIndex(UsersColumns.REMARK));
        final int index = cursor.getColumnIndex(UsersColumns.REFERRED_COUNT);
        result.referred_count = index < 0 ? 0 : cursor.getInt(index);
        return result;
    }

    public static class RecommendUser implements Comparable,
            java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public ArrayList<QiupuSimpleUser> friends;// or likes

        public RecommendUser() {
            friends = new ArrayList<QiupuSimpleUser>();
        }

        public void despose() {
            friends.clear();
            friends = null;
        }

        public RecommendUser clone() {
            RecommendUser user = new RecommendUser();
            for (QiupuSimpleUser item : friends) {
                user.friends.add(item.clone());
            }

            return user;
        }

        public int compareTo(Object arg0) {
            return 0;
        }
    }

    public static class Circle implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public List<CircleInfo> circleList;

        public Circle() {
            circleList = new ArrayList<CircleInfo>();
        }

        public void despose() {
            while (circleList.size() > 0) {
                CircleInfo item = circleList.get(0);
                item.despose();

                circleList.remove(0);
            }

            circleList = null;
        }

        public static class CircleInfo implements Comparable, java.io.Serializable {
            private static final long serialVersionUID = 1L;
            public String circle_id;
            public String circle_name;

            public CircleInfo clone() {
                CircleInfo item = new CircleInfo();
                item.circle_id = circle_id;
                item.circle_name = circle_name;
                return item;
            }

            public void despose() {
                circle_id = null;
                circle_name = null;
            }

            public String toString() {
                return " circle_id             = " + circle_id +
                        " circle_name           = " + circle_name;
            }

            public int compareTo(Object another) {
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        }
    }

    public static class PerhapsName implements Comparable, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public int count;

        public PerhapsName clone() {
            PerhapsName item = new PerhapsName();
            item.name = name;
            item.count = count;
            return item;
        }

        public void despose() {
            name = null;
            count = 0;
        }

        public String toString() {
            return " name = " + name + " count = " + count;
        }

        public int compareTo(Object another) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PerhapsName)) {
                return false;
            }
            PerhapsName ap = (PerhapsName) obj;
            return (ap.name.equals(name));
        }
    }

    public int getRecommendingType() {
        return USER_SUGGEST_TYPE_NONE;
    }

    public static QiupuUser instance(QiupuSimpleUser simple) {
        if (null == simple) return null;
        QiupuUser user = new QiupuUser();
        user.id = simple.id;
        user.uid = simple.uid;
        user.name = simple.name;
        user.nick_name = simple.nick_name;
        user.name_pinyin = simple.name_pinyin;
        user.location = simple.location;
        user.profile_image_url = simple.profile_image_url;
        user.profile_limage_url = simple.profile_limage_url;
        user.profile_simage_url = simple.profile_simage_url;
        user.reset_image_url = simple.reset_image_url;
        user.distance = simple.distance;
        return user;
    }

    public static class Recommendation extends QiupuUser {
        public int suggest_type;
        public RecommendUser recommendUser;

        public Recommendation clone() {
            Recommendation suggestion = new Recommendation();
            return suggestion;
        }

        protected Recommendation clone(Recommendation suggestion) {
            if (null == suggestion) suggestion = new Recommendation();
            super.clone(suggestion);
            suggestion.suggest_type = suggest_type;

            if (recommendUser != null)
                suggestion.recommendUser = recommendUser.clone();

            return suggestion;
        }

        public void despose() {
            super.despose();

            if (recommendUser != null) {
                recommendUser.despose();
                recommendUser = null;
            }
        }

        @Override
        public int getRecommendingType() {
            return suggest_type;
        }

        public RecommendUser getRecommendUser() {
            return recommendUser;
        }
    }
}
