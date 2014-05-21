package Spider_Movie;

import java.util.*;
import java.io.*;
import java.sql.*;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Extraction implements Runnable {

	static final String ParentUrl = "http://www.tiantangbbs.com/";
	static final MyDatabase MDB = new MyDatabase();
	static int Number = 1;
	private String MovieType;
	private String ThreadNum;
	private ArrayList<String> MovieLinks;
	private Hashtable<String, String> MoviesName;
	//private Connection Conn;
	
	@SuppressWarnings("unchecked")
	public Extraction( ArrayList<String> MovieLinks, String ThreadNum, String MovieType, Hashtable<String, String> MoviesName ) {
		
		this.ThreadNum = ThreadNum;
		this.MovieLinks = (ArrayList<String>) MovieLinks.clone();
		this.MoviesName = (Hashtable<String, String>) MoviesName.clone();
		this.MovieType = MovieType;
		//this.Conn = Conn;
	}
	
	public void run() {
		while( MovieLinks.size() > 0 ) {
			try {
				String MovieLink = MovieLinks.remove(0);
				String MovieName = MoviesName.get(MovieLink);
				SaveThisMovie( MovieLink, MovieName, MovieType );
				System.out.println(Number+". "+ThreadNum+": "+MovieType+": "+MovieLink+": "+MovieName);
				Number ++;
			} catch( Exception e ) {
				System.out.println(e);
			}
		}
	}
	
	public void SaveThisMovie( String MovieUrl, String MovieName, String MovieType ) throws Exception { 	//������������ҳ
			
		String Definition = null, Type = null, Area = null, Year = null,
			   Director_Screenwriter = null, Starring = null, Score = null, 
			   Plot = null, DownloadUrl = null;
		try {
			Document Doc = Jsoup.parse( ReadInfo.GetHTML(MovieUrl, MovieName) );
			Elements MovieInfos = Doc.select("th"); 	//��ȡ��Ӱ�����Ϣ��ǩ
			
			String MovieHTML = "<html><head><title>"+MovieName+"</title></head><body><b>"+MovieName+"</b><br/></body></html>"; 	//������HTML
			Document NewDoc = Jsoup.parse(MovieHTML);
			NewDoc.body().append("<a href="+MovieUrl+" target=_blank>ԭ����: "+MovieUrl+"</a></br>");
			for ( Element MovieInfo:MovieInfos ) {
				String Info = MovieInfo.text();
				String ContentText = "";
			
				if ( Info.compareTo("��������:") != 0 ) {
					Elements Contents = MovieInfo.siblingElements().select("a");
					for ( Element Content:Contents ) {
						ContentText += (Content.text() +"/ ");
						if ( Info.compareTo("�� ��:") == 0 ) {
							Definition = Content.text();
						}
					}
					if ( Info.compareTo("�� ��:") == 0 ) {
						Type = ContentText;
					}
					if ( Info.compareTo("�� ��:") == 0 ) {
						Area = ContentText;
					}
					if ( Info.compareTo("�� ��:") == 0 ) {
						Year = ContentText;
					}
					if ( Info.compareTo("����/���:") == 0 ) {
						Director_Screenwriter = ContentText;
					}
					if ( Info.compareTo("�� ��:") == 0 ) {
						Starring = ContentText;
					}
					NewDoc.body().append("<a>"+Info+" "+ContentText+"</a><br/>");
				}
				else {
					String Content = MovieInfo.siblingElements().text();
					NewDoc.body().append("<a>"+Info+" "+Content+"  "+"</a>");
					if ( Content.toCharArray()[0] == '/' ) {
						Score = "0";
					}
					else if ( Content.indexOf('/') > 0 ){
						Score = Content.substring(0, Content.indexOf('/'));
					}
					else {
						Score = Content;
					}
				}
			}
			NewDoc.body().prepend("<br></br>");
			
			MovieName = ChangeName(MovieName);
			Elements Images = Doc.select("img[zoomfile]"); 			//��ȡͼƬ����
			ArrayList<String> ImgUrls = new ArrayList<String>();
			for ( Element Image:Images ) {
				String ImgUrl = Image.attr("zoomfile");
				NewDoc.body().prepend("<a href="+ImgUrl.substring(32)+" target = _blank><img src="+ImgUrl.substring(32)+" height = \"200px\"></a>");
				if ( ImgUrl.indexOf("http://") == -1 ) {
					ImgUrl = ParentUrl + ImgUrl;
				}
				ImgUrls.add(ImgUrl);
				//FileUtils.writeByteArrayToFile(new File(new File(new File(new File("Movie"), MovieType), MovieName+"_"+Definition), ImgUrl.substring(ImgUrl.lastIndexOf('/'))), ReadInfo.ReadByte(ImgUrl, MovieName));
			}
			
			Elements DownloadLinks = Doc.select("span[style=white-space: nowrap]"); 	//��ȡ��������
			DownloadUrl = DownloadLinks.select("a[href]").attr("href");
			NewDoc.body().append("<a href="+ParentUrl+DownloadUrl+" >���ص�ַ</a>");
			
			Elements Plots = Doc.select("td[class=t_f]"); 		//��ȡ������
			Plots.select("ignore_js_op").remove();
			//Plots.select("font").remove();
			Plot = Plots.first().text();
			NewDoc.body().append("<a><br/><br/>�����飺</a><a>"+Plots.first().html()+"</a>");
					
			//FileUtils.writeStringToFile(new File(new File(new File(new File("Movie"), MovieType), MovieName+"_"+Definition), MovieName+"_"+Definition+".htm"), NewDoc.toString(), "UTF-8");
			//if ( DownloadUrl.length() != 0 ) {
			//	FileUtils.writeStringToFile(new File(new File(new File(new File("Movie"), MovieType), MovieName+"_"+Definition), "DownloadUrl.txt"), ParentUrl+DownloadUrl);
			//}
			//else {
			//	FileUtils.writeStringToFile(new File(new File(new File(new File("Movie"), MovieType), MovieName+"_"+Definition), "DownloadUrl.txt"), "��δ�ṩ�����������");
			//}
			//while( ImgUrls.size() > 0 ) {
				//String ImgUrl = ImgUrls.remove(0);
				//FileUtils.writeByteArrayToFile(new File(new File(new File(new File("Movie"), MovieType), MovieName+"_"+Definition), ImgUrl.substring(ImgUrl.lastIndexOf('/'))), ReadInfo.ReadByte(ImgUrl, MovieName));
			//}
			SaveToDatabase( MovieName, MovieUrl, Type, Area, Year, Definition, Director_Screenwriter, Starring, Score, Plot, DownloadUrl );
			//System.out.println("After" + Number+". "+ThreadNum+": "+MovieType+": "+MovieUrl+": "+MovieName);
		} catch( Exception e ) {
			System.out.println(e);
			FileUtils.writeStringToFile(new File("Movie", "syslog.txt"), "SaveThisMovie: "+MovieType+": "+MovieName+": "+MovieUrl+": "+e+"\r\n", "GBK", true);
		}
	}
	
	public void SaveToDatabase( String MovieName, String MovieUrl, String Type, String Area, String Year,
								String Definition, String Director_Screenwriter, String Starring, String Score,
								String Plot, String DownloadUrl) throws Exception {
		Connection Conn = MDB.ConnectionDB("MovieSpider");
		MDB.CreatTable(Conn, MovieType);
		MDB.setMovieName(MovieName+"_"+Definition);
		MDB.setMovieUrl(MovieUrl);
		MDB.setType(Type);
		MDB.setArea(Area);
		MDB.setYear(Year);
		MDB.setDefinition(Definition);
		MDB.setDirector_Screenwriter(Director_Screenwriter);
		MDB.setStarring(Starring);
		MDB.setScore(new Double(Score));
		MDB.setPlot(Plot);
		MDB.setDownloadUrl(ParentUrl+DownloadUrl);
		MDB.InsertSQL(Conn, MovieType);
		MDB.CutConnectionDB(Conn);
		Conn.close();
	}
	
	public String ChangeName( String MovieName ) {
		char[] Temp = MovieName.toCharArray();
		
		for ( int i = 0; i < MovieName.length(); i ++ ) {
			if ( Temp[i] == ':' ) {
				Temp[i] = '��';
			}
			else if ( Temp[i] == '?' ) {
				Temp[i] = '��';
			}
			else if ( Temp[i] == '"' ) {
				Temp[i] = '��';
			}
			else if ( Temp[i] == '\\' ) {
				Temp[i] = '_';
			}
			else if ( Temp[i] == '/' ) {
				Temp[i] = '_';
			}
		}	
		return (new String(Temp));
	}
	
	static public int getNumber() {
		return Number;
	}
}
