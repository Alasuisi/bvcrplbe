package mcsa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import bvcrplbe.domain.Transfer;

public class McsaResult {
	
 private LinkedList<McsaSolution> results=new LinkedList<McsaSolution>();
 
 public McsaResult(LinkedList<LinkedList<McsaConnection>> resList,long departureTime,HashMap<Integer,boolean[]> specialNeeds,Transfer passenger) throws Exception
 	{
	  Iterator<LinkedList<McsaConnection>> iter = resList.iterator();
	  int counter=0;
	  while(iter.hasNext())
	  	{
		  McsaSolution thisSol = new McsaSolution(iter.next(),departureTime,specialNeeds,passenger,counter);
		  counter++;
		  results.add(thisSol);
	  	}
 	}

public LinkedList<McsaSolution> getResults() {
	return results;
}
public LinkedList<McsaSolution> getResults(int limit)
	{
	 if(limit<3) limit=3;
	 int maxChanges=0;
	 int maxChangesIndex=0;
	 long maxTripTime=0;
	 int maxTripIndex=0;
	 long maxWait=0;
	 int maxWaitIndex=0;
	 HashMap<Integer,McsaSolution> changeList = new HashMap<Integer,McsaSolution>();
	 HashMap<Integer,McsaSolution> timeList = new HashMap<Integer,McsaSolution>();
	 HashMap<Integer,McsaSolution> waitList = new HashMap<Integer,McsaSolution>();
	 HashMap<Integer,McsaSolution> needList = new HashMap<Integer,McsaSolution>();
	 //HashSet<Integer> checkSet = new HashSet<Integer>();
	 Iterator<McsaSolution> resIter= results.iterator();
	 while(resIter.hasNext())
	 	{
		 McsaSolution sol = resIter.next();
		 if(changeList.size()<=limit)
		 {
			 if(sol.getChanges()>maxChanges)
			 	{
				 maxChanges=sol.getChanges();
				 maxChangesIndex=sol.getSolutionID();
			 	}
	         	changeList.put(new Integer(sol.getSolutionID()), sol);
		 	}else
		 		{
		 		 if(sol.getChanges()<maxChanges)
		 		 	{
		 			 int tempChange=sol.getChanges();
		 			 int tempChangeIndex=sol.getSolutionID();
		 			 changeList.remove(maxChangesIndex);
		 			 changeList.put(new Integer(sol.getSolutionID()), sol);
		 			 Iterator<Integer> keyIte = changeList.keySet().iterator();
		 			 while(keyIte.hasNext())
		 			 	{
		 				 Integer key = keyIte.next();
		 				 if(changeList.get(key).getChanges()>tempChange)
		 				 	{
		 					 tempChange=changeList.get(key).getChanges();
		 					 tempChangeIndex=changeList.get(key).getSolutionID();
		 				 	}
		 			 	}
		 			 maxChanges=tempChange;
		 			 maxChangesIndex=tempChangeIndex;
		 		 	}
		 		}
		 if(timeList.size()<=limit)
		 	{
			 if(sol.getTotalTripTime()>maxTripTime)
			 	{
				 maxTripTime=sol.getTotalTripTime();
				 maxTripIndex=sol.getSolutionID();
			 	}
			 timeList.put(new Integer(sol.getSolutionID()), sol);
		 	}else
		 		{
		 		 if(sol.getTotalTripTime()<maxTripTime)
		 		 	{
		 			 long tempTripTime=sol.getTotalTripTime();
		 			 int tempTripIndex=sol.getSolutionID();
		 			 timeList.remove(maxTripIndex);
		 			 timeList.put(new Integer(sol.getSolutionID()), sol);
		 			 Iterator<Integer> keyIte = timeList.keySet().iterator();
		 			 while(keyIte.hasNext())
		 			 	{
		 				 Integer key = keyIte.next();
		 				 if(timeList.get(key).getTotalTripTime()>tempTripTime)
		 				 	{
		 					 tempTripTime=timeList.get(key).getTotalTripTime();
		 					 tempTripIndex=timeList.get(key).getSolutionID();
		 				 	}
		 			 	}
		 			 maxTripTime=tempTripTime;
		 			 maxTripIndex=tempTripIndex;
		 		 	}
		 		}
		 if(waitList.size()<=limit)
		 	{
			 if(sol.getTotalWaitTime()>maxWait)
			 	{
				  maxWait=sol.getTotalWaitTime();
				  maxWaitIndex=sol.getSolutionID();
			 	}
			 waitList.put(new Integer(sol.getSolutionID()), sol);
		 	}else
		 		{
		 		 if(sol.getTotalWaitTime()<maxWait)
		 		 	{
		 			 long tempWait=sol.getTotalWaitTime();
		 			 int tempWaitIndex=sol.getSolutionID();
		 			 waitList.remove(maxWaitIndex);
		 			 waitList.put(new Integer(sol.getSolutionID()), sol);
		 			 Iterator<Integer> keyIte = waitList.keySet().iterator();
		 			 while(keyIte.hasNext())
		 			 	{
		 				 Integer key = keyIte.next();
		 				 if(waitList.get(key).getTotalWaitTime()>tempWait)
		 				 	{
		 					 tempWait=waitList.get(key).getTotalWaitTime();
		 					 tempWaitIndex=waitList.get(key).getSolutionID();
		 				 	}
		 			 	}
		 			 maxWait=tempWait;
		 			 maxWaitIndex=tempWaitIndex;
		 		 	}
		 		}
		 
	 	}
	}

@Override
public String toString() {
	return "McsaResult [results=" + results + "]";
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((results == null) ? 0 : results.hashCode());
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
	McsaResult other = (McsaResult) obj;
	if (results == null) {
		if (other.results != null)
			return false;
	} else if (!results.equals(other.results))
		return false;
	return true;
}


 
}
