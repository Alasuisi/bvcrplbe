package mcsa;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import bvcrplbe.domain.TimedPoint2D;
import bvcrplbe.domain.Transfer;



public class McsaTimetable {
	protected ArrayList<McsaConnection> connections;
    private int source=0;
    private int destinationIndex=0;
    private int index=0;
    
    public McsaTimetable(LinkedList<Transfer> drivers, Transfer passenger)
    	{
    	 connections= new ArrayList<McsaConnection>();
    	 double passDet=passenger.getDet_range();
    	 
    	 TimedPoint2D departure=null;
    	 Iterator<Transfer> driverIter = drivers.iterator();
    	 while(driverIter.hasNext())
    	 	{
    		 Transfer thisTran = driverIter.next();
    		 Iterator<TimedPoint2D> pathIter = thisTran.getPath().iterator();
    		 while(pathIter.hasNext())
    		 	{
    			 if(departure==null) 
    			 	{
    				 departure=pathIter.next();
    				 index++;
    			 	}
    			 else
    			 	{
    				 TimedPoint2D arrival = pathIter.next();
    				 connections.add(new McsaConnection(departure,arrival,index,index+1,thisTran.getTran_id()));
    				 departure=arrival;
    				 index++;
    			 	}
    		 	}
    		 index++;
    	 	}
    	 destinationIndex=index;
    	 
    	 TimedPoint2D passDep = new TimedPoint2D(passenger.getDep_gps().getX(),passenger.getDep_gps().getY(),passenger.getDep_time());
    	 TimedPoint2D passArr =new TimedPoint2D(passenger.getArr_gps().getX(),passenger.getArr_gps().getY(),0);
    	 ArrayList<McsaConnection>srcDstList = new ArrayList<McsaConnection>();
    	 Iterator<McsaConnection> srcDstIter = connections.iterator();
    	 int lastConTranId=0;
    	 while(srcDstIter.hasNext())
    	 	{
    		 McsaConnection thisCon=srcDstIter.next();
    		 if(thisCon.getTransferID()!=lastConTranId)
    		 	{
    			 double srcTo1dst = evaluateDistance(passDep,thisCon.getFirst_point());
    			 double srcTo2dst = evaluateDistance(passDep,thisCon.getSecond_point());
    			 double desTo1dst = evaluateDistance(passArr,thisCon.getFirst_point());
    			 double desTo2dst = evaluateDistance(passArr,thisCon.getSecond_point());
    			 long srcTo1time = walkTime(srcTo1dst)+passenger.getDep_time();
    			 long srcTo2time = walkTime(srcTo2dst)+passenger.getDep_time();
    			 long desTo1time = walkTime(desTo1dst)+thisCon.getFirst_point().getTouchTime();
    			 long desTo2time = walkTime(desTo2dst)+thisCon.getSecond_point().getTouchTime();
    			 if(srcTo1dst<=passDet)
    				 if(srcTo1time<=thisCon.getFirst_point().getTouchTime())
    				 	{
    					 TimedPoint2D pathPoint = thisCon.getFirst_point();
    					 pathPoint.setTouchTime(srcTo1time);
    					 McsaConnection toAdd = new McsaConnection(passDep,pathPoint,source,thisCon.getDeparture_station(),passenger.getTran_id());
    					 toAdd.setConnectedTo(thisCon.getTransferID());
    					 srcDstList.add(toAdd);
    				 	}
    			 if(srcTo2dst<=passDet)
    			 	{
    				 if(srcTo2time<=thisCon.getSecond_point().getTouchTime())
    				 	{
    					 TimedPoint2D pathPoint = thisCon.getSecond_point();
    					 pathPoint.setTouchTime(srcTo2time);
    					 McsaConnection toAdd = new McsaConnection(passDep,pathPoint,source,thisCon.getArrival_station(),passenger.getTran_id());
    					 toAdd.setConnectedTo(thisCon.getTransferID());
    					 srcDstList.add(toAdd);
    				 	}
    			 	}
    			 if(desTo1dst<=passDet)
    			 	{
    				 TimedPoint2D pathPoint= thisCon.getFirst_point();
    				 passArr.setTouchTime(desTo1time);
    				 McsaConnection toAdd = new McsaConnection(pathPoint,passArr,thisCon.getDeparture_station(),destinationIndex,passenger.getTran_id());
    				 toAdd.setConnectedTo(thisCon.getTransferID());
    				 srcDstList.add(toAdd);
    			 	}
    			 if(desTo2dst<=passDet)
    			 	{
    				 TimedPoint2D pathPoint = thisCon.getSecond_point();
    				 passArr.setTouchTime(desTo2time);
    				 McsaConnection toAdd = new McsaConnection(pathPoint,passArr,thisCon.getArrival_station(),destinationIndex,passenger.getTran_id());
    				 toAdd.setConnectedTo(thisCon.getTransferID());
    				 srcDstList.add(toAdd);
    			 	}
    			 lastConTranId=thisCon.getTransferID();
    		 	}else
    		 		{
    		 		double srcTo2dst = evaluateDistance(passDep,thisCon.getSecond_point());
    		 		double desTo2dst = evaluateDistance(passArr,thisCon.getSecond_point());
    		 		long srcTo2time = walkTime(srcTo2dst)+passenger.getDep_time();
    		 		long desTo2time = walkTime(desTo2dst)+thisCon.getSecond_point().getTouchTime();
    		 		if(srcTo2dst<=passDet)
	    			 	{
	    				 if(srcTo2time<=thisCon.getSecond_point().getTouchTime())
	    				 	{
	    					 TimedPoint2D pathPoint = thisCon.getSecond_point();
	    					 pathPoint.setTouchTime(srcTo2time);
	    					 McsaConnection toAdd = new McsaConnection(passDep,pathPoint,source,thisCon.getArrival_station(),passenger.getTran_id());
	    					 toAdd.setConnectedTo(thisCon.getTransferID());
	    					 srcDstList.add(toAdd);
	    				 	}
	    			 	}
    		 		 if(desTo2dst<=passDet)
	     			 	{
	     				 TimedPoint2D pathPoint = thisCon.getSecond_point();
	     				 passArr.setTouchTime(desTo2time);
	     				 McsaConnection toAdd = new McsaConnection(pathPoint,passArr,thisCon.getArrival_station(),destinationIndex,passenger.getTran_id());
	     				 toAdd.setConnectedTo(thisCon.getTransferID());
	     				 srcDstList.add(toAdd);
	     			 	}
    		 		}
    	 	}
    	 
    	 Iterator<McsaConnection> connIter = connections.iterator();
    	 while(connIter.hasNext())
    	 	{
    		 System.out.println(connIter.next());
    	 	}
    	 System.out.println(System.lineSeparator()+"Printing source and destination connection to paths"+System.lineSeparator());
    	 Iterator<McsaConnection> linkIter = srcDstList.iterator();
	     while(linkIter.hasNext())
	     	{
	    	 System.out.println(linkIter.next());
	     	}
    	 System.out.println(System.lineSeparator()+"MCSATIMETABLE.JAVA arrival station index:"+destinationIndex);
	        System.out.println("MCSATIMETABLE.JAVA connection list size: "+connections.size());
	        connections.addAll(srcDstList);
	     
    	}
    	
    
    public void FAILMcsaTimetable(LinkedList<Transfer> drivers,Transfer passenger)
    	{
	    	connections = new ArrayList<McsaConnection>();
	        double passDet=passenger.getDet_range();
	        int totalPoints=0;
	        Iterator<Transfer> driverIter = drivers.iterator();
	        while(driverIter.hasNext())
	        	{
	        	 totalPoints=totalPoints+driverIter.next().getPath().size();
	        	}
	        driverIter =drivers.iterator();
	        destinationIndex=totalPoints+1;
	        
	        while(driverIter.hasNext())
        	{
        		//logger.info("reading a transfer");
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
        				 connections.add(new McsaConnection(driCoord,passenger.getDep_gps(),passenger.getDep_time(),index,source,passenger.getTran_id()));
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
        				 connections.add(new McsaConnection(driCoord,passenger.getArr_gps(),driCoord.getTouchTime()+ptdWalkTime,index,destinationIndex,passenger.getTran_id()));
        			 	}
        			 if(previous==null)
        			 	{
        				 previous=driCoord;
        				// if(index>1)index=++index;
        				 //logger.info("previous was null, NOT increasing index "+index);
        			 	}else
        			 		{
        			 			//logger.info("Previous is not null,index "+index+" previous"+previous+" successor"+driCoord);
        			 			connections.add(new McsaConnection(previous,driCoord,index,++index,thisTran.getTran_id()));
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
        		index++;
        	}
	        
	      //checking if possible connection between different transfers can be made
	        
	        //logger.info("Adding interconnection between driver transfers:");
	        LinkedList<McsaConnection> interconn = new LinkedList<McsaConnection>();
	        int connSize = connections.size();
	        int first=0;
	        int next=1;
	        while(first!=connSize)
	        	{
	        		while(next!=connSize)
	        			{
	        			 McsaConnection firstCon=connections.get(first);
	        			 McsaConnection secondCon = connections.get(next);
	        			 if(firstCon.getTransferID()!=secondCon.getTransferID()){
	        				 
	        			 if(firstCon.getDeparture_station()!=source && secondCon.getDeparture_station()!=source){
	        			 double distance =evaluateDistance(firstCon.getFirst_point(),secondCon.getFirst_point());
				 			if(distance<=passDet)
				 				{
				 				//System.out.println("spatial compatibility between 1 and 1");
				 				 long walktime = walkTime(distance);
				 				 long timeSkew =firstCon.getFirst_point().getTouchTime()-secondCon.getFirst_point().getTouchTime();
				 				 if(walktime<=Math.abs(timeSkew))
				 				 	{
				 					//System.out.println("Compatible time schedule (1,1),evaluating");
				 					if(timeSkew<0)interconn.add(new McsaConnection(firstCon.getFirst_point(),secondCon.getFirst_point(),firstCon.getDeparture_station(),secondCon.getDeparture_station(),passenger.getTran_id()));
				 					else interconn.add(new McsaConnection(secondCon.getFirst_point(),firstCon.getFirst_point(),secondCon.getDeparture_station(),firstCon.getDeparture_station(),passenger.getTran_id()));
				 							 				 	}
				 				}
	        			}
	        			 if(firstCon.getDeparture_station()!=0 && secondCon.getArrival_station()!=destinationIndex){
	        			 double distance =evaluateDistance(firstCon.getFirst_point(),secondCon.getSecond_point());
	        			 if(distance<=passDet)
	        			 	{
	        				 //System.out.println("spatial compatibility between 1 and 2");
	        				 long walktime = walkTime(distance);
			 				 long timeSkew =firstCon.getFirst_point().getTouchTime()-secondCon.getSecond_point().getTouchTime();
			 				 if(walktime<=Math.abs(timeSkew))
			 				 	{
			 					//System.out.println("Compatible time schedule (1,2),evaluating");
			 					if(timeSkew<0)interconn.add(new McsaConnection(firstCon.getFirst_point(),secondCon.getSecond_point(),firstCon.getDeparture_station(),secondCon.getArrival_station(),passenger.getTran_id()));
			 					else interconn.add(new McsaConnection(secondCon.getSecond_point(),firstCon.getFirst_point(),secondCon.getArrival_station(),firstCon.getDeparture_station(),passenger.getTran_id()));	 
			 				 	}
	        			 	}
	        			 }
	        			 if(firstCon.getArrival_station()!=destinationIndex && secondCon.getDeparture_station()!=source){
	        			 double distance = evaluateDistance(firstCon.getSecond_point(),secondCon.getFirst_point());
	        			 if(distance<=passDet)
	        			 	{
	        				 //System.out.println("spatial compatibility between 2 and 1");
	        				 long walktime = walkTime(distance);
			 				 long timeSkew =firstCon.getSecond_point().getTouchTime()-secondCon.getFirst_point().getTouchTime();
			 				 if(walktime<=Math.abs(timeSkew))	
				 				 {
			 					//System.out.println("Compatible time schedule (2,1),evaluating");
			 					 	if(timeSkew<0)interconn.add(new McsaConnection(firstCon.getSecond_point(),secondCon.getFirst_point(),firstCon.getArrival_station(),secondCon.getDeparture_station(),passenger.getTran_id()));
			 					 	else interconn.add(new McsaConnection(secondCon.getSecond_point(),firstCon.getFirst_point(),secondCon.getArrival_station(),firstCon.getDeparture_station(),passenger.getTran_id()));
				 				 }
	        			 	}
	        			 }
	        			 if(firstCon.getArrival_station()!=destinationIndex && secondCon.getArrival_station()!=destinationIndex){
	        			 double distance = evaluateDistance(firstCon.getSecond_point(),secondCon.getSecond_point());
	        			 if(distance<=passDet)
	        			 	{
	        				// System.out.println("spatial compatibility between 2 and 2");
	        				 long walktime = walkTime(distance);
	        				 long timeSkew = firstCon.getSecond_point().getTouchTime()-secondCon.getSecond_point().getTouchTime();
	        				 if(walktime<=Math.abs(timeSkew))
	        				 	{
	        					 //System.out.println("Compatible time schedule (2,2),adding interconnection"); 
	        					 if(timeSkew<0)interconn.add(new McsaConnection(firstCon.getSecond_point(),secondCon.getSecond_point(),firstCon.getArrival_station(),secondCon.getArrival_station(),passenger.getTran_id()));
	        					 else interconn.add(new McsaConnection(secondCon.getSecond_point(),firstCon.getSecond_point(),secondCon.getArrival_station(),firstCon.getArrival_station(),passenger.getTran_id()));
	        				 	}
	        			 	}
	        			 }
	               			
	        			 }
	        			 ++next;
	        			 
	        			}
	        		++first;
	        		next = first+1;
	        	}
	        
	        Iterator<McsaConnection> iter = connections.iterator();
	        System.out.println("Printing connection list");
	        while(iter.hasNext())
	        	{
	        	System.out.println(iter.next());
	        	//logger.info(iter.next().toString());
	        	}
	        System.out.println("Printing interconnection list");
	        Iterator<McsaConnection> interIter = interconn.iterator();
	        while(interIter.hasNext())
	        	{
	        	System.out.println(interIter.next());
	        	}
	        System.out.println(System.lineSeparator()+"MCSATIMETABLE.JAVA arrival station index:"+destinationIndex);
	        System.out.println("MCSATIMETABLE.JAVA connection list size: "+connections.size());
	        System.out.println("MCSATIMETABLE.JAVA interconnection list size: "+interconn.size());
	        
    	}
    
    public int getSourceIndex()
	{
	 return source;
	}
public int getDestinationIndex()
	{
		return destinationIndex;
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
	
	GeodeticCalculator geoCalc = new GeodeticCalculator();

	Ellipsoid reference = Ellipsoid.WGS84;  

	GlobalPosition pointA = new GlobalPosition(dPoint.getX(), dPoint.getY(), 0.0); // Point A

	GlobalPosition userPos = new GlobalPosition(pPoint.getLatitude(), pPoint.getLongitude(), 0.0); // Point B

	double distance = geoCalc.calculateGeodeticCurve(reference, userPos, pointA).getEllipsoidalDistance(); // Distance between Point A and Point B
	return distance;
	}
}
