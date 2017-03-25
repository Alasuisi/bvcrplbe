package bvcrplbe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionManager {

	
		
		/*private final String url = "jdbc:postgresql://82.223.67.189:5432/bonvoyage";
		private final String user = "alessio";
		private final String password = "bvpsql2016";

		
		 
	    public Connection connect() {
	    	try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("Madonna driver");
				e1.printStackTrace();
			}
	        Connection conn = null;
	        try {
	            conn = DriverManager.getConnection(url, user, password);
	            System.out.println("Connected to the PostgreSQL server successfully.");
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	 
	        return conn;
	    }*/
		
		
		private Connection con;
		//private Session session;
		private int nLocalPort = 6000;
		private boolean tunnelUp=false;
		private String UNABLE_TO_ESTABLISH_SQLCONNECTION="08001";
		
		
		  public Connection connect2() throws ClassNotFoundException, SQLException
		  	{
				  try {
					return connectToDB(5432);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("portanna la madonna"+e.getSQLState()+" "+e.getErrorCode());
					if(e.getSQLState().equals(UNABLE_TO_ESTABLISH_SQLCONNECTION))
						{
						 System.out.println("Unable to connect locally, trying to establish ssh tunnel");
						 if(!tunnelUp)
						 	{
							 	String strSshUser = "bonvoyage";                  // SSH loging username
					    		String strSshPassword = "bnvyg2017";                   // SSH login password
					    		String strSshHost = "82.223.67.189";          // hostname or ip or SSH server
					    		int nSshPort = 2222;                                    // remote SSH host port number
					    		String strRemoteHost = "localhost";  // hostname or ip of your database server
					                                      // local port number use to bind SSH tunnel
					    		int nRemotePort = 5432;
					    		try{
					    			doSshTunnel(strSshUser, strSshPassword, strSshHost, nSshPort, strRemoteHost, nLocalPort, nRemotePort);
					    			}catch(JSchException ssh)
					    				{
					    				System.out.println(ssh.getMessage()+"This may result of a still opened connection, if you happen to republish the app without restarting the server, usually you can ignore this message if all seems working normally");
					    				}
					    		tunnelUp=true;
					    		return connectToDB(nLocalPort);
						 	}
						}
				}
				 // return null;
				return null;
		  	}
		   
		  public Connection connect()
		  {
		    try
		    {
		    	if(!tunnelUp)
		    	{
		    		System.out.println("dentro primo if");
		    		String strSshUser = "bonvoyage";                  // SSH loging username
		    		String strSshPassword = "bnvyg2017";                   // SSH login password
		    		String strSshHost = "82.223.67.189";          // hostname or ip or SSH server
		    		int nSshPort = 2222;                                    // remote SSH host port number
		    		String strRemoteHost = "localhost";  // hostname or ip of your database server
		                                      // local port number use to bind SSH tunnel
		    		int nRemotePort = 5432;                               // remote port number of your database 
		      
		    		try{
		    			doSshTunnel(strSshUser, strSshPassword, strSshHost, nSshPort, strRemoteHost, nLocalPort, nRemotePort);
		    			}catch(JSchException e)
		    				{
		    				System.out.println(e.getMessage()+"This may result of a still opened connection, if you happen to republish the app without restarting the server, usually you can ignore this message if all seems working normally");
		    				}
		    		tunnelUp=true;
		    	}
		    	try{ 
			    	  String strDbUser = "alessio";                    // database loging username
				      String strDbPassword = "bvpsql2016";                    // database login password
				      Class.forName("org.postgresql.Driver");
				      con = DriverManager.getConnection("jdbc:postgresql://localhost:"+nLocalPort+"/bonvoyage", strDbUser, strDbPassword);
				      int isolation = Connection.TRANSACTION_SERIALIZABLE;
				      con.setTransactionIsolation(isolation);
				      con.setReadOnly(false);
					  con.setAutoCommit(false);
				      return con;
		    		
			      }catch(Exception e)
			      	{
			    	  e.printStackTrace();
			      	}
		      //con.close();
		    }
		    catch( Exception e )
		    {
		      e.printStackTrace();
		    }
			return con;
		  }
		  
		  private static void doSshTunnel( String strSshUser, String strSshPassword, String strSshHost, int nSshPort, String strRemoteHost, int nLocalPort, int nRemotePort ) throws JSchException
		  {
		    final JSch jsch = new JSch();
		    Session session = jsch.getSession( strSshUser, strSshHost, nSshPort );
		    session.setPassword( strSshPassword );
		    final Properties config = new Properties();
		    config.put( "StrictHostKeyChecking", "no" );
		    session.setConfig( config );
		    session.connect();
		    session.setPortForwardingL(nLocalPort, strRemoteHost, nRemotePort);
		  }
		  
		  private Connection connectToDB(int port) throws ClassNotFoundException, SQLException
		  	{
			  String strDbUser = "alessio";                    // database loging username
		      String strDbPassword = "bvpsql2016";                    // database login password
		      Class.forName("org.postgresql.Driver");
		      con = DriverManager.getConnection("jdbc:postgresql://localhost:"+port+"/bonvoyage", strDbUser, strDbPassword);
		      int isolation = Connection.TRANSACTION_SERIALIZABLE;
		      con.setTransactionIsolation(isolation);
		      con.setReadOnly(false);
			  con.setAutoCommit(false);
		      return con;
		  	}
		  
		  
		  public void close()	
		  {
			  if(con!=null)
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  }
	}

