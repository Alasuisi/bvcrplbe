package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
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

}
