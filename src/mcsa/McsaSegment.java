package mcsa;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;

public class McsaSegment {
	public McsaSegment(){};
	public McsaSegment(int fromTransferID, int toTransferID, boolean animal, boolean handicap, boolean luggage,
			boolean smoke, long segmentDeparture, long segmentArrival, long departureWaitTime, long segmentDuration,
			LinkedList<TimedPoint2D> segmentPath) {
		super();
		this.fromTransferID = fromTransferID;
		this.toTransferID = toTransferID;
		this.animal = animal;
		this.handicap = handicap;
		this.luggage = luggage;
		this.smoke = smoke;
		this.segmentDeparture = segmentDeparture;
		this.segmentArrival = segmentArrival;
		this.departureWaitTime = departureWaitTime;
		this.segmentDuration = segmentDuration;
		this.segmentPath = segmentPath;
	}

	private int fromTransferID;
	private int toTransferID;
	private boolean animal;
	private boolean handicap;
	private boolean luggage;
	private boolean smoke;
	private long segmentDeparture;
	private long segmentArrival;
	private long departureWaitTime;
	private long segmentDuration;
	LinkedList<TimedPoint2D> segmentPath=new LinkedList<TimedPoint2D>();
	
	public McsaSegment(LinkedList<McsaConnection> conList,HashMap<Integer,boolean[]> specialNeeds)
		{
		if(conList.peek().getTransferID()==128&&conList.peek().getConnectedTo()==128)System.out.println("MCSASEGMENT: lista connessioni problematica size "+conList.size());
		 Iterator<McsaConnection> iter = conList.iterator();
		 McsaConnection first = iter.next();
		 fromTransferID=first.getTransferID();
		 toTransferID=first.getTransferID();
		 TimedPoint2D firstPoint = new TimedPoint2D();
		 firstPoint.setLatitude(first.getFirst_point().getLatitude());
		 firstPoint.setLongitude(first.getFirst_point().getLongitude());
		 firstPoint.setTouchTime(first.getFirst_point().getTouchTime());
		 segmentArrival=firstPoint.getTouchTime(); ////era segmentDeparture
		 segmentPath.add(firstPoint);
		 int tranID = first.getTransferID();
		 boolean[] sneeds = specialNeeds.get(new Integer(tranID));
		 animal=sneeds[0];
		 handicap=sneeds[1];
		 luggage=sneeds[2];
		 smoke=sneeds[3];
		 
		 while(iter.hasNext())
		 	{
			 McsaConnection conn = iter.next();
			 TimedPoint2D toAdd = new TimedPoint2D();
			 toAdd.setLatitude(conn.getFirst_point().getLatitude());
			 toAdd.setLongitude(conn.getFirst_point().getLongitude());
			 toAdd.setTouchTime(conn.getFirst_point().getTouchTime());
			 segmentPath.add(toAdd);
			 if(!iter.hasNext())
			 	{
				 TimedPoint2D toAdd2 = new TimedPoint2D();
				 toAdd2.setLatitude(conn.getSecond_point().getLatitude());
				 toAdd2.setLongitude(conn.getSecond_point().getLongitude());
				 toAdd2.setTouchTime(conn.getSecond_point().getTouchTime());
				 segmentPath.add(toAdd2);
				 segmentDeparture=toAdd.getTouchTime(); //era segmentArrival
				 System.out.println("ultimo punto dell'ultima diocane di connection");
			 	}
		 	}
		 Collections.reverse(segmentPath);
		 
		
		 /*
		 System.out.println(System.lineSeparator()+"MCSASEGMENT Printing mcsaSegment"+System.lineSeparator());
		 Iterator<TimedPoint2D> titer = segmentPath.iterator();
		 while(titer.hasNext())
		 	{
			 System.out.println(titer.next());
		 	}
		 System.out.println(System.lineSeparator()+"END MCSA SEGMENT"+System.lineSeparator());*/
		 segmentDuration=segmentArrival-segmentDeparture;
		 if(conList.peek().getTransferID()==128&&conList.peek().getConnectedTo()==128)System.out.println("MCSASEGMENT: dimensioni segmento creato "+segmentPath.size());
		}
	
	public long getSegmentDeparture() {
		return segmentDeparture;
	}

	public long getSegmentArrival() {
		return segmentArrival;
	}

	public McsaSegment(McsaConnection interConn,Transfer passenger,long time,boolean useGoogle) throws Exception
		{
		 fromTransferID=interConn.getTransferID();
		 toTransferID=interConn.getConnectedTo();
		 animal=passenger.isAnimal();
		 handicap=passenger.isHandicap();
		 luggage=passenger.isLuggage();
		 smoke=passenger.isSmoke();
		 segmentDeparture=interConn.getDeparture_timestamp();
		 segmentArrival=interConn.getArrival_timestamp();
		 //segmentDeparture=time;
		 if(useGoogle)
		 {
			 String alternative="AIzaSyBt2BW_V8EvbbFp5t1Qh_U06-7lx3ZhsMI";
			 String original="AIzaSyBA-NgbRwnecHN3cApbnZoaCZH0ld66fT4";
			 GeoApiContext context = new GeoApiContext().setApiKey(alternative);
			 DirectionsResult results=null;
			 String from=""+interConn.getFirst_point().getLatitude()+","+interConn.getFirst_point().getLongitude()+"";
			 String to= ""+interConn.getSecond_point().getLatitude()+","+interConn.getSecond_point().getLongitude()+"";
			 ///////////////////////////////////////////System.out.println("MCSASEGMENT from: "+interConn.getFirst_point().toString()+" to: "+interConn.getSecond_point().toString());
			 //results = DirectionsApi.getDirections(context, from, to).await();
			 
			 DirectionsApiRequest req=DirectionsApi.newRequest(context);
			 req.mode(TravelMode.WALKING);
			 req.origin(from);
			 req.destination(to);
			 results=req.await();
			 DirectionsRoute[] routes = results.routes;
			 //DirectionsLeg lines=routes[0].legs[0];
			 //DirectionsStep[] steps = lines.steps;
			 //segmentArrival =segmentDeparture+(lines.duration.inSeconds*1000);
			 EncodedPolyline poly = routes[0].overviewPolyline;
			 List<LatLng> polyList = poly.decodePath();
			 Iterator<LatLng> polIter = polyList.iterator();
			 NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
			 nf.setMaximumFractionDigits(5);    
			 nf.setMinimumFractionDigits(5);
			 nf.setGroupingUsed(false);
			//////////////////////////////////////// System.out.println("MCSASEGMENT polylist lenght:"+polyList.size());
			 LatLng firstLatLng =polIter.next();
			 TimedPoint2D firstPoint= new TimedPoint2D();
			 firstPoint.setLatitude(new Double(nf.format(firstLatLng.lat)));
			 firstPoint.setLongitude(new Double(nf.format(firstLatLng.lng)));
			 segmentPath.add(firstPoint);
			 LatLng lastLatLng = null;
			 while(polIter.hasNext())
			 	{
				 TimedPoint2D toAdd = new TimedPoint2D();
				 LatLng actual = polIter.next();
				 toAdd.setLatitude(new Double(nf.format(actual.lat)));
				 toAdd.setLongitude(new Double(nf.format(actual.lng)));
				 segmentPath.add(toAdd);
				 lastLatLng=actual;
			 	}
			/* if(polyList.size()==1)
			 	{
				 segmentArrival=segmentDeparture+(travelTime(evaluateDistance(firstLatLng,firstLatLng)));
			 	}else segmentArrival=segmentDeparture+(travelTime(evaluateDistance(firstLatLng,lastLatLng)));
			 */
		 }else
		 	{
			 segmentPath.add(interConn.getFirst_point());
			 segmentPath.add(interConn.getSecond_point());
		 	}
		 segmentDuration=segmentArrival-segmentDeparture;
		}
	
	public void updateSegmentPath() throws Exception 
		{
		 if(this.getSegmentPath().size()!=2) throw new McsaException("Called update segment for a segment which already have a defined path");
		 else
		 	{
			 TimedPoint2D fromPoint = this.getSegmentPath().getFirst();
			 TimedPoint2D toPoint = this.getSegmentPath().getLast();
			 String alternative="AIzaSyBt2BW_V8EvbbFp5t1Qh_U06-7lx3ZhsMI";
			 String original="AIzaSyBA-NgbRwnecHN3cApbnZoaCZH0ld66fT4";
			 GeoApiContext context = new GeoApiContext().setApiKey(alternative);
			 DirectionsResult results=null;
			 String from=""+fromPoint.getLatitude()+","+fromPoint.getLongitude()+"";
			 String to= ""+toPoint.getLatitude()+","+toPoint.getLongitude()+"";
			 DirectionsApiRequest req=DirectionsApi.newRequest(context);
			 req.mode(TravelMode.WALKING);
			 req.origin(from);
			 req.destination(to);
			 results=req.await();
			 DirectionsRoute[] routes = results.routes;
			 EncodedPolyline poly = routes[0].overviewPolyline;
			 List<LatLng> polyList = poly.decodePath();
			 Iterator<LatLng> polIter = polyList.iterator();
			 NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
			 nf.setMaximumFractionDigits(5);    
			 nf.setMinimumFractionDigits(5);
			 nf.setGroupingUsed(false);
			 LinkedList<TimedPoint2D> updatedPath = new LinkedList<TimedPoint2D>();
			 LatLng firstLatLng =polIter.next();
			 TimedPoint2D firstPoint= new TimedPoint2D();
			 firstPoint.setLatitude(new Double(nf.format(firstLatLng.lat)));
			 firstPoint.setLongitude(new Double(nf.format(firstLatLng.lng)));
			 updatedPath.add(firstPoint);
			 LatLng lastLatLng = null;
			 while(polIter.hasNext())
			 	{
				 TimedPoint2D toAdd = new TimedPoint2D();
				 LatLng actual = polIter.next();
				 toAdd.setLatitude(new Double(nf.format(actual.lat)));
				 toAdd.setLongitude(new Double(nf.format(actual.lng)));
				 updatedPath.add(toAdd);
				 lastLatLng=actual;
			 	}
			 this.segmentPath=updatedPath;
		 	}
		}
	

	private long travelTime(double distance)
	{
	 double meanSpeed = 1.39;
	 double timeSeconds = distance/meanSpeed;
	 double millitime =timeSeconds*1000;
	 if(millitime<0) System.err.println("che e'successo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!?"+distance);
	 return Math.round(millitime);
	}
	
	private double evaluateDistance(LatLng previous,LatLng actual)
	{
		System.out.println("MCSASEGMENT evaluate distance input previousNull:"+(previous==null)+" actualNull:"+(actual==null));
	/*double dlon = pPoint.getLongitude()-dPoint.getLongitude();
	double dlat = pPoint.getLatitude()-dPoint.getLatitude();
	double a = Math.pow((Math.sin(dlat/2)),2) + Math.cos(dPoint.getLatitude());
	
			dlon = lon2 - lon1 
			dlat = lat2 - lat1 
			a = (sin(dlat/2))^2 + cos(lat1) * cos(lat2) * (sin(dlon/2))^2 
			c = 2 * atan2( sqrt(a), sqrt(1-a) ) 
			d = R * c (where R is the radius of the Earth)*/
	GeodeticCalculator geoCalc = new GeodeticCalculator();

	Ellipsoid reference = Ellipsoid.WGS84;  

	GlobalPosition pointA = new GlobalPosition(previous.lat, previous.lng, 0.0); // Point A

	GlobalPosition userPos = new GlobalPosition(actual.lat, actual.lng, 0.0); // Point B

	double distance = geoCalc.calculateGeodeticCurve(reference, userPos, pointA).getEllipsoidalDistance(); // Distance between Point A and Point B
	return distance;
	}
	

	public int getFromTransferID() {
		return fromTransferID;
	}

	public int getToTransferID() {
		return toTransferID;
	}

	public LinkedList<TimedPoint2D> getSegmentPath() {
		return segmentPath;
	}

	public long getDepartureWaitTime() {
		return departureWaitTime;
	}

	protected void setDepartureWaitTime(long departureWaitTime) {
		this.departureWaitTime = departureWaitTime;
	}
	public long getSegmentDuration() {
		return segmentDuration;
	}

	public boolean isAnimal() {
		return animal;
	}

	public boolean isHandicap() {
		return handicap;
	}

	public boolean isLuggage() {
		return luggage;
	}

	public boolean isSmoke() {
		return smoke;
	}


	@Override
	public String toString() {
		return "McsaSegment [fromTransferID=" + fromTransferID + ", toTransferID=" + toTransferID + ", animal=" + animal
				+ ", handicap=" + handicap + ", luggage=" + luggage + ", smoke=" + smoke + ", segmentPath="
				+ segmentPath + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (animal ? 1231 : 1237);
		result = prime * result + fromTransferID;
		result = prime * result + (handicap ? 1231 : 1237);
		result = prime * result + (luggage ? 1231 : 1237);
		result = prime * result + ((segmentPath == null) ? 0 : segmentPath.hashCode());
		result = prime * result + (smoke ? 1231 : 1237);
		result = prime * result + toTransferID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		McsaSegment other = (McsaSegment) obj;
		if (animal != other.animal)
			return false;
		if (fromTransferID != other.fromTransferID)
			return false;
		if (handicap != other.handicap)
			return false;
		if (luggage != other.luggage)
			return false;
		if (segmentPath == null) {
			if (other.segmentPath != null)
				return false;
		} else if (!segmentPath.equals(other.segmentPath))
			return false;
		if (smoke != other.smoke)
			return false;
		if (toTransferID != other.toTransferID)
			return false;
		return true;
	}

	
	

}
