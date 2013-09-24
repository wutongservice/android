package com.borqs.account.login.provider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.login.util.BLog;

/**
 * Created with IntelliJ IDEA.
 * User: b251
 * Date: 12-7-28
 * Time: 下午5:23
 */
class FileStore {
    private static final String FILE_NAME = "account.data";
    private Context mContext;
    private static HashMap<String, String> mValues = new HashMap<String, String>();

    public FileStore(Context context){
        mContext = context;
    }

    public HashMap<String, String> getAll(){        
        if (mValues.isEmpty()){
            BLog.d("file get all 2");
            Properties data = loadData();
            Set<Entry<Object, Object>> sets = data.entrySet();
            if (sets != null){
                Iterator<Entry<Object, Object>> it = sets.iterator();
                while (it.hasNext()){
                    Entry<Object, Object> et = it.next();
                    mValues.put(et.getKey().toString(), et.getValue().toString());
                }
            }
        }
        
        return mValues;
    }
    
    public String get(String key, String defaultV){
        /*Properties data = loadData();
        String value = data.getProperty(key);
        if(TextUtils.isEmpty(value)){
            value = defaultV;
        }*/
        if (!mValues.containsKey(key)){
            getAll();
        }        
        String value = mValues.get(key);
        if(TextUtils.isEmpty(value)){
            value = defaultV;
        }
        BLog.d("file get result:" + key+", " + value);        
        return value;
    }

    public void clean(){
        BLog.d("file clean");
        try {
            throw new Exception("file clean");
        } catch (Exception e){
            BLog.d("file clean exp:" + Log.getStackTraceString(e));
        }
        mValues.clear();
        Properties data = new Properties();
        saveData(data);
    }
    
    public void set(String key, String value){
        BLog.d("file set:" + key+", " + value);
        if (key.equals("borqs_session") && TextUtils.isEmpty(value)){
            try {
                throw new Exception("file set session null");
            } catch (Exception e){
                BLog.d("file set session null:" + Log.getStackTraceString(e));
            }
        }
        mValues.remove(key);        
        mValues.put(key, value);
        Properties data = loadData();
        data.setProperty(key, value);
        saveData(data);
    }

    private Properties loadData(){
        Properties data = new Properties();
        InputStream input = null;
        try {
            input = mContext.openFileInput(FILE_NAME);
            data.load(input);
        } catch (FileNotFoundException e){
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(input != null){
                try { input.close();} catch (IOException e) { }
            }
        }
        return data;
    }
    
    private void saveData(Properties data){
        OutputStream output = null;
        try{
            output = mContext.openFileOutput(FILE_NAME, 0);
            data.store(output, "Account data");
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(output != null){
                try { output.flush();output.close();} catch (IOException e) { }
            }
        }
    }
}
