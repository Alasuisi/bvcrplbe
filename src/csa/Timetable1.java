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
    		totalPoints=totalPoints+temp.getPath().size();
    		}
    	destination=totalPoints+1;
    	drivIter=drivers.iterator();
    	while(drivIter.hasNext())
    		{
    		Transfer temp = drivIter.next();
    		System.out.println("new transfer "+temp.getTran_id());
    		//totalPoints=totalPoints+temp.getPath().size();
    		LinkedList<TimedPoint2D> thisPath = temp.getPath();
    		Iterator<TimedPoint2D> pathIter=thisPath.iterator();
    		TimedPoint2D previous = null;
       		while(pathIter.hasNext())
    			{
    			 if(previous==null) 
    			 	{
    				 previous=pathIter.next();
    				 //index++;
    				 System.out.println("previous was null");
    				 ///////
    				 TimedPoint2D source = new TimedPoint2D();
    				 source.setLatitude(passenger.getDep_gps().getX());
    				 source.setLongitude(passenger.getDep_gps().getY());
    				 source.setTouchTime(passenger.getDep_time());
    				 if(source.getTouchTime()<previous.getTouchTime())
    		    		{
    		    		double sToPath1=evaluateDistance(previous,source);
    		    		if(sToPath1<passenger.getDet_range())
    		    			{
    		    				long sWalktime1=walkTime(sToPath1);
    		    				long timeSkew=previous.getTouchTime()-source.getTouchTime();
    		    				if(sWalktime1<timeSkew)
    		    				{
    		    					
    		    					connections.add(new Connection(source,previous,0,index,passenger.getTran_id()));
    		    				}
    		    			}
    		    		}
    				 TimedPoint2D destination = new TimedPoint2D();
    			     destination.setLatitude(passenger.getArr_gps().getX());
    			     destination.setLongitude(passenger.getArr_gps().getY());
    				 double pToDest1=evaluateDistance(previous,destination);
    					if(pToDest1<passenger.getDet_range())
    						{
    						long pWalktime1=walkTime(pToDest1);
    						destination.setTouchTime((previous.getTouchTime()+pWalktime1));
    						connections.add(new Connection(previous,destination,index,(totalPoints+1),passenger.getTran_id()));
    						}
    				 
    				 ///////
    				 index++;
    			 	}else
    			 		{
    			 		int prevIndex=index-1;
    			 		TimedPoint2D pathPoint = pathIter.next();
    			 		connections.add(new Connection(previous,pathPoint,prevIndex,index,temp.getTran_id()));
    			 		previous=pathPoint;
    			 		
    			 		/////////
    			 			 TimedPoint2D source = new TimedPoint2D();
		       				 source.setLatitude(passenger.getDep_gps().getX());
		       				 source.setLongitude(passenger.getDep_gps().getY());
		       				 source.setTouchTime(passenger.getDep_time());
		       				 if(source.getTouchTime()<pathPoint.getTouchTime())
		       		    		{
		       		    		double sToPath1=evaluateDistance(pathPoint,source);
		       		    		if(sToPath1<passenger.getDet_range())
		       		    			{
		       		    				long sWalktime1=walkTime(sToPath1);
		       		    				long timeSkew=pathPoint.getTouchTime()-source.getTouchTime();
		       		    				if(sWalktime1<timeSkew)
		       		    				{
		       		    					
		       		    					connections.add(new Connection(source,pathPoint,0,index,passenger.getTran_id()));
		       		    				}
		       		    			}
		       		    		}
		       				TimedPoint2D destination = new TimedPoint2D();
		    			     destination.setLatitude(passenger.getArr_gps().getX());
		    			     destination.setLongitude(passenger.getArr_gps().getY());
		    				 double pToDest1=evaluateDistance(pathPoint,destination);
		    					if(pToDest1<passenger.getDet_range())
		    						{
		    						long pWalktime1=walkTime(pToDest1);
		    						destination.setTouchTime((pathPoint.getTouchTime()+pWalktime1));
		    						connections.add(new Connection(pathPoint,destination,index,(totalPoints+1),passenger.getTran_id()));
		    						}
    			 		/////////
    			 		
    			 		
    			 		index++;
    			 		}
    			}
    		}
    	ArrayList<Connection> temp = new ArrayList<Connection>();
    	Iterator<Connection> connIter = connections.iterator();
    	Connection previous = null;
    	while(connIter.hasNext())
    		{
    		 if(previous==null)previous=connIter.next();
    		 else
    		 	{
    			 Iterator<Connection> connIter2 = connections.iterator();
    			 while(connIter2.hasNext())
    			 	{
    				 Connection actual = connIter2.next();
    				 if(previous.getTransferID()!=actual.getTransferID())
    				 	{
    					 TimedPoint2D prevFirst=previous.getFirst_point();
    					 TimedPoint2D prevSecond=previous.getSecond_point();
    					 TimedPoint2D actuFirst = actual.getFirst_point();
    					 TimedPoint2D actuSecond= actual.getSecond_point();
    					 double preAct11=evaluateDistance(prevFirst,actuFirst);
    					 double preAct12=evaluateDistance(prevFirst,actuSecond);
    					 double preAct21=evaluateDistance(prevSecond,actuFirst);
    					 double preAct22=evaluateDistance(prevSecond,actuSecond);
    					 long walk11=walkTime(preAct11);
    					 long walk12=walkTime(preAct12);
    					 long walk21=walkTime(preAct21);
    					 long walk22=walkTime(preAct22);
    					 long touch11 = prevFirst.getTouchTime()+walk11;
    					 long touch12 = prevFirst.getTouchTime()+walk12;
    					 long touch21 = prevSecond.getTouchTime()+walk21;
    					 long touch22 = prevSecond.getTouchTime()+walk22;
    					 if(preAct11<passenger.getDet_range())
    					 	{
    						 if(touch11<actuFirst.getTouchTime());
    					 	}
    				 	}
    			 	}
    		 	}
    		}
    	/*
    	System.out.println("Linking source and destination, dest index="+(totalPoints-1));
    	TimedPoint2D source = new TimedPoint2D();
    	source.setLatitude(passenger.getDep_gps().getX());
    	source.setLongitude(passenger.getDep_gps().getY());
    	source.setTouchTime(passenger.getDep_time());
    	
    	
    	    	
    	TimedPoint2D destination = new TimedPoint2D();
    	destination.setLatitude(passenger.getArr_gps().getX());
    	destination.setLongitude(passenger.getArr_gps().getY());
    	ArrayList<Connection> complement = new ArrayList<Connection>();
    	
    	////////////////////////////////////////////////////////////////////////
    	///Checking if possible to link first point to source and destination///
    	////////////////////////////////////////////////////////////////////////
    	TimedPoint2D firstPoint=connections.get(0).getFirst_point();
    	if(source.getTouchTime()<firstPoint.getTouchTime())
    		{
    		double sToPath1=evaluateDistance(firstPoint,source);
    		if(sToPath1<passenger.getDet_range())
    			{
    				long sWalktime1=walkTime(sToPath1);
    				long timeSkew=firstPoint.getTouchTime()-source.getTouchTime();
    				if(sWalktime1<timeSkew)
    				{
    					
    					complement.add(new Connection(source,firstPoint,0,connections.get(0).getDeparture_station(),passenger.getTran_id()));
    				}
    			}
    		}
    	double pToDest1=evaluateDistance(firstPoint,destination);
		if(pToDest1<passenger.getDet_range())
			{
			long pWalktime1=walkTime(pToDest1);
			destination.setTouchTime((connections.get(0).getDeparture_timestamp()+pWalktime1));
			complement.add(new Connection(firstPoint,destination,connections.get(0).getDeparture_station(),(totalPoints+1),passenger.getTran_id()));
			}
    	
		////////////////////////////////////////////////////////////////////////
    	///checking if all other points are linkable to source or destination///
		////////////////////////////////////////////////////////////////////////
    	Iterator<Connection> linkIter = connections.iterator();
    	while(linkIter.hasNext())
    		{
    			Connection temp=linkIter.next();
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
    			
    			double pToDest2=evaluateDistance(temp.getSecond_point(),destination);
    			if(pToDest2<passenger.getDet_range())
    				{
	    			long pWalktime2=walkTime(pToDest2);
	    			destination.setTouchTime((temp.getArrival_timestamp()+pWalktime2));
	    			complement.add(new Connection(temp.getSecond_point(),destination,temp.getArrival_station(),(totalPoints+1),passenger.getTran_id()));
    				}
    			
    		}
    	System.out.print("Printing connections"+System.lineSeparator());
    	Iterator<Connection> connIter = connections.iterator();
    	while(connIter.hasNext())
    		{
    		System.out.println(connIter.next().toString());
    		}
    	////////////////////////////////////////////////////////
    	///appending complement connection to connection list///
    	////////////////////////////////////////////////////////
    	connections.addAll(complement);
    	//complement=null;
    	
    	
    	System.out.println("Printing complement"+System.lineSeparator());
    	Iterator<Connection> compIter = complement.iterator();
    	while(compIter.hasNext())
    		{
    		System.out.println(compIter.next().toString());
    		}*/
    	Iterator<Connection> connIter = connections.iterator();
    	while(connIter.hasNext())
    		{
    		System.out.println(connIter.next().toString());
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
