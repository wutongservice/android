//package com.borqs.qiupu;
//
//import com.igexin.sdk.Consts;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//
//
//public class GexinSdkMsgReceiver extends BroadcastReceiver {
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		Bundle bundle = intent.getExtras();
//		Log.d("GexinSdkDemo", "onReceive() action=" + bundle.getInt("action"));
//		switch (bundle.getInt(Consts.CMD_ACTION)) {
//
//		case Consts.GET_MSG_DATA:
//			// 获取透传数据
//			// String appid = bundle.getString("appid");
//			byte[] payload = bundle.getByteArray("payload");
//
//			if (payload != null) {
//				String data = new String(payload);
//
//				Log.d("GexinSdkDemo", "Got Payload:" + data);
//				/*
//				if (GexinSdkDemoActivity.tLogView != null)
//					GexinSdkDemoActivity.tLogView.append(data + "\n");
//				*/
//
//				Toast.makeText(context, "data: "+data, Toast.LENGTH_LONG).show();
//			}
//
//			break;
//		case Consts.GET_CLIENTID:
//			// 获取ClientID(CID)
//			// 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
//			String cid = bundle.getString("clientid");
//
//			//Toast.makeText(context, "clientid: "+cid, Toast.LENGTH_LONG).show();
//			/*
//			if (GexinSdkDemoActivity.tView != null)
//				GexinSdkDemoActivity.tView.setText(cid);
//		    */
//			break;
//
//		case Consts.BIND_CELL_STATUS:
//			String cell = bundle.getString("cell");
//
//			Log.d("GexinSdkDemo", "BIND_CELL_STATUS:" + cell);
//
//			//Toast.makeText(context, "BIND_CELL_STATUS: "+cell, Toast.LENGTH_LONG).show();
//			/*
//			if (GexinSdkDemoActivity.tLogView != null)
//				GexinSdkDemoActivity.tLogView.append("BIND_CELL_STATUS:" + cell + "\n");
//			*/
//			break;
//		default:
//			break;
//		}
//	}
//}
//
