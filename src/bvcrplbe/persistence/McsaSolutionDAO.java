package bvcrplbe.persistence;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.ConnectionManager;
import bvcrplbe.domain.NotificationMessage;
import bvcrplbe.domain.TimedPoint2D;
import mcsa.McsaSegment;
import mcsa.McsaSolution;

public class McsaSolutionDAO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5057669235697699612L;
	
	////USED ONLY FOR DEBUG PURPOSES, NOT TO BE USED IN PRODUCTION MODE
	private static final String VOID_SOLUTION= "DELETE from booked_solutions WHERE transfer_id=?";
	private static final String GET_NEEDED_SEATS="SELECT (needed_seats) from booked_solutions where transfer_id=?";
	protected static int voidSolution(Connection con,PreparedStatement pstm,int tranid) throws DaoException, SQLException
		{
		 if(con==null) throw new DaoException("called void solution with a closed connection as input parameter");
		 pstm=con.prepareStatement(GET_NEEDED_SEATS);
		 pstm.setInt(1, tranid);
		 ResultSet rs = pstm.executeQuery();
		 int neededSeats;
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 neededSeats=rs.getInt(1);
		 	}else 
		 		{
		 		con.rollback();
		 		throw new DaoException("Error retrieving needed seats for transfer "+tranid);
		 		}
		 rs.close();
		 pstm=con.prepareStatement(VOID_SOLUTION);
		 pstm.setInt(1, tranid);
		 pstm.executeUpdate();
		 return neededSeats;
		}
	
	private static final String READ_TRANSFER_SET = "SELECT transfer_set FROM booked_solutions WHERE transfer_id=?";
	private static final String CHECK_USER = "SELECT (\"User_ID\"),(\"User_Role\") FROM transfer WHERE \"Transfer_ID\"=?";
	private static final String GET_TRANSFER_CALLBACK="SELECT (\"Callback_URI\") FROM transfer where \"Transfer_ID\"=?";
	public static LinkedList<NotificationMessage> deleteBookedSolution(int userid,int tranid,boolean debug) throws SQLException, DaoException, JsonParseException, JsonMappingException, IOException, ClassNotFoundException
		{
			Connection con=null;
			PreparedStatement pstm=null;
			ConnectionManager manager = new ConnectionManager();
			ResultSet rs=null;
			LinkedList<NotificationMessage> messages = new LinkedList<NotificationMessage>();
			con=manager.connect();
			pstm=con.prepareStatement(CHECK_USER);
			pstm.setInt(1, tranid);
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
					rs.next();
					int retrievedUserid=rs.getInt(1);
					String retrievedRole=rs.getString(2);
					con.rollback();
					if(retrievedUserid!=userid) throw new DaoException("Passed userid is different from the one associated to transferid");
					System.out.println("Retrieved Role "+retrievedRole);
					if(!retrievedRole.equals("{\"role\": \"passenger\"}")) throw new DaoException("Transfer associated to this transferid is of a driver, not a passenger");
				}else
					{
					con.rollback();
					throw new DaoException("Something went wrong veryfing userid and transfer id, empty result set");
					}
		 pstm=con.prepareStatement(READ_TRANSFER_SET);
		 pstm.setInt(1, tranid);
		 rs=pstm.executeQuery();
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 ObjectMapper mapper = new ObjectMapper();
			 String transferSetString = rs.getString(1);
			 HashSet<Integer> transferSet = mapper.readValue(transferSetString, new TypeReference<HashSet<Integer>>(){});
			 transferSet.remove(new Integer(tranid));
			 int freeSeats = voidSolution(con,pstm,tranid);
			 Iterator<Integer> PassDriverIter = transferSet.iterator();
			 while(PassDriverIter.hasNext())
			 	{
				 int driverID=PassDriverIter.next().intValue();
				 String driverCallBack=null;
				 PoolDAO.removePassenger(con, pstm, driverID, tranid, freeSeats);
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
							throw new DaoException("deleteBokedSolution() ERROR: unable to read callback addres for driver transfer "+driverID);
							}
					NotificationMessage driverMessage= new NotificationMessage();
					driverMessage.setRoleDriver();
					driverMessage.setTransferID(driverID);
					driverMessage.setRelatedToTransfer(tranid);
					driverMessage.setTypeNotification();
					driverMessage.setCallBackURI(driverCallBack);
					driverMessage.setMessage("One of your passengers has canceled his reservation, you have now "+freeSeats+" more free seats available");
					messages.add(driverMessage);
			 	}
		 	}else throw new DaoException("Someting went wrong reading the transfer set associated to transfer: "+tranid);
		 con.commit();
		 if(rs!=null) rs.close();
		 if(pstm!=null)pstm.close();
		 if(con!=null)con.close();
		 if(manager!=null)manager.close();
		 return messages;
		}
	
	
	private static final String GET_ALL_BOOKED_SOLUTION = "SELECT DISTINCT (transfer_id),(solution_id),(changes),(needed_seats),(arrival_time),(total_waittime),(total_triptime),(animal),(smoke),(luggage),(handicap),(transfer_set),(solution_details),(callback_url)"
														+ "FROM booked_solutions AS bs, transfer AS ts "
														+ "WHERE  bs.transfer_id IN (SELECT \"Transfer_ID\" FROM transfer WHERE \"User_ID\"=?) AND ts.\"User_Role\"='{\"role\":\"passenger\"}' ;";
	public static LinkedList<McsaSolution> readAllBookedSolution(int userid) throws SQLException, JsonParseException, JsonMappingException, IOException, DaoException, ClassNotFoundException
		{
		Connection con=null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		ConnectionManager manager = new ConnectionManager();
		LinkedList<McsaSolution> resultList = null;
		con=manager.connect();
		pstm=con.prepareStatement(GET_ALL_BOOKED_SOLUTION);
		pstm.setInt(1, userid);
		rs=pstm.executeQuery();
		if(rs.isBeforeFirst())
			{
			 resultList= new LinkedList<McsaSolution>();
			 while(!rs.isAfterLast())
			 	{
				 rs.next();
				 McsaSolution result=new McsaSolution();
				 result.setTransferID(rs.getInt(1));
				 result.setSolutionID(rs.getInt(2));
				 result.setChanges(rs.getInt(3));
				 result.setNeededSeats(rs.getInt(4));
				 result.setArrivalTime(rs.getLong(5));
				 result.setTotalWaitTime(rs.getLong(6));
				 result.setTotalTripTime(rs.getLong(7));
				 result.setAnimal(rs.getBoolean(8));
				 result.setSmoke(rs.getBoolean(9));
				 result.setLuggage(rs.getBoolean(10));
				 result.setHandicap(rs.getBoolean(11));
				 ObjectMapper mapper = new ObjectMapper();
				 String tranSetString = rs.getString(12);
				 LinkedHashSet<Integer> transferSet =mapper.readValue(tranSetString, new TypeReference<LinkedHashSet<Integer>>(){});
				 result.setTransferSet(transferSet);
				 String segmentString = rs.getString(13);
				 LinkedList<McsaSegment> segments = mapper.readValue(segmentString, new TypeReference<LinkedList<McsaSegment>>(){});
				 result.setSolution(segments);
				 resultList.add(result);
			 	}
			}else throw new DaoException("Error retrieving list of booked solution, or there are no booked solution for userid: "+userid);
		if(rs!=null) rs.close();
		if(pstm!=null) pstm.close();
		if(con!=null) con.close();
		if(manager!=null) manager.close();
		return resultList;
		}

	
	private static final String GET_BOOKED_SOLUTION = "SELECT * FROM booked_solutions WHERE transfer_id=?";
	public static McsaSolution readBookedSolution(int tranid) throws SQLException, DaoException, JsonParseException, JsonMappingException, IOException, ClassNotFoundException
		{
		Connection con=null;
		PreparedStatement pstm=null;
		ConnectionManager manager = new ConnectionManager();
		ResultSet rs=null;
		con=manager.connect();
		pstm=con.prepareStatement(GET_BOOKED_SOLUTION);
		pstm.setInt(1, tranid);
		rs=pstm.executeQuery();
		McsaSolution result=null;
		if(rs.isBeforeFirst())
			{
			 rs.next();
			 result=new McsaSolution();
			 result.setSolutionID(rs.getInt(2));
			 result.setChanges(rs.getInt(3));
			 result.setNeededSeats(rs.getInt(4));
			 result.setArrivalTime(rs.getLong(5));
			 result.setTotalWaitTime(rs.getLong(6));
			 result.setTotalTripTime(rs.getLong(7));
			 result.setAnimal(rs.getBoolean(8));
			 result.setSmoke(rs.getBoolean(9));
			 result.setLuggage(rs.getBoolean(10));
			 result.setHandicap(rs.getBoolean(11));
			 ObjectMapper mapper = new ObjectMapper();
			 String tranSetString = rs.getString(12);
			 LinkedHashSet<Integer> transferSet =mapper.readValue(tranSetString, new TypeReference<LinkedHashSet<Integer>>(){});
			 result.setTransferSet(transferSet);
			 String segmentString = rs.getString(13);
			 LinkedList<McsaSegment> segments = mapper.readValue(segmentString, new TypeReference<LinkedList<McsaSegment>>(){});
			 result.setSolution(segments);
			 
			 
			}else throw new DaoException("Something went wrong retrieving the solution, empty result set");
		rs=null;
		pstm=null;
		manager.close();
		con.close();
		return result;
		}
	
	
	private static final String GET_COMPUTED_SOLUTION = "SELECT * FROM solution WHERE transfer_id=?";
	private static final String CHECK_TRANSFER_EXISTANCE = "select count(*) from transfer where \"User_ID\"=? AND \"Transfer_ID\"=?;";
	public static LinkedList<McsaSolution> readSolutions(int userid,int transferid) throws SQLException, IOException, ClassNotFoundException
		{
		Connection con = null;
		PreparedStatement pstm = null;
		ConnectionManager manager = new ConnectionManager();
		ResultSet rs = null;
		con=manager.connect();
		pstm=con.prepareStatement(CHECK_TRANSFER_EXISTANCE);
		pstm.setInt(1, userid);
		pstm.setInt(2, transferid);
		rs = pstm.executeQuery();
		rs.next();
		int rows=rs.getInt(1);
		System.out.println("rows="+rows);
		if(rows!=1)
			{
			rs=null;
			pstm=null;
			manager.close();
			manager=null;
			con.close();
			con=null;
			throw new SQLException("Get solution failed preliminary check: there are "+rows+" transfer associated to the provided user ID, which is wrong");
			}else
				{
				pstm=con.prepareStatement(GET_COMPUTED_SOLUTION);
				pstm.setInt(1, transferid);
				rs = pstm.executeQuery();
				LinkedList<McsaSolution> result = new LinkedList<McsaSolution>();
				if(rs.isBeforeFirst())
					{
					 rs.next();
					 ObjectMapper mapper = new ObjectMapper();
					 while(!rs.isAfterLast())
					 	{
						 McsaSolution s = new McsaSolution();
						 s.setTransferID(rs.getInt(1));
						 s.setSolutionID(rs.getInt(2));
						 s.setChanges(rs.getInt(3));
						 s.setNeededSeats(rs.getInt(4));
						 s.setArrivalTime(rs.getLong(5));
						 s.setTotalWaitTime(rs.getLong(6));
						 s.setTotalTripTime(rs.getLong(7));
						 s.setAnimal(rs.getBoolean(8));
						 s.setSmoke(rs.getBoolean(9));
						 s.setLuggage(rs.getBoolean(10));
						 s.setHandicap(rs.getBoolean(11));
						 String jsonTransferSet = rs.getString(12);
						 LinkedHashSet<Integer> transferSet = mapper.readValue(jsonTransferSet, new TypeReference<LinkedHashSet<Integer>>(){});
						 s.setTransferSet(transferSet);
						 String jsonSolutionDetail = rs.getString(13);
						 LinkedList<McsaSegment> solutionSegments = mapper.readValue(jsonSolutionDetail, new TypeReference<LinkedList<McsaSegment>>(){});
						 s.setSolution(solutionSegments);
						 result.add(s);
						 rs.next();
					 	}
					}
				rs=null;
				pstm=null;
				manager.close();
				manager=null;
				con.close();
				con=null;
				return result;
				}
		
		}
	
	private static String BOOK_SOLUTION ="INSERT INTO booked_solutions(transfer_id,solution_id,changes,needed_seats,"
																   + "arrival_time,total_waittime,total_triptime,animal,smoke,luggage,"
																   + "handicap,transfer_set,solution_details,callback_url) "
																   + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String READ_SEATS = "SELECT (\"Occupied_Seats\"),(\"Available_Seats\") FROM transfer WHERE \"Transfer_ID\"=?";
	private static String UPDATE_SEATS = "UPDATE transfer SET \"Occupied_Seats\"=\"Occupied_Seats\"+? WHERE \"Transfer_ID\"=?";
	private static String CHECK_OLD = "SELECT count(*) FROM booked_solutions WHERE transfer_id=?";
	private static String DELETE_OLD = "DELETE FROM booked_solutions WHERE transfer_id=?";
	private static String GET_CALLBACK_URI = "SELECT (\"Callback_URI\") FROM transfer WHERE \"Transfer_ID\"=?";
	public static McsaSolution bookSolution(int userid, int tranid, int solid) throws Exception
		{
		 LinkedList<McsaSolution> list =readSolutions(userid,tranid);
		 if(list.size()==0) throw new DaoException("You ar trying to book a solution, but no computed solution exists");
		 Iterator<McsaSolution> iter = list.iterator();
		 McsaSolution toBook=null;
		 boolean found=false;
		 while(iter.hasNext() && !found)
		 	{
			 McsaSolution temp = iter.next();
			 if(temp.getSolutionID()==solid)
			 	{
				 toBook=temp;
				 found=true;
			 	}
		 	}
		 toBook.generateWalinkgPaths();
		 
		 Connection con=null;
		 PreparedStatement pstm=null;
		 ConnectionManager manager = new ConnectionManager();
		 ResultSet rs=null;
		 con=manager.connect();
		 HashSet<Integer> tranSet =toBook.getTransferSet();
		 Iterator<Integer> setIter = tranSet.iterator();
		 while(setIter.hasNext())
		 	{
			 pstm=con.prepareStatement(READ_SEATS);
			 int thisTran = setIter.next().intValue();
			 if(thisTran!=tranid)
			 {
				 pstm.setInt(1, thisTran);
				 rs = pstm.executeQuery();
				 if(rs.isBeforeFirst())
				 	{
					 rs.next();
					 int freeSeats = rs.getInt(2)-rs.getInt(1);
					 if (freeSeats<toBook.getNeededSeats()) 
					 	{
						 throw new DaoException("not enough free seats in transfer: "+thisTran+". Someone may already have booked the remaining seats");
					 	}
					 else
					 	{
						 pstm=con.prepareStatement(UPDATE_SEATS);
						 pstm.setInt(1, toBook.getNeededSeats());
						 pstm.setInt(2, thisTran);
						 pstm.executeUpdate();
						 Iterator<McsaSegment> segIter = toBook.getSolution().iterator();
						 while(segIter.hasNext())
						 	{
							 McsaSegment segment = segIter.next();
							 if(segment.getFromTransferID()==segment.getToTransferID())
							 	{
								 if(segment.getFromTransferID()==thisTran&&segment.getToTransferID()==thisTran)
								 	{
									 PoolDAO.updatePassengers(con, pstm, segment, thisTran, tranid);
								 	}
							 	}
						 	}
					 	}
				 	}else throw new DaoException("Empty result set");
		 		}
		 	}
		 pstm=con.prepareStatement(CHECK_OLD);
		 pstm.setInt(1, tranid);
		 rs=pstm.executeQuery();
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 int count = rs.getInt(1);
			 if(count>1) throw new DaoException("Unexpected results, there were more than a booked solution for transfer: "+tranid);
			 if(count==1)
			 	{
				 pstm=con.prepareStatement(DELETE_OLD);
				 pstm.setInt(1, tranid);
				 pstm.executeUpdate();
			 	}
		 	}else throw new DaoException("Something went wrong when evaluating presence of already booked solution for this transfe: "+tranid);
		 
		 
		 pstm=con.prepareStatement(GET_CALLBACK_URI);
		 pstm.setInt(1, tranid);
		 rs=pstm.executeQuery();
		 String callback=null;
		 if(rs.isBeforeFirst())
		 	{
			 rs.next();
			 callback=rs.getString(1);
		 	}else throw new DaoException("Something went wrong retrieving callback uri for transfer "+tranid);
		 
		 pstm=con.prepareStatement(BOOK_SOLUTION);
		 pstm.setInt(1, tranid);
		 pstm.setInt(2, toBook.getSolutionID());
		 pstm.setInt(3, toBook.getChanges());
		 pstm.setInt(4, toBook.getNeededSeats());
		 pstm.setLong(5, toBook.getArrivalTime());
		 pstm.setLong(6, toBook.getTotalWaitTime());
		 pstm.setLong(7, toBook.getTotalTripTime());
		 pstm.setBoolean(8, toBook.isAnimal());
		 pstm.setBoolean(9, toBook.isSmoke());
		 pstm.setBoolean(10, toBook.isLuggage());
		 pstm.setBoolean(11, toBook.isHandicap());
		 ObjectMapper mapper = new ObjectMapper();
		 String transferString = mapper.writeValueAsString(toBook.getTransferSet());
		 PGobject transferJson = new PGobject();
		 transferJson.setType("json");
		 transferJson.setValue(transferString);
		 pstm.setObject(12, transferJson);
		 String solString = mapper.writeValueAsString(toBook.getSolution());
		 PGobject solJson = new PGobject();
		 solJson.setType("json");
		 solJson.setValue(solString);
		 pstm.setObject(13, solJson);
		 pstm.setString(14, callback);
		 pstm.executeUpdate();
		 pstm=con.prepareStatement(DELETE_SOUTION);
		 pstm.setInt(1, tranid);
		 pstm.executeUpdate();
		 con.commit();
		 pstm.close();
		 con.close();
         manager.close();
		 return toBook;
		}
	
	private static final String INSERT_SOLUTION = "INSERT INTO solution(transfer_id,solution_id,changes,needed_seats,arrival_time,total_waittime,total_triptime,animal,smoke,luggage,"
			+ "handicap,transfer_set,solution_details) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_SOUTION = "DELETE FROM solution WHERE transfer_id=?";
	
	public static void saveSolutions(LinkedList<McsaSolution> solutionList, int transferId) throws SQLException, JsonProcessingException, ClassNotFoundException
		{
			Connection conn = null;
			PreparedStatement pstm = null;
			ConnectionManager manager = new ConnectionManager();
			conn=manager.connect();
			pstm=conn.prepareStatement(DELETE_SOUTION);
			pstm.setInt(1, transferId);
			pstm.execute();
			pstm=conn.prepareStatement(INSERT_SOLUTION);
			Iterator<McsaSolution> solIter = solutionList.iterator();
			while(solIter.hasNext())
				{
				 McsaSolution thisSol = solIter.next();
				 pstm.setInt(1, transferId);
				 pstm.setInt(2, thisSol.getSolutionID());
				 pstm.setInt(3, thisSol.getChanges());
				 pstm.setInt(4, thisSol.getNeededSeats());
				 pstm.setLong(5, thisSol.getArrivalTime());
				 pstm.setLong(6, thisSol.getTotalWaitTime());
				 pstm.setLong(7, thisSol.getTotalTripTime());
				 pstm.setBoolean(8, thisSol.isAnimal());
				 pstm.setBoolean(9, thisSol.isSmoke());
				 pstm.setBoolean(10, thisSol.isLuggage());
				 pstm.setBoolean(11, thisSol.isHandicap());
				 HashSet<Integer> transferSet = thisSol.getTransferSet();
				 ObjectMapper mapper = new ObjectMapper();
				 String transferSetString = mapper.writeValueAsString(transferSet);
				 PGobject transferSetPg = new PGobject();
				 transferSetPg.setType("json");
				 transferSetPg.setValue(transferSetString);
				 pstm.setObject(12, transferSetPg);
				 LinkedList<McsaSegment> solution = thisSol.getSolution();
				 String solutionString = mapper.writeValueAsString(solution);
				 PGobject solutionPg = new PGobject();
				 solutionPg.setType("json");
				 solutionPg.setValue(solutionString);
				 pstm.setObject(13, solutionPg);
				 pstm.executeUpdate();
				}
			conn.commit();
			pstm=null;
			conn.close();
			conn=null;
			manager=null;
		}
}
