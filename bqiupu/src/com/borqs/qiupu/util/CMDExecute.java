package com.borqs.qiupu.util;

import java.io.File;
import java.io.InputStream;

public class CMDExecute {

	public synchronized String run(String[] cmd,String workdirectory){
		String result = "";
		try{
			ProcessBuilder builder = new ProcessBuilder(cmd);
			InputStream in = null;
			if(workdirectory != null)
			{
				builder.directory(new File(workdirectory));
				builder.redirectErrorStream(true);
                Process process = builder.start();
                in = process.getInputStream();
                byte[] re = new byte[1024];
                while(in.read(re) != -1)
                {
                	result = result + new String(re);
                }
			}
			if(in != null)
			{
				in.close();
			}
		}
		catch(Exception exs)
		{
			exs.printStackTrace();
		}
		return result;
	}
}
