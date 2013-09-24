package com.borqs.syncml.ds.imp.tag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.xml.SyncmlXml;
import com.borqs.syncml.ds.xml.SyncmlXml.MetInf;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;
import com.borqs.util.android.Base64;


//<!-- Credentials -->
//<!ELEMENT Cred (Meta?, Data)>
public class TagCred implements ITag{
	private IProfile mProfile;
	private String mType;

	private byte[] mNextNonce;

	public void setType(String type) {
		mType = type;
	}

	public TagCred(IProfile profile) {
		mProfile = profile;
	}

	public TagCred(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parse(parser);
	}

	public void parse(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		SyncmlXml.bypassTag(parser);
	}

	public void put(XmlSerializer writer) throws IOException {
		writer.startTag(null, SyncML.Cred);
		String name = mProfile.getUserName();
		String sessionId = mProfile.getPassword();
		

		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		if (name != null) {
			byteOutput.write(name.getBytes("UTF-8"));
		}
		byteOutput.write(':');
		if (sessionId != null) {
			byteOutput.write(sessionId.getBytes("UTF-8"));
		}

		if ("syncml:auth-md5".equals(mType)) { // md5
			// String nonce = mProfile.getNonce();
			// if (mNextNonce != null) {
			// byteOutput.write(':');
			// byteOutput.write(DataConvert.decode("b64", mNextNonce));
			// } else if (!TextUtils.isEmpty(nonce)) {
			// byteOutput.write(':');
			// byteOutput.write(DataConvert.decode("b64", nonce));
			// }

			// byte[] login = byteOutput.toByteArray();

			writer.startTag(null, SyncML.Meta);
			SyncmlXml.putTagText(writer, MetInf.Format, "b64");
			SyncmlXml.putTagText(writer, MetInf.Type, "syncml:auth-md5");
			writer.endTag(null, SyncML.Meta);

			// byte[] loginStringByte;
			// try {
			// MessageDigest digest = java.security.MessageDigest
			// .getInstance("MD5");
			// loginStringByte = digest.digest(login);
			// } catch (NoSuchAlgorithmException e) {
			// loginStringByte = login;
			// }

			String b64login = new String(createMD5Credit(name, sessionId,
					mNextNonce));
			SyncmlXml.putTagText(writer, SyncML.Data, b64login);
		} else if ("syncml:auth-basic".equals(mType)) { // basic
			byte[] login = byteOutput.toByteArray();
			writer.startTag(null, SyncML.Meta);
			SyncmlXml.putTagText(writer, MetInf.Format, "b64");
			SyncmlXml.putTagText(writer, MetInf.Type, "syncml:auth-basic");
			writer.endTag(null, SyncML.Meta);

			String b64login = new String(Base64.encode(login, Base64.DEFAULT));
			SyncmlXml.putTagText(writer, SyncML.Data, b64login);
		} else {
			// TODO:Handle other type
		}

		writer.endTag(null, SyncML.Cred);
	}

	public void setNextNonce(byte[] nextNonce) {
		mNextNonce = nextNonce;
	}

	private byte[] createMD5Credit(String name, String passwd, byte[] nonce) {
		// B64(MD5( B64(MD5("username":"password")):"clientNonce" ))
		String login = name + ":" + passwd;
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			byte[] loginMd5 = digest.digest(login.getBytes());
			byte[] loginMd5B64 = Base64.encode(loginMd5, Base64.DEFAULT);

			byte[] fullB64 = new byte[loginMd5B64.length + 1 + nonce.length];
			System.arraycopy(loginMd5B64, 0, fullB64, 0, loginMd5B64.length);
			fullB64[loginMd5B64.length] = (byte) ':';
			System.arraycopy(nonce, 0, fullB64, loginMd5B64.length + 1,
					nonce.length);
			return Base64.encode(digest.digest(fullB64), Base64.DEFAULT);

		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public String name() {
		return SyncML.Cred;
	}

	public int size(String encode) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 0;
		
	}
}
