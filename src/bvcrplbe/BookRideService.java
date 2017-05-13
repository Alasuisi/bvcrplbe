package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.McsaSolutionDAO;

@Path("/BookRide")
public class BookRideService {
	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/{userid}/{tranid}/{solid}")
	public Response bookRide(@PathParam("userid") int userid,@PathParam("tranid") int tranid,@PathParam("solid") int solid, String callback)
		{
		 try {
			McsaSolutionDAO.bookSolution(userid, tranid, solid, callback);
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Problems with the DB"+System.lineSeparator()+e.getMessage()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Serialization/Deserialization problem:"+System.lineSeparator()+e.getMessage()).build();
		} catch (DaoException e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity("Problems with rides:"+System.lineSeparator()+e.getMessage()).build();
		}
		 return Response.status(Status.OK).entity("Solution booked, temporary solutions cleared from db").build();
		}

}
