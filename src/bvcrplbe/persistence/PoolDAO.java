package bvcrplbe.persistence;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.ConnectionManager;
import bvcrplbe.domain.NotificationMessage;
import bvcrplbe.domain.Passenger;
import bvcrplbe.domain.Pool;
import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;
import mcsa.McsaSegment;
import mcsa.McsaSolution;

public class PoolDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5629709939884942236L;
	
	private static final String GET_TRANSFER_CALLBACK="SELECT (\"Callback_URI\") FROM transfer where \"Transfer_ID\"=?";
	private static final String REMOVE_POOL= "DELETE FROM pool WHERE pool_id=?";
	private static final String REMOVE_TRANSFER = "DELETE FROM transfer WHERE \"Transfer_ID\"=?";
	public static LinkedList<NotificationMessage> deletePool(int userid,int driTranId,boolean debug) throws JsonParseException, JsonMappingException, SQLException, IOException, DaoException
		{
		Connection con=null;
		PreparedStatement pstm=null;
		ConnectionManager manager = new ConnectionManager();
		Pool driverPool=PoolDAO.readPool(userid, driTranId);
		LinkedList<Passenger> passList = driverPool.getPassengerList();
		HashMap<Integer,HashSet<Integer>> passMap = new HashMap<Integer,HashSet<Integer>>();
		//LinkedList<String> passCallback=new LinkedList<String>();
		//LinkedList<String> driverCallback = new LinkedList<String>();
		LinkedList<NotificationMessage> messages = new LinkedList<NotificationMessage>();
		Iterator<Passenger> passIter = passList.iterator();
		while(passIter.hasNext())
		  	{
			  Passenger tempPass = passIter.next();
			  int passTran = tempPass.getTransferID();
			  McsaSolution thisSol = McsaSolutionDAO.readBookedSolution(passTran);
			  HashSet<Integer> toAdd = thisSol.getTransferSet();
			  toAdd.remove(new Integer(passTran));
			  passMap.put(new Integer(passTran), toAdd);
		  	}
		Iterator<Integer> pasMapIter = passMap.keySet().iterator();
		con=manager.connect();
		while(pasMapIter.hasNext())
			{
			Integer key = pasMapIter.next();
			int freeSeats=McsaSolutionDAO.voidSolution(con, pstm, key.intValue());
			String passCallBack=null;
			pstm=con.prepareStatement(GET_TRANSFER_CALLBACK);
			pstm.setInt(1, key.intValue());
			ResultSet rs = pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				rs.next();
				passCallBack=rs.getString(1);
				//rs.close();
				}else 
					{
					con.rollback();
					pstm.close();
					rs.close();
					con.close();
					manager.close();
					throw new DaoException("Problem loading callback addres for transfer "+key.intValue());
					}
			NotificationMessage passMessage = new NotificationMessage();
			passMessage.setRolePassenger();
			passMessage.setTransferID(key.intValue());
			passMessage.setRelatedToTransfer(driTranId);
			passMessage.setTypeNotification();
			passMessage.setCallBackURI(passCallBack);
			passMessage.setMessage("One of your drivers has canceled his trip, your solution is not valid anymore, the ride is still in the system, you can try search for a new solution list");
			messages.add(passMessage);
			HashSet<Integer> PassDriverSet = passMap.get(key);
			Iterator<Integer> PassDriverIter = PassDriverSet.iterator();
			while(PassDriverIter.hasNext())
				{
				int driverID=PassDriverIter.next().intValue();			
				int passID= key.intValue();
				String driverCallBack=null;
				PoolDAO.removePassenger(con, pstm, driverID, passID, freeSeats);
				if(driverID!=driTranId)
					{
					pstm=con.prepareStatement(GET_TRANSFER_CALLBACK);
					pstm.setInt(1, driverID);
					rs = pstm.executeQuery();
					if(rs.isBeforeFirst())
						{
						rs.next();
						driverCallBack=rs.getString(1);
						}else 
							{
							con.rollback();
							pstm.close();
							rs.close();
							con.close();
							manager.close();
							throw new DaoException("deletePool() ERROR: unable to read callback addres for driver transfer "+driverID);
							}
					NotificationMessage driverMessage= new NotificationMessage();
					driverMessage.setRoleDriver();
					driverMessage.setTransferID(driverID);
					driverMessage.setRelatedToTransfer(passID);
					driverMessage.setTypeNotification();
					driverMessage.setCallBackURI(driverCallBack);
					driverMessage.setMessage("One of your passengers has canceled his reservation, you have now "+freeSeats+" more free seats available");
					messages.add(driverMessage);
					}
				}
			}
		if(!debug)
			{
			pstm=con.prepareStatement(REMOVE_POOL);
			pstm.setInt(1, driTranId);
			pstm.executeUpdate();
			pstm=con.prepareStatement(REMOVE_TRANSFER);
			pstm.setInt(1, driTranId);
			pstm.executeUpdate();
			}
		//con.rollback(); //RICORDARSI DI CAMBIARE CON COMMIT SE SEMBRA FUNZIONARE A DOVERE
		
		con.commit();
		pstm.close();
		con.close();
		manager.close();
		return messages;
		//TODO ricordarsi di committare alla fine
		}
	
	private static String READ_POOL_UNSAFE="SELECT * FROM pool WHERE pool_id=?";
	private static String SAVE_UPDATED_PASSLIST = "UPDATE pool SET passenger_list=? WHERE pool_id=?";
	private static void removePassenger(Connection con,PreparedStatement pstm,int driverID,int passengerID,int freeSeats) throws SQLException, JsonParseException, JsonMappingException, IOException, DaoException
		{
		 pstm=con.prepareStatement(READ_POOL_UNSAFE);
		 pstm.setInt(1, driverID);
		 ResultSet rs=pstm.executeQuery();
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 ObjectMapper mapper = new ObjectMapper();
			 String passString = rs.getString(4);
			 LinkedList<Passenger> passList = mapper.readValue(passString, new TypeReference<LinkedList<Passenger>>(){});
			 LinkedList<Passenger> updatedList = new LinkedList<Passenger>();
			 Iterator<Passenger> iter = passList.iterator();
			 while(iter.hasNext())
			 	{
				 Passenger temp = iter.next();
				 if(temp.getTransferID()!=passengerID)
				 	{
					 updatedList.add(temp);
				 	}
			 	}
			 pstm=con.prepareStatement(SAVE_UPDATED_PASSLIST);
			 if(updatedList.size()!=0)
			 	{
				 PGobject passListJson = new PGobject();
				 passListJson.setType("json");
				 passListJson.setValue(mapper.writeValueAsString(updatedList));
				 pstm.setObject(1, passListJson);
			 	}else pstm.setNull(1, Types.NULL);
			 pstm.setInt(2, driverID);
			 pstm.executeUpdate();
			 TransferDAO.restoreFreeSeats(con, pstm, driverID, freeSeats);
			 
		 	}else throw new DaoException("removePassenger: Error loading passenger's driver pool object, passenger="+passengerID+" driver="+driverID);
		}
	
	
	private static String READ_POOL = "SELECT * FROM pool WHERE pool_id=? AND driver_id=?";
	public static Pool readPool(int userid,int tranid) throws SQLException, JsonParseException, JsonMappingException, IOException
		{
		Connection con=null;
		PreparedStatement pstm=null;
		ResultSet rs = null;
		ConnectionManager manager = new ConnectionManager();
		con = manager.connect();
		pstm=con.prepareStatement(READ_POOL);
		pstm.setInt(1, tranid);
		pstm.setInt(2, userid);
		rs=pstm.executeQuery();
		if(rs.isBeforeFirst())
			{
				rs.next();
				int poolid = rs.getInt(1);
				int driverid = rs.getInt(2);
				String pathString = rs.getString(3);
				String passString = rs.getString(4);
				ObjectMapper mapper = new ObjectMapper();
				LinkedList<TimedPoint2D> path =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
				LinkedList<Passenger> pass = new LinkedList<Passenger>();
				if(passString!=null)
					{
					pass = mapper.readValue(passString, new TypeReference<LinkedList<Passenger>>(){});
					}
				Pool result = new Pool(poolid, driverid, path, pass);
				rs.close();
				pstm.close();
				con.close();
				manager.close();
				return result;
			}
		rs.close();
		pstm.close();
		con.close();
		manager.close();
		return null;
		}
	
	
	private static String READ_PASSENGERS = "SELECT (passenger_list) from pool WHERE pool_id=?";
	private static String UPDATE_PASSENGERS = "UPDATE pool SET passenger_list=? where pool_id=?";
	
	public static void updatePassengers(Connection con,PreparedStatement pstm, McsaSegment segment, int poolid,int passTranId) throws SQLException, DaoException, JsonParseException, JsonMappingException, IOException
		{
			pstm=con.prepareStatement(READ_PASSENGERS);
			pstm.setInt(1, poolid);
			ResultSet rs = pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				 rs.next();
				 String passengersString = rs.getString(1);
				 LinkedList<Passenger> passList=null;
				 ObjectMapper mapper = new ObjectMapper();
				 if(passengersString!=null)
				 	{
					 passList = mapper.readValue(passengersString, new TypeReference<LinkedList<Passenger>>(){});
					 Iterator<Passenger> iter = passList.iterator();
					 while(iter.hasNext())
					 	{
						 Passenger thisPass = iter.next();
						 if(thisPass.getTransferID()==passTranId) throw new DaoException("There is already a reservation, for passenger transfer "+passTranId+ " with the driver with transfer "+poolid);
					 	}
				 	}else passList= new LinkedList<Passenger>();
				 
				 Passenger toAdd = new Passenger(passTranId,segment.getSegmentPath().getFirst(),segment.getSegmentPath().getLast());
				 passList.add(toAdd);
				 String updateList = mapper.writeValueAsString(passList);
				 pstm=con.prepareStatement(UPDATE_PASSENGERS);
				 PGobject listJson = new PGobject();
				 listJson.setType("json");
				 listJson.setValue(updateList);
				 pstm.setObject(1, listJson);
				 pstm.setInt(2, poolid);
				 pstm.executeUpdate();
				}else throw new DaoException("Problem reading passenger list of driver pool");
			
		}
	
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
		 System.out.println("userid "+tran.getUser_id()+" tranid "+tran.getTran_id());
		 rs=pstm.executeQuery();
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 int count =rs.getInt("count");
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
