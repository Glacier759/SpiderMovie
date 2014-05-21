
package Spider_Movie;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Spider {

	static final Integer ThreadsNum = 10;
	static final String SeedUrl = "http://www.tiantangbbs.com/forum-2-1.html";
	static final String ParentUrl = "http://www.tiantangbbs.com/";
	static private HashSet<String> TypeUrls = new HashSet<String>();
	static private List<String> Types = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		
		Spider Spider = new Spider();
		
		Date Before = new Date();
		Spider.Start(SeedUrl);
		Date After = new Date();
		
		System.out.println("Time = " + FormatDuring((After.getTime() - Before.getTime())));
		System.out.println("Movie Number = " + Extraction.getNumber());
		//FileUtils.writeStringToFile(new File("Movie", "Report.txt"), 
		//									"��������ʱ�䣺"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Before)+"\r\n"
		//									+"�������ʱ�䣺"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(After)+"\r\n"
		//									+"����ץȡ��ʱ��"+FormatDuring( After.getTime()-Before.getTime() )+"\r\n"
		//									+"����ץȡ��Ŀ��"+Extraction.getNumber()+"\r\n");
		
		//Spider.Build();
		System.out.println("Build Success.");
	}
	
	public void Start( String SeedUrl ) throws Exception {
		
		//MyDatabase MDB = new MyDatabase();
		//Connection Conn = MDB.ConnectionDB();
		
		Types = FileUtils.readLines(new File("TypeList.txt"), "GBK"); 	//���ļ��ж�ȡ���е�Ӱ����
		Document Doc = Jsoup.parse( ReadInfo.GetHTML(SeedUrl, "����") ); 		//��������URL  ��ȡ��ҳ��Ϣ
		Elements MovieTypes = Doc.select("a[class=xi2]"); 				//����ҳ��Ϣ����ȡ�� class=xi2 ��<a>��ǩ
		
		for ( Element MovieType:MovieTypes ) {
			if( Types.contains(MovieType.text()) ) { 				//��ȡ����Ч����Ϣ
				TypeUrls.clear();
				System.out.println(MovieType.text() +" ...");
				ReadThisType( ParentUrl + MovieType.attr("href"), MovieType.text()); 	//��ȡ�����͵�Ӱ������Ϣ
				//break;
			}
		}
		
		while(Thread.activeCount() > 1) {	
			Thread.sleep(10000);
		}
		System.out.println("Success.");
	}
	
	public void ReadThisType( String TypeUrl, String MovieType ) throws Exception { 	//�����һ���Ӱ����ץȡ
		
		try {
			System.out.println(TypeUrl);
			Document Doc = Jsoup.parse( ReadInfo.GetHTML(TypeUrl, MovieType) );
			Elements MovieLinks = Doc.select("span[class=mnch]");
			
			Hashtable<String, String> MoviesName = new Hashtable<String, String>();
			for ( Element MovieLink:MovieLinks ) {
				String MovieUrl = MovieLink.select("a[href]").attr("href");
				String MovieName = MovieLink.select("a[href]").text();
				if( MovieName.compareTo(" ") == 0 || MovieName.length() <= 0 ) {
					MovieName = MovieLink.select("a[href]").attr("title");
				}
				if ( !TypeUrls.contains(ParentUrl+MovieUrl) ) { 							//�����һ���Ӱ��ǰҳ���µ�����δץȡ�ĵ�Ӱ����ץȡ
					//ReadThisMovie( ParentUrl+MovieUrl, MovieName, MovieType ); 	//���ⲿ��Ӱ������Ϣץȡ
					TypeUrls.add(ParentUrl+MovieUrl);
					MoviesName.put(ParentUrl+MovieUrl, MovieName);
				}
			}
			
			ArrayList<String> TempList = new ArrayList<String>();
			Enumeration<String> Temp = MoviesName.keys();
			int count = MoviesName.size()/ThreadsNum;
			int i, j;
			for ( i = 1; Temp.hasMoreElements(); i ++ ) {
				for ( j = 1; j <= count+1; j ++ ) {
					if ( !Temp.hasMoreElements() ) {
						break;
					}
					String MovieLink = Temp.nextElement();
					TempList.add(MovieLink);
				}
				Extraction T = new Extraction( TempList, "�߳�"+i, MovieType, MoviesName );
				new Thread( T ).start();
				TempList.clear();
			}
			String NextPage = Doc.select("a[class=nxt]").attr("href");
			if ( NextPage.length() <= 0 ) {
				return;
			}
			ReadThisType( ParentUrl+NextPage, MovieType );
		} catch( Exception e ) {
			System.out.println(e);
			FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), "ReadThisType: "+TypeUrl+": "+MovieType+": "+e+"\r\n", "GBK", true);
		}
	}
	
	public static String FormatDuring(long mss) {  
	    long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);  
	    long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);  
	    long seconds = (mss % (1000 * 60)) / 1000;  
	    return hours + ":" + minutes + ":" + seconds;  
	}
	
	public void Build() throws Exception {
		
		File MovieDir = new File("Movie");
		
		File[] TypeDirs = MovieDir.listFiles();
		String MovieHTML = "<html><head><title>HomePage</title></head><body><br/></body></html>"; 	//������HTML
		Document MovieDoc = Jsoup.parse(MovieHTML);
		for ( File TypeDir:TypeDirs ) {
			if ( TypeDir.isDirectory() && TypeDir.getName().compareTo("HomePage") != 0 ) {
				MovieDoc.body().append("<a href="+TypeDir.getName()+".htm>"+TypeDir.getName()+"</a><br/>");		
				
				String[] MovieNames = TypeDir.list();
				File[] MovieName = TypeDir.listFiles();
				for ( int i = 0; i < MovieName.length; i ++ ) {
					if ( !MovieName[i].isDirectory() ) {
						MovieName[i].delete();
					}
				}
				String TypeHTML = "<html><head><title>"+TypeDir.getName()+"</title></head><body><br/></body></html>";
				Document TypeDoc = Jsoup.parse(TypeHTML);
				for ( int i = 1; i <= MovieNames.length; i ++ ) {
					//if ( i % 5 == 0 ) {
					//	TypeDoc.body().append("<br/>");
					//}
					TypeDoc.body().append("<a href=../"+TypeDir.getName()+"/"+MovieNames[i-1]+"/"+MovieNames[i-1]+".htm>"+MovieNames[i-1]+"</a>");
				}
				FileUtils.writeStringToFile(new File(new File(MovieDir, "HomePage"), TypeDir.getName()+".htm"), TypeDoc.toString());
			}
		}
		FileUtils.writeStringToFile(new File(new File(MovieDir, "HomePage"), "HomePage.htm"), MovieDoc.toString());
	}
}
