package com.urqa.common;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.urqa.Collector.ErrorReport;
import com.urqa.common.JsonObj.IDInstance;

public class SendErrorProcessURLConnection extends Thread{
	
	static private String		   boundary = "abcdefg123";
	
	private ErrorReport			   report;
	private String 		   		   url;
	private String				   filename;
	
	
	public SendErrorProcessURLConnection(ErrorReport report,String url,String FileName) 
	{
		// TODO Auto-generated constructor stub
		this.report = report; 
		this.url = url;
		this.filename = FileName;
	}
	
	@Override
	public void run()
	{
		IDInstance idinstance = null;

			
			
			try {
				Gson gson = new Gson();
				
				HttpClient client = new DefaultHttpClient();
				String nativeurl = StateData.ServerAddress + url;
				HttpPost post = new HttpPost(nativeurl);
				
				post.setHeader("Content-Type", "application/json; charset=utf-8");
				client.getParams().setParameter("http.protocol.expect-continue", false);
				client.getParams().setParameter("http.connection.timeout", 5000);
				client.getParams().setParameter("http.socket.timeout", 5000);
				
				String test = gson.toJson(report.ErrorData);
				StringEntity input = new StringEntity(test,"UTF-8");

				post.setEntity(input);
				HttpResponse responsePOST = client.execute(post);
				HttpEntity resEntity = responsePOST.getEntity();
				
				if(StateData.TransferLog == false)
					return;

				String jsondata = "";
				try {
					jsondata = EntityUtils.toString(resEntity);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 idinstance =gson.fromJson(jsondata, IDInstance.class);

				try {
				   HttpClient logclient = new DefaultHttpClient();
				   
				   HttpPost logpost = new HttpPost( StateData.ServerAddress + 
						   							"client/send/exception/log/"+ 
						   							idinstance.idinstance);

				   logclient.getParams().setParameter("http.protocol.expect-continue", false);
				   logclient.getParams().setParameter("http.connection.timeout", 5000);
				   logclient.getParams().setParameter("http.socket.timeout", 5000);
				   
				   // 1. 파일의 내용을 body 로 설정함 
				   logpost.setHeader("Content-Type", "text/plain; charset=utf-8");
				   StringEntity entity = new StringEntity(report.LogData, "UTF-8");
				   logpost.setEntity(entity);
				   
				   
				   HttpResponse response = logclient.execute(logpost);
				  } catch (Exception e) {
						e.printStackTrace();
					}
						
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			
			
			try {
				   HttpClient dumpclient = new DefaultHttpClient();
				   
				   HttpPost dumppost = new HttpPost( StateData.ServerAddress + 
						   							"client/send/exception/dump/"+ 
						   							idinstance.idinstance);

				   dumpclient.getParams().setParameter("http.protocol.expect-continue", false);
				   dumpclient.getParams().setParameter("http.connection.timeout", 5000);
				   dumpclient.getParams().setParameter("http.socket.timeout", 5000);
				   
				   // 1. 파일의 내용을 body 로 설정함 
				   dumppost.setHeader("Content-Type", "multipart/form-data; charset=utf-8");
				   File file = new File(filename);
				   FileEntity entity = new FileEntity(file,"multipart/form-data");
				   dumppost.setEntity(entity);
				   
				   
				   HttpResponse response = dumpclient.execute(dumppost);
				  } catch (Exception e) {
						e.printStackTrace();
					}

	}

}
