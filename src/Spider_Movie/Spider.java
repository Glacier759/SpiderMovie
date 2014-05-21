
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
		//									"程序运行时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Before)+"\r\n"
		//									+"程序结束时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(After)+"\r\n"
		//									+"本次抓取耗时："+FormatDuring( After.getTime()-Before.getTime() )+"\r\n"
		//									+"共计抓取数目："+Extraction.getNumber()+"\r\n");
		
		//Spider.Build();
		System.out.println("Build Success.");
	}
	
	public void Start( String SeedUrl ) throws Exception {
		
		//MyDatabase MDB = new MyDatabase();
		//Connection Conn = MDB.ConnectionDB();
		
		Types = FileUtils.readLines(new File("TypeList.txt"), "GBK"); 	//从文件中读取所有电影类型
		Document Doc = Jsoup.parse( ReadInfo.GetHTML(SeedUrl, "种子") ); 		//访问种子URL  获取首页信息
		Elements MovieTypes = Doc.select("a[class=xi2]"); 				//从首页信息中提取出 class=xi2 的<a>标签
		
		for ( Element MovieType:MovieTypes ) {
			if( Types.contains(MovieType.text()) ) { 				//提取出有效的信息
				TypeUrls.clear();
				System.out.println(MovieType.text() +" ...");
				ReadThisType( ParentUrl + MovieType.attr("href"), MovieType.text()); 	//读取该类型电影所有信息
				//break;
			}
		}
		
		while(Thread.activeCount() > 1) {	
			Thread.sleep(10000);
		}
		System.out.println("Success.");
	}
	
	public void ReadThisType( String TypeUrl, String MovieType ) throws Exception { 	//针对这一类电影进行抓取
		
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
				if ( !TypeUrls.contains(ParentUrl+MovieUrl) ) { 							//针对这一类电影当前页面下的所有未抓取的电影进行抓取
					//ReadThisMovie( ParentUrl+MovieUrl, MovieName, MovieType ); 	//对这部电影进行信息抓取
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
				Extraction T = new Extraction( TempList, "线程"+i, MovieType, MoviesName );
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
		String MovieHTML = "<html><head><title>HomePage</title></head><body><br/></body></html>"; 	//构建新HTML
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
