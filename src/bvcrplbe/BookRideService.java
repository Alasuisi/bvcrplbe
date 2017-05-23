package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import bvcrplbe.domain.NotificationMessage;
import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.McsaSolutionDAO;
import mcsa.McsaSolution;

@Path("/BookRide")
public class BookRideService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{userid}/{tranid}/{solid}")
	public Response bookRide(@PathParam("userid") int userid,@PathParam("tranid") int tranid,@PathParam("solid") int solid)
		{
		McsaSolution updated=null;
		 try {
			updated =McsaSolutionDAO.bookSolution(userid, tranid, solid);
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Problems with the DB"+System.lineSeparator()+e.getMessage()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Serialization/Deserialization problem:"+System.lineSeparator()+e.getMessage()).build();
		} catch (DaoException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Problems with rides:"+System.lineSeparator()+e.getMessage()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Problems with google geoapi"+System.lineSeparator()+e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String resString=null;
		try {
			resString = mapper.writeValueAsString(updated);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Response object serialization error"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(resString).build();
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{userid}")
	public Response getAllBookedSolution(@PathParam("userid") int userid)
		{
		LinkedList<McsaSolution> result=null;
		 try {
			result = McsaSolutionDAO.readAllBookedSolution(userid);
		} catch (SQLException | IOException | DaoException | ClassNotFoundException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving all booked solution:"+System.lineSeparator()+e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String responseString=null;
		 try {
			 responseString = mapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error serializing response content"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(responseString).build();
		}
	
	@DELETE
	@Path("/{userid}/{tranid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteBookRideDebug(@PathParam("userid") int userid,@PathParam("tranid") int pasTranId)
		{
		LinkedList<NotificationMessage> notificationList=null;
			 try {
				 notificationList=McsaSolutionDAO.deleteBookedSolution(userid, pasTranId, true);
			} catch (SQLException | DaoException | IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing delete reservation request: "+e.getMessage()).build();
			}
			 Client client = Client.create();
				boolean errorOccured=false;
				ObjectMapper mapper = new ObjectMapper();
				Iterator<NotificationMessage> iter = notificationList.iterator();
					while(iter.hasNext())
						{
						try{
							NotificationMessage message = iter.next();
							String address = message.getCallBackURI();
							WebResource resource = client.resource(address);
							//String responseMessage = message.getMessage();
							String responseMessage = mapper.writeValueAsString(message);
							ClientResponse response = resource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,responseMessage);
							if(response.getStatus()!=200) errorOccured=true;
							System.out.println(message.toString());
							}
						catch(Exception w)
							{
							 errorOccured=true;
							}
						}
		 
					if(!errorOccured) return Response.status(Status.OK).entity("Ride deleted and drivers clients correctly informed").build();
					else return Response.status(Status.OK).entity("Your reservation has been deleted as requested").build();
		}
	

}
