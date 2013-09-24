package com.borqs.common;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午5:39
 * To change this template use File | Settings | File Templates.
 */

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PeopleSearchResults
  implements Parcelable
{
  public static final Parcelable.Creator<PeopleSearchResults> CREATOR;
  private static final String[] PROJECTION;
  private final ArrayList<Contact> mContacts = new ArrayList();
    // todo implement this later.
//  private EsMatrixCursor mCursor;
  private boolean mCursorValid;
  private final HashMap<Long, String> mGaiaIdsAndCircles = new HashMap();
  private boolean mGaiaIdsAndCirclesLoaded = false;
  private boolean mHasMoreResults;
  private final ArrayList<LocalProfile> mLocalProfiles = new ArrayList();
  private boolean mLocalProfilesLoaded = false;
  private String mMyPersonId;
  private long mNextId;
  private final ArrayList<PublicProfile> mPublicProfiles = new ArrayList();
  private String mQuery;
  private String mToken;

  static
  {
    String[] arrayOfString = new String[11];
    arrayOfString[0] = "_id";
    arrayOfString[1] = "person_id";
    arrayOfString[2] = "lookup_key";
    arrayOfString[3] = "gaia_id";
    arrayOfString[4] = "name";
    arrayOfString[5] = "packed_circle_ids";
    arrayOfString[6] = "matched_email";
    arrayOfString[7] = "email";
    arrayOfString[8] = "phone";
    arrayOfString[9] = "phone_type";
    arrayOfString[10] = "snippet";
    PROJECTION = arrayOfString;
    CREATOR = new Parcelable.Creator()
    {
      public PeopleSearchResults createFromParcel(Parcel paramParcel)
      {
        return new PeopleSearchResults(paramParcel);
      }

      public PeopleSearchResults[] newArray(int paramInt)
      {
        return new PeopleSearchResults[paramInt];
      }
    };
  }

  public PeopleSearchResults()
  {
  }

    public PeopleSearchResults(Parcel paramParcel) {
        this.mMyPersonId = paramParcel.readString();
        this.mQuery = paramParcel.readString();
        this.mToken = paramParcel.readString();
        if (paramParcel.readInt() != 0) {
            this.mHasMoreResults = true;
        } else {
            this.mHasMoreResults = false;
        }
        int i = paramParcel.readInt();
        for (int j = 0; ; j++) {
            if (j >= i)
                return;
            String str3 = paramParcel.readString();
            Long localLong = Long.valueOf(paramParcel.readLong());
            String str1 = paramParcel.readString();
            String str2 = paramParcel.readString();
            this.mPublicProfiles.add(new PublicProfile(str3, localLong, str1, str2));
        }
    }

  public void addContact(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
  {
    this.mContacts.add(new Contact(paramString1, paramString2, paramString3, paramString4, paramString5, paramString6));
  }

  public void addGaiaIdAndCircles(Long paramLong, String paramString)
  {
    this.mGaiaIdsAndCircles.put(paramLong, paramString);
  }

  public void addLocalProfile(String paramString1, Long paramLong, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
  {
    if (!paramString1.equals(this.mMyPersonId))
      this.mLocalProfiles.add(new LocalProfile(paramString1, paramLong, paramString2, paramString3, paramString4, paramString5, paramString6));
  }

  public void addPublicProfile(String paramString1, Long paramLong, String paramString2, String paramString3)
  {
    if (!paramString1.equals(this.mMyPersonId))
    {
      this.mPublicProfiles.add(new PublicProfile(paramString1, paramLong, paramString2, paramString3));
      this.mCursorValid = false;
    }
  }

  public int describeContents()
  {
    return 0;
  }

  public int getCount()
  {
    return getCursor().getCount();
  }

    // todo : implement this method.
  public Cursor getCursor()
  {
//    Object localObject1;
//    if (!this.mCursorValid)
//    {
//      this.mCursor = new EsMatrixCursor(PROJECTION);
//      this.mCursorValid = true;
//      if ((this.mLocalProfilesLoaded) && (this.mGaiaIdsAndCirclesLoaded))
//      {
//        localObject1 = new HashSet();
//        HashSet localHashSet = new HashSet();
//        Object localObject3 = this.mLocalProfiles.iterator();
//        while (true)
//        {
//          long l1;
//          if (!((Iterator)localObject3).hasNext())
//          {
//            localObject5 = this.mContacts.iterator();
//            while (true)
//            {
//              if (!((Iterator)localObject5).hasNext())
//              {
//                localObject2 = this.mPublicProfiles.iterator();
//                while (true)
//                {
//                  if (!((Iterator)localObject2).hasNext())
//                  {
//                    localObject3 = this.mPublicProfiles.iterator();
//                    while (true)
//                    {
//                      if (!((Iterator)localObject3).hasNext())
//                      {
//                        localObject1 = this.mCursor;
//                        break;
//                      }
//                      localObject4 = (PublicProfile)((Iterator)localObject3).next();
//                      localObject2 = ((PublicProfile)localObject4).gaiaId;
//                      if (((HashSet)localObject1).contains(localObject2))
//                        continue;
//                      localObject5 = this.mCursor;
//                      arrayOfObject = new Object[11];
//                      l1 = this.mNextId;
//                      this.mNextId = (1L + l1);
//                      arrayOfObject[0] = Long.valueOf(l1);
//                      arrayOfObject[1] = ((PublicProfile)localObject4).personId;
//                      arrayOfObject[2] = null;
//                      arrayOfObject[3] = localObject2;
//                      arrayOfObject[4] = ((PublicProfile)localObject4).name;
//                      arrayOfObject[5] = null;
//                      arrayOfObject[6] = null;
//                      arrayOfObject[7] = null;
//                      arrayOfObject[8] = null;
//                      arrayOfObject[9] = null;
//                      arrayOfObject[10] = ((PublicProfile)localObject4).snippet;
//                      ((EsMatrixCursor)localObject5).addRow(arrayOfObject);
//                    }
//                  }
//                  localObject3 = (PublicProfile)((Iterator)localObject2).next();
//                  localObject5 = ((PublicProfile)localObject3).gaiaId;
//                  localObject4 = (String)this.mGaiaIdsAndCircles.get(localObject5);
//                  if ((((HashSet)localObject1).contains(localObject5)) || (TextUtils.isEmpty((CharSequence)localObject4)))
//                    continue;
//                  ((HashSet)localObject1).add(localObject5);
//                  localObject6 = this.mCursor;
//                  Object[] arrayOfObject = new Object[11];
//                  l1 = this.mNextId;
//                  this.mNextId = (1L + l1);
//                  arrayOfObject[0] = Long.valueOf(l1);
//                  arrayOfObject[1] = ((PublicProfile)localObject3).personId;
//                  arrayOfObject[2] = null;
//                  arrayOfObject[3] = localObject5;
//                  arrayOfObject[4] = ((PublicProfile)localObject3).name;
//                  arrayOfObject[5] = localObject4;
//                  arrayOfObject[6] = null;
//                  arrayOfObject[7] = null;
//                  arrayOfObject[8] = null;
//                  arrayOfObject[9] = null;
//                  arrayOfObject[10] = null;
//                  ((EsMatrixCursor)localObject6).addRow(arrayOfObject);
//                }
//              }
//              localObject3 = (Contact)((Iterator)localObject5).next();
//              if (l1.contains(((Contact)localObject3).name))
//                continue;
//              localObject2 = this.mCursor;
//              localObject4 = new Object[11];
//              l2 = this.mNextId;
//              this.mNextId = (1L + l2);
//              localObject4[0] = Long.valueOf(l2);
//              localObject4[1] = ((Contact)localObject3).personId;
//              localObject4[2] = ((Contact)localObject3).lookupKey;
//              localObject4[3] = null;
//              localObject4[4] = ((Contact)localObject3).name;
//              localObject4[5] = null;
//              localObject4[6] = null;
//              localObject4[7] = ((Contact)localObject3).email;
//              localObject4[8] = ((Contact)localObject3).phoneNumber;
//              localObject4[9] = ((Contact)localObject3).phoneType;
//              localObject4[10] = null;
//              ((EsMatrixCursor)localObject2).addRow(localObject4);
//            }
//          }
//          Object localObject6 = (LocalProfile)((Iterator)localObject3).next();
//          Object localObject2 = ((LocalProfile)localObject6).gaiaId;
//          ((HashSet)localObject1).add(localObject2);
//          l1.add(((LocalProfile)localObject6).name);
//          Object localObject4 = this.mCursor;
//          Object localObject5 = new Object[11];
//          long l2 = this.mNextId;
//          this.mNextId = (1L + l2);
//          localObject5[0] = Long.valueOf(l2);
//          localObject5[1] = ((LocalProfile)localObject6).personId;
//          localObject5[2] = null;
//          localObject5[3] = localObject2;
//          localObject5[4] = ((LocalProfile)localObject6).name;
//          localObject5[5] = ((LocalProfile)localObject6).packedCircleIds;
//          localObject5[6] = ((LocalProfile)localObject6).email;
//          localObject5[7] = null;
//          localObject5[8] = ((LocalProfile)localObject6).phoneNumber;
//          localObject5[9] = ((LocalProfile)localObject6).phoneType;
//          localObject5[10] = null;
//          ((EsMatrixCursor)localObject4).addRow(localObject5);
//        }
//      }
//      localObject1 = this.mCursor;
//    }
//    else
//    {
//      localObject1 = this.mCursor;
//    }
//    return (Cursor)(Cursor)(Cursor)(Cursor)(Cursor)(Cursor)localObject1;
      return null;
  }

  public int getPublicProfileCount()
  {
    return this.mPublicProfiles.size();
  }

  public String getToken()
  {
    return this.mToken;
  }

  public boolean hasMoreResults()
  {
    return this.mHasMoreResults;
  }

  public boolean isParcelable()
  {
    int i;
    if (this.mLocalProfiles.size() + this.mPublicProfiles.size() > 1000)
      i = 0;
    else
      i = 1;
    return i > 0;
  }

  public void onFinishContacts()
  {
  }

  public void onFinishGaiaIdsAndCircles()
  {
    this.mGaiaIdsAndCirclesLoaded = true;
  }

  public void onFinishLocalProfiles()
  {
    this.mLocalProfilesLoaded = true;
  }

  public void onStartContacts()
  {
    this.mContacts.clear();
    this.mCursorValid = false;
  }

  public void onStartGaiaIdsAndCircles()
  {
    this.mCursorValid = false;
  }

  public void onStartLocalProfiles()
  {
    this.mLocalProfiles.clear();
    this.mLocalProfilesLoaded = false;
    this.mCursorValid = false;
  }

  public void setHasMoreResults(boolean paramBoolean)
  {
    this.mHasMoreResults = paramBoolean;
  }

  public void setMyProfile(String paramString)
  {
    this.mMyPersonId = paramString;
  }

  public void setQueryString(String paramString)
  {
    if (!TextUtils.equals(this.mQuery, paramString))
    {
      this.mQuery = paramString;
      this.mLocalProfiles.clear();
      this.mPublicProfiles.clear();
      this.mLocalProfilesLoaded = false;
      this.mCursorValid = false;
      this.mToken = null;
    }
  }

  public void setToken(String paramString)
  {
    this.mToken = paramString;
  }

  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeString(this.mMyPersonId);
    paramParcel.writeString(this.mQuery);
    paramParcel.writeString(this.mToken);
    int i;
    if (!this.mHasMoreResults)
      i = 0;
    else
      i = 1;
    paramParcel.writeInt(i);
    int j = this.mPublicProfiles.size();
    paramParcel.writeInt(j);
    for (int k = 0; ; k++)
    {
      if (k >= j)
        return;
      PublicProfile localPublicProfile = (PublicProfile)this.mPublicProfiles.get(k);
      paramParcel.writeString(localPublicProfile.personId);
      paramParcel.writeLong(localPublicProfile.gaiaId.longValue());
      paramParcel.writeString(localPublicProfile.name);
      paramParcel.writeString(localPublicProfile.snippet);
    }
  }

  private static class PublicProfile extends PeopleSearchResults.Profile
  {
    String snippet;

    PublicProfile(String paramString1, Long paramLong, String paramString2, String paramString3)
    {
      super(paramString1, paramLong, paramString2);
      this.snippet = paramString3;
    }
  }

  private static class Contact extends PeopleSearchResults.Profile
  {
    String email;
    String lookupKey;
    String phoneNumber;
    String phoneType;

    Contact(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    {
      super(paramString1, null, paramString3);
      this.lookupKey = paramString2;
      this.email = paramString4;
      this.phoneNumber = paramString5;
      this.phoneType = paramString6;
    }
  }

  private static class LocalProfile extends PeopleSearchResults.Profile
  {
    String email;
    String packedCircleIds;
    String phoneNumber;
    String phoneType;

    LocalProfile(String paramString1, Long paramLong, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    {
      super(paramString1, paramLong, paramString2);
      this.packedCircleIds = paramString3;
      this.email = paramString4;
      this.phoneNumber = paramString5;
      this.phoneType = paramString6;
    }
  }

  private static abstract class Profile
  {
    Long gaiaId;
    String name;
    String personId;

    Profile(String paramString1, Long paramLong, String paramString2)
    {
      this.personId = paramString1;
      this.gaiaId = paramLong;
      this.name = paramString2;
    }
  }
}
