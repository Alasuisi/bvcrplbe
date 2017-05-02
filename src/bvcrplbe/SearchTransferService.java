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

import bvcrplbe.domain.Transfer;
import bvcrplbe.persistence.McsaSolutionDAO;
import bvcrplbe.persistence.TransferDAO;
import mcsa.MCSA;
import mcsa.McsaConnection;
import mcsa.McsaResult;
import mcsa.McsaSolution;

@Path("/SearchRide")
public class SearchTransferService {
	
	@Path("/search")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response SearchRide(String transferString)
		{
		System.out.println("CALLED SEARCHRIDE");
		 Transfer passenger=null;
		 LinkedList<Transfer> drivers=null;
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			passenger = mapper.readValue(transferString, Transfer.class);
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Malformed Ride Object:"+System.lineSeparator()+e.getMessage()).build();
		}
		 try {
			drivers = TransferDAO.readAllOfferings();
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
		 
		 System.out.println("DRIVERLIST"+System.lineSeparator());
		 Iterator<Transfer> tranItre = drivers.iterator();
		 while(tranItre.hasNext())
		 	{
			 System.out.println(tranItre.next().toString());
		 	}
		 System.out.println(System.lineSeparator()+"PASSENGER TRANSFER"+System.lineSeparator());
		 System.out.println(passenger.toString());
		 
		 MCSA mcsa = new MCSA(drivers,passenger);
		 mcsa.McsaIterative(passenger.getDep_time());
		 //mcsa.computeMCSA(passenger.getDep_time());
		 mcsa.removeBadOnes();
		 LinkedList<LinkedList<McsaConnection>> result2 = mcsa.result;
		 System.out.println("Cleaned solutions size" +result2.size());
		 McsaResult result=null;
		 try {
			//result = mcsa.getResults();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Multipath Connection Scan Algorithm: FAIL"+System.lineSeparator()+e.getMessage()).build();
		}
		 LinkedList<McsaSolution> solutionList = result.getResults();
		 //try {
			//McsaSolutionDAO.saveSolutions(solutionList, passenger.getTran_id());
			Iterator<McsaSolution> iter = solutionList.iterator();
			while(iter.hasNext())
				{
				 System.out.println(iter.next().toString());
				}
			
		/*} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Parser error:"+System.lineSeparator()+e.getMessage()).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL error:"+System.lineSeparator()+e.getMessage()+System.lineSeparator()+e.getSQLState()).build();
		}*/
		 String responseString=null;
		 try {
			responseString = mapper.writeValueAsString(solutionList);
			System.out.println(responseString);
		} catch (JsonProcessingException e) {
		
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Response JSON conversion error:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}	
	
	@Path("/myrequest/{userid}/{transferid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response readMyRequest(@PathParam("userid") int user,@PathParam("transferid") int transfer)
		{
		Transfer passenger=null;
		 try {
			passenger = TransferDAO.getMySearchRequest(user, transfer);
		} catch (JSONException | SQLException | DaoException | IOException e) {
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
