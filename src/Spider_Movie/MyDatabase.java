
package Spider_Movie;

import java.io.UnsupportedEncodingException;
import java.sql.*;
//import java.util.concurrent.locks.ReentrantLock;

public class MyDatabase {
	
	private String MovieName, MovieUrl, Type, Area, Definition, Director_Screenwriter, Starring;
	private String DownloadUrl, Plot, Year;
	private Double Score;
	//private ReentrantLock MyLock = new ReentrantLock();
	private String Database;
	
	public void CreatDatabase( String Database) {
			try {
				//System.out.println("CreatDatabase.");
				String SQL = "CREATE DATABASE "+Database;
				ConnectionDB("test").prepareStatement(SQL).execute();
			} catch ( SQLException e ) {
			} catch ( Exception e ) {
				System.out.println(e);
			}
	}
	
	public void CreatTable( Connection conn, String TableName ) {
		try {
			String SQL = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='"+Database+"' AND TABLE_NAME='"+TableName+"'";
			ResultSet RS = conn.prepareStatement(SQL).executeQuery();

			//System.out.println(SQL);
			if ( RS.next() ) {
				//System.out.println(TableName+"数据库表已存在.");
			}
			//else {
				SQL = "CREATE TABLE "+TableName+" (" +
					  "MovieName VARCHAR(100) ,"+
					  "Score DOUBLE ,"+
					  "Definition VARCHAR(100) ,"+
					  "Type VARCHAR(100) ,"+
					  "Area VARCHAR(100) ,"+
					  "Year VARCHAR(50), "+
					  "Director_Screenwriter VARCHAR(300) ,"+
					  "Starring VARCHAR(300) ,"+
					  "Plot VARCHAR(1000) ,"+
					  "DownloadUrl VARCHAR(200) ,"+
					  "MovieUrl VARCHAR(100) ,"+
					  "primary key(MovieName))";
				conn.prepareStatement(SQL).executeUpdate();
				//System.out.println(TableName+"数据库表已建立.");
			//}
		} catch(SQLException e) {
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public boolean InsertSQL( Connection conn, String TableName ) throws Exception {
		
		
		try {
			String SQL = "INSERT INTO "+TableName+" (MovieName,Score,Definition,Type,Area,Year,Director_Screenwriter,"
												 + "Starring,Plot,DownloadUrl,MovieUrl)"+"values(?,?,?,?,?,?,?,?,?,?,?)";
			//System.out.println(SQL);
			
			PreparedStatement PS = conn.prepareStatement(SQL);
				
			PS.setString(1, this.MovieName);
			PS.setDouble(2, this.Score);
			PS.setString(3, this.Definition);
			PS.setString(4, this.Type);
			PS.setString(5, this.Area);
			PS.setString(6, this.Year);
			PS.setString(7, this.Director_Screenwriter);
			PS.setString(8, this.Starring);
			PS.setString(9, this.Plot);
			PS.setString(10, this.DownloadUrl);
			PS.setString(11, this.MovieUrl);
			
			//System.out.println(this.MovieName);
			//MyLock.lock();
			int result = PS.executeUpdate();
			//MyLock.unlock();
			//System.out.println("Test~~~.");
			
			if ( result > 0 ) {
				return true;
			}
			MovieName = null; MovieUrl = null; DownloadUrl = null;
			Type = null; Area = null; Year = null; Score = null;
			Definition = null; Director_Screenwriter = null;
			Starring = null; Plot = null;
				
		}catch( SQLException e) {
			System.out.println(new String( e.toString().getBytes(), "UTF-8" ));
			//e.printStackTrace();
		}catch( Exception e ) {
			e.printStackTrace();
		}
	
	return false;
	}
	
	
	public Connection ConnectionDB( String Database ) {
		
		String Driver = "com.mysql.jdbc.Driver";
		String URL = null;
		String User = "root";
		String Password = "root";
		Connection conn = null;
		this.Database = Database;
			
		if ( Database == null ) {
			URL = "jdbc:mysql://localhost/test";
		}
		else {
			URL = "jdbc:mysql://localhost/"+Database;
		}
			
		try {
			Class.forName(Driver);
			conn = DriverManager.getConnection(URL, User, Password);
			if ( !conn.isClosed() ) {
				//System.out.println("Succeeded connection to the Database.");
			}
			else {
				//System.out.println("Falled connection to the Database.");
			}
		}catch ( SQLException e ) {
			System.out.println(e);
			CreatDatabase( Database );
			conn = ConnectionDB( Database );
		}catch ( Exception e ) {
			e.printStackTrace();
		}
	return conn;
	}
	
	public void CutConnectionDB( Connection conn ) throws Exception {
		
			try {
				if ( conn == null ) {
					//System.out.println("Connection is null");
				}
				else {
					//System.out.println("Connection is not null");
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			} finally {
				conn.close();
			}
	}
	
	public void setYear( String Year ) {	this.Year = Year;	}
	public void setType( String Type ) {	this.Type = Type;	}
	public void setArea( String Area ) {	this.Area = Area;	}
	public void setDirector_Screenwriter( String Director_Screenwriter ) {	this.Director_Screenwriter = Director_Screenwriter;	}
	public void setStarring( String Starring ) {	this.Starring = Starring;	}
	public void setMovieName( String MovieName ) {	this.MovieName = MovieName;	}
	public void setMovieUrl( String MovieUrl ) {	this.MovieUrl = MovieUrl;	}
	public void setDefinition( String Definition ) {	this.Definition = Definition;	}
	public void setScore( Double Score ) {	this.Score = Score;	}
	public void setDownloadUrl( String DownloadUrl ) {	this.DownloadUrl = DownloadUrl;	}
	public void setPlot( String Plot ) {	this.Plot = Plot;	}
	
	public String getYear() {	return this.Year;	}
	public Double getScore() {	return this.Score;	}
	public String getType() {	return this.Type;	}
	public String getArea() {	return this.Area;	}
	public String getMovieName() {	return this.MovieName;	}
	public String getMovieUrl() {	return this.MovieUrl;	}
	public String getDefinition() {	return this.Definition;	}
	public String getDirector_Screenwriter() {	return this.Director_Screenwriter;	}
	public String getStarring() {	return this.Starring;	}
	public String getDownloadUrl() {	return this.DownloadUrl;	}
	public String getPlot() {	return this.Plot;	}
	
	
}