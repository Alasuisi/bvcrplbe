package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.domain.TranException;
import bvcrplbe.domain.Transfer;
import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.McsaSolutionDAO;
import bvcrplbe.persistence.TransferDAO;
import mcsa.MCSA;
import mcsa.McsaConnection;
import mcsa.McsaResult;
import mcsa.McsaSolution;

@Path("/SearchRide")
public class SearchTransferService {
	
	@Path("/{timeFrame}/{limit}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response SearchRideTimeFrame(@PathParam("timeFrame") long timeFrame,@PathParam("limit") int limit,String transferString)
		{
		System.out.println("CALLED SEARCHRIDE");
		 Transfer passenger=null;
		 LinkedList<Transfer> drivers=null;
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			 System.out.println("ma che ho in ingresso?  "+transferString);
			passenger = mapper.readValue(transferString, Transfer.class);
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Malformed Ride Object:"+System.lineSeparator()+e.getMessage()).build();
		}
		 try {
			 long worstArrival=passenger.getPath().getLast().getTouchTime();
			 long depTime = passenger.getPath().getFirst().getTouchTime();
			 if(worstArrival==0 || worstArrival<passenger.getDep_time()) 
			 	{
				 String error="Malformed transfer object: the last point in the path has no arrival time, or arrival_time < departure_time";
				 return Response.status(Status.BAD_REQUEST).entity(error).build();
			 	}
			 System.out.println("timeframe portannato? "+(timeFrame==Long.MAX_VALUE));
			drivers = TransferDAO.readAllOfferings(depTime,worstArrival,timeFrame);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Mapper error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL error:"+System.lineSeparator()+e.getMessage()+System.lineSeparator()+e.getSQLState()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (JSONException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		}
		 MCSA mcsa = new MCSA(drivers,passenger);
		 mcsa.removeDeadEnds();
		 mcsa.computeMCSA(passenger.getDep_time());
		 mcsa.removeBadOnes();
		 LinkedList<LinkedList<McsaConnection>> result2 = mcsa.result;
		 System.out.println("Cleaned solutions size" +result2.size());
		 McsaResult result=null;
		 try {
			result = mcsa.getResults();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Multipath Connection Scan Algorithm: FAIL"+System.lineSeparator()+e.getMessage()).build();
		}
		 LinkedList<McsaSolution> solutionList = result.getResults(limit);
		 try {
			McsaSolutionDAO.saveSolutions(solutionList, passenger.getTran_id());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL error:"+System.lineSeparator()+e.getMessage()+System.lineSeparator()+e.getSQLState()).build();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		}
		 String responseString=null;
		 try {
			responseString = mapper.writeValueAsString(solutionList);
			//System.out.println(responseString);
		} catch (JsonProcessingException e) {
		
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Response JSON conversion error:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}
	
	//@Path("/search")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response SearchRide(String transferString)
		{
		System.out.println("CALLED SEARCHRIDE");
		 Transfer passenger=null;
		 LinkedList<Transfer> drivers=null;
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			 System.out.println("ma che ho in ingresso?  "+transferString);
			passenger = mapper.readValue(transferString, Transfer.class);
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Malformed Ride Object:"+System.lineSeparator()+e.getMessage()).build();
		}
		 try {
			 long worstArrival=passenger.getPath().getLast().getTouchTime();
			 long depTime = passenger.getPath().getFirst().getTouchTime();
			 if(worstArrival==0 || worstArrival<passenger.getDep_time()) 
			 	{
				 String error="Malformed transfer object: the last point in the path has no arrival time, or arrival_time < departure_time";
				 return Response.status(Status.BAD_REQUEST).entity(error).build();
			 	}
			drivers = TransferDAO.readAllOfferings(depTime,worstArrival,Long.MAX_VALUE);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Mapper error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL error:"+System.lineSeparator()+e.getMessage()+System.lineSeparator()+e.getSQLState()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		}
		 /*
		 System.out.println("DRIVERLIST"+System.lineSeparator());
		 Iterator<Transfer> tranItre = drivers.iterator();
		 while(tranItre.hasNext())
		 	{
			 System.out.println(tranItre.next().toString());
		 	}
		 System.out.println(System.lineSeparator()+"PASSENGER TRANSFER"+System.lineSeparator());
		 System.out.println(passenger.toString());*/ catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		}
		 
		 MCSA mcsa = new MCSA(drivers,passenger);
		 //mcsa.McsaIterative(passenger.getDep_time());
		 mcsa.removeDeadEnds();
		 mcsa.computeMCSA(passenger.getDep_time());
		 mcsa.removeBadOnes();
		 LinkedList<LinkedList<McsaConnection>> result2 = mcsa.result;
		 System.out.println("Cleaned solutions size" +result2.size());
		 McsaResult result=null;
		 try {
			result = mcsa.getResults();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Multipath Connection Scan Algorithm: FAIL"+System.lineSeparator()+e.getMessage()).build();
		}
		 LinkedList<McsaSolution> solutionList = result.getResults();
		 try {
			McsaSolutionDAO.saveSolutions(solutionList, passenger.getTran_id());
			/*
		 	Iterator<McsaSolution> iter = solutionList.iterator();
			while(iter.hasNext())
				{
				 System.out.println(iter.next().toString());
				}*/
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL error:"+System.lineSeparator()+e.getMessage()+System.lineSeparator()+e.getSQLState()).build();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		}
		 String responseString=null;
		 try {
			responseString = mapper.writeValueAsString(solutionList);
			//System.out.println(responseString);
		} catch (JsonProcessingException e) {
		
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Response JSON conversion error:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}	
	
	
	@Path("{user_id}/{transfer_id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response readMySolutions(@PathParam("user_id") int user,@PathParam("transfer_id") int transfer)
		{
		System.out.println("called readMySolutions");
		LinkedList<McsaSolution> result=null;
		 try {
			result = McsaSolutionDAO.readSolutions(user, transfer);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("JSON parsing internal error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("JSON mapping internal error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Unable to fulfill request due to incorrect user/transfer objects matching"+System.lineSeparator()+e.getSQLState()+System.lineSeparator()+e.getMessage()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("JSON conversion to domain objects failed!"+System.lineSeparator()+e.getMessage()).build();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error!"+System.lineSeparator()+e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String responseString=null;
		 try {
			 System.out.println("sto per mappare");
			responseString = mapper.writeValueAsString(result);
			System.out.println("mappato");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error serializing response entity:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}
	
	
	
	@Path("myrequest/{userid}/{transferid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response readMyRequest(@PathParam("userid") int user,@PathParam("transferid") int transfer)
		{
		System.out.println("CALLED readMyRequest");
		Transfer passenger=null;
		 try {
			passenger = TransferDAO.getMySearchRequest(user, transfer);
		} catch (JSONException | SQLException | DaoException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("God hates your request: "+System.lineSeparator()+e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String responseString=null;
		 try {
			responseString = mapper.writeValueAsString(passenger);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("God hates your request: "+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}

}
