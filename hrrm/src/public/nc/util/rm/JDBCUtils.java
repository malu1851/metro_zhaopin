package nc.util.rm;

import java.sql.Connection;
import java.sql.DriverManager;
//�Զ������ݿ�����   ������
public class JDBCUtils {
	public Connection getConnection()
	{
	    Connection con = null;// ����һ�����ݿ�����	 
	    try
	    {
	        Class.forName("oracle.jdbc.driver.OracleDriver");// ����Oracle��������	       
	        String url = "jdbc:oracle:" + "thin:@172.16.100.61:1521:orcl";//���ݿ��ַ
	        String user = "NC633GOLD";//�û���
	        String password = "1";//����
	        con = DriverManager.getConnection(url, user, password);// ��ȡ����	        	       
	     }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }	   
	    return con;
	    
	}
}
