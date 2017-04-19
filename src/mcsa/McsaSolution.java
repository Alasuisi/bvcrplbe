package mcsa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import bvcrplbe.domain.Transfer;

public class McsaSolution {
	private int changes=0;
	private int neededSeats=0;
	private long arrivalTime=0;
	private long totalWaitTime=0;
	private boolean animal;
	private boolean smoke;
	private boolean luggage;
	private boolean handicap;
	private LinkedList<McsaConnection> path= new LinkedList<McsaConnection>();
	private LinkedHashSet<Integer> transferSet =new LinkedHashSet<Integer>();
	private LinkedList<McsaSegment> solution = new LinkedList<McsaSegment>();
	
	
	
	public McsaSolution(LinkedList<McsaConnection> resultList,long departureTime,HashMap<Integer,boolean[]> specialNeeds,Transfer passenger) throws Exception
		{
		 animal=passenger.isAnimal();
		 smoke=passenger.isSmoke();
		 luggage=passenger.isLuggage();
		 handicap=passenger.isHandicap();
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
					 McsaSegment segment2 = new McsaSegment(tempList,specialNeeds);
					 solution.add(segment2);
					 tempList = new LinkedList<McsaConnection>();
				 	}
				 McsaSegment segment = new McsaSegment(temp,passenger);
				 solution.add(segment);
			 	}else
			 		{
			 		tempList.add(temp);
			 		}
			 if(previous==null) 
			 	{
				 previous=temp;
				 long wait = temp.getDeparture_timestamp()-departureTime;
				 totalWaitTime=totalWaitTime+wait;
				 //System.out.println("MCSASOLUTION  waitTimeNull="+wait+" totalNull="+totalWaitTime+" tempDepTime="+temp.getDeparture_timestamp()+" depTime="+departureTime);
			 	}
			 else
			 	{
				 if(previous.getArrival_timestamp()!=temp.getDeparture_timestamp())
				 	{
					 long wait = temp.getDeparture_timestamp()-previous.getArrival_timestamp();
					 totalWaitTime=totalWaitTime+wait;
					 //System.out.println("MCSASOLUTION  waitTime="+wait+" total="+totalWaitTime);
					 previous=temp;
				 	}
			 	}
			 if(!iter.hasNext())
			 	{
				 arrivalTime=temp.getArrival_timestamp();
			 	}
		 	}
		 //Collections.reverse(resultList);
		 path=resultList;
		 Collections.reverse(solution);
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
