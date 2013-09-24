package com.borqs.sync.client.vdata.card;

import java.util.List;

import com.borqs.pim.jcontact.JContact;


public class VcardOperator {
	static private final String TAG = "VcardOperator";
	private static List<ContactStruct> mContactStructList = null;

	/**
	 * 
	 * @param contact
	 * @param version
	 * @param compose
	 * @param hasPref
	 * @param isContainPhoto
	 * @param onlyContainFGname true:only support FamilyName and GivenName in N property(For PIM)
	 *  false:support Middle,Pre,Suffix Name
	 * @return
	 */
	public static String create(ContactStruct contact) {
		try {
			JContactConverter jch = new JContactConverter();
			return jch.convertToJson(contact);
		} catch (Exception e) {
		    e.printStackTrace();
			return null;
		}
	}
	/**
	 * Parser a vcard string and return a ContactStruct list
	 * 
	 * @param data
	 * @return a list of ContactStruct
	 */
	public static List<ContactStruct> parseList(String data, int vcardVersion) {
		return parseList(data.getBytes(), vcardVersion);
	}

	public static List<ContactStruct> parseList(byte[] data, int vcardVersion) {
//		VCardDataParser mParser = new VCardDataParser();
//		try {
//			switch(vcardVersion){
//			case VCardDataParser.VERSION_VCARD21_INT:
//				//oms26 2011-4-2,resource :VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8
//				VCardDataBuilder v21builder = new VCardDataBuilder(VCardConfig.VCARD_TYPE_V21_GENERIC);
//				v21builder.addEntryHandler(mHandler);
//				mParser.parseVcard21(new ByteArrayInputStream(data), v21builder);
//				break;
//			case VCardDataParser.VERSION_VCARD30_INT:
//				//oms26 2011-4-2,resource :VCardConfig.VCARD_TYPE_V30_GENERIC_UTF8
//				VCardDataBuilder v30builder = new VCardDataBuilder(VCardConfig.VCARD_TYPE_V30_GENERIC);
//				v30builder.addEntryHandler(mHandler);
//				mParser.parseVcard30(new ByteArrayInputStream(data), v30builder);
//				break;
//			default:
//				VCardDataBuilder builder = new VCardDataBuilder(VCardDataParser.VERSION_UNKNOWN_INT);
//				builder.addEntryHandler(mHandler);
//				mParser.parse(new ByteArrayInputStream(data), builder);
//				break;
//			}
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage(), e);
//			return null;
//		} catch (Throwable e) {
//			Log.e(TAG, e.getMessage(), e);
//			return null;
//		}
		return mContactStructList;
	}

	public static ContactStruct parse(String data, int vcardVersion) {
		return parse(data.getBytes(), vcardVersion);
	}
	
	public static ContactStruct parse(byte[] data, int vcardVersion) {
		JContact jContact = JContact.fromJsonString(new String(data));
		JContactConverter jch = new JContactConverter();
		return jch.convertToContactStruct(jContact);
	}
	
  
    private static String listToString(List<String> list){
        final int size = list.size();
        if (size > 1) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String type : list) {
                builder.append(type);
                if (i < size - 1) {
                    builder.append(";");
                }
            }
            return builder.toString();
        } else if (size == 1) {
            return list.get(0);
        } else {
            return "";
        }
    }
}
