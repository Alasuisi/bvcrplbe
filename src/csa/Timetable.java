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
    int index=1;

    // Timetable constructor: reads all the connections from stdin
    Timetable(LinkedList<Transfer> drivers,Transfer passenger) {
    	System.out.println("passenger transfer "+passenger);
        connections = new ArrayList<Connection>();
        double passDet=passenger.getDet_range();
       
        int totalPoints=0;
        Iterator<Transfer> driverIter = drivers.iterator();
        while(driverIter.hasNext())
        	{
        	 totalPoints=totalPoints+driverIter.next().getPath().size();
        	}
        driverIter =drivers.iterator();
        destination=totalPoints+1;
        System.out.println("destination index "+destination);
        while(driverIter.hasNext())
        	{
        		System.out.println("reading a transfer");
        		Transfer thisTran = driverIter.next();
        		double drivDet=thisTran.getDet_range();
        		Iterator<TimedPoint2D> pathIter=thisTran.getPath().iterator();
        		TimedPoint2D previous=null;
        		while(pathIter.hasNext())
        			{
        			//System.out.println("previous is "+previous);
        			 TimedPoint2D driCoord = pathIter.next();
        			 double sourceToPath =evaluateDistance(driCoord,passenger.getDep_gps());
        			 long stpWalkTime = walkTime(sourceToPath);
        			 //System.out.println("Walktime: "+stpWalkTime);
        			 
       
        			// if(sourceToPath<=passDet+drivDet && (passenger.getDep_time()+stpWalkTime)<driCoord.getTouchTime() )
        			 if(sourceToPath<=passDet)
        			 	{
        				 System.out.println("source to path distance: "+sourceToPath);
            			 System.out.println("from point A-->B"+driCoord+"-->"+passenger.getDep_gps());
        				 System.out.println("generated connection from passenger source, to point "+driCoord+"-->"+passenger.getDep_gps()+" pass walk time"+stpWalkTime+" time to path passenger "+passenger.getDep_time()+stpWalkTime);
        				 System.out.println("");
        				 connections.add(new Connection(driCoord,passenger.getDep_gps(),passenger.getDep_time(),index,source,passenger.getTran_id()));
        				 //index=index++;
        			 	}
        			 double destToPath=evaluateDistance(driCoord,passenger.getArr_gps());
        			 long ptdWalkTime = walkTime(destToPath);
        			 if(destToPath<=passDet+drivDet)
        			 	{
        				 System.out.println("path to destination distance: "+destToPath);
            			 System.out.println("from point A-->B"+driCoord+"-->"+passenger.getArr_gps());
        				 System.out.println("generated connection from point "+driCoord+" to passenger destination");
        				 System.out.println("");
        				 connections.add(new Connection(driCoord,passenger.getArr_gps(),driCoord.getTouchTime()+ptdWalkTime,destination,index,passenger.getTran_id()));
        			 	}
        			 if(previous==null)
        			 	{
        				 previous=driCoord;
        				// if(index>1)index=++index;
        				 System.out.println("previous was null, NOT increasing index "+index);
        			 	}else
        			 		{
        			 			System.out.println("Previous is not null,index "+index+" previous"+previous+" successor"+driCoord);
        			 			connections.add(new Connection(previous,driCoord,index,++index,thisTran.getTran_id()));
        			 			previous=driCoord;
        			 			
        			 		/*int prevIndex = index-1;
        			 		connections.add(new Connection(previous,driCoord,prevIndex,index,thisTran.getTran_id()));
        			 		System.out.println("previous index "+(index-1)+" next index "+index);
        			 		index=++index;
        			 		System.out.println("index increased, new index= "+index);
        			 		previous=driCoord;*/
        			 		}
        			}
        		//previous=null;
        		++index;
        	}
        System.out.println("Printing all connections");
        Iterator<Connection> iter = connections.iterator();
        while(iter.hasNext())
        	{
        	System.out.println(iter.next());
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
    	 if(millitime<0) System.out.println("che porcaddio è successo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!?"+distance);
    	 return Math.round(millitime);
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
