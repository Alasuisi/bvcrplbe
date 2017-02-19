package bvcrplbe;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import bvcrplbe.persistence.TransferDAO;
import bvcrplbe.persistence.UserProfileDAO;
import bvcrplbe.domain.Transfer;
import bvcrplbe.domain.UserProfile;

@Path("/offertran")
public class OfferTransferService {
	
	  
	  @Path("{userid}")
	  @GET
	  @Produces("application/json")
	  public Response getUserTransfers(@PathParam("userid") int userid) throws JSONException, SQLException, DaoException, IOException
	 	{
		 UserProfile user = UserProfileDAO.load(userid);
		 LinkedList<Transfer> userTransfers=TransferDAO.readMyOfferings(user);
		 ObjectMapper mapper = new ObjectMapper();
		 String jsonInString = mapper.writeValueAsString(userTransfers);
		 //JsonObject userTranJson = (new JsonParser()).parse(jsonInString).getAsJsonObject();
		 return Response.status(200).entity(jsonInString).build();
		 
	 	}
	  
	  @PUT
	  @Consumes(MediaType.APPLICATION_JSON)
	  public Response registerNewTransfer(String jsonInput)
	  	{
		  	ObjectMapper mapper = new ObjectMapper();
		  	try {
				Transfer toAdd = mapper.readValue(jsonInput, Transfer.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
			} catch (JsonMappingException e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (IOException e) {
				e.printStackTrace();
				return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(e.getMessage()).build();
			}
			return Response.status(Status.OK).entity("da mettere").build();
		  
	  	}
	  
	  @Path("/test/{userid}")
	  @GET
	  @Produces("text/plain")
	  public Response testUserTransfers(@PathParam("userid") int userid) throws JSONException, SQLException, DaoException, IOException
	 	{
		 UserProfile user = UserProfileDAO.load(userid);
		 
		 GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBA-NgbRwnecHN3cApbnZoaCZH0ld66fT4");
			DirectionsResult results=null;
			try {
				results = DirectionsApi.getDirections(context, "roma", "ferentino").await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DirectionsRoute[] routes = results.routes;
			EncodedPolyline lines=routes[0].overviewPolyline;
			List<LatLng> path = lines.decodePath();
			LinkedList<Point2D.Double> pathpp = new LinkedList<Point2D.Double>();
			Iterator<LatLng> iter = path.iterator();
			while(iter.hasNext())
				{
				 LatLng temp =iter.next();
				 Point2D.Double toAdd = new Point2D.Double();
				 toAdd.setLocation(temp.lat, temp.lng);
				 pathpp.add(toAdd);
				 
				}
			Transfer testTran = new Transfer();
			testTran.setAnimal(true);
			testTran.setArr_addr("ferentino");
			Point2D arrpoint = new Point2D.Double();
			arrpoint.setLocation(path.get(path.size()-1).lat,path.get(path.size()-1).lng);
			testTran.setArr_gps(arrpoint);
			testTran.setAva_seats(4);
			testTran.setClass_id(6);
			testTran.setDep_addr("roma");
			Point2D deppoint = new Point2D.Double();
			deppoint.setLocation(path.get(0).lat, path.get(0).lng);
			testTran.setDep_gps(deppoint);
			testTran.setDep_time(System.currentTimeMillis());
			testTran.setHandicap(true);
			testTran.setLuggage(true);
			testTran.setOcc_seats(3);
			testTran.setPath(pathpp);
			testTran.setPool_id(666);
			testTran.setPrice(35);
			testTran.setProf_id(2);
			testTran.setReser_id(4);
			testTran.setSmoke(true);
			testTran.setStatus("booh");
			testTran.setType("tipo a caso");
			testTran.setUser_id(84);
			testTran.setUser_role("driver");
			TransferDAO.insert(testTran);
			
			
		 
		 
		 
		 
		 
		 LinkedList<Transfer> userTransfers=TransferDAO.readMyOfferings(user);
		 ObjectMapper mapper = new ObjectMapper();
		 String jsonInString = mapper.writeValueAsString(userTransfers);
		 //JsonObject userTranJson = (new JsonParser()).parse(jsonInString).getAsJsonObject();
		 return Response.status(200).entity(jsonInString).build();
		 
	 	}

}
