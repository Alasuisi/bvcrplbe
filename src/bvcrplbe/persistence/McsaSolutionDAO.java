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
						 System.out.println("DIOSTRINGA "+jsonTransferSet);
						 LinkedHashSet<Integer> transferSet = mapper.readValue(jsonTransferSet, new TypeReference<LinkedHashSet<Integer>>(){});
						 s.setTransferSet(transferSet);
						 String jsonSolutionDetail = rs.getString(13);
						 System.out.println("DIOSTRINGA2 "+jsonSolutionDetail);
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
				Iterator<McsaSolution> iter = result.iterator();
				while(iter.hasNext())
					{
					System.out.println("MCSASOLUTIONDAO "+iter.next().toString());
					}
				return result;
				}
		
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
