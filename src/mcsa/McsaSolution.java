package mcsa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import bvcrplbe.domain.Transfer;

public class McsaSolution {
	private int solutionID;
	private int changes=0;
	private int neededSeats=0;
	private long arrivalTime=0;
	private long totalWaitTime=0;
	private long totalTripTime=0;
	private boolean animal;
	private boolean smoke;
	private boolean luggage;
	private boolean handicap;
	private LinkedList<McsaConnection> path= new LinkedList<McsaConnection>();
	private LinkedHashSet<Integer> transferSet =new LinkedHashSet<Integer>();
	private LinkedList<McsaSegment> solution = new LinkedList<McsaSegment>();
	
	public McsaSolution(){};

	
	public void setSolutionID(int solutionID) {
		this.solutionID = solutionID;
	}


	public void setChanges(int changes) {
		this.changes = changes;
	}


	public void setNeededSeats(int neededSeats) {
		this.neededSeats = neededSeats;
	}


	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}


	public void setTotalWaitTime(long totalWaitTime) {
		this.totalWaitTime = totalWaitTime;
	}


	public void setTotalTripTime(long totalTripTime) {
		this.totalTripTime = totalTripTime;
	}


	public void setAnimal(boolean animal) {
		this.animal = animal;
	}


	public void setSmoke(boolean smoke) {
		this.smoke = smoke;
	}


	public void setLuggage(boolean luggage) {
		this.luggage = luggage;
	}


	public void setHandicap(boolean handicap) {
		this.handicap = handicap;
	}


	public void setTransferSet(LinkedHashSet<Integer> transferSet) {
		this.transferSet = transferSet;
	}


	public void setSolution(LinkedList<McsaSegment> solution) {
		this.solution = solution;
	}


	public McsaSolution(LinkedList<McsaConnection> resultList,long departureTime,HashMap<Integer,boolean[]> specialNeeds,Transfer passenger,int solID) throws Exception
		{
		 solutionID=solID;
		 long time =departureTime;
		 /*
		 animal=passenger.isAnimal();
		 smoke=passenger.isSmoke();
		 luggage=passenger.isLuggage();
		 handicap=passenger.isHandicap();
		 */
		 Iterator<HashMap.Entry<Integer,boolean[]>> specialIter=specialNeeds.entrySet().iterator();
		 boolean[] specialResult = new boolean[4];
		 for(int i=0;i<specialResult.length;i++)
		 	{
			 specialResult[i]=true;
		 	}
		 while(specialIter.hasNext())
		 	{
			 Entry<Integer, boolean[]> tempEntry = specialIter.next();
			 boolean[] tempSpecial = tempEntry.getValue();
			 for(int i=0;i<tempSpecial.length;i++)
			 	{
				 if(tempSpecial[i]==false) specialResult[i]=false;
			 	}
		 	}
		 if(!passenger.isAnimal()) specialResult[0]=false;
		 if(!passenger.isHandicap()) specialResult[0]=false;
		 if(!passenger.isLuggage()) specialResult[0]=false;
		 if(!passenger.isSmoke()) specialResult[0]=false;
		 animal=specialResult[0];
		 handicap=specialResult[1];
		 luggage=specialResult[2];
		 smoke=specialResult[3];
		 neededSeats=passenger.getOcc_seats();
		 Iterator<McsaConnection> iter = resultList.iterator();
		 McsaConnection previous =null;
		 LinkedList<McsaConnection>tempList = new LinkedList<McsaConnection>();
		 while(iter.hasNext())
		 	{
			 McsaConnection temp = iter.next();
			 transferSet.add(new Integer(temp.getTransferID()));
			 if(temp.getTransferID()!=temp.getConnectedTo())
			 	{
				 changes++;
				 if(!tempList.isEmpty())
				 	{
					 //Collections.reverse(tempList);/// un po random qeusta cosa
					 /*if(tempList.size()<3)
					 	{
						 System.out.println("DAFUQQQQQQQQQQQQQQ la lista delle connessioni � piccola: dimensione="+tempList.size());
						 Iterator<McsaConnection> mah = tempList.iterator();
						 while(mah.hasNext())
						 	{
							 System.out.println(mah.next().toString());
						 	}
					 	}*/
					 McsaSegment segment2 = new McsaSegment(tempList,specialNeeds);
					 solution.add(segment2);
					 tempList = new LinkedList<McsaConnection>();
					
				 	}
				 //System.out.println(System.lineSeparator()+"MCSASEGMENT called new segment depTime:"+departureTime+" time:"+time);
				 //System.out.println(temp.getDeparture_station()+" "+temp.getArrival_station()+" "+temp.getFirst_point().toString()+" "+temp.getSecond_point().toString()+" "+temp.getTransferID()+"->"+temp.getConnectedTo());
				 McsaSegment segment = new McsaSegment(temp,passenger,time,false);
				 solution.add(segment);
			 	}else
			 		{
			 		tempList.add(temp);
			 		time=temp.getArrival_timestamp(); /////DA CONTROLLARE SE CI VUOLE QUESTO O L'ALTRO
			 		}
			 if(previous==null) 
			 	{
				 previous=temp;
				 //long wait = temp.getDeparture_timestamp()-departureTime;
				 //totalWaitTime=totalWaitTime+wait;
				 //System.out.println("MCSASOLUTION "+temp.getTransferID()+" "+temp.getConnectedTo()+" "+temp.getDeparture_station()+" "+temp.getArrival_station() +" waitTimeNull="+wait+" totalNull="+totalWaitTime+" tempDepTime="+temp.getDeparture_timestamp()+" depTime="+departureTime);
			 	}
			 else
			 	{
				 if(previous.getArrival_timestamp()!=temp.getDeparture_timestamp()) //previous.getArrival_timestamp()!=temp.getDeparture_timestamp()
				 	{
					 
					 //long wait = temp.getDeparture_timestamp()-previous.getArrival_timestamp();
					 //totalWaitTime=totalWaitTime+wait;
					 //System.out.println("MCSASOLUTION " +temp.getTransferID()+" "+temp.getConnectedTo()+" "+" "+temp.getDeparture_station()+" "+temp.getArrival_station() + "waitTime="+wait+" total="+totalWaitTime+" "+previous.getArrival_timestamp()+" "+temp.getDeparture_timestamp());
					 previous=temp;
				 	}
			 	}
			 /*if(!iter.hasNext())
			 	{
				 arrivalTime=temp.getArrival_timestamp();
			 	}*/
		 	}
		 
		 //Collections.reverse(resultList);
		 path=resultList;
		 Collections.reverse(solution);
		 
		 McsaSegment temp = null;
		 Iterator<McsaSegment> segIter = solution.iterator();
		 while(segIter.hasNext())
		 	{
			 if(temp==null)
			 	{
				 temp=segIter.next();
			 	}else
			 		{
			 		 McsaSegment toRead = segIter.next();
			 		 long tempWaiting = toRead.getSegmentDeparture()-temp.getSegmentArrival();
			 		 totalWaitTime=totalWaitTime+tempWaiting;
			 		 toRead.setDepartureWaitTime(tempWaiting);
			 		 temp=toRead;
			 		 if(!segIter.hasNext()) arrivalTime=toRead.getSegmentArrival();
			 		}
		 	}
		 
		 totalTripTime=arrivalTime-departureTime;
		 //Collections.reverse(solution);
		}

	public int getNeededSeats() {
		return neededSeats;
	}

	/*public LinkedList<McsaConnection> getPath() {
		return path;
	}*/

	public HashSet<Integer> getTransferSet() {
		return transferSet;
	}

	public int getChanges() {
		return changes;
	}

	public long getTotalWaitTime() {
		return totalWaitTime;
	}

	public long getArrivalTime() {
		return arrivalTime;
	}

	public LinkedList<McsaSegment> getSolution() {
		//Collections.reverse(solution);
		return solution;
	}

	public long getTotalTripTime() {
		return totalTripTime;
	}

	public int getSolutionID() {
		return solutionID;
	}

	public boolean isAnimal() {
		return animal;
	}

	public boolean isSmoke() {
		return smoke;
	}

	public boolean isLuggage() {
		return luggage;
	}

	public boolean isHandicap() {
		return handicap;
	}

	@Override
	public String toString() {
		return "McsaSolution [path=" + path + ", solution=" + solution + ", transferSet=" + transferSet + ", changes="
				+ changes + ", totalWaitTime=" + totalWaitTime + ", arrivalTime=" + arrivalTime + ", animal=" + animal
				+ ", smoke=" + smoke + ", luggage=" + luggage + ", handicap=" + handicap + ", neededSeats="
				+ neededSeats + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (animal ? 1231 : 1237);
		result = prime * result + (int) (arrivalTime ^ (arrivalTime >>> 32));
		result = prime * result + changes;
		result = prime * result + (handicap ? 1231 : 1237);
		result = prime * result + (luggage ? 1231 : 1237);
		result = prime * result + neededSeats;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (smoke ? 1231 : 1237);
		result = prime * result + ((solution == null) ? 0 : solution.hashCode());
		result = prime * result + (int) (totalWaitTime ^ (totalWaitTime >>> 32));
		result = prime * result + ((transferSet == null) ? 0 : transferSet.hashCode());
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
		McsaSolution other = (McsaSolution) obj;
		if (animal != other.animal)
			return false;
		if (arrivalTime != other.arrivalTime)
			return false;
		if (changes != other.changes)
			return false;
		if (handicap != other.handicap)
			return false;
		if (luggage != other.luggage)
			return false;
		if (neededSeats != other.neededSeats)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (smoke != other.smoke)
			return false;
		if (solution == null) {
			if (other.solution != null)
				return false;
		} else if (!solution.equals(other.solution))
			return false;
		if (totalWaitTime != other.totalWaitTime)
			return false;
		if (transferSet == null) {
			if (other.transferSet != null)
				return false;
		} else if (!transferSet.equals(other.transferSet))
			return false;
		return true;
	}

	

	
	

}
