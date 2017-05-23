package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

import bvcrplbe.domain.Transfer;
import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.PoolDAO;
import bvcrplbe.persistence.TransferDAO;

@Path("/debugging")
public class DebuggingServices {
	
	@Path("/resetpool")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response resetPoolTable()
		{
			LinkedList<Transfer> trans=null;
			 try {
				trans=TransferDAO.getAllTransfers();
			} catch (ClassNotFoundException | JSONException | SQLException | IOException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("reset pool table failed, unbale to read transfers").build();
			}
			Iterator<Transfer> iter = trans.iterator();
			while(iter.hasNext())
				{
				 Transfer toReset = iter.next();
				 try {
					 if(toReset.getUser_role().equals("driver"))
					 	{
						 PoolDAO.writePool(toReset);
					 	}
				} catch (JsonProcessingException | SQLException | DaoException | ClassNotFoundException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error writing pools").build();
				}
				}
			return Response.status(Status.OK).entity("all pool entries correctly reset").build();
		}
	

}
