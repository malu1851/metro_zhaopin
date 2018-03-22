package nc.util.rm;

import java.sql.Connection;
import java.sql.DriverManager;
//自定义数据库连接   马鹏鹏
public class JDBCUtils {
	public Connection getConnection()
	{
	    Connection con = null;// 创建一个数据库连接	 
	    try
	    {
	        Class.forName("oracle.jdbc.driver.OracleDriver");// 加载Oracle驱动程序	       
	        String url = "jdbc:oracle:" + "thin:@172.16.100.61:1521:orcl";//数据库地址
	        String user = "NC633GOLD";//用户名
	        String password = "1";//密码
	        con = DriverManager.getConnection(url, user, password);// 获取连接	        	       
	     }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }	   
	    return con;
	    
	}
}
