package bvcrplbe.persistence;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.geometric.PGpoint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.model.LatLng;

import bvcrplbe.ConnectionManager;
import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;
import bvcrplbe.domain.UserProfile;



public class TransferDAO implements Serializable{
	
		/**
	 * 
	 */
	private static final long serialVersionUID = -4018466579143180128L;
		/*private static final String INSERT_TRANSFER = "INSERT INTO Transfer(\"Transfer_ID\",\"User_ID\",\"Profile_ID\",\"Class_ID\",\"Reservation_ID\",\"Pool_ID\",\"User_Role\",\"Departure_Address\",\"Arrival_Address\",\"Departure_GPS\","
																			+ "\"Arrival_GPS\",\"Departure_Time\",\"Type\",\"Occupied_Seats\",\"Available_Seats\",\"Animal\",\"Handicap\",\"Smoke\",\"Luggage\",\"Status\",\"Price\",\"Path\",\"Detour_range\",\"Ride_Details\")"
																			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";*/
		private static final String INSERT_TRANSFER = "INSERT INTO Transfer(\"User_ID\",\"Profile_ID\",\"Class_ID\",\"Reservation_ID\",\"Pool_ID\",\"User_Role\",\"Departure_Address\",\"Arrival_Address\",\"Departure_GPS\","
				+ "\"Arrival_GPS\",\"Departure_Time\",\"Type\",\"Occupied_Seats\",\"Available_Seats\",\"Animal\",\"Handicap\",\"Smoke\",\"Luggage\",\"Status\",\"Price\",\"Path\",\"Detour_Range\",\"Ride_Details\",\"Callback_URI\")"
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		public static int insert(Transfer transfer) throws SQLException, JsonParseException, JsonMappingException, IOException, ClassNotFoundException
		{
			Connection con = null;
			PreparedStatement pstm = null;
			ConnectionManager manager = new ConnectionManager();
			con = manager.connect();
			pstm = con.prepareStatement(INSERT_TRANSFER,Statement.RETURN_GENERATED_KEYS);
			//pstm.setInt(1, transfer.getTran_id());
			pstm.setInt(1, transfer.getUser_id());
			pstm.setInt(2, transfer.getProf_id());
			pstm.setInt(3, transfer.getClass_id());
			pstm.setInt(4, transfer.getReser_id());
			pstm.setInt(5, transfer.getPool_id());
			//pstm.setString(6, transfer.getUser_role().toString());
			
			//JsonObject userRoleJ = new JsonObject();
			//userRoleJ.addProperty("user_role", transfer.getUser_role());
			PGobject userRole = new PGobject();
			userRole.setType("json");
			userRole.setValue("{\"role\":"+"\""+transfer.getUser_role()+"\"}");
			
			//userRole.setValue(transfer.getUser_role());
			pstm.setObject(6, userRole);
			
			pstm.setString(7, transfer.getDep_addr());
			pstm.setString(8, transfer.getArr_addr());
			
			PGpoint depGps = new PGpoint(transfer.getDep_gps().getX(),transfer.getDep_gps().getY());
			//pstm.setString(9, transfer.getDep_gps().toString());
			pstm.setObject(9, depGps);
						
			PGpoint arrGps = new PGpoint(transfer.getArr_gps().getX(),transfer.getArr_gps().getY());
			pstm.setObject(10,arrGps);
			Timestamp timestamp = new Timestamp(transfer.getDep_time());
			pstm.setTimestamp(11, timestamp);
			pstm.setString(12, transfer.getType());
			pstm.setInt(13, transfer.getOcc_seats());
			pstm.setInt(14, transfer.getAva_seats());
			pstm.setBoolean(15, transfer.isAnimal());
			pstm.setBoolean(16, transfer.isHandicap());
			pstm.setBoolean(17, transfer.isSmoke());
			pstm.setBoolean(18, transfer.isLuggage());
			pstm.setString(19, transfer.getStatus());
			pstm.setDouble(20, transfer.getPrice());
			
			/*PGobject path = new PGobject();
			path.setType("json");
			path.setValue(transfer.getPath().toString());
			pstm.setObject(21, path);*/
			LinkedList<TimedPoint2D> path=transfer.getPath();
			ObjectMapper mapper = new ObjectMapper();
			String pathInJson = mapper.writeValueAsString(path);
			PGobject pathpg = new PGobject();
			pathpg.setType("json");
			pathpg.setValue(pathInJson);
			pstm.setObject(21, pathpg);
			pstm.setDouble(22,transfer.getDet_range());
			pstm.setString(23, transfer.getRide_details());
			pstm.setString(24, transfer.getCallback_uri());
			pstm.executeUpdate();
			ResultSet chiave = pstm.getGeneratedKeys();
			con.commit();
			con.close();
			chiave.next();
			return chiave.getInt(1);
		}
		
		private static String READ_ALL_TRANSFERS="Select * from Transfer";
		public static LinkedList<Transfer> getAllTransfers() throws ClassNotFoundException, SQLException, JSONException, IOException
		{
			Connection con = null;
			PreparedStatement pstm = null;
			ResultSet rs=null;
			LinkedList<Transfer> result = null;
			ConnectionManager manager = new ConnectionManager();
			con = manager.connect();
			pstm=con.prepareStatement(READ_ALL_TRANSFERS);
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				rs.next();
				result = new LinkedList<Transfer>();
				while(!rs.isAfterLast())
					{
					 Transfer toAdd = new Transfer();
					 toAdd.setTran_id(rs.getInt(1));
					 toAdd.setUser_id(rs.getInt(2));
					 toAdd.setProf_id(rs.getInt(3));
					 toAdd.setClass_id(rs.getShort(4));
					 toAdd.setReser_id(rs.getInt(5));
					 toAdd.setPool_id(rs.getInt(6));
					 JSONObject roleJson = new JSONObject(rs.getString(7));
					 toAdd.setUser_role(roleJson.getString("role"));
					 toAdd.setDep_addr(rs.getString(8));
					 toAdd.setArr_addr(rs.getString(9));
					 PGpoint depGps = new PGpoint(rs.getString(10));
					 Point2D.Double depPoint = new Point2D.Double(depGps.x, depGps.y);
					 toAdd.setDep_gps(depPoint);
					 PGpoint arrGps = new PGpoint(rs.getString(11));
					 Point2D.Double arrPoint = new Point2D.Double(arrGps.x, arrGps.y);
					 toAdd.setArr_gps(arrPoint);
					 Timestamp depTimestamp = rs.getTimestamp(12);
					 toAdd.setDep_time(depTimestamp.getTime());
					 toAdd.setType(rs.getString(13));
					 toAdd.setOcc_seats(rs.getInt(14));
					 toAdd.setAva_seats(rs.getInt(15));
					 toAdd.setAnimal(rs.getBoolean(16));
					 toAdd.setHandicap(rs.getBoolean(17));
					 toAdd.setSmoke(rs.getBoolean(18));
					 toAdd.setLuggage(rs.getBoolean(19));
					 toAdd.setStatus(rs.getString(20));
					 toAdd.setPrice(rs.getDouble(21));
					 
					 String pathString =rs.getString(22);
					 //JsonObject path = (new JsonParser()).parse(pathString).getAsJsonObject();
					 ObjectMapper mapper = new ObjectMapper();
					 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
					 
					 toAdd.setPath(pathFromJson);
					 toAdd.setDet_range(rs.getDouble(23));
					 toAdd.setRide_details(rs.getString(24));
					 toAdd.setCallback_uri(rs.getString(25));
					 result.add(toAdd);
					 rs.next();
					 }
				if(rs!=null) rs.close();
				if(pstm!=null) pstm.close();
				if(con!=null) con.close();
				
				}
			return result;
		}
		
		private static String READ_MY_OFFERINGS="Select * from Transfer WHERE \"User_ID\"=?";
		public static LinkedList<Transfer> readMyOfferings(UserProfile user) throws SQLException, IOException, ClassNotFoundException
			{
			Connection con = null;
			PreparedStatement pstm = null;
			ResultSet rs=null;
			LinkedList<Transfer> result = null;
			ConnectionManager manager = new ConnectionManager();
			con = manager.connect();
			pstm=con.prepareStatement(READ_MY_OFFERINGS);
			pstm.setInt(1, user.getUserID());
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				rs.next();
				result = new LinkedList<Transfer>();
				while(!rs.isAfterLast())
					{
					 Transfer toAdd = new Transfer();
					 toAdd.setTran_id(rs.getInt(1));
					 toAdd.setUser_id(rs.getInt(2));
					 toAdd.setProf_id(rs.getInt(3));
					 toAdd.setClass_id(rs.getShort(4));
					 toAdd.setReser_id(rs.getInt(5));
					 toAdd.setPool_id(rs.getInt(6));
					 
					 /*String roleString = rs.getString(7);
					 JsonObject roleJson = (new JsonParser()).parse(roleString).getAsJsonObject();
					 toAdd.setUser_role(roleJson);*/
					 
					 //System.out.println("che cacchio c'� in questo json?"+rs.getString(7));
					 JSONObject roleJson = new JSONObject(rs.getString(7));
					 toAdd.setUser_role(roleJson.getString("role"));
					 toAdd.setDep_addr(rs.getString(8));
					 toAdd.setArr_addr(rs.getString(9));
					 PGpoint depGps = new PGpoint(rs.getString(10));
					 Point2D.Double depPoint = new Point2D.Double(depGps.x, depGps.y);
					 toAdd.setDep_gps(depPoint);
					 PGpoint arrGps = new PGpoint(rs.getString(11));
					 Point2D.Double arrPoint = new Point2D.Double(arrGps.x, arrGps.y);
					 toAdd.setArr_gps(arrPoint);
					 Timestamp depTimestamp = rs.getTimestamp(12);
					 toAdd.setDep_time(depTimestamp.getTime());
					 toAdd.setType(rs.getString(13));
					 toAdd.setOcc_seats(rs.getInt(14));
					 toAdd.setAva_seats(rs.getInt(15));
					 toAdd.setAnimal(rs.getBoolean(16));
					 toAdd.setHandicap(rs.getBoolean(17));
					 toAdd.setSmoke(rs.getBoolean(18));
					 toAdd.setLuggage(rs.getBoolean(19));
					 toAdd.setStatus(rs.getString(20));
					 toAdd.setPrice(rs.getDouble(21));
					 
					 String pathString =rs.getString(22);
					 //JsonObject path = (new JsonParser()).parse(pathString).getAsJsonObject();
					 ObjectMapper mapper = new ObjectMapper();
					 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
					 
					 toAdd.setPath(pathFromJson);
					 toAdd.setDet_range(rs.getDouble(23));
					 toAdd.setRide_details(rs.getString(24));
					 toAdd.setCallback_uri(rs.getString(25));
					 result.add(toAdd);
					 rs.next();
					 }
				if(rs!=null) rs.close();
				if(pstm!=null) pstm.close();
				if(con!=null) con.close();
				
				}
			return result;
			}
		
		private static String READ_MY_TRANSFER_REQUEST = "SELECT * FROM transfer WHERE \"User_Role\" = '{\"role\":\"passenger\"}' AND \"User_ID\" = ? AND \"Transfer_ID\" = ? ";
		public static Transfer getMySearchRequest(int userId, int transferID) throws SQLException, DaoException, JSONException, IOException, ClassNotFoundException
			{
			Connection con=null;
			PreparedStatement pstm=null;
			ResultSet rs=null;
			Transfer result=null;
			ConnectionManager manager = new ConnectionManager();
			con=manager.connect();
			pstm=con.prepareStatement(READ_MY_TRANSFER_REQUEST);
			pstm.setInt(1, userId);
			pstm.setInt(2, transferID);
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				rs.next();
				result= new Transfer();
				result.setTran_id(rs.getInt(1));
				 result.setUser_id(rs.getInt(2));
				 result.setProf_id(rs.getInt(3));
				 result.setClass_id(rs.getShort(4));
				 result.setReser_id(rs.getInt(5));
				 result.setPool_id(rs.getInt(6));
				 JSONObject roleJson = new JSONObject(rs.getString(7));
				 result.setUser_role(roleJson.getString("role"));
				 result.setDep_addr(rs.getString(8));
				 result.setArr_addr(rs.getString(9));
				 PGpoint depGps = new PGpoint(rs.getString(10));
				 Point2D.Double depPoint = new Point2D.Double(depGps.x, depGps.y);
				 result.setDep_gps(depPoint);
				 PGpoint arrGps = new PGpoint(rs.getString(11));
				 Point2D.Double arrPoint = new Point2D.Double(arrGps.x, arrGps.y);
				 result.setArr_gps(arrPoint);
				 Timestamp depTimestamp = rs.getTimestamp(12);
				 result.setDep_time(depTimestamp.getTime());
				 result.setType(rs.getString(13));
				 result.setOcc_seats(rs.getInt(14));
				 result.setAva_seats(rs.getInt(15));
				 result.setAnimal(rs.getBoolean(16));
				 result.setHandicap(rs.getBoolean(17));
				 result.setSmoke(rs.getBoolean(18));
				 result.setLuggage(rs.getBoolean(19));
				 result.setStatus(rs.getString(20));
				 result.setPrice(rs.getDouble(21));
				 
				 String pathString =rs.getString(22);
				 ObjectMapper mapper = new ObjectMapper();
				 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
				 
				 result.setPath(pathFromJson);
				 result.setDet_range(rs.getDouble(23));
				 result.setRide_details(rs.getString(24));
				 result.setCallback_uri(rs.getString(25));
				}else throw new DaoException("No result, the combination of userid and tranferid is not associated to any ride request in the database");
			 return result;
			}
		
		private static boolean timeConstraint(long drivDepTim,long drivArrTime,long passDepTime,long passArrTime,long extendIntervalBy)
			{
			 long plusMinusTimeExtension=extendIntervalBy;
			 if(plusMinusTimeExtension==Long.MAX_VALUE)return true;
			 if(drivArrTime<passDepTime)
			 	{
				  if((passDepTime-drivArrTime)<plusMinusTimeExtension) return true;
				  else return false;
			 	}
			 if(drivArrTime>passDepTime)
			 	{
				 if(drivArrTime>passArrTime)
				 	{
					  if((drivArrTime-passArrTime)<plusMinusTimeExtension) return true;
					  else return false;
				 	}
			 	}
			 return true;
			}
		private static String READ_ALL_OFFERINGS= " SELECT * FROM transfer WHERE \"User_Role\"= '{\"role\":\"driver\"}' AND \"User_ID\"<>?"; //AND \"Transfer_ID\">120 AND \"Transfer_ID\"<135
		public static LinkedList<Transfer> readAllOfferings(int userid,long depTime,long worstArrival, long extendIntervalBy) throws SQLException, JSONException, IOException, ClassNotFoundException
			{
			Connection con=null;
			PreparedStatement pstm=null;
			ResultSet rs=null;
			LinkedList<Transfer> result=null;
			ConnectionManager manager = new ConnectionManager();
			con=manager.connect();
			pstm=con.prepareStatement(READ_ALL_OFFERINGS);
			pstm.setInt(1, userid);
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				rs.next();
				result=new LinkedList<Transfer>();
				while(!rs.isAfterLast())
					{
					String pathString =rs.getString(22);
					 ObjectMapper mapper = new ObjectMapper();
					 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
					 long drivDepTime=pathFromJson.getFirst().getTouchTime();
					 long drivArrTime=pathFromJson.getLast().getTouchTime();
					 if(timeConstraint(drivDepTime,drivArrTime,depTime,worstArrival,extendIntervalBy)) //&& drivDepTime<worstArrival) || !(drivDepTime>worstArrival)
					 {
						 Transfer toAdd = new Transfer();
						 toAdd.setTran_id(rs.getInt(1));
						 /*if(toAdd.getTran_id()==146)
						 	{
							 System.out.println("drivArrTime:  "+drivArrTime+" drivDepTime: "+drivDepTime);
							 System.out.println("worstArrival: "+worstArrival+" depTime:     "+depTime);
						 	}*/
						 toAdd.setUser_id(rs.getInt(2));
						 toAdd.setProf_id(rs.getInt(3));
						 toAdd.setClass_id(rs.getShort(4));
						 toAdd.setReser_id(rs.getInt(5));
						 toAdd.setPool_id(rs.getInt(6));
						 JSONObject roleJson = new JSONObject(rs.getString(7));
						 toAdd.setUser_role(roleJson.getString("role"));
						 toAdd.setDep_addr(rs.getString(8));
						 toAdd.setArr_addr(rs.getString(9));
						 PGpoint depGps = new PGpoint(rs.getString(10));
						 Point2D.Double depPoint = new Point2D.Double(depGps.x, depGps.y);
						 toAdd.setDep_gps(depPoint);
						 PGpoint arrGps = new PGpoint(rs.getString(11));
						 Point2D.Double arrPoint = new Point2D.Double(arrGps.x, arrGps.y);
						 toAdd.setArr_gps(arrPoint);
						 Timestamp depTimestamp = rs.getTimestamp(12);
						 toAdd.setDep_time(depTimestamp.getTime());
						 toAdd.setType(rs.getString(13));
						 toAdd.setOcc_seats(rs.getInt(14));
						 toAdd.setAva_seats(rs.getInt(15));
						 toAdd.setAnimal(rs.getBoolean(16));
						 toAdd.setHandicap(rs.getBoolean(17));
						 toAdd.setSmoke(rs.getBoolean(18));
						 toAdd.setLuggage(rs.getBoolean(19));
						 toAdd.setStatus(rs.getString(20));
						 toAdd.setPrice(rs.getDouble(21));
						 
						 /*String pathString =rs.getString(22);
						 ObjectMapper mapper = new ObjectMapper();
						 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
						 long drivDepTime=pathFromJson.getFirst().getTouchTime();
						 long drivArrTime=pathFromJson.getLast().getTouchTime();*/
						 
						 toAdd.setPath(pathFromJson);
						 toAdd.setDet_range(rs.getDouble(23));
						 toAdd.setRide_details(rs.getString(24));
						 toAdd.setCallback_uri(rs.getString(25));
						 System.out.println("added transfer: "+toAdd.getTran_id()+" userid:"+toAdd.getUser_id());
						 result.add(toAdd);
					 }
					 rs.next();
					}
				if(rs!=null) rs.close();
				if(pstm!=null) pstm.close();
				if(con!=null) con.close();
				}
			manager=null;
			return result;
			
			}
		
		//private static String READ_TRANSFERS_IN_RANGE= "SELECT * FROM transfer WHERE \"Departure_GPS\"[0]>=? AND \"Departure_GPS\"[1]>=? AND \"Arrival_GPS\"[0]<=? AND \"Arrival_GPS\"[1]<=?";
		private static String READ_TRANSFERS_IN_RANGE= "SELECT * FROM transfer WHERE \"Departure_GPS\"[0]>=? AND \"Departure_GPS\"[1]>=? AND \"Arrival_GPS\"[0]<=? AND \"Arrival_GPS\"[1]<=?";
		
		public static LinkedList<Transfer> readTransferInRange(double latSta,double lonSta,double latEnd,double lonEnd) throws SQLException, JSONException, ClassNotFoundException, IOException
			{
			Connection con = null;
			PreparedStatement pstm = null;
			ResultSet rs=null;
			LinkedList<Transfer> result = null;
			ConnectionManager manager = new ConnectionManager();
			con = manager.connect();
			pstm=con.prepareStatement(READ_TRANSFERS_IN_RANGE);
			pstm.setDouble(1, latSta);
			pstm.setDouble(2, lonSta);
			pstm.setDouble(3, latEnd);
			pstm.setDouble(4, lonEnd);
			rs=pstm.executeQuery();
			if(rs.isBeforeFirst())
				{
				 rs.next();
				 result = new LinkedList<Transfer>();
				 while(!rs.isAfterLast())
				 	{
					 Transfer toAdd = new Transfer();
					 toAdd.setTran_id(rs.getInt(1));
					 toAdd.setUser_id(rs.getInt(2));
					 toAdd.setProf_id(rs.getInt(3));
					 toAdd.setClass_id(rs.getShort(4));
					 toAdd.setReser_id(rs.getInt(5));
					 toAdd.setPool_id(rs.getInt(6));
					 
					 /*String roleString = rs.getString(7);
					 JsonObject roleJson = (new JsonParser()).parse(roleString).getAsJsonObject();
					 toAdd.setUser_role(roleJson);*/
					 
					 //System.out.println("che cacchio c' in questo json?"+rs.getString(7));
					 JSONObject roleJson = new JSONObject(rs.getString(7));
					 toAdd.setUser_role(roleJson.getString("role"));
					 toAdd.setDep_addr(rs.getString(8));
					 toAdd.setArr_addr(rs.getString(9));
					 PGpoint depGps = new PGpoint(rs.getString(10));
					 Point2D.Double depPoint = new Point2D.Double(depGps.x, depGps.y);
					 toAdd.setDep_gps(depPoint);
					 PGpoint arrGps = new PGpoint(rs.getString(11));
					 Point2D.Double arrPoint = new Point2D.Double(arrGps.x, arrGps.y);
					 toAdd.setArr_gps(arrPoint);
					 Timestamp depTimestamp = rs.getTimestamp(12);
					 toAdd.setDep_time(depTimestamp.getTime());
					 toAdd.setType(rs.getString(13));
					 toAdd.setOcc_seats(rs.getInt(14));
					 toAdd.setAva_seats(rs.getInt(15));
					 toAdd.setAnimal(rs.getBoolean(16));
					 toAdd.setHandicap(rs.getBoolean(17));
					 toAdd.setSmoke(rs.getBoolean(18));
					 toAdd.setLuggage(rs.getBoolean(19));
					 toAdd.setStatus(rs.getString(20));
					 toAdd.setPrice(rs.getDouble(21));
					 
					 String pathString =rs.getString(22);
					 //JsonObject path = (new JsonParser()).parse(pathString).getAsJsonObject();
					 ObjectMapper mapper = new ObjectMapper();
					 LinkedList<TimedPoint2D> pathFromJson =mapper.readValue(pathString, new TypeReference<LinkedList<TimedPoint2D>>() {});
					 toAdd.setPath(pathFromJson);
					 toAdd.setDet_range(rs.getDouble(23));
					 toAdd.setRide_details(rs.getString(24));
					 toAdd.setCallback_uri(rs.getString(25));
					 result.add(toAdd);
					 rs.next();
				 	}
				}
			return result;
			
			}
		
		private static String RESTORE_FREE_SEATS = "UPDATE transfer SET \"Occupied_Seats\"=\"Occupied_Seats\"-? WHERE \"Transfer_ID\"=?";
		protected static void restoreFreeSeats(Connection con, PreparedStatement pstm,int driverTransfer,int freeSeats) throws SQLException
			{
			System.out.println("restoring "+freeSeats+" seats to transfer "+driverTransfer);
			pstm=con.prepareStatement(RESTORE_FREE_SEATS);
			pstm.setInt(1, freeSeats);
			pstm.setInt(2, driverTransfer);
			pstm.executeUpdate();
			}
		
		
	
	
	
	
	/*			
		public static void insert(Persona tizio,Citta cit) throws SQLException, ClassNotFoundException
			{
			Connection con=null;
			PreparedStatement PSpersona=null;
			PreparedStatement PScitta=null;
			PreparedStatement PSrnato=null;
			PreparedStatement PSfamiglia=null;
			PreparedStatement PSappartiene=null;
			PreparedStatement PSidFam=null;
			ResultSet rs=null;
			try{
				ConnectionManagerSER manager = new ConnectionManagerSER();
				con = manager.getConnection();
				
				PSpersona = con.prepareStatement(INSERT_PERSONA);
				PSpersona.setString(1,tizio.getCF());
				PSpersona.setString(2,tizio.getNome());
				PSpersona.setString(3,tizio.getCognome());
				PSpersona.setString(4,tizio.getUser());
				PSpersona.setString(5,tizio.getPass());
				PSpersona.setString(6,tizio.getSesso());
				PSpersona.executeUpdate();
				
				PScitta=con.prepareStatement(INSERT_CITTA);
				PScitta.setString(1,cit.getNome());
				PScitta.setString(2,cit.getRegione());
				PScitta.executeUpdate();
				
				PSrnato=con.prepareStatement(INSERT_R_NATO);
				PSrnato.setString(1,tizio.getCF());
				PSrnato.setString(2, cit.getNome());
				PSrnato.setString(3,cit.getRegione());
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = sdf.format(tizio.getNato());
				PSrnato.setString(4,currentTime);
				PSrnato.executeUpdate();
				
				PSfamiglia=con.prepareStatement(INSERT_FAMIGLIA);
				PSfamiglia.setString(1,tizio.getCognome());
				PSfamiglia.executeUpdate();
				
				PSidFam=con.prepareStatement(GET_MAX_FAM);
				rs=PSidFam.executeQuery();
				rs.next();
				
				PSappartiene=con.prepareStatement(INSERT_R_APPARTIENE);
				PSappartiene.setString(1,tizio.getCF());
				PSappartiene.setInt(2, rs.getInt(1));
				PSappartiene.executeUpdate();
				
				con.commit();
				}catch (SQLException sql)
							{
							sql.printStackTrace();
							if(con!=null)try
										{
										System.err.println("Errore imprevisto durante la transazione, rollback dell'operazione in corso...");
										con.rollback();
										}catch (SQLException sql2)
													{sql2.printStackTrace();
													System.err.println("Errore fatale, impossibile effettuare il rollback, possibile rischio di inconsistenza della base di dati");
													}
							}
			finally
				{
				if(PSpersona!=null) PSpersona.close();
				if(PScitta!=null) PScitta.close();
				if(PSrnato!=null) PSrnato.close();
				if(PSfamiglia!=null) PSfamiglia.close();
				if(PSappartiene!=null) PSappartiene.close();
				if(PSidFam!=null) PSidFam.close();
				if(rs!=null) rs.close();
				if(con!=null) con.close();
				}
			}

*/
}
