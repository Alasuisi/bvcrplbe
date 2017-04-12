package mcsa;

import java.util.Iterator;
import java.util.LinkedList;

public class McsaResult {
	
 private LinkedList<McsaSolution> results=new LinkedList<McsaSolution>();
 
 public McsaResult(LinkedList<LinkedList<McsaConnection>> resList,long departureTime) throws Exception
 	{
	  Iterator<LinkedList<McsaConnection>> iter = resList.iterator();
	  while(iter.hasNext())
	  	{
		  McsaSolution thisSol = new McsaSolution(iter.next(),departureTime);
		  results.add(thisSol);
	  	}
 	}

public LinkedList<McsaSolution> getResults() {
	return results;
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

@Override
public String toString() {
	return "McsaResult [results=" + results + "]";
}
 
}
