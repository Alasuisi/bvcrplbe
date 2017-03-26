package csa;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;

public class Timetable1 {
	protected List<Connection> connections;
    private int source=0;
    private int destination=0;
    int index=1;
    int totalPoints=0;

    // Timetable constructor: reads all the connections from stdin
    Timetable1(LinkedList<Transfer> drivers,Transfer passenger) {
    	connections = new ArrayList<Connection>();
    	Iterator<Transfer> drivIter = drivers.iterator();
    	while(drivIter.hasNext())
    		{
    		Transfer temp = drivIter.next();
    		System.out.println("new transfer "+temp.getTran_id());
    		totalPoints=totalPoints+temp.getPath().size();
    		LinkedList<TimedPoint2D> thisPath = temp.getPath();
    		Iterator<TimedPoint2D> pathIter=thisPath.iterator();
    		TimedPoint2D previous = null;
       		while(pathIter.hasNext())
    			{
    			 if(previous==null) 
    			 	{
    				 previous=pathIter.next();
    				 index++;
    				 System.out.println("previous was null");
    			 	}else
    			 		{
    			 		int prevIndex=index-1;
    			 		TimedPoint2D pathPoint = pathIter.next();
    			 		connections.add(new Connection(previous,pathPoint,prevIndex,index,temp.getTran_id()));
    			 		previous=pathPoint;
    			 		index++;
    			 		}
    			}
    		}
    	System.out.println("Linking source and destination, dest index="+(totalPoints-1));
    	TimedPoint2D source = new TimedPoint2D();
    	source.setLatitude(passenger.getDep_gps().getX());
    	source.setLongitude(passenger.getDep_gps().getY());
    	source.setTouchTime(passenger.getDep_time());
    	
    	TimedPoint2D destination = new TimedPoint2D();
    	destination.setLatitude(passenger.getArr_gps().getX());
    	destination.setLongitude(passenger.getArr_gps().getY());
    	ArrayList<Connection> complement = new ArrayList<Connection>();
    	Iterator<Connection> linkIter = connections.iterator();
    	while(linkIter.hasNext())
    		{
    			Connection temp=linkIter.next();
    			if(source.getTouchTime()<temp.getFirst_point().getTouchTime())
    				{
    					double sToPath1=evaluateDistance(temp.getFirst_point(),source);
    					if(sToPath1<passenger.getDet_range())
    					{
	    					long sWalktime1=walkTime(sToPath1);
	    					long timeSkew = temp.getFirst_point().getTouchTime()-source.getTouchTime();
	    					if(sWalktime1<timeSkew)
	    						{
	    						complement.add(new Connection(source,temp.getFirst_point(),0,temp.getDeparture_station(),passenger.getTran_id()));
	    						}
    					}
    				}
    			
    			if(source.getTouchTime()<temp.getSecond_point().getTouchTime())
    				{
    					double sToPath2=evaluateDistance(temp.getSecond_point(),source);
    					if(sToPath2<passenger.getDet_range())
    					{
	    					long sWalktime2=walkTime(sToPath2);
	    					long timeSkew=temp.getSecond_point().getTouchTime()-source.getTouchTime();
	    					if(sWalktime2<timeSkew)
	    						{
	    						complement.add(new Connection(source,temp.getSecond_point(),0,temp.getArrival_station(),passenger.getTran_id()));
	    						}
    					}
    				}
    			
    			double pToDest1=evaluateDistance(temp.getFirst_point(),destination);
    			if(pToDest1<passenger.getDet_range())
    				{
	    			long pWalktime1=walkTime(pToDest1);
	    			destination.setTouchTime((temp.getDeparture_timestamp()+pWalktime1));
	    			complement.add(new Connection(temp.getFirst_point(),destination,temp.getDeparture_station(),(totalPoints+1),passenger.getTran_id()));
    				}
    			
    			double pToDest2=evaluateDistance(temp.getSecond_point(),destination);
    			if(pToDest2<passenger.getDet_range())
    				{
	    			long pWalktime2=walkTime(pToDest2);
	    			destination.setTouchTime((temp.getArrival_timestamp()+pWalktime2));
	    			complement.add(new Connection(temp.getSecond_point(),destination,temp.getArrival_station(),(totalPoints+1),passenger.getTran_id()));
    				}
    			
    		}
    	
    	/*System.out.print("Printing connections"+System.lineSeparator());
    	Iterator<Connection> connIter = connections.iterator();
    	while(connIter.hasNext())
    		{
    		System.out.println(connIter.next().toString());
    		}*/
    	System.out.println("Printing complement"+System.lineSeparator());
    	Iterator<Connection> compIter = complement.iterator();
    	while(compIter.hasNext())
    		{
    		System.out.println(compIter.next().toString());
    		}
    }
    
    
    

	public int getSourceIndex()
    	{
    	 return source;
    	}
    public int getDestinationIndex()
    	{
    		return destination;
    	}
    
    private long walkTime(double distance)
    	{
    	 double meanSpeed = 1.39;
    	 double timeSeconds = distance/meanSpeed;
    	 double millitime =timeSeconds*1000;
    	 if(millitime<0) System.err.println("che e'successo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!?"+distance);
    	 return Math.round(millitime);
    	}
    
    private double evaluateDistance(TimedPoint2D pPoint,TimedPoint2D dPoint)
    	{
    	Point2D.Double secondPoint = new Point2D.Double(dPoint.getLatitude(), dPoint.getLongitude());
    	return evaluateDistance(pPoint,secondPoint);
    	}
    private double evaluateDistance(TimedPoint2D pPoint,Point2D.Double dPoint)
    	{
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

    	GlobalPosition pointA = new GlobalPosition(dPoint.getX(), dPoint.getY(), 0.0); // Point A

    	GlobalPosition userPos = new GlobalPosition(pPoint.getLatitude(), pPoint.getLongitude(), 0.0); // Point B

    	double distance = geoCalc.calculateGeodeticCurve(reference, userPos, pointA).getEllipsoidalDistance(); // Distance between Point A and Point B
    	return distance;
    	}

}
