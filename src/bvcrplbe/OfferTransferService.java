package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import bvcrplbe.domain.NotificationMessage;
import bvcrplbe.domain.Passenger;
import bvcrplbe.domain.Pool;
import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;
import bvcrplbe.domain.UserProfile;
import bvcrplbe.persistence.DaoException;
import bvcrplbe.persistence.McsaSolutionDAO;
import bvcrplbe.persistence.PoolDAO;
import bvcrplbe.persistence.TransferDAO;
import bvcrplbe.persistence.UserProfileDAO;
import csa.CSA;
import mcsa.MCSA;
import mcsa.McsaConnection;
import mcsa.McsaResult;
import mcsa.McsaSegment;
import mcsa.McsaSolution;

@Path("/OfferRide")
public class OfferTransferService {
	
	  @Path("{latSta}/{lonSta}/{latEnd}/{lonEnd}")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getOfferTransferInRange(@PathParam("latSta") double latSta,
			  								  @PathParam("lonSta") double lonSta,
			  								  @PathParam("latEnd") double latEnd,
			  								  @PathParam("lonEnd") double lonEnd){
		  LinkedList<Transfer> transferInRange= new LinkedList<Transfer>();
		  	try {
		  		transferInRange =TransferDAO.readTransferInRange(latSta, lonSta, latEnd, lonEnd);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	ObjectMapper mapper = new ObjectMapper();
		  	String jsonInString = null;
			try {
				jsonInString = mapper.writeValueAsString(transferInRange);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Response.status(Status.OK).entity(jsonInString).build();
		  
	  	}
	  
	  
	  
	  
	  @Path("{userid}")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getUserTransfers(@PathParam("userid") int userid) 
	 	{
		 UserProfile user;
		try {
			user = UserProfileDAO.load(userid);
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL STATE: "+e.getSQLState()+" "+e.getMessage()).build();
		} catch (DaoException e) {
			e.printStackTrace();
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		 LinkedList<Transfer> userTransfers;
		try {
			userTransfers = TransferDAO.readMyOfferings(user);
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL STATE: "+e.getSQLState()+" "+e.getMessage()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		 ObjectMapper mapper = new ObjectMapper();
		 String jsonInString;
		try {
			jsonInString = mapper.writeValueAsString(userTransfers);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		 //JsonObject userTranJson = (new JsonParser()).parse(jsonInString).getAsJsonObject();
		System.out.println("server has this transfer: "+jsonInString);
		 return Response.status(Status.OK).entity(jsonInString).build();
		 
	 	}
	  
	  @POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  public Response registerNewTransfer(String jsonInput)
	  	{
		  	ObjectMapper mapper = new ObjectMapper();
		  	try {
				Transfer toAdd = mapper.readValue(jsonInput, Transfer.class);
				if(toAdd.getUser_id()==0) return Response.status(Status.FORBIDDEN).entity("cannot register a transfer with undefined user_id field").build();
				
				int transId;
				try {
					transId = TransferDAO.insert(toAdd);
					toAdd.setTran_id(transId);
					PoolDAO.writePool(toAdd);
				} catch (ClassNotFoundException e) {

					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
				} catch(DaoException e)
					{
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
					}
				String strTranId = new Integer(transId).toString();
				return Response.status(Status.CREATED).entity(strTranId).build();
			} catch (JsonParseException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
			} catch (JsonMappingException e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (IOException e) {
				e.printStackTrace();
				return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).entity(e.getMessage()).build();
			} catch (SQLException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("SQL STATE: "+e.getSQLState()+" "+e.getMessage()).build();
			}
			//return Response.status(Status.OK).entity("da mettere").build();
		  
	  	}
	  
	  @DELETE
	  @Path("/{userid}/{tranid}/debug")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response deleteRideDebug(@PathParam("userid") int userid,@PathParam("tranid") int driTranId)
	  	{
		  System.out.println("SERVER-DELETE(DEBUG): received delete request for driver "+userid+" and transfer "+driTranId);
		  LinkedList<NotificationMessage> notificationList=null;
			try {
				notificationList = PoolDAO.deletePool(userid, driTranId,true);
			} catch (SQLException | IOException | DaoException | RuntimeException | ClassNotFoundException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing delete request: "+e.getMessage()).build();
			}
			System.out.println("TESTING NOTIFICATION MESSAGE LIST");
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
			if(!errorOccured) return Response.status(Status.OK).entity("Ride deleted and passengers clients correctly informed").build();
			else return Response.status(Status.OK).entity("Ride deleted, NOT ALL PASSENGERS CLIENTS GOT INFOMERD").build();
	  	}
	  
	  
	  @DELETE
	  @Path("/{userid}/{tranid}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response deleteRide(@PathParam("userid") int userid,@PathParam("tranid") int driTranId)
	  	{
		  System.out.println("SERVER-DELETE: received delete request for driver "+userid+" and transfer "+driTranId);
			LinkedList<NotificationMessage> notificationList=null;
			try {
				notificationList = PoolDAO.deletePool(userid, driTranId,false);
			} catch (SQLException | IOException | DaoException | ClassNotFoundException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error processing delete request: "+e.getMessage()).build();
			}
			System.out.println("TESTING NOTIFICATION MESSAGE LIST");
			Client client = Client.create();
			boolean errorOccured=false;
			Iterator<NotificationMessage> iter = notificationList.iterator();
			while(iter.hasNext())
				{
				NotificationMessage message = iter.next();
				String address = message.getCallBackURI();
				WebResource resource = client.resource(address);
				String responseMessage = message.getMessage();
				ClientResponse response = resource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class,responseMessage);
				if(response.getStatus()!=200) errorOccured=true;
				System.out.println(message.toString());
				}
			if(!errorOccured) return Response.status(Status.OK).entity("Ride deleted and passengers clients correctly informed").build();
			else return Response.status(Status.OK).entity("Ride deleted, NOT ALL PASSENGERS CLIENTS GOT INFOMERD").build();
			
			
		 
		  /*TEST CALLBACK DOWN HERE
		  Client client = Client.create();
		  String address="http://localhost:8080/testCallback/callback/driver/delete/";
		  WebResource resource = client.resource(address);
		  String resString = "SERVER: DELETED BOOKED RIDE RELATIVE TO TRANSFER"+driTranId;
		  ClientResponse response = resource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, resString);
		  if(response.getStatus()!=200)
		  	{
			  System.out.println("something went wrong");
			  return Response.status(Status.SERVICE_UNAVAILABLE).entity("Unable to inform passenger of transfer cancellation").build();
			}else return Response.status(Status.OK).entity("Transfer deleted, and passengers informed").build();*/
	  	}
	  
	  
	  @Path("/pool/{userid}/{transferid}")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getPool(@PathParam("userid") int userid, @PathParam("transferid") int poolid)
	  	{
		  Pool result=null;
		  try {
			  	result = PoolDAO.readPool(userid, poolid);
			  } catch (SQLException | IOException | ClassNotFoundException e) {
							e.printStackTrace();
							if(e instanceof SQLException)return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving pool object:"+System.lineSeparator()+e.getMessage()).build();
							if(e instanceof IOException) return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error deserializing pool object"+System.lineSeparator()+e.getMessage()).build();
			  }
		  ObjectMapper mapper = new ObjectMapper();
		  String resvalue=null;
		  try {
			   resvalue = mapper.writeValueAsString(result);
			  } catch (JsonProcessingException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error serializing response content"+System.lineSeparator()+e.getMessage()).build();
			  }
		  return Response.status(Status.OK).entity(resvalue).build();
	  	}
	  
	  /*
	  @Path("/pool/{userid}/{transferid}")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response getPool(@PathParam("userid") int userid, @PathParam("transferid") int poolid)
	  	{
		  TimedPoint2D a = new TimedPoint2D(33.234,55.324234,1234124312);
		  TimedPoint2D s = new TimedPoint2D(33.234,55.324234,1234124312);
		  TimedPoint2D d = new TimedPoint2D(33.234,55.324234,1234124312);
		  TimedPoint2D f = new TimedPoint2D(33.234,55.324234,1234124312);
		  LinkedList<TimedPoint2D> path = new LinkedList<TimedPoint2D>();
		  path.add(a);
		  path.add(s);
		  path.add(d);
		  path.add(f);
		  Passenger q = new Passenger(100, a, d);
		  Passenger w = new Passenger(123, s, d);
		  Passenger e = new Passenger(145, s, f);
		  LinkedList<Passenger> passengers = new LinkedList<Passenger>();
		  passengers.add(q);
		  passengers.add(w);
		  passengers.add(e);
		  Pool pool = new Pool(23, 89, path, passengers);
		  ObjectMapper mapper = new ObjectMapper();
		  String responseString=null;
		  try {
			responseString = mapper.writeValueAsString(pool);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  
		  return Response.status(Status.OK).entity(responseString).build();
	  	}*/
	  
	  /*
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
			Point2D.Double arrpoint = new Point2D.Double();
			arrpoint.setLocation(path.get(path.size()-1).lat,path.get(path.size()-1).lng);
			testTran.setArr_gps(arrpoint);
			testTran.setAva_seats(4);
			testTran.setClass_id(6);
			testTran.setDep_addr("roma");
			Point2D.Double deppoint = new Point2D.Double();
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
		 
	 	}*/

}
