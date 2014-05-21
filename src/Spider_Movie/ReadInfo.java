package Spider_Movie;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;


public class ReadInfo {
	
	static public String GetHTML( String URL, String Name ) throws Exception {
		
		String url = URL.substring(0, 7);
		if ( url.compareTo("http://") != 0 ) {
			URL = "http://"+URL;
		}
		
		String HTML = new String( ReadByte(URL, Name),"UTF-8" );
		return HTML;
	}
	
	static public byte[] ReadByte( String URL, String Name ) throws Exception {
		
		URL = ToUTF8.toUtf8String(URL);
		byte[] Bytes = null;
        HttpClient httpclient = new HttpClient();
        //httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        //httpclient.getHttpConnectionManager().getParams().setSoTimeout(5000);
        GetMethod getMethod = new GetMethod(URL);
        getMethod.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.8 (KHTML, like Gecko) Chrome/20.0.1105.2 Safari/536.8");    
        getMethod.getParams().setParameter("http.socket.timeout", 50000);
        getMethod.getParams().setParameter("http.connection.timeout", 50000);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler()); 	//������������
        
        try {
            int statusCode = httpclient.executeMethod(getMethod);
            if(statusCode != HttpStatus.SC_OK){
            	FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), URL+": "+Name+": Method failed:"+getMethod.getStatusLine()+"\r\n", "GBK", true);
                System.err.println(URL+": Method failed: "+ getMethod.getStatusLine());
            }
            //��ȡ����
            InputStream responseBody =  getMethod.getResponseBodyAsStream();
            //��������
            Bytes = inputStream2Bytes(responseBody, URL, Name);
        } catch (HttpException e) {
            //�����������쳣��������Э�鲻�Ի��߷��ص�����������
            System.out.println("Please check your provided http address!");
            System.out.println(e);
            FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), "ReadByte: "+URL+": "+Name+": "+e+"\r\n", "GBK", true);
        } catch (IOException e) {
            //���������쳣
            System.out.println(e);
            FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), "ReadByte: "+URL+": "+Name+": "+e+"\r\n", "GBK", true);
        }finally {
            //�ͷ�����
            getMethod.releaseConnection();
        }
        return Bytes;
    }
	
	static public byte[] inputStream2Bytes(InputStream is, String URL, String Name) throws IOException{
		
		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		byte[] temp = new byte[1000];
		
		try {
	        int i = 0;
	        while( (i = is.read(temp, 0, 100)) > 0 ) {
	        	BAOS.write(temp, 0, i);
	        }
		} catch( Exception e ) {
			System.out.println("inputStream2Bytes: "+e);
			FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), "inputStream2Bytes: "+URL+": "+Name+": "+e+"\r\n", "GBK", true);
		}
        return BAOS.toByteArray();
	}
}