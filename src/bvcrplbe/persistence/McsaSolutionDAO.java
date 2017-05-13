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
import bvcrplbe.domain.TimedPoint2D;
import mcsa.McsaSegment;
import mcsa.McsaSolution;

public class McsaSolutionDAO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5057669235697699612L;
	
	
	private static final String GET_COMPUTED_SOLUTION = "SELECT * FROM solution WHERE transfer_id=?";
	private static final String CHECK_TRANSFER_EXISTANCE = "select count(*) from transfer where \"User_ID\"=? AND \"Transfer_ID\"=?;";
	public static LinkedList<McsaSolution> readSolutions(int userid,int transferid) throws SQLException, IOException
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
	public static void bookSolution(int userid, int tranid, int solid,String callback) throws SQLException, IOException, DaoException
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
		 
		}
	
	private static final String INSERT_SOLUTION = "INSERT INTO solution(transfer_id,solution_id,changes,needed_seats,arrival_time,total_waittime,total_triptime,animal,smoke,luggage,"
			+ "handicap,transfer_set,solution_details) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_SOUTION = "DELETE FROM solution WHERE transfer_id=?";
	
	public static void saveSolutions(LinkedList<McsaSolution> solutionList, int transferId) throws SQLException, JsonProcessingException
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
