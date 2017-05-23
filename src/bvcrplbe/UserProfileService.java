package bvcrplbe;

import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bvcrplbe.domain.UserProfile;
import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.UserProfileDAO;

@Path("/userprofile")
public class UserProfileService {
	
	@Path("/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getUserProfile(@PathParam("userid") int userid)
		{
		System.out.println("CALLED getUserProfile");
		 UserProfile prof=null;
		 try {
			prof=UserProfileDAO.load(userid);
		} catch (ClassNotFoundException | SQLException | DaoException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error loading user profile:"+System.lineSeparator()+e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String response=null;
		 try {
			 System.out.println("STAMPO LO USER PROFILE PRIMA DI INVIARLO"+prof.toString());
			response = mapper.writeValueAsString(prof);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error serializing user profile:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity(response).build();
		}

}
