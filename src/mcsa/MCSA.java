package mcsa;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import bvcrplbe.domain.Transfer;



public class MCSA {
	
	private McsaTimetable timetable;
	LinkedList<LinkedList<McsaConnection>> result=new LinkedList<LinkedList<McsaConnection>>();
	LinkedList<McsaConnection>[] connection_list;
	private long departureTime;
	
	/* MCSA main construcor, takes a bufferedReader of strings of this format:
	 *  departure_Station arrival_Station departure_timestamp arrival_timestamp
	 *  EX: 1 2 10 20
	 *  and creates the timetable structure, then creates the connectionList for the
	 *  real MCSA compute algorithm
	 */
	public MCSA(LinkedList<Transfer> driver,Transfer passenger)
		{
		long t1 =System.currentTimeMillis();
		timetable = new McsaTimetable(driver,passenger);
		long t2 = System.currentTimeMillis();
		computeConnectionList();
		long t3= System.currentTimeMillis();
		System.out.println(System.lineSeparator()+"Creating timetable took "+(t2-t1)+System.lineSeparator()+"Computing the connection list took "+(t3-t2));
		}
	
	/*
	 * Simple method to print the found solutions, simply iterates over che solution
	 * list of connection list.
	 */
	public void printSolutions(int n)
		{
		System.out.println(System.lineSeparator()+"result list size"+result.size());
		Iterator<LinkedList<McsaConnection>> resultIterator = result.iterator();
		int stop=0;
		while(resultIterator.hasNext()&& stop<=n)
			{
			LinkedList<McsaConnection> solution = resultIterator.next();
			Collections.reverse(solution);
			Iterator<McsaConnection> solIter = solution.iterator();
			System.out.print(System.lineSeparator()+"Printing a found solution"+System.lineSeparator());
			while(solIter.hasNext())
				{
				McsaConnection toPrint = solIter.next();
				 System.out.println(toPrint.departure_station+"->"+toPrint.arrival_station+" "+toPrint.departure_timestamp+" "+toPrint.arrival_timestamp+" 1tt "+toPrint.getFirst_point().getTouchTime()+" 2tt "+toPrint.getSecond_point().getTouchTime()+" "+toPrint.getTransferID()+"->"+toPrint.getConnectedTo()+" "+toPrint.getFirst_point().getLatitude()+","+toPrint.getFirst_point().getLongitude()+"  "+toPrint.getSecond_point().getLatitude()+","+toPrint.getSecond_point().getLongitude());
				}
			solIter=solution.iterator();
			System.out.println("---Printing points---");
			McsaConnection first =solIter.next();
			System.out.println(first.getFirst_point().getLatitude()+","+first.getFirst_point().getLongitude());
			while(solIter.hasNext())
				{
				McsaConnection print=solIter.next();
				System.out.println(print.getSecond_point().getLatitude()+","+print.getSecond_point().getLongitude());
				}
			stop++;
			}
		}
	
	/* Public method which encapsulate the real algorithm, initializing it with
	 * correct values for the first call
	 */
	public void computeMCSA(int destinationStation,int departureStation,long departureTime)
		{
		 this.departureTime=departureTime;
		 doMCSA(destinationStation,departureStation,departureTime,Long.MAX_VALUE,new LinkedList<McsaConnection>());
		}
	public void computeMCSA(long departureTime)
		{
		 this.departureTime=departureTime;
		 doMCSA(timetable.getDestinationIndex(),0,departureTime,Long.MAX_VALUE,new LinkedList<McsaConnection>());
		}

	/* This is the real Multi Connection Scan Algorithm and is structured in such a way that,
	 * if a station is reachable by multiple other stations, then every possible branch gets
	 * recursively evaluated, COPYING the partial result along every doMCSA recursive call.
	 * In the available iterative version, only the connection with the lowest arrival timestamp 
	 * to the next reachable connection was kept in the solution space, meaning that if that
	 * specific connection, wasn't part of the solution but had a lower arrival time to the next connection,
	 *  then the algorithm would fail, even if a solution exists. By the nature of this implementation,
	 *  this algorithms finds not only the solution with the EARLIEST ARRIVAL TIMESTAMP, but all the admissible
	 *  solutions; this opens to a more generic scenarios where the optimal criteria, is not necessary the earliest
	 *  arrival criterion, but also, the number of traversed stations (easily computed by looking at every solution
	 *  list size), and more in general, in the carpooling scenario, offers multiple choices to the end user
	 */
	
	private void doMCSA(int dest_station,int source_station,long source_dt,long next_ts,LinkedList<McsaConnection> tempRes)
	{
		if(connection_list[dest_station].isEmpty()) return;
		else
			{
				//System.out.println("Connection list not empty");
				Iterator<McsaConnection> iter = connection_list[dest_station].iterator();
				//System.out.println("iterating over destination station n°:"+dest_station+" of size: "+connection_list[dest_station].size());
				while(iter.hasNext())
					{
					McsaConnection toAdd=iter.next();
					if(toAdd.departure_station==source_station)
						{
						if(toAdd.arrival_timestamp<=next_ts)
							{
							//System.out.println("departure station = source");
							LinkedList<McsaConnection> copyRes=deepCopy(tempRes);
							copyRes.add(toAdd);
							result.add(copyRes);
							}else return; 
						}else if(toAdd.arrival_timestamp<=next_ts)
							{
							//System.out.println("Not to the source, addding connection to temporary solution and piggodding");
							LinkedList<McsaConnection> copyRes=deepCopy(tempRes);
							//System.out.print("Copied temporary list");
							copyRes.add(toAdd);
							doMCSA(toAdd.departure_station,source_station,source_dt,toAdd.departure_timestamp,copyRes);
							}else return;
					}
			}
	}
	public void removeBadOnes()
		{
		boolean changedTransfer=false;
		int changeCount=0;
		boolean badSolution=false;
		int transfer=Integer.MAX_VALUE;
		Iterator<LinkedList<McsaConnection>> resIter = result.iterator();
		LinkedList<LinkedList<McsaConnection>> cleanRes=new LinkedList<LinkedList<McsaConnection>>();
		while(resIter.hasNext())
			{
			 LinkedList<McsaConnection> solution =resIter.next();
			 Iterator<McsaConnection> solIter=solution.iterator();
			 while(solIter.hasNext() && !badSolution)
			 	{
				 McsaConnection temp = solIter.next();
				 if(transfer==Integer.MAX_VALUE) transfer=temp.getTransferID();
				 else
				 	{
					 if(temp.getTransferID()!=transfer)
					 	{
						 //System.out.println("MCSA.JAVA changed transfer");
						 changedTransfer=true;
						 changeCount++;
						 transfer=temp.getTransferID();
					 	}else 
					 		{
					 		//System.out.println("MCSA.JAVA not changed transfer");
					 		changedTransfer=false;
					 		changeCount=0;
					 		}
					 if(changeCount==2 && changedTransfer)badSolution=true;
				 	}
			 	}
			 if(!badSolution) 
			 	{
				 
				 cleanRes.add(solution);
			 	}
			 else
			 	{
				 //System.out.println("MCSA.JAVA found a bad one");
				 changedTransfer=false;
				 badSolution=false;
			 	}
			}
		result=cleanRes;
		}
	
	/* Algorithm for the deep cloning of a list, doing something like:
	 * List<Objet> copied=null;
	 * List<Object> toCopy;
	 * copied=toCopy    ---->copies only the pointer to the list, every modification,
	 * 						 done on the copy would be reflected also in the original
	 * Cycling on the elements of the list, would have a similar behaviour, but with
	 * respect to every single object (changes the granularity, but the result is the same)
	 * 
	 * So the only solution, is to read the entire list, create new object of the correct
	 * type, and adding the deep cloned object to a new list.
	 * 
	 * this method is used in the doMCSA recursive algorithm when a new branch of the computation
	 * needs to be executed, to clone the partial result list data strucure, so that the changes
	 * of that branch are separeted from the caller one
	 */
	private static LinkedList<McsaConnection> deepCopy(LinkedList<McsaConnection> listToCopy)
	{
		LinkedList<McsaConnection> copied=new LinkedList<McsaConnection>();
		Iterator<McsaConnection> copIter = listToCopy.iterator();
		while(copIter.hasNext())
			{
			 McsaConnection conToCopy=copIter.next();
			 McsaConnection copiedCon =new McsaConnection(conToCopy.getFirst_point(),conToCopy.getSecond_point(),conToCopy.getDeparture_station(),conToCopy.getArrival_station(),conToCopy.getTransferID());
			 copiedCon.setConnectedTo(conToCopy.getConnectedTo());
			 copied.add(copiedCon);
			}
		return copied;
	}
	
	/* Initialize support structures and populate the connection list with the elements
	 * of the timetable; connection_list is an array, where every element of the array
	 * is a variable length list of connections, a cell in position i of the array, encodes
	 * the i-th arrival station index of a connection, and every element of the list associated
	 * with the i-th element of the array encodes a connection which have i-th index station as arrival station;
	 * 
	 * EX: if the element in position 8 (which is a list of connection) contains 3 connections
	 * 	   such as 5->8
	 * 			   3->8
	 * 			   7->8
	 * 		this means that station with index 8, considered as an arrival station, has 3 incoming
	 * 		connection, from station with index number 5, 3 and 7
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void computeConnectionList()
	{
	//System.out.println("MCSA.JAVA computing connection list, timetable size: "+timetable.connections.size()+", destination index:"+timetable.getDestinationIndex());
	connection_list=new LinkedList[timetable.getDestinationIndex()+1];
	for(int i=0;i<timetable.getDestinationIndex()+1;i++)
		{
		connection_list[i]=new LinkedList<McsaConnection>();
		}
	Iterator<McsaConnection> iter = timetable.connections.iterator();
	while(iter.hasNext())
		{
		McsaConnection temp=iter.next();
		connection_list[temp.arrival_station].add(temp);
		}
	//ystem.out.println(System.lineSeparator()+"connection list computed");
	}
	
	public McsaResult getResults() throws Exception
		{
			McsaResult res = null;
			if(result!=null)
				{
				 res = new McsaResult(result,this.departureTime,timetable.getSpecialNeeds(),timetable.getPassengerTransfer());
				}
			return res;
		}
}
