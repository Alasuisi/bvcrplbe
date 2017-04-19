package bvcrplbe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ws.rs.Consumes;
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

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;
import bvcrplbe.domain.UserProfile;
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
	  
	  
	  @Path("/CSA")
	  @GET
	  public Response testCSA() throws Exception
	  	{
		  try {
			LinkedList<Transfer> allTran = TransferDAO.getAllTransfers();
			LinkedList<Transfer> driverTran = new LinkedList<Transfer>();
			LinkedList<Transfer> passenger = new LinkedList<Transfer>();
			Iterator<Transfer> alliter = allTran.iterator();
			while(alliter.hasNext())
				{
					Transfer thisTran = alliter.next();
					if(thisTran.getUser_role().equals("driver"))
						{
							if(thisTran.getTran_id()>130) driverTran.add(thisTran);
						}
					else passenger.add(thisTran);
				}
			Iterator<Transfer> passIter = passenger.iterator();
			System.out.println("dimensione passenger lista "+passenger.size());
			Transfer toCompute = null;
			while(passIter.hasNext())
				{
					Transfer temp=passIter.next();
					System.out.println("transfer con id "+temp.getTran_id());
				 if(temp.getTran_id()==118) toCompute=temp;
				}
			MCSA mcsa = new MCSA(driverTran,toCompute);
			//csa.computeCSA();
			//mcsa.computeMCSA(30, 0, 0);
			long t1=System.currentTimeMillis();
			mcsa.computeMCSA(toCompute.getDep_time());
			long t2=System.currentTimeMillis();
			System.out.println("compute solution time "+(t2-t1)+" millis");
			long t3 = System.currentTimeMillis();
			mcsa.removeBadOnes();
			long t4 = System.currentTimeMillis();
			System.out.println("removing bad ones time: "+(t4-t3));
			/////////SOLUTION TEST/////////
			/*mcsa.printSolutions(Integer.MAX_VALUE);
			System.out.println(System.lineSeparator());
			McsaResult res = mcsa.getResults();
			LinkedList<McsaSolution> solutions = res.getResults();
			System.out.println("Solution list has lenght: "+solutions.size());
			Iterator<McsaSolution> iter = solutions.iterator();
			while(iter.hasNext())
				{
				McsaSolution temp =iter.next();
				System.out.println(System.lineSeparator()+"transfers:"+temp.getTransferSet()+" changes:"+temp.getChanges()+" wait:"+temp.getTotalWaitTime()+" arrival:"+temp.getArrivalTime());
				LinkedList<McsaConnection> path = temp.getPath();
				Iterator<McsaConnection> pathIter = path.iterator();
				while(pathIter.hasNext())
					{
					System.out.println(pathIter.next());
					}
				}*/
			McsaResult res = mcsa.getResults();
			LinkedList<McsaSolution> solutions = res.getResults();
			System.out.println(System.lineSeparator()+" TESTING THE NEW SOLUTION OBJECT ");
			Iterator<McsaSolution> iter2 =solutions.iterator();
			while(iter2.hasNext())
				{
				 McsaSolution thisSol = iter2.next();
				 LinkedList<McsaSegment> segments = thisSol.getSolution();
				 System.out.println(System.lineSeparator()+"changes:"+thisSol.getChanges()+" transfers:"+thisSol.getTransferSet());
				 System.out.println("passenger needs: animal="+thisSol.isAnimal()+" handicap="+thisSol.isHandicap()+" luggage="+thisSol.isLuggage()+" smoke="+thisSol.isSmoke());
				 System.out.println("passenger occupied seats: "+thisSol.getNeededSeats());
				 Iterator<McsaSegment> segIter = segments.iterator();
				 while(segIter.hasNext())
				 	{
					 McsaSegment segment = segIter.next();
					 System.out.println("segment from: "+segment.getFromTransferID()+" to: "+segment.getToTransferID());
					 System.out.println("segment accepted needs: animal="+segment.isAnimal()+" handicap="+segment.isHandicap()+" luggage="+segment.isLuggage()+" smoke="+segment.isSmoke());
					 LinkedList<TimedPoint2D> path = segment.getSegmentPath();
					 Iterator<TimedPoint2D> pathIter = path.iterator();
					 while(pathIter.hasNext())
					 	{
						 TimedPoint2D toPrint =pathIter.next();
						 System.out.println(toPrint.getLatitude()+","+toPrint.getLongitude());
					 	}
				 	}
				}
			
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString;
			jsonInString=mapper.writeValueAsString(solutions);
			System.out.println(jsonInString);
			////////////////////////////////////
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
