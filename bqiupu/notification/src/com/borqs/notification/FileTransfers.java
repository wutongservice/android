package com.borqs.notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;




public class FileTransfers {
    
    static final String TAG = "FileTransfer";
    private IFileTransferListener mFileTransferListener;

    public FileTransfers() {
    }

    public FileTransfers(IFileTransferListener listener) {
        mFileTransferListener =  listener;
    }

    public void sendFile(File file,String toUser,Account account) {
        //String url = "http://192.168.5.6/upload_file.php";
        String url = LinxSettings.BORQS_SEND_FILE_URL;
        boolean sent = false;
        if(url == null || file == null){
            try {
                if (mFileTransferListener != null) {
                    mFileTransferListener.onFinished(sent);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify FileTransfer.", e);
            }
            return;
        }
        String fromUser = account.getUserName();
        HttpPostGetFile postfile;
        try {
            postfile = new HttpPostGetFile(url);
            postfile.addFileParameter("file", file);
            postfile.addTextParameter("addordelete", "add");
            postfile.addTextParameter("from", fromUser);
            postfile.addTextParameter("to", toUser);
            postfile.addTextParameter("fromp", account.getPhoneNumber());

            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, postfile.url.toString());
            }
            
            int nResCode = postfile.SendFile();
            sent = (nResCode == 200);
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "SendFile: " + nResCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mFileTransferListener != null) {
                mFileTransferListener.onFinished(sent);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to notify FileTransfer.", e);
        }
    }

    // Download the file from remote server.
    public void getFile(String sentTime, String filename, String fromuser, String fromp){
        String geturl = LinxSettings.BORQS_GET_FILE_URL + sentTime;
        String deletefileurl = LinxSettings.BORQS_DELETE_FILE_URL + sentTime;
        
        if (Constants.LOGD_ENABLED) {
            Log.d(TAG, "geturl:" +  geturl);
        }
        
        HttpPostGetFile getFile;
        boolean fileDownloaded = false;
        Intent intent = new Intent(Constants.BORQS_FILE_NOTIFICATION_INTENT);
        try {    
            getFile = new HttpPostGetFile(geturl); 
            boolean b = getFile.HttpGetFile(filename);
            if(true == b){    //receive
                fileDownloaded = true;
                intent.putExtra("file_name", LinxSettings.DEFAULT_DOWNLOAD_PATH + filename);

                // Remove the file from remote server since we have downloaded it.
                HttpPostGetFile deleteFile;
                deleteFile = new HttpPostGetFile(deletefileurl);
                deleteFile.DeleteDownloadFile();
            } else {
                intent.putExtra("file_name", geturl);
                // TODO: The host app may feel interested in the sent time.
                //intent.putExtra("sent_time", sentTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }  
        
        intent.putExtra("app_id", Constants.APP_ID_XMESSAGE);// TODO: might be other apps!!
        intent.putExtra("status", fileDownloaded);
        intent.putExtra("from", fromuser);
        intent.putExtra("fromp", fromp);
        NotificationService.getInstance().sendBroadcast(intent);                        
    }    
}