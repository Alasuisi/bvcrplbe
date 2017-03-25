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
	/*Connection(TimedPoint2D driverPoint,TimedPoint2D passengerPoint,long passWalkTime,int arrivalIndex,int sourceIndex,int tranId)
		{
		 Point2D.Double passenger = new Point2D.Double(passengerPoint.getLatitude(), passengerPoint.getLongitude());
		 new Connection(driverPoint,passenger,passWalkTime,arrivalIndex,sourceIndex,tranId);
		}*/
	
    Connection(TimedPoint2D driverPoint,Point2D.Double passengerPoint,long passWalkTime,int driverIndex,int passengerIndex,int tranId)
    	{
    	 //departure_station =driverIndex;
    	 //arrival_station = passengerIndex;
    	 //departure_timestamp = driverPoint.getTouchTime();
    	 //arrival_timestamp = passWalkTime;
    	 if(driverIndex<passengerIndex)
    	 	{
    		 departure_station =driverIndex;
        	 arrival_station = passengerIndex;
    		 second_point = new TimedPoint2D();
    		 second_point.setLatitude(passengerPoint.getX());
    		 second_point.setLongitude(passengerPoint.getY());
    		 second_point.setTouchTime(passWalkTime);
    		 departure_timestamp=driverPoint.getTouchTime();
    		 arrival_timestamp=passWalkTime;
    		 first_point=driverPoint;
    	 	}else
    	 		{
    	 			departure_station =passengerIndex;
    	 			arrival_station = driverIndex;
    	 			second_point=driverPoint;
    	 			first_point = new TimedPoint2D();
    	 			first_point.setLatitude(passengerPoint.getX());
    	 			first_point.setLongitude(passengerPoint.getY());
    	 			first_point.setTouchTime(passWalkTime);
    	 			departure_timestamp = driverPoint.getTouchTime();
    	 	    	arrival_timestamp = passWalkTime;
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
    
    public int getDeparture_station() {
		return departure_station;
	}
	public void setDeparture_station(int departure_station) {
		this.departure_station = departure_station;
	}
	public int getArrival_station() {
		return arrival_station;
	}
	public void setArrival_station(int arrival_station) {
		this.arrival_station = arrival_station;
	}
	public long getDeparture_timestamp() {
		return departure_timestamp;
	}
	public void setDeparture_timestamp(long departure_timestamp) {
		this.departure_timestamp = departure_timestamp;
	}
	public long getArrival_timestamp() {
		return arrival_timestamp;
	}
	public void setArrival_timestamp(long arrival_timestamp) {
		this.arrival_timestamp = arrival_timestamp;
	}
	public TimedPoint2D getFirst_point() {
		return first_point;
	}
	public void setFirst_point(TimedPoint2D first_point) {
		this.first_point = first_point;
	}
	public TimedPoint2D getSecond_point() {
		return second_point;
	}
	public void setSecond_point(TimedPoint2D second_point) {
		this.second_point = second_point;
	}
	public int getTransferID() {
		return transferID;
	}
	public void setTransferID(int transferID) {
		this.transferID = transferID;
	}
    
    
	@Override
	public String toString() {
		return "Connection [departure_station=" + departure_station + ", arrival_station=" + arrival_station
				+ ", departure_timestamp=" + departure_timestamp + ", arrival_timestamp=" + arrival_timestamp
				+ ", first_point=" + first_point + ", second_point=" + second_point + ", transferID=" + transferID
				+ "]";
	}
};