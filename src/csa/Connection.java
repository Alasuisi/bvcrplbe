package csa;

import java.awt.geom.Point2D;

import bvcrplbe.domain.TimedPoint2D;

class Connection {
    int departure_station, arrival_station;
    long departure_timestamp, arrival_timestamp;
    TimedPoint2D first_point, second_point;
    int transferID;

    // Connection constructor
   /* Connection(String line) {
        line.trim();
        String[] tokens = line.split(" ");

        departure_station = Integer.parseInt(tokens[0]);
        arrival_station = Integer.parseInt(tokens[1]);
        departure_timestamp = Integer.parseInt(tokens[2]);
        arrival_timestamp = Integer.parseInt(tokens[3]);
    }*/
    
    Connection(TimedPoint2D driverPoint,Point2D.Double passengerPoint,long passWalkTime,int arrivalIndex,int sourceIndex,int tranId)
    	{
    	 departure_station =sourceIndex;
    	 arrival_station = arrivalIndex;
    	 departure_timestamp = driverPoint.getTouchTime();
    	 arrival_timestamp = passWalkTime;
    	 if(sourceIndex<arrivalIndex)
    	 	{
    		 first_point = new TimedPoint2D();
    		 first_point.setLatitude(passengerPoint.getX());
    		 first_point.setLongitude(passengerPoint.getY());
    		 first_point.setTouchTime(passWalkTime);
    		 second_point=driverPoint;
    	 	}else
    	 		{
    	 			first_point=driverPoint;
    	 			second_point = new TimedPoint2D();
    	 			second_point.setLatitude(passengerPoint.getX());
    	 			second_point.setLongitude(passengerPoint.getY());
    	 			second_point.setTouchTime(passWalkTime);
    	 		}
    	 transferID=tranId;
    	}
    Connection(TimedPoint2D previous,TimedPoint2D successor, int prevIndex,int succIndex, int tranId)
    	{
    	 departure_station = prevIndex;
    	 arrival_station = succIndex;
    	 departure_timestamp=previous.getTouchTime();
    	 arrival_timestamp = successor.getTouchTime();
    	 first_point = previous;
    	 second_point = successor;
    	 transferID=tranId;
    	 
    	}
};