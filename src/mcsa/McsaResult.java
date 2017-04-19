package mcsa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import bvcrplbe.domain.Transfer;

public class McsaResult {
	
 private LinkedList<McsaSolution> results=new LinkedList<McsaSolution>();
 
 public McsaResult(LinkedList<LinkedList<McsaConnection>> resList,long departureTime,HashMap<Integer,boolean[]> specialNeeds,Transfer passenger) throws Exception
 	{
	  Iterator<LinkedList<McsaConnection>> iter = resList.iterator();
	  while(iter.hasNext())
	  	{
		  McsaSolution thisSol = new McsaSolution(iter.next(),departureTime,specialNeeds,passenger);
		  results.add(thisSol);
	  	}
 	}

public LinkedList<McsaSolution> getResults() {
	return results;
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
