package csa;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
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



public class Timetable {
    protected List<Connection> connections;
    private int source=0;
    private int destination=0;
    int index=1;

    // Timetable constructor: reads all the connections from stdin
    Timetable(LinkedList<Transfer> drivers,Transfer passenger) {
    	///////////LOGGER///////////////////
    	Logger logger = Logger.getLogger("MyLog");  
        FileHandler fh;  

        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("/Users/diegolisi/Desktop/LOGS/MyLogFile.txt");  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

            // the following statement is used to log any messages  
            logger.info("My first log dio can");  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  

        
    	
    	///////////////////////////////////////////////////
    	
    	
    	
    	
    	logger.info("passenger transfer "+passenger);
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
        logger.info("destination index "+destination);
        while(driverIter.hasNext())
        	{
        		logger.info("reading a transfer");
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
        			 if(sourceToPath<=passDet+drivDet) ///CAMBIATO QUI SE CI SONO COSE STRANE RIMUOVER DRIVDET
        			 	{
        				/* logger.info("source to path distance: "+sourceToPath);
            			 logger.info("from point A-->B"+driCoord+"-->"+passenger.getDep_gps());
        				 logger.info("generated connection from passenger source, to point "+driCoord+"-->"+passenger.getDep_gps()+" pass walk time"+stpWalkTime+" time to path passenger "+passenger.getDep_time()+stpWalkTime);
        				 logger.info("");*/
        				 connections.add(new Connection(driCoord,passenger.getDep_gps(),passenger.getDep_time(),index,source,passenger.getTran_id()));
        				 //index=index++;
        			 	}
        			 double destToPath=evaluateDistance(driCoord,passenger.getArr_gps());
        			 long ptdWalkTime = walkTime(destToPath);
        			 if(destToPath<=passDet+drivDet)
        			 	{
        				 /*logger.info("path to destination distance: "+destToPath);
            			 logger.info("from point A-->B"+driCoord+"-->"+passenger.getArr_gps());
        				 logger.info("generated connection from point "+driCoord+" to passenger destination");
        				 logger.info("");*/
        				 connections.add(new Connection(driCoord,passenger.getArr_gps(),driCoord.getTouchTime()+ptdWalkTime,destination,index,passenger.getTran_id()));
        			 	}
        			 if(previous==null)
        			 	{
        				 previous=driCoord;
        				// if(index>1)index=++index;
        				 logger.info("previous was null, NOT increasing index "+index);
        			 	}else
        			 		{
        			 			logger.info("Previous is not null,index "+index+" previous"+previous+" successor"+driCoord);
        			 			connections.add(new Connection(previous,driCoord,index,++index,thisTran.getTran_id()));
        			 			//connections.add(new Connection(driCoord,previous,++index,index,thisTran.getTran_id()));
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
        
        //checking if possible connection between different transfers can be made
        /*
        logger.info("Adding interconnection between driver transfers:");
        int connSize = connections.size();
        int first=0;
        int next=1;
        while(first!=connSize)
        	{
        		while(next!=connSize)
        			{
        			 Connection firstCon=connections.get(first);
        			 Connection secondCon = connections.get(next);
        			 if(firstCon.getTransferID()!=secondCon.getTransferID()){
        				 
        			 if(firstCon.getDeparture_station()!=source && secondCon.getDeparture_station()!=source){
        			 double distance =evaluateDistance(firstCon.getFirst_point(),secondCon.getFirst_point());
			 			if(distance<=passDet)
			 				{
			 				logger.info("spatial compatibility between 1 and 1");
			 				 long walktime = walkTime(distance);
			 				 long timeSkew =Math.abs(firstCon.getFirst_point().getTouchTime()-secondCon.getFirst_point().getTouchTime());
			 				 if(walktime<=timeSkew||true)
			 				 	{
			 					logger.info("Compatible time schedule (1,1),evaluating");
			 					connections.add(new Connection(firstCon.getFirst_point(),secondCon.getFirst_point(),firstCon.getDeparture_station(),secondCon.getDeparture_station(),passenger.getTran_id()));
			 					connections.add(new Connection(secondCon.getFirst_point(),firstCon.getFirst_point(),secondCon.getDeparture_station(),firstCon.getDeparture_station(),passenger.getTran_id()));
			 							 				 	}
			 				}
        			}
        			 if(firstCon.getDeparture_station()!=0 && secondCon.getArrival_station()!=destination){
        			 double distance =evaluateDistance(firstCon.getFirst_point(),secondCon.getSecond_point());
        			 if(distance<=passDet)
        			 	{
        				 logger.info("spatial compatibility between 1 and 2");
        				 long walktime = walkTime(distance);
		 				 long timeSkew =Math.abs(firstCon.getFirst_point().getTouchTime()-secondCon.getSecond_point().getTouchTime());
		 				 if(walktime<=timeSkew||true)
		 				 	{
		 					logger.info("Compatible time schedule (1,2),evaluating");
		 					connections.add(new Connection(firstCon.getFirst_point(),secondCon.getSecond_point(),firstCon.getDeparture_station(),secondCon.getArrival_station(),passenger.getTran_id()));
		 					connections.add(new Connection(secondCon.getSecond_point(),firstCon.getFirst_point(),secondCon.getArrival_station(),firstCon.getDeparture_station(),passenger.getTran_id()));	 
		 				 	}
        			 	}
        			 }
        			 if(firstCon.getArrival_station()!=destination && secondCon.getDeparture_station()!=source){
        			 double distance = evaluateDistance(firstCon.getSecond_point(),secondCon.getFirst_point());
        			 if(distance<=passDet)
        			 	{
        				 logger.info("spatial compatibility between 2 and 1");
        				 long walktime = walkTime(distance);
		 				 long timeSkew =Math.abs(firstCon.getSecond_point().getTouchTime()-secondCon.getFirst_point().getTouchTime());
		 				 if(walktime<=timeSkew||true)	
			 				 {
		 					 	logger.info("Compatible time schedule (2,1),evaluating");
		 					 	connections.add(new Connection(firstCon.getSecond_point(),secondCon.getFirst_point(),firstCon.getArrival_station(),secondCon.getDeparture_station(),passenger.getTran_id()));
		 					 	connections.add(new Connection(secondCon.getSecond_point(),firstCon.getFirst_point(),secondCon.getArrival_station(),firstCon.getDeparture_station(),passenger.getTran_id()));
			 				 }
        			 	}
        			 }
        			 if(firstCon.getArrival_station()!=destination && secondCon.getArrival_station()!=destination){
        			 double distance = evaluateDistance(firstCon.getSecond_point(),secondCon.getSecond_point());
        			 if(distance<=passDet)
        			 	{
        				 logger.info("spatial compatibility between 2 and 2");
        				 long walktime = walkTime(distance);
        				 long timeSkew = Math.abs(firstCon.getSecond_point().getTouchTime()-secondCon.getSecond_point().getTouchTime());
        				 if(walktime<=timeSkew ||true)
        				 	{
        					 logger.info("Compatible time schedule (2,2),adding interconnection"); 
        					 connections.add(new Connection(firstCon.getSecond_point(),secondCon.getSecond_point(),firstCon.getArrival_station(),secondCon.getArrival_station(),passenger.getTran_id()));
        					 connections.add(new Connection(secondCon.getSecond_point(),firstCon.getSecond_point(),secondCon.getArrival_station(),firstCon.getArrival_station(),passenger.getTran_id()));
        				 	}
        			 	}
        			 }
               			
        			 }
        			 ++next;
        			 
        			}
        		++first;
        		next = first+1;
        	}*/
        
        
        //logger.info("Printing all connections");
        Iterator<Connection> iter = connections.iterator();
        while(iter.hasNext())
        	{
        	System.out.println(iter.next());
        	//logger.info(iter.next().toString());
        	}
        
        
        
        
    }
    
    
    private void activateLog(boolean b) {
		// TODO Auto-generated method stub
		
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
