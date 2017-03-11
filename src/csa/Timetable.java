package csa;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;



public class Timetable {
    protected List<Connection> connections;
    private int source=0;
    private int destination=0;

    // Timetable constructor: reads all the connections from stdin
    Timetable(LinkedList<Transfer> drivers,Transfer passenger) {
        connections = new ArrayList<Connection>();
        double passDet=passenger.getDet_range();
        int index=1;
        int totalPoints=0;
        Iterator<Transfer> driverIter = drivers.iterator();
        while(driverIter.hasNext())
        	{
        	 totalPoints=totalPoints+driverIter.next().getPath().size();
        	}
        driverIter =drivers.iterator();
        destination=totalPoints+1;
        while(driverIter.hasNext())
        	{
        		Transfer thisTran = driverIter.next();
        		double drivDet=thisTran.getDet_range();
        		Iterator<TimedPoint2D> pathIter=thisTran.getPath().iterator();
        		TimedPoint2D previous=null;
        		while(pathIter.hasNext())
        			{
        			 TimedPoint2D driCoord = pathIter.next();
        			 double sourceToPath =evaluateDistance(driCoord,passenger.getDep_gps());
        			 long stpWalkTime = walkTime(sourceToPath-drivDet);
        			 if(sourceToPath<=passDet+drivDet && (passenger.getDep_time()+stpWalkTime)<driCoord.getTouchTime() )
        			 	{
        				 connections.add(new Connection(driCoord,passenger.getDep_gps(),passenger.getDep_time(),index,source,passenger.getTran_id()));
        				 //index=index++;
        			 	}
        			 double destToPath=evaluateDistance(driCoord,passenger.getArr_gps());
        			 long ptdWalkTime = walkTime(destToPath-drivDet);
        			 if(destToPath<=passDet+drivDet)
        			 	{
        				 connections.add(new Connection(driCoord,passenger.getArr_gps(),driCoord.getTouchTime()+ptdWalkTime,index,destination,passenger.getTran_id()));
        			 	}
        			 if(previous==null)
        			 	{
        				 previous=driCoord;
        				 index=index++;
        			 	}else
        			 		{
        			 		connections.add(new Connection(previous,driCoord,index--,index,thisTran.getTran_id()));
        			 		index=index++;
        			 		previous=driCoord;
        			 		}
        			}
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
    	 return Math.round(timeSeconds*1000);
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
