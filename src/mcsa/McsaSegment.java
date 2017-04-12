package mcsa;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import bvcrplbe.domain.TimedPoint2D;

public class McsaSegment {
	private int fromTransferID;
	private int toTransferID;
	LinkedList<TimedPoint2D> segmentPath=new LinkedList<TimedPoint2D>();
	
	public McsaSegment(LinkedList<McsaConnection> conList)
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
	
	public McsaSegment(McsaConnection interConn) throws Exception
		{
		 fromTransferID=interConn.getTransferID();
		 toTransferID=interConn.getConnectedTo();
		 GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBA-NgbRwnecHN3cApbnZoaCZH0ld66fT4");
		 DirectionsResult results=null;
		 String from=""+interConn.getFirst_point().getLatitude()+","+interConn.getFirst_point().getLongitude()+"";
		 String to= ""+interConn.getSecond_point().getLatitude()+","+interConn.getSecond_point().getLongitude()+"";
		 results = DirectionsApi.getDirections(context, from, to).await();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromTransferID;
		result = prime * result + ((segmentPath == null) ? 0 : segmentPath.hashCode());
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
		if (fromTransferID != other.fromTransferID)
			return false;
		if (segmentPath == null) {
			if (other.segmentPath != null)
				return false;
		} else if (!segmentPath.equals(other.segmentPath))
			return false;
		if (toTransferID != other.toTransferID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "McsaSegment [fromTransferID=" + fromTransferID + ", toTransferID=" + toTransferID + ", segmentPath="
				+ segmentPath + "]";
	}
	

}
