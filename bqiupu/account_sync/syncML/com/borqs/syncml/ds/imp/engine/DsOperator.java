package com.borqs.syncml.ds.imp.engine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.sync.client.common.Logger;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.common.DataConvert;
import com.borqs.syncml.ds.imp.common.Util;
import com.borqs.syncml.ds.imp.tag.AlertCode;
import com.borqs.syncml.ds.imp.tag.ICmdTag;
import com.borqs.syncml.ds.imp.tag.ITag;
import com.borqs.syncml.ds.imp.tag.StatusValue;
import com.borqs.syncml.ds.imp.tag.TagAdd;
import com.borqs.syncml.ds.imp.tag.TagAlert;
import com.borqs.syncml.ds.imp.tag.TagAnchor;
import com.borqs.syncml.ds.imp.tag.TagChal;
import com.borqs.syncml.ds.imp.tag.TagCred;
import com.borqs.syncml.ds.imp.tag.TagDelete;
import com.borqs.syncml.ds.imp.tag.TagGet;
import com.borqs.syncml.ds.imp.tag.TagItem;
import com.borqs.syncml.ds.imp.tag.TagMap;
import com.borqs.syncml.ds.imp.tag.TagMapItem;
import com.borqs.syncml.ds.imp.tag.TagMeta;
import com.borqs.syncml.ds.imp.tag.TagPut;
import com.borqs.syncml.ds.imp.tag.TagReplace;
import com.borqs.syncml.ds.imp.tag.TagResults;
import com.borqs.syncml.ds.imp.tag.TagSource;
import com.borqs.syncml.ds.imp.tag.TagStatus;
import com.borqs.syncml.ds.imp.tag.TagSync;
import com.borqs.syncml.ds.imp.tag.TagSyncBody;
import com.borqs.syncml.ds.imp.tag.TagSyncHdr;
import com.borqs.syncml.ds.imp.tag.TagTarget;
import com.borqs.syncml.ds.imp.tag.devinfo.TagDevInf;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.IDeviceInfo;
import com.borqs.syncml.ds.protocol.IDsOperator;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.IRequest;
import com.borqs.syncml.ds.protocol.IResponse;
import com.borqs.syncml.ds.protocol.ISyncItem;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;



public class DsOperator implements IDsOperator {
	private static final String TAG = "syncml";
	private IProfile mProfile;
	private IdManager mIdManager;
	private String mServerUrl;
	private String mDeviceId;
	private String mUserName;
	private int mMaxMsgSize;
	//private int mMaxObjSize;

	private IDeviceInfo mDeviceInfo;
	private IDatastore mDatastore;

	private List<TagStatus> mStatus;
	private TagAlert mAlert;
	private TagResults mResult;
	private String mRecMsgId;

	private ISyncListener mListener;
	private int mSyncRequestItem;
	private TagDevInf mServerInf;
	private boolean mIsDeviceFull = false;

	public DsOperator(IProfile profile, int syncRequestItem) {
		mProfile = profile;
		mDeviceInfo = profile.getDeviceInfo();
		mIdManager = new IdManager();
		mServerUrl = profile.getServerUrl();
		mUserName = profile.getUserName();
		mMaxMsgSize = mDeviceInfo.getMaxMsgSize();
		//mMaxObjSize = mDeviceInfo.getMaxObjSize();
		mDeviceId = mDeviceInfo.deviceId();
		mSyncRequestItem = syncRequestItem;

		mStatus = new LinkedList<TagStatus>();
	}

	public void sync(IDatastore datastore) throws DsException, IOException,
			XmlPullParserException {
		mDatastore = datastore;
		mListener = mDatastore.getListener();

		InitializationPhase init = new InitializationPhase();
		SyncPhase sync = new SyncPhase();
		mIdManager.init();

		try {
			init.work();

			if (init.isPass()) {
				sync.work();
			}
		} finally {
			Logger.logD(TAG, "Sync End");	
		}
	}

	private TagSyncHdr getSyncHeader() {
		TagSyncHdr header = new TagSyncHdr();
		header.VerDTD = "1.1";
		header.VerProto = "SyncML/1.1";
		header.SessionID = mIdManager.sessionId();
		header.MsgID = mIdManager.nextMsgId();

		TagTarget target = new TagTarget();
		target.LocURI = mServerUrl;
		header.Target = target;

		TagSource source = new TagSource();
		source.LocURI = mDeviceId;
		source.LocName = mUserName;

		header.Source = source;
		TagMeta meta = new TagMeta();
		meta.MaxMsgSize = Integer.toString(mMaxMsgSize);
		header.Meta = meta;
		return header;
	}

	private void handleSyncHeader(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		TagSyncHdr syncHdr = new TagSyncHdr();
		syncHdr.parse(parser);
		mRecMsgId = syncHdr.MsgID;
		if (!TextUtils.isEmpty(syncHdr.RespURI)) {
			mServerUrl = syncHdr.RespURI;
		}

		TagStatus status = new TagStatus(mIdManager.nextCmdId(), syncHdr.MsgID,
				"0", SyncML.SyncHdr, mServerUrl, mDeviceId, StatusValue.SUCCESS);
		mStatus.add(status);
	}

	private void putStatus(TagSyncBody body) {
		if (mStatus != null) {
			for (TagStatus tag : mStatus) {
				body.addChild(tag);
			}
			mStatus.clear();
		}
	}

	private void putAlert(TagSyncBody body) {
		if (mAlert != null) {
			body.addChild(mAlert);
			mAlert = null;
		}
	}

	private void addStatus(TagStatus tag) {
		mStatus.add(tag);
	}

	private class IdManager {
		private String mSessionId;
		private int mMsgId;
		private int mCmdId;

		public void init() {
			mSessionId = Util.formatDate(System.currentTimeMillis());

			mMsgId = 0;
			mCmdId = 0;
		}

		public String nextMsgId() {
			return Integer.toString(++mMsgId);
		}

		public String sessionId() {
			return mSessionId;
		}

		public String nextCmdId() {
			return Integer.toString(++mCmdId);
		}

		public void resetCmdId() {
			mCmdId = 0;
		}

	}

	private class InitializationPhase {
		private boolean mTryAgain;
		private boolean mPass;

		private boolean mHandleChallenge;
		private boolean mHandledCredit;

		private String mAuthType;
		private byte[] mNextNonce;

		InitializationPhase() {
			mAuthType = "syncml:auth-basic";
		}

		boolean isPass() {
			return mPass;
		}

		public void work() throws DsException, IOException,
				XmlPullParserException {
			mListener.setPhase(ISyncListener.PHASE_INITIALIZATION);
			mListener.pleaseWaiting();
			do {
				mTryAgain = false;

				IRequest request = prepareRequest();

				IResponse response = mProfile.getTransport().post(request);

				handleResponse(response);
				response.close();
			} while (mTryAgain);
		}

		private IRequest prepareRequest() throws IOException {
			SyncRequest request = new SyncRequest(mServerUrl);
			request.begin();
			// HAEDER
			TagSyncHdr header = getSyncHeader();
			// creditor
			// if (mHandleChallenge) {
			addCred(header);
			// }
			request.write(header);
			// BODY
			request.write(getSyncBody());
			request.end();

			return request;
		}

		private ITag getSyncBody() {
			TagSyncBody body = new TagSyncBody();
			putStatus(body);

			if (!mPass) {
				addAlert(body);
			}

			addPut(body);

			addGet(body);

			body.Final = true;

			return body;
		}

		private void addCred(TagSyncHdr header) {
			TagCred tagCred = new TagCred(mProfile);
			tagCred.setType(mAuthType);
			tagCred.setNextNonce(mNextNonce);
			header.Cred = tagCred;
		}

		private void addAlert(TagSyncBody body) {
			TagAlert alert = new TagAlert(mIdManager.nextCmdId(), Integer
					.toString(mDatastore.getSyncMode()));

			TagItem item = new TagItem();
			item.Target = new TagTarget(mDatastore.getServerUri(), null);
			item.Source = new TagSource(mDatastore.getName(), null);

			TagMeta meta = new TagMeta();

			String lastAnchor = null;
			if (mDatastore.getLastAnchor() > 0) {
				lastAnchor = Long.toString(mDatastore.getLastAnchor());
			}

			TagAnchor anchor = new TagAnchor(lastAnchor, Long
					.toString(mDatastore.getNextAnchor()));

			meta.setAnchor(anchor);
			meta.MaxObjSize = Integer.toString(mDeviceInfo.getMaxObjSize());

			item.Meta = meta;
			alert.addItem(item);
			body.addChild(alert);
		}

		private void addPut(TagSyncBody body) {
			TagPut tagPut = new TagPut(mDeviceInfo, mSyncRequestItem);
			tagPut.CmdID = mIdManager.nextCmdId();
			body.addChild(tagPut);
		}

		private void addGet(TagSyncBody body) {
			TagGet tagGet = new TagGet();
			tagGet.CmdID = mIdManager.nextCmdId();
			TagMeta meta = new TagMeta();
			meta.Type = "application/vnd.syncml-devinf+wbxml";
			TagItem item = new TagItem();
			item.Target = new TagTarget("./devinf11", null);
			tagGet.Meta = meta;
			tagGet.addItem(item);

			body.addChild(tagGet);
		}

		private void handleResponse(IResponse response)
				throws XmlPullParserException, IOException, DsException {
			mIdManager.resetCmdId();

			XmlPullParser parser = response.parser();
			// SyncML
			parser.nextTag();
			// SyncHdr
			parser.nextTag();
			handleSyncHeader(parser);
			// SyncBody
			handleSyncBody(parser);

			// SyncML end
			// parser.nextTag();
		}

		private void handleSyncBody(XmlPullParser parser) throws IOException,
				XmlPullParserException, DsException {
			TagSyncBody syncBody = new TagSyncBody();
			syncBody.parse(parser);
			
			if(syncBody.BodyCmds != null){
				for(ITag tag: syncBody.BodyCmds){
					handleCmd(tag);
				}
			}
		}

		public void handleCmd(ITag tag) throws DsException {
			if (SyncML.Alert.equals(tag.name())) {
				handleAlert((TagAlert) tag);
			} else if (SyncML.Status.equals(tag.name())) {
				handleStatus((TagStatus) tag);
			} else if (SyncML.Get.equals(tag.name())) {
				handleGet((TagGet) tag);
			} else if (SyncML.Results.equals(tag.name())) {
				handleResults((TagResults) tag);
			} else {

			}
		}

		private void handleResults(TagResults tag) {
			TagStatus status = new TagStatus(mIdManager.nextCmdId(), mRecMsgId,
					tag.CmdID, SyncML.Results, null, null, StatusValue.SUCCESS);
			addStatus(status);

			mServerInf = tag.getDevInf();
		}

		private void handleGet(TagGet tag) {
			TagStatus status = new TagStatus(mIdManager.nextCmdId(), mRecMsgId,
					tag.CmdID, SyncML.Get, null, null, StatusValue.SUCCESS);
			addStatus(status);

			mResult = new TagResults(mDeviceInfo, mSyncRequestItem);
			mResult.CmdID = mIdManager.nextCmdId();
			mResult.MsgRef = mRecMsgId;
			mResult.CmdRef = tag.CmdID;
		}

		private void handleStatus(TagStatus status) throws DsException {
			if (SyncML.Alert.equals(status.getCmd())) {
				if (StatusValue.NOT_FOUND == status.getStatus()) {
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus(), status.getCmd());
				} else if(StatusValue.OPTIONAL_FEATURE_NOT_SUPPORTED == status.getStatus()){
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus());
				}				

			} else if (SyncML.SyncHdr.equals(status.getCmd())) {
				TagChal chal = status.Chal;
				if (chal != null) {
					TagMeta meta = chal.Meta;
					if (meta != null && meta.NextNonce != null) {
						mNextNonce = DataConvert.decode(meta.Format,
								meta.NextNonce);
					}
				}

				switch (status.getStatus()) {
				case StatusValue.MISSING_CREDENTIALS:
					if (TextUtils.isEmpty(mProfile.getUserName())
							&& TextUtils.isEmpty(mProfile.getPassword())) {
						throw new DsException(DsException.CATEGORY_SYNC_STATUS,
								status.getStatus());
					}

					if (!mHandleChallenge) {
						if (chal != null && chal.Meta != null) {
							mAuthType = chal.Meta.Type;
						}
						mHandleChallenge = true;
						mTryAgain = true;
						return;
					}
					break;
				case StatusValue.INVALID_CREDENTIALS:
					if (!mHandledCredit) {
						if (chal != null && chal.Meta != null) {
							if (TextUtils.equals(mAuthType, chal.Meta.Type)) {
								break;
							}
							mAuthType = chal.Meta.Type;
						}
						mHandledCredit = true;
						mTryAgain = true;
						return;
					}
					break;
				case StatusValue.AUTHENTICATION_ACCEPTED:
					mPass = true;
					break;
				}
				if (!status.isSuccess()) {
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus());
				}
			}
		}

		public void handleAlert(TagAlert alert) {
			TagStatus status = new TagStatus(mIdManager.nextCmdId(), mRecMsgId,
					alert.CmdID, SyncML.Alert, null, null, StatusValue.SUCCESS);

			TagAnchor alertAnchor = alert.getItemMetaAnchor();
			if (alertAnchor != null) {
				TagItem item = new TagItem();
				TagAnchor anchor = new TagAnchor();
				anchor.setNext(alertAnchor.getNext());
				item.setData(anchor);
				status.addItem(item);
			}

			if (alert.Item != null) {
				for (TagItem i : alert.Item) {
					if (i.Target != null) {
						mDatastore.setSyncMode(Integer.parseInt(alert.Data));

						status.addTgtRef(i.Target.LocURI);
					}

					if (i.Source != null) {
						status.addSrcRef(i.Source.LocURI);
					}
				}
			}
			addStatus(status);
		}
	}

	static final class SyncCmdItemStatus {
		String key;
		int status;

		SyncCmdItemStatus(String key, int status) {
			this.key = key;
			this.status = status;
		}
	}

	private class SyncPhase {
		// private boolean mSyncOperation;
		private boolean mSyncFinish;
		private boolean mPutNoc;
		// private boolean mHandledSyncCmd;
		private boolean mGetFinal;
		// private boolean mPutMapping;
		private boolean mTryAgain;
		private LargeObjectDownload mLargeObjectDown;
		private LargeObjectUpload mLargeObjectUp;
		private ISyncItem mNextSyncItem;
		private List<SyncCmdItemStatus> mAddCmdItemStatus;
		private List<SyncCmdItemStatus> mDelCmdItemStatus;
		private List<SyncCmdItemStatus> mReplaceCmdItemStatus;
		
		private static final String TAG_ENCODING = "UTF-8";

		private void prepareSync() throws DsException {
			mProfile.checkCancelSync();
			mDatastore.prepareSync(mServerInf);
			mListener.setLocalNoc(mDatastore.getNoc());
		}

		public void work() throws DsException, IOException,
				XmlPullParserException {
			mListener.setPhase(ISyncListener.PHASE_DEVICE_PREPARE);
			mListener.pleaseWaiting();
			prepareSync();
			mListener.setPhase(ISyncListener.PHASE_SYNC_DEVICE_TO_SERVER);
			mListener.pleaseWaiting();
			// sync
			do {
				mTryAgain = false;
				IRequest request = prepareRequest();
				IResponse response = mProfile.getTransport().post(request);
				handleResponse(response);
				response.close();
			} while (mTryAgain || !mGetFinal);
			
			if(mIsDeviceFull){
				throw new SQLiteException();
			}
		}

		private IRequest prepareRequest() throws IOException, DsException {
			SyncRequest request = new SyncRequest(mServerUrl);
			request.begin();
			// HAEDER
			TagSyncHdr header = getSyncHeader();

			request.write(header);
			// BODY
			request.write(getSyncBody(header.size(TAG_ENCODING)));
			request.end();

			return request;
		}

		private ITag getSyncBody(int wbxmlHeaderSize) throws DsException {
			TagSyncBody body = new TagSyncBody();

			putStatus(body);
			putAlert(body);

			if (mResult != null) {
				body.addChild(mResult);
				mResult = null;
			}

			putMappings(body);
			putSync(wbxmlHeaderSize, body);
			if (mGetFinal) {
				body.Final = true;
			}
			return body;
		}

		private void putSync(int wbxmlHeaderSize, TagSyncBody body) throws DsException {
			if (mSyncFinish) {
				return;
			}

			TagSync tagSync = new TagSync();
			tagSync.CmdID = mIdManager.nextCmdId();
			tagSync.Target = new TagTarget(mDatastore.getServerUri(), null);
			tagSync.Source = new TagSource(mDatastore.getName(), null);

			if (!mPutNoc) {
				tagSync.NumberOfChanges = Integer.toString(mDatastore.getNoc());
				mPutNoc = true;
			}

			try {
				int wbxmlCurFileSize = wbxmlHeaderSize+ body.size(TAG_ENCODING) +  tagSync.size(TAG_ENCODING); 
				putSyncItem(tagSync, wbxmlCurFileSize);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			body.addChild(tagSync);

			if (mSyncFinish) {
				body.Final = true;
			}
		}

		private static final int TAG_CODE_SIZE = 200;

		private void putSyncItem(TagSync sync, int wbxmlFileSize) throws DsException, UnsupportedEncodingException {
			ICmdTag lastCmd = null;
			int wbxmlTotalSize = wbxmlFileSize;
			do {
				mProfile.checkCancelSync();
				if (mLargeObjectUp != null) {
					if (!mLargeObjectUp.isEnd()) {
						mLargeObjectUp.upload(sync);
						break;
					} else {
						mLargeObjectUp = null;
					}
				}

				if (mNextSyncItem != null) {
					if (LargeObjectUpload.isLargeObject(mNextSyncItem
							.getContent(), mMaxMsgSize)) {
						mLargeObjectUp = new LargeObjectUpload(mMaxMsgSize);
						mLargeObjectUp.setSyncItem(mNextSyncItem);
						mLargeObjectUp.setTagCmdId(mIdManager.nextCmdId());
						mNextSyncItem = null;
						mLargeObjectUp.upload(sync);
						break;
					} else {
						ICmdTag tag = mNextSyncItem.getCmdTag(lastCmd);
						wbxmlTotalSize += mNextSyncItem.getSize(TAG_ENCODING);
						mListener.handleLocal();

						if (tag != lastCmd) {
							lastCmd = tag;
							lastCmd.setCmdId(mIdManager.nextCmdId());
							sync.addSyncCmd(lastCmd);
						}
					}
				}
				mNextSyncItem = mDatastore.getNextSyncItem();

				// No item for this source
				if (mNextSyncItem == null) {
					mSyncFinish = true;
					break;
				}

				if (mNextSyncItem.getSize(TAG_ENCODING) + wbxmlTotalSize + TAG_CODE_SIZE > mMaxMsgSize) {
					break;
				}
			} while (true);
		}

		private void putMappings(TagSyncBody body) {
			List<TagMapItem> mappings = mDatastore.mappings();
			if (mappings != null && mappings.size() > 0) {
				TagMap map = new TagMap(mIdManager.nextCmdId(), new TagTarget(
						mDatastore.getServerUri(), null), new TagSource(
						mDatastore.getName(), null), null, null, mappings);
				body.addChild(map);
			}
		}

		private void handleResponse(IResponse response)
				throws XmlPullParserException, IOException, DsException {
			mIdManager.resetCmdId();

			XmlPullParser parser = response.parser();
			// SyncML
			parser.nextTag();
			// SyncHdr
			parser.nextTag();
			handleSyncHeader(parser);
			// SyncBody
			handleSyncBody(parser);

			// SyncML end
			// parser.nextTag();
		}

		private void handleSyncBody(XmlPullParser parser) throws IOException,
				XmlPullParserException, DsException {
			TagSyncBody syncBody = new TagSyncBody();
			syncBody.parse(parser);
			
			if(syncBody.BodyCmds != null){
				for(ITag tag: syncBody.BodyCmds){
					handleCmd(tag);
				}
			}

			
			mGetFinal = syncBody.Final;
			
			//execute on serveradd/update/delete
			mDatastore.onHandleSyncBodyEnd();
		}

		public void handleCmd(ITag tag) throws DsException {
			if (SyncML.Status.equals(tag.name())) {
				handleStatus((TagStatus) tag);
			} else if(SyncML.Sync.equals(tag.name())){	
				handleSync((TagSync)tag);
			} else {

			}
		}

		public void handleSync(TagSync tagSync) throws DsException {
			mTryAgain = true;
			mListener.setPhase(ISyncListener.PHASE_SYNC_SERVER_TO_CLIENT);
			mListener.pleaseWaiting();

			TagStatus status = new TagStatus(mIdManager.nextCmdId(), mRecMsgId,
					tagSync.CmdID, SyncML.Sync, tagSync.Target.LocURI,
					tagSync.Source.LocURI, StatusValue.SUCCESS);
			addStatus(status);

			List<TagAdd> adds = new ArrayList<TagAdd>();
			List<TagDelete> deletions = new ArrayList<TagDelete>();

			if (tagSync.mSyncCmds != null) {
				for (ITag tag : tagSync.mSyncCmds) {
					if (SyncML.Add.equals(tag.name())) {
						TagAdd add = (TagAdd) tag;
						if (mDatastore.supportBatchAdd()) {
							// handle large object
							TagAdd addToBatch = new TagAdd();
							if (add.items != null && add.items.size() > 0) {
								for (TagItem item : add.items) {
									if (!handleLargeObjectDown(ISyncItem.CMD_ADD,
											add.Meta, add.CmdID, item)) {
										addToBatch.addItem(item);
									}									
								}
								addToBatch.CmdID = add.CmdID;
								addToBatch.Cred = add.Cred;
								addToBatch.Meta = add.Meta;
								addToBatch.NoResp = add.NoResp;
								// add TagAdd to batch operations
								if (addToBatch.items != null && addToBatch.items.size() > 0) {
									adds.add(addToBatch);
								}
							}
						} else {
							if (add.items != null) {
								for (TagItem item : add.items) {
									handleAddItem(add, item);
								}
							}
							handleAddItemEnd(add);
						}
					} else if (SyncML.Delete.equals(tag.name())) {
						TagDelete del = (TagDelete) tag;
						if (mDatastore.supportBatchDelete()) {
							deletions.add(del);
						} else {
							if (del.items != null) {
								for (TagItem item : del.items) {
									handleDeleteItem(del, item);
								}
							}
							handleDeleteItemEnd(del);
						}
					} else if (SyncML.Replace.equals(tag.name())) {
						TagReplace replace = (TagReplace) tag;
						if (replace.items != null) {
							for (TagItem item : replace.items) {
								handleReplaceItem(replace, item);
							}
						}
						handleReplaceItemEnd(replace);
					} else {

					}
				}
			}

			if (adds.size() > 0) {
				int[] st = mDatastore.addItems(adds);
				int i = 0;
				for (TagAdd add : adds) {
					for (TagItem item : add.items) {
						addAddCmdItemStatus(item, st[i]);
						if(st[i] == StatusValue.DEVICE_FULL){
							mIsDeviceFull = true;
						}
						i++;
					}
					handleAddItemEnd(add);
				}
			}

			if (deletions.size() > 0) {
				int[] st = mDatastore.deleteItems(deletions);
				int i = 0;
				for (TagDelete del : deletions) {
					for (TagItem item : del.items) {
						addDelCmdItemStatus(item, st[i]);
						i++;
					}
					handleDeleteItemEnd(del);
				}
			}
		}

		private void handleStatus(TagStatus status) throws DsException {
			// Check status to SyncHdr and Sync
			if (SyncML.SyncHdr.equals(status.getCmd())
					|| SyncML.Sync.equals(status.getCmd())) {
				if (!status.isSuccess()) {
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus());
				}
			} else if (SyncML.Add.equals(status.getCmd())
					|| SyncML.Delete.equals(status.getCmd())
					|| SyncML.Replace.equals(status.getCmd())) {
				mDatastore.handleSyncingItemStatus(status);
				if (!status.isSuccess()) {
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus());
				}
			} else if (SyncML.Map.equals(status.getCmd())) {
				if (status.isSuccess()) {
					mDatastore.delMap();
				} else if (status.getStatus() == StatusValue.OPTIONAL_FEATURE_NOT_SUPPORTED) {
					mDatastore.delMap();
				} else {
					throw new DsException(DsException.CATEGORY_SYNC_STATUS,
							status.getStatus());
				}
			} else if (!status.isSuccess()) {
				// throw new DsException(DsException.CATEGORY_SYNC_STATUS,
				// status
				// .getStatus());
			}
		}

		private boolean handleLargeObjectDown(int cmd, TagMeta meta,
				String cmdId, TagItem item) {
			// Handle large object. Check same item
			if (mLargeObjectDown != null && !mLargeObjectDown.isSameItem(item)) {
				mAlert = new TagAlert();
				mAlert.CmdID = mIdManager.nextCmdId();
				mAlert.Data = Integer
						.toString(AlertCode.ALERT_CODE_NO_END_OF_DATA);

				TagItem alertItem = new TagItem();
				TagSource source = new TagSource();
				source.LocURI = mLargeObjectDown.getSrcLocUri();
				alertItem.Source = source;
				mAlert.addItem(alertItem);
				mLargeObjectDown = null;
			}

			if (item.MoreData) {
				// handle large object
				if (mLargeObjectDown == null) {
					mLargeObjectDown = new LargeObjectDownload(cmd, item, meta);
				} else {
				}
				mLargeObjectDown.addItem(item);

				TagStatus status = new TagStatus(mIdManager.nextCmdId(),
						mRecMsgId, cmdId, cmd == ISyncItem.CMD_ADD ? SyncML.Add
								: SyncML.Replace, item.getSrcLocUri(), null,
						StatusValue.CHUNKED_ITEM_ACCEPTED_AND_BUFFERED);
				mStatus.add(status);
				return true;
			} else {
				if (mLargeObjectDown != null) {
					mLargeObjectDown.addItem(item);

					if (!mLargeObjectDown.isSizeMatch()) {
						TagStatus status = new TagStatus(
								mIdManager.nextCmdId(), mRecMsgId, cmdId,
								cmd == ISyncItem.CMD_ADD ? SyncML.Add
										: SyncML.Replace, item.getSrcLocUri(),
								null, StatusValue.SIZE_MISMATCH);
						mStatus.add(status);
						mLargeObjectDown = null;
						return true;
					}

					item.setData(mLargeObjectDown.getFullData());
					mLargeObjectDown = null;
				}
			}
			return false;
		}

		void addAddCmdItemStatus(TagItem item, int status){
			if (mAddCmdItemStatus == null) {
				mAddCmdItemStatus = new LinkedList<SyncCmdItemStatus>();
			}
			mAddCmdItemStatus.add(new SyncCmdItemStatus(item.getSrcLocUri(),
					status));
		}
		public void handleAddItem(TagAdd tagAdd, TagItem item)
				throws DsException {
			mProfile.checkCancelSync();

			if (handleLargeObjectDown(ISyncItem.CMD_ADD, tagAdd.Meta,
					tagAdd.CmdID, item)) {
				return;
			}

			int status = mDatastore.addItem(item);
			if(status == StatusValue.DEVICE_FULL){
				mIsDeviceFull = true;
			}
			addAddCmdItemStatus(item, status);
//			PerformanceLog.event("In DsOperator. handleAddItem exit");

			// TagStatus preStatus = lastStatus();
			// if (preStatus != null &&
			// preStatus.getCmdRef().equals(tagAdd.CmdID)
			// && preStatus.getStatus() == status) {
			// preStatus.addSrcRef(item.getSrcLocUri());
			// } else {
			// TagStatus newStatus = new TagStatus(mIdManager.nextCmdId(),
			// mRecMsgId, tagAdd.CmdID, SyncML.Add, item
			// .getSrcLocUri(), null, status);
			// addStatus(newStatus);
			// }
		}

		public void handleReplaceItem(TagReplace tagReplace, TagItem item)
				throws DsException {
			mProfile.checkCancelSync();

			if (handleLargeObjectDown(ISyncItem.CMD_REPLACE, tagReplace.Meta,
					tagReplace.CmdID, item)) {
				return;
			}

			int status = mDatastore.replaceItem(item);
			if(status == StatusValue.DEVICE_FULL){
				mIsDeviceFull = true;
			}
			if (mReplaceCmdItemStatus == null) {
				mReplaceCmdItemStatus = new LinkedList<SyncCmdItemStatus>();
			}
			mReplaceCmdItemStatus.add(new SyncCmdItemStatus(
					item.getTarLocUri(), status));
			// TagStatus preStatus = lastStatus();
			// if (preStatus != null
			// && preStatus.getCmdRef().equals(tagReplace.CmdID)
			// && preStatus.getStatus() == status) {
			// preStatus.addTgtRef(item.getTarLocUri());
			// } else {
			// TagStatus newStatus = new TagStatus(mIdManager.nextCmdId(),
			// mRecMsgId, tagReplace.CmdID, SyncML.Replace, null, item
			// .getTarLocUri(), status);
			// addStatus(newStatus);
			// }
		}

		void addDelCmdItemStatus(TagItem item, int status){
			if (mDelCmdItemStatus == null) {
				mDelCmdItemStatus = new LinkedList<SyncCmdItemStatus>();
			}
			mDelCmdItemStatus.add(new SyncCmdItemStatus(item.getTarLocUri(),
					status));
		}

		public void handleDeleteItem(TagDelete tagDelete, TagItem item)
				throws DsException {
			int status = mDatastore.deleteItem(item);
			addDelCmdItemStatus(item, status);
			// TagStatus preStatus = lastStatus();
			// if (preStatus != null
			// && preStatus.getCmdRef().equals(tagDelete.CmdID)
			// && preStatus.getStatus() == status) {
			// preStatus.addTgtRef(item.getTarLocUri());
			// } else {
			// TagStatus newStatus = new TagStatus(mIdManager.nextCmdId(),
			// mRecMsgId, tagDelete.CmdID, SyncML.Delete, null, item
			// .getTarLocUri(), status);
			// addStatus(newStatus);
			// }
		}

		public void handleAddItemEnd(TagAdd tagAdd) {
			if (mAddCmdItemStatus != null) {
				TagStatus newStatus = null;
//				boolean sameStatus = true;
				for (SyncCmdItemStatus item : mAddCmdItemStatus) {
					if (newStatus == null) {
						newStatus = new TagStatus(mIdManager.nextCmdId(),
								mRecMsgId, tagAdd.CmdID, SyncML.Add, item.key,
								null, item.status);
					} else if (item.status == newStatus.getStatus()) {
						newStatus.addSrcRef(item.key);
					} else {
						addStatus(newStatus);
						newStatus = new TagStatus(mIdManager.nextCmdId(),
								mRecMsgId, tagAdd.CmdID, SyncML.Add, item.key,
								null, item.status);
//						sameStatus = false;
					}
				}
				if (newStatus != null) {
//					if (sameStatus) {
//						newStatus.mSourceRefs = null;
//					}
					addStatus(newStatus);
				}
				mAddCmdItemStatus.clear();
				mAddCmdItemStatus = null;
			}
		}

		private void putSyncCmdStatus(List<SyncCmdItemStatus> itemStatus,
				String Cmd, String CmdID) {
			if (itemStatus != null) {
				TagStatus newStatus = null;
//				boolean sameStatus = true;
				for (SyncCmdItemStatus item : itemStatus) {
					if (newStatus == null) {
						newStatus = new TagStatus(mIdManager.nextCmdId(),
								mRecMsgId, CmdID, Cmd, null, item.key,
								item.status);
					} else if (item.status == newStatus.getStatus()) {
						newStatus.addTgtRef(item.key);
					} else {
						addStatus(newStatus);
						newStatus = new TagStatus(mIdManager.nextCmdId(),
								mRecMsgId, CmdID, Cmd, null, item.key,
								item.status);
//						sameStatus = false;
					}
				}
				if (newStatus != null) {
//					if (sameStatus) {
//						newStatus.mTargetRefs = null;
//					}
					addStatus(newStatus);
				}
				itemStatus.clear();
			}
		}

		public void handleDeleteItemEnd(TagDelete tagDelete) {
			putSyncCmdStatus(mDelCmdItemStatus, SyncML.Delete, tagDelete.CmdID);
		}

		public void handleReplaceItemEnd(TagReplace tagReplace) {
			putSyncCmdStatus(mReplaceCmdItemStatus, SyncML.Replace,
					tagReplace.CmdID);
		}
	}
}
