package bvcrplbe.persistence;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.ConnectionManager;
import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;

public class PoolDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5629709939884942236L;
	
	
	
	private static String CREATE_POOL= "INSERT INTO pool(pool_id,driver_id,driver_path) VALUES (?,?,?)";
	private static String CHECK_DRIVER = "select count(*) from transfer where \"User_ID\"=? AND \"Transfer_ID\"=? AND \"User_Role\"= '{\"role\": \"driver\"}'";
	public static void writePool(Transfer tran) throws SQLException, DaoException, JsonProcessingException
		{
		 Connection con=null;
		 PreparedStatement pstm=null;
		 ResultSet rs=null;
		 ConnectionManager manager = new ConnectionManager();
		 con=manager.connect();
		 pstm=con.prepareStatement(CHECK_DRIVER);
		 pstm.setInt(1, tran.getUser_id());
		 pstm.setInt(2, tran.getTran_id());
		 rs=pstm.executeQuery();
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 int count =rs.getInt(1);
			 if(count==0) throw new DaoException("The transfer passed as input is not associated to a driver, but a passenger");
			 if(count>1) throw new DaoException("Something very bad happened, there is inconsitency with the db with respect to transfer_id="+tran.getTran_id());
			 if(count==1)
			 	{
				 pstm=con.prepareStatement(CREATE_POOL);
				 pstm.setInt(1, tran.getTran_id());
				 pstm.setInt(2, tran.getUser_id());
				 LinkedList<TimedPoint2D> path = tran.getPath();
				 ObjectMapper mapper = new ObjectMapper();
				 String pathString = mapper.writeValueAsString(path);
				 PGobject pathJson = new PGobject();
				 pathJson.setType("json");
				 pathJson.setValue(pathString);
				 pstm.setObject(3, pathJson);
				 pstm.execute();
				 con.commit();
				 rs=null;
				 pstm=null;
				 con.close();
				 con=null;
				 manager.close();
				 manager=null;
			 	}
			 
		 	}else throw new DaoException("Something went wrong evaluating the rows in PoolDAO-->writePool method");
		}

}
