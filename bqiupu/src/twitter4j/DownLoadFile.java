package twitter4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;


public class DownLoadFile {
	 private static final String TAG = "Qiupu.DownLoadFile";
	 
	 public static boolean dowloadFile(String packagename, String url,String filepath,String filename,long filesize,TwitterListener listener)  throws TwitterException{         
	     Log.d(TAG,"dowloadFile url:"+url+" filepath:"+filepath+" filename:"+filename+" filesize:"+filesize);
	     
		 if(listener != null){
		    listener.startProcess();
         }

		 boolean result = false;
         URL apkurl;
		 try {
			apkurl = new URL(url);
			createDirectory(filepath);
			File finalfile = new File(filepath+File.separator+filename);				
            if(finalfile.exists() == true)
            {
            	finalfile.delete();
            }
            
            finalfile.createNewFile();
            Log.d(TAG,"entering downloadFile==storefilepath="+finalfile.getAbsolutePath());

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                 TrafficStats.setThreadStatsTag(0xB0AD);
            HttpURLConnection conn = null;
            try {
                OutputStream os = new FileOutputStream(finalfile);
                conn = (HttpURLConnection)apkurl.openConnection();    
                if(listener != null)
                    listener.beginDownload(packagename, conn);
                
                conn.setConnectTimeout(15*1000);
                conn.setReadTimeout(60*1000);
                InputStream in =conn.getInputStream();
                
                int len = -1;                
                
                long contentLen = conn.getContentLength();                
                if(contentLen <=0)
                {
                	contentLen = filesize;
                }
                
                byte []buf = new byte[1024*4];
                long processedsize = 0;
                while((len = in.read(buf, 0, 1024*4)) > 0)
                {   
                	os.write(buf, 0, len);
                	processedsize += len;
                	
                	if(listener != null){
                		if(((int)((processedsize/(1.f*contentLen))*100))%10 == 0) {
                			listener.updateProcess(processedsize,contentLen);
                		}
                    }
                }
                
                buf = null;
                in.close();
                os.close();                    
                result = true; 
            } catch(Exception ne) {
            	finalfile.delete();
            	Log.d(TAG,"===download file exception="+ne.getMessage()+"=="+ne.getLocalizedMessage());
                throw new TwitterException(ne.getMessage(),ne);
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    TrafficStats.clearThreadStatsTag();

            	if(listener != null)
            	listener.endDownload(packagename);
            	
                if(conn != null)
                {
                    conn.disconnect();
                }
            }  
		} catch (MalformedURLException e) {
			throw new TwitterException(e.getMessage(),e);
		} catch (IOException e) {
			throw new TwitterException(e.getMessage(),e);
		}
		return result;
    }

    	public static void createDirectory(String dir) throws IOException {
		File file = new File(dir);
		if (file.exists()) {
			if (!file.isDirectory()) {
				String message = "File "
						+ dir
						+ " exists and is not a directory. Unable to create directory";
				throw new IOException(message);
			}
		} else {
			if (!file.mkdirs()) {
				if (!file.isDirectory()) {
					String message = "Unable to create directory " + dir;
					throw new IOException(message);
				}
			}
		}
	}
}
