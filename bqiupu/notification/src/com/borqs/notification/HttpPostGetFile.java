package com.borqs.notification;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;
import android.util.Log;

    
public class HttpPostGetFile {
    static final String TAG = "FileTransfer";

    URL url;
    URL deletefileurl;
    String uri = "";
    HttpURLConnection httpconnection;  
    String boundary = "--------httppostfile";  
    ArrayList<TextEntity> textParams = new ArrayList<TextEntity>();  
    ArrayList<FileEntity> fileparams= new ArrayList<FileEntity>();   
    DataOutputStream datastream;  
	   
	     class FileEntity{  
	    	 private String name;  
	    	 private File value;  
	    	        
	         public String getName() {  
	             return name;  
	    	 }  
	    	 public void setName(String name) {  
	    	          this.name = name;  
	    	 }  
	    	 public File getValue() {  
	    	         return value;  
	    	 }  
	    	 public void setValue(File value) {  
	    	         this.value = value;  
	    	 }  
	    	 public FileEntity(String name, File value) {  
	    	          super();  
	    	          this.name = name;  
	    	          this.value = value;  
	    	 }  
	     }  
	     class TextEntity{  
	    	 private String name;  
	    	 private String value;  
	    	        
	         public String getName() {  
	             return name;  
	    	 }  
	    	 public void setName(String name) {  
	    	          this.name = name;  
	    	 }  
	    	 public String getValue() {  
	    	         return value;  
	    	 }  
	    	 public void setValue(String value) {  
	    	         this.value = value;  
	    	 }  
	    	 public TextEntity(String name, String value) {  
	    	          super();  
	    	          this.name = name;  
	    	          this.value = value;  
	    	 }  
	     }
         public HttpPostGetFile(String url) throws Exception {  
	         this.url = new URL(url);
	         uri = url;
	         this.deletefileurl = new URL(url); 
	     }  
	     //重新设置要请求的服务器地址  
	     public void setUrl(String url) throws Exception {  
	         this.url = new URL(url); 
	         this.deletefileurl = new URL(url); 
	     }  
	     //增加一个普通字符串数据到form表单数据中  
	     public void addTextParameter(String name, String value) {  
	    	 TextEntity textEntity = new TextEntity(name,value);
	         textParams.add(textEntity);  
	     }  
	     //增加一个文件到Arraylist表单数据中  
	     public void addFileParameter(String name, File value) {  
	    	 FileEntity fileEntity =  new FileEntity(name,value);	 
	    	 fileparams.add(fileEntity);
	     }  
	     // 清空所有已添加的form表单数据  
	     public void clearAllParameters() {  
	         textParams.clear();  
	         fileparams.clear();  
	     }  
	     // 发送数据到服务器，返回一个字节包含服务器的返回结果的数组  
	     public int SendFile() throws Exception {
	         initConnection();
	         try {
	        	 httpconnection.connect();
	         } catch (SocketTimeoutException e) {
	             // something  
	             throw new RuntimeException();
	         }  
	         datastream = new DataOutputStream(httpconnection.getOutputStream());
	         writeFileParams();
	         writeStringParams();
	         paramsEnd(); //结尾
	         InputStream in = httpconnection.getInputStream();
	         
	         ByteArrayOutputStream out = new ByteArrayOutputStream();
	         int b;
	         while ((b = in.read()) != -1) {
	             out.write(b);
	         }
	         
	         int resCode = httpconnection.getResponseCode();
	         if (Constants.LOGD_ENABLED) {
	             InputStream is = httpconnection.getInputStream();
	             byte[] buffer = new byte[102400];
	             int length = is.read(buffer);
	             if(length > 0) {
    	             String result = new String(buffer, 0, length);
    	             Log.d(TAG, "Result: " + result);
	             } else {
	                 Log.d(TAG, "Result: null");
	             }
	         }
	         out.close();
	         in.close();   
	         httpconnection.disconnect(); 
	         return resCode;  
	     }
	     
	     
	     public boolean HttpGetFile(String filename){
		 	 if( !(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) )  //判断sd卡是否存在
	         {                              
		 			return false;
		 	 }
	     	 String filePathName = LinxSettings.DEFAULT_DOWNLOAD_PATH + filename;
	     	 File creatfile = new File(LinxSettings.DEFAULT_DOWNLOAD_PATH);
	     	 try {
				creatfile.mkdirs(); //按照指定的路径创建文件夹
			 } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
	   	     File file = new File(filePathName);
	   	     if (file.exists()){
		   	     try{
		   	    	file.delete();
		   	     }catch(Exception ee){
		   	    	 Log.v("fileTransferRequest","delete:" + "fail");	
		   	     }
	         }
	   	    System.out.println("uri:" +  uri);
	    	 HttpGet httpGet = new HttpGet(uri);
	    	 try
	    	 {
		    	   HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
		    	   int nResponse = httpResponse.getStatusLine().getStatusCode();
		    	   if (200 == nResponse)
		    	   {
			    	    InputStream inStream = httpResponse.getEntity().getContent();
			    	    // 开始下载文件
			    	    FileOutputStream fileStream = new FileOutputStream(filePathName);
			    	    byte[] buffer = new byte[8192];
			    	    int count = 0;
			    	    while ((count = inStream.read(buffer)) != -1)
			    	    {
			    	    	fileStream.write(buffer, 0, count);
			    	    }
			    	    fileStream.close();
			    	    inStream.close();
		    	   } else {
                        return false;
		    	   }
	    	 } catch (Exception e){	
	    		 return false;
	    	 } 
	    	 //Modification to the original FileName
	    	 /*String filePathName2 = sdDir.toString() + "/" + filename;
	   	     File file2 = new File(filePathName2);
		 	 file1.renameTo(file2);	*/   	     
	    	
		 	return true;	    	 
	     }
	     // 发送数据到Openfire serverlet服务器上删除文件
	     public boolean DeleteDownloadFile() throws Exception { 
	    	 System.out.println("DeleteDownloadFile deletefileurl:" +  deletefileurl);
	    	 initdeletefileConnection();  
	        try {  
	        	 httpconnection.connect();
	        	 System.out.println("DeleteDownloadFile deletefileurl:" +  deletefileurl);
	         } catch (SocketTimeoutException e) {  
	             // something  
	             throw new RuntimeException();  
	         } 
	         datastream = new DataOutputStream(httpconnection.getOutputStream());   
	         InputStream in = httpconnection.getInputStream();
	         
	         ByteArrayOutputStream out = new ByteArrayOutputStream();  
	         int b;  
	         while ((b = in.read()) != -1) {  
	             out.write(b);  
	         }
	         
	         out.close();
	         in.close(); 
	    	 int resCode = httpconnection.getResponseCode();
	         httpconnection.disconnect();          
	         return true;
	     }
	     private void initdeletefileConnection() throws Exception {  
	    	 httpconnection = (HttpURLConnection)deletefileurl.openConnection();	    	 
	    	 httpconnection.setDoOutput(true);
	    	 httpconnection.setDoInput(true);
	    	 httpconnection.setUseCaches(false);
	    	 httpconnection.setRequestMethod("POST");// 设置提交方法
	    	 httpconnection.setConnectTimeout(50000);// 连接超时时间
	    	 httpconnection.setReadTimeout(50000);
	     } 
	     
	     //文件上传的connection的一些必须设置  
	     private void initConnection() throws Exception {  
	    	 httpconnection = (HttpURLConnection) this.url.openConnection();  
	    	 httpconnection.setDoOutput(true);  	 
	    	 httpconnection.setUseCaches(false);  
	    	 httpconnection.setConnectTimeout(10000); //连接超时为10秒  
	    	 httpconnection.setRequestMethod("POST"); 
	    	 httpconnection.setRequestProperty("Accept-Encoding", "gzip");  //It is gzip.  
	    	 httpconnection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);	    	   	
	     }   
	    	     
	     //普通字符串数据  
	     private void writeStringParams() throws Exception {  
	    	 for (TextEntity textEntity:textParams) {                      
	             String name = textEntity.getName();  
	             String value = textEntity.getValue();  
	             datastream.writeBytes("--" + boundary + "\r\n");  
	             datastream.writeBytes("Content-Disposition: form-data; name=\"" + name  
	                     + "\"\r\n");  
	             datastream.writeBytes("\r\n");  
	             //datastream.writeBytes(value + "\r\n");  
	             datastream.writeBytes(encode(value) + "\r\n");
	         }
	         
	     } 
	     
	     //文件数据  
	     private void writeFileParams() throws Exception {   
	         for (FileEntity fileEntity:fileparams) {  
                 String name = fileEntity.getName();  
                 File value = fileEntity.getValue();                 
                 datastream.writeBytes("--" + boundary + "\r\n");          
                 datastream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" 
                                        + encode(value.getName()) + "\"\r\n");                 
                 datastream.writeBytes("Content-Type: " + getContentType(value) + "\r\n\r\n"); 
                 datastream.write(getBytes(value), 0, getBytes(value).length);
                 datastream.writeBytes("\r\n"); 
                 //datastream.writeBytes("\r\n--" + boundary + "--" + "\r\n");   	    	 
    	    }
	        System.out.println("datastream:" + datastream); 
	     }  
	     //获取文件的上传类型
	     private String getContentType(File file) throws Exception {  
	           
	        return "application/octet-stream";  // 此行不再细分是否为图片，全部作为application/octet-stream 类型  
	   
	     }
	     
	     //添加结尾数据  
	     private void paramsEnd() throws Exception {  
	    	datastream.writeBytes("--" + boundary + "--" + "\r\n");    
	     }
	     
	     //把文件转换成字节数组  
	     private byte[] getBytes(File file) throws Exception {  
	         FileInputStream in = new FileInputStream(file);  
	         ByteArrayOutputStream out = new ByteArrayOutputStream();  
	         byte[] b = new byte[1024];  
	         int n;  
	         while ((n = in.read(b)) != -1) {  
	             out.write(b, 0, n);  
	         }  
	         in.close();  
	         return out.toByteArray();  
	     }  
	     // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码  
	     private String encode(String value) throws Exception{  
	         return URLEncoder.encode(value, "UTF-8");  
	     }  

}