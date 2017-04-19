package mcsa;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;

public class McsaSegment {
	private int fromTransferID;
	private int toTransferID;
	private boolean animal;
	private boolean handicap;
	private boolean luggage;
	private boolean smoke;
	LinkedList<TimedPoint2D> segmentPath=new LinkedList<TimedPoint2D>();
	
	public McsaSegment(LinkedList<McsaConnection> conList,HashMap<Integer,boolean[]> specialNeeds)
		{
		 Iterator<McsaConnection> iter = conList.iterator();
		 McsaConnection first = iter.next();
		 fromTransferID=first.getTransferID();
		 toTransferID=first.getTransferID();
		 TimedPoint2D firstPoint = new TimedPoint2D();
		 firstPoint.setLatitude(first.getFirst_point().getLatitude());
		 firstPoint.setLongitude(first.getFirst_point().getLongitude());
		 firstPoint.setTouchTime(first.getFirst_point().getTouchTime());
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
			 toAdd.setLatitude(conn.getSecond_point().getLatitude());
			 toAdd.setLongitude(conn.getSecond_point().getLongitude());
			 toAdd.setTouchTime(conn.getSecond_point().getTouchTime());
			 segmentPath.add(toAdd);
		 	}
		}
	
	public McsaSegment(McsaConnection interConn,Transfer passenger) throws Exception
		{
		 fromTransferID=interConn.getTransferID();
		 toTransferID=interConn.getConnectedTo();
		 animal=passenger.isAnimal();
		 handicap=passenger.isHandicap();
		 luggage=passenger.isLuggage();
		 smoke=passenger.isSmoke();
		 GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBA-NgbRwnecHN3cApbnZoaCZH0ld66fT4");
		 DirectionsResult results=null;
		 String from=""+interConn.getFirst_point().getLatitude()+","+interConn.getFirst_point().getLongitude()+"";
		 String to= ""+interConn.getSecond_point().getLatitude()+","+interConn.getSecond_point().getLongitude()+"";
		 //results = DirectionsApi.getDirections(context, from, to).await();
		 
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
		 while(polIter.hasNext())
		 	{
			 TimedPoint2D toAdd = new TimedPoint2D();
			 LatLng actual = polIter.next();
			 toAdd.setLatitude(new Double(nf.format(actual.lat)));
			 toAdd.setLongitude(new Double(nf.format(actual.lng)));
			 segmentPath.add(toAdd);
		 	}
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
