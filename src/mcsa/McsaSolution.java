package mcsa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class McsaSolution {
	private LinkedList<McsaConnection> path= new LinkedList<McsaConnection>();
	private LinkedList<McsaSegment> solution = new LinkedList<McsaSegment>();
	private LinkedHashSet<Integer> transferSet =new LinkedHashSet<Integer>();
	private int changes=0;
	private long totalWaitTime=0;
	private long arrivalTime=0;
	
	public McsaSolution(LinkedList<McsaConnection> resultList,long departureTime) throws Exception
		{
		 Iterator<McsaConnection> iter = resultList.iterator();
		 McsaConnection previous =null;
		 while(iter.hasNext())
		 	{
			 McsaConnection temp = iter.next();
			 transferSet.add(new Integer(temp.getTransferID()));
			 if(temp.getTransferID()!=temp.getConnectedTo())
			 	{
				 changes++;
				 McsaSegment segment = new McsaSegment(temp);
				 solution.add(segment);
			 	}
			 if(previous==null) 
			 	{
				 previous=temp;
				 long wait = temp.getDeparture_timestamp()-departureTime;
				 totalWaitTime=totalWaitTime+wait;
			 	}
			 else
			 	{
				 if(previous.getArrival_timestamp()!=temp.getDeparture_timestamp())
				 	{
					 long wait = temp.getDeparture_timestamp()-previous.getArrival_timestamp();
					 totalWaitTime=totalWaitTime+wait;
					 previous=temp;
				 	}
			 	}
			 if(!iter.hasNext())
			 	{
				 arrivalTime=temp.getArrival_timestamp();
			 	}
		 	}
		 Collections.reverse(resultList);
		 path=resultList;
		}

	public LinkedList<McsaConnection> getPath() {
		return path;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (arrivalTime ^ (arrivalTime >>> 32));
		result = prime * result + changes;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		if (arrivalTime != other.arrivalTime)
			return false;
		if (changes != other.changes)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
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

	@Override
	public String toString() {
		return "McsaSolution [transferSet=" + transferSet + ", changes=" + changes
				+ ", totalWaitTime=" + totalWaitTime + ", arrivalTime=" + arrivalTime +", path=" + path +  "]";
	}
	

}
