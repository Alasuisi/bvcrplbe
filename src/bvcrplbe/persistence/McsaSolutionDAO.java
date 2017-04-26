package bvcrplbe.persistence;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.ConnectionManager;
import mcsa.McsaSegment;
import mcsa.McsaSolution;

public class McsaSolutionDAO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5057669235697699612L;
	
	
	private static final String INSERT_SOLUTION = "INSERT INTO solution(transfer_id,solution_id,changes,needed_seats,arrival_time,total_waittime,total_triptime,animal,smoke,luggage,"
			+ "handicap,transfer_set,solution_details) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static void saveSolutions(LinkedList<McsaSolution> solutionList, int transferId) throws SQLException, JsonProcessingException
		{
			Connection conn = null;
			PreparedStatement pstm = null;
			ConnectionManager manager = new ConnectionManager();
			conn=manager.connect();
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
