package mcsa;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

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
	/*public void computeMCSA(int destinationStation,int departureStation,long departureTime)
	{
	 this.departureTime=departureTime;
	 doMCSA(destinationStation,departureStation,departureTime,Long.MAX_VALUE,new ArrayList<int[]>());
	}
	public void computeMCSA(long departureTime)
	{
	 this.departureTime=departureTime;
	 doMCSA(timetable.getDestinationIndex(),0,departureTime,Long.MAX_VALUE,new ArrayList<int[]>());
	}*/

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
							tempRes=null;
							return;
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
	public void removeDeadEnds(HashSet<Integer> emptySet)
		{
		 int emptyCount=emptySet.size();
		 Iterator<Integer> empIter = emptySet.iterator();
		 while(empIter.hasNext())
		 	{
			 int badStation = empIter.next().intValue();
			 System.out.println(badStation);
		 	}
		 
		 for(int i=1;i<connection_list.length;i++)
		 	{
			 Iterator<McsaConnection> connIter = connection_list[i].iterator();
			 LinkedList<McsaConnection> cleaned = new LinkedList<McsaConnection>();
			 while(connIter.hasNext())
			 	{
				 McsaConnection thisCon =connIter.next();
				 if(!emptySet.contains(new Integer(thisCon.getDeparture_station())))
				 	{
					 cleaned.add(thisCon);
				 	}
			 	}
			 if(cleaned.size()==0) emptySet.add(new Integer(i));
			 connection_list[i]=cleaned;
		 	}
		 if(emptySet.size()!=emptyCount) 
		 	{
			 System.out.println("Calling egain removeDeadEnds");
			 removeDeadEnds(emptySet);
		 	}else System.out.print("Finished removing dead ends");
		 
		}
	
	
	public void McsaIterative(long source_dt)
		{
		System.out.println("connection list lenght: "+connection_list.length);
		for(int i=0;i<connection_list.length;i++)
			{
			 System.out.println("station number "+i+" has "+connection_list[i].size()+" outgoing connection");
			 Iterator<McsaConnection> conIter = connection_list[i].iterator();
			 while(conIter.hasNext())
			 	{
				 McsaConnection con = conIter.next();
				 System.out.println("   "+con.getDeparture_station()+"  "+con.getArrival_station());
			 	}
			}
		 int dest_station=timetable.getDestinationIndex();
		 int source_station=timetable.getSourceIndex();
		 LinkedList<McsaConnection> temp= new LinkedList<McsaConnection>();
		 int[] visitStatus = new int[connection_list.length];
		 int[] endVisit = new int[connection_list.length];
		 int[] totalVisit= new int[connection_list.length];
		 HashSet<Integer> emptySet= new HashSet<Integer>();
		 removeDeadEnds(emptySet);
		 String endString="endVisit   [ ";
		 for(int i=0;i<visitStatus.length;i++)
		 	{
			 visitStatus[i]=0;
			 endVisit[i]=(connection_list[i].size()); ///avevo tolto -1 qui
			 if(endVisit[i]==0 && i!=source_station) emptySet.add(new Integer(i));
			 endString=endString+endVisit[i]+" , ";
		 	}
		 endString=endString+"]";
		 
		 /////printing again cleaned connection list
		 System.out.println(System.lineSeparator()+"Printing cleaned connection list");
		 for(int i=0;i<connection_list.length;i++)
			{
			 System.out.println("station number "+i+" has "+connection_list[i].size()+" outgoing connection");
			 Iterator<McsaConnection> conIter = connection_list[i].iterator();
			 while(conIter.hasNext())
			 	{
				 McsaConnection con = conIter.next();
				 System.out.println("   "+con.getDeparture_station()+"  "+con.getArrival_station());
			 	}
			}
		 ///////////////////////////////////////////
		 
		 
		 int visiting = dest_station;
		// int previous=0;
		 Stack<Integer> visitList = new Stack<Integer>();
		// while(!Arrays.equals(visitStatus, endVisit))
		 int count=1000;
		 while(count!=0)
		 	{
			 System.out.println("nuova ricerca soluzione, partendo dalla stazione "+visiting);
			 while(visiting != source_station)
			 	{
				 System.out.println("cazzo di size: "+connection_list[visiting].size());
				 if(connection_list[visiting].size()!=0)
				 	{
					 if(visitStatus[visiting]<=endVisit[visiting]) ///aggiunto -1 qui
					 {
						 System.out.println("visitSatus[visiting]="+visitStatus[visiting]+" endVisit[visiting]="+endVisit[visiting]+" visiting="+visiting);
						 McsaConnection con = connection_list[visiting].get(visitStatus[visiting]);
						 visitList.push(new Integer(visiting));
						 System.out.println("con is null: "+con==null);
						// previous=con.getArrival_station();
						// visitStatus[visiting]++; //////qualcosa qui
						 visiting=con.getDeparture_station();
						 System.out.println(con.toString());
						 System.out.println("next visit "+visiting);
						 temp.add(con);
					 }
				 	}/*else 
					 	{
						 System.out.println("uguali? "+Arrays.equals(visitStatus, endVisit));
						 if(!Arrays.equals(visitStatus, endVisit)){
							 System.out.println("Punto morto, aumento un cazzo di indice");
							 temp = new LinkedList<McsaConnection>();
							 boolean branchPointFound=false;
							 while(!branchPointFound)
							 	{
								 int index=visitList.pop().intValue();
								 if(visitStatus[index]!=endVisit[index])
								 	{
									 visitStatus[index]++;
									 visiting=dest_station;
									 branchPointFound=true;
								 	}
							 	}
							 String begin="visitStatus[ ";
							 for(int i=0;i<visitStatus.length;i++)
							 	{
								 begin=begin+visitStatus[i]+" , ";
							 	}
							 begin=begin+"]";
							 System.out.println(endString);
							 System.out.println(begin);
							// visitStatus[previous]++;
							 //visiting=previous;
						 }
					 	}*/
			 	}
			 System.out.println("uguali? "+Arrays.equals(visitStatus, endVisit));
			 LinkedList<Integer> leftIndexes = new LinkedList<Integer>(); ////quasi sicuramente serve una coda
			 boolean done=false;
			 while(!done)
			 	{
				 int index = visitList.peek();
				 if(visitStatus[index]==endVisit[index]) //c'era -1 qui
				 	{
					 leftIndexes.add(new Integer(index));
					 visitList.pop();
					 System.out.println("to reset "+index);
				 	}else
				 		{
				 		visitStatus[index]++;
				 		totalVisit[index]++;
				 		//visitList.pop();
				 		System.out.println("incremented "+index);
				 		done=true;
				 		}
			 	}
			 while(!leftIndexes.isEmpty())
			 	{
				 int resetIndex = leftIndexes.removeFirst();
				 visitStatus[resetIndex]=0;
				 System.out.println("resetting index="+resetIndex);
			 	}
			
			 /////////////////////////////////////////////////////
			 /*
			 while(!visitList.isEmpty())
			 	{
				 int index = visitList.pop().intValue();
				 leftIndexes.add(new Integer(index));
				 visitStatus[index]++;
				 if(visitStatus[index]==endVisit[index])
				 	{
					 int previousIndex = visitList.pop().intValue();
					 visitStatus[previousIndex]++;
					 Iterator<Integer> leftIter = leftIndexes.iterator();
					 while(leftIter.hasNext())
					 	{
						 int resetIndex = leftIter.next().intValue();
						 visitStatus[resetIndex]=0;
					 	}
				 	}
			 	}*/
			 /////////////////////////////////////////////////////////
			 //System.out.println("VisitList empty="+visitList.isEmpty()+" "+visitList.toString()+" first="+visitList.peek());
			 String begin="visitStatus[ ";
			 for(int i=0;i<visitStatus.length;i++)
			 	{
				 begin=begin+visitStatus[i]+" , ";
			 	}
			 begin=begin+"]";
			 String begin2="totalVisit [ ";
			 for(int i=0;i<totalVisit.length;i++)
			 	{
				 begin2=begin2+totalVisit[i]+" , ";
			 	}
			 begin2=begin2+"]";
			 
			 System.out.println(endString);
			 System.out.println(begin);
			 System.out.println(begin2);
			
			 System.out.println("Soluzione ad cazzum "+System.lineSeparator());
			 Iterator<McsaConnection> test = temp.iterator();
			 McsaConnection first = test.next();
			 System.out.println(first.getDeparture_station()+"      "+first.getFirst_point().getLatitude()+","+first.getFirst_point().getLongitude());
			 System.out.println(first.getArrival_station()+"      "+first.getSecond_point().getLatitude()+","+first.getSecond_point().getLongitude());
			 while(test.hasNext())
			 	{
				 McsaConnection toPrint = test.next();
				 System.out.println(toPrint.getArrival_station()+"     "+toPrint.getSecond_point().getLatitude()+","+toPrint.getSecond_point().getLongitude());
			 	}
			 count--;
		 	}
		 
		 /*int visiting=dest_station;
		   do
		 	{
			 int visiting2 =dest_station;
			 do
			 	{
				 if(visiting2!=-1)
				 	{
					 System.out.println("cazzo di size: "+connection_list[visiting2].size());
					 McsaConnection con = connection_list[visiting2].get(visitStatus[visiting2]);
					 System.out.println("con is null: "+con==null);
					 //inserire qui le condizioni per l'inserimento
					 temp.add(con);
					 visiting2=con.getDeparture_station();
					 System.out.println(con.toString());
					 System.out.println("next visit "+visiting2);
				 	}else System.out.println("PORCAMADONNAAAAAAAAAAAAAAAAAAAAAAAAAAAAH");
			 	}while(visiting2==source_station || visiting2==-1);
			 System.out.println("in the end visitn2="+visiting2+" and source="+source_station);
		 	}
		 	while(visitStatus!=endVisit);*/
		 	
		}
	
	/*ArrayList<ArrayList<int[]>> solIndexes = new ArrayList<ArrayList<int[]>>();
	private void doMCSA(int dest_station,int source_station,long source_dt,long next_ts,ArrayList<int[]> tempRes)
	{
		if(connection_list[dest_station].isEmpty()) return;
		else
			{
				//System.out.println("Connection list not empty");
				Iterator<McsaConnection> iter = connection_list[dest_station].iterator();
				//System.out.println("iterating over destination station n°:"+dest_station+" of size: "+connection_list[dest_station].size());
				int listIndex=0;
				while(iter.hasNext())
					{
					McsaConnection toAdd=iter.next();
					if(toAdd.departure_station==source_station)
						{
						if(toAdd.arrival_timestamp<=next_ts)
							{
							int[] conIndex = new int[2];
							conIndex[0]=dest_station;
							conIndex[1]=listIndex;
							ArrayList<int[]> copyRes = new ArrayList<int[]>();
							Iterator<int[]> tempIter = tempRes.iterator();
							while(tempIter.hasNext())
								{
								 int[] toRead = tempIter.next();
								 int[] toCopy = new int[2];
								 toCopy[0]=toRead[0];
								 toCopy[1]=toRead[1];
								 copyRes.add(toCopy);
								}
							copyRes.add(conIndex);
							solIndexes.add(copyRes);
							listIndex++;
							//System.out.println("departure station = source");
							//LinkedList<McsaConnection> copyRes=deepCopy(tempRes);
							//copyRes.add(toAdd);
							//result.add(copyRes);
							//tempRes=null;
							//return;
							}else return; 
						}else if(toAdd.arrival_timestamp<=next_ts)
							{
							int[] conIndex = new int[2];
							conIndex[0]=dest_station;
							conIndex[1]=listIndex;
							ArrayList<int[]> copyRes = new ArrayList<int[]>();
							Iterator<int[]> tempIter = tempRes.iterator();
							while(tempIter.hasNext())
								{
								int[] toRead = tempIter.next();
								int[] toCopy = new int[2];
								toCopy[0]=toRead[0];
								toCopy[1]=toRead[1];
								copyRes.add(toCopy);
								}
							copyRes.add(conIndex);
							//System.out.println("Not to the source, addding connection to temporary solution and piggodding");
							//LinkedList<McsaConnection> copyRes=deepCopy(tempRes);
							//System.out.print("Copied temporary list");
							//copyRes.add(toAdd);
							doMCSA(toAdd.departure_station,source_station,source_dt,toAdd.departure_timestamp,copyRes);
							listIndex++;
							}else return;
					}
			}
	}*/
	
	
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
	
	/*public McsaResult getResults() throws Exception
		{
		 Iterator<ArrayList<int[]>> indexIter = solIndexes.iterator();
		 while(indexIter.hasNext())
		 	{
			 ArrayList<int[]> thisSolIndexes = indexIter.next();
			 LinkedList<McsaConnection> conList = new LinkedList<McsaConnection>();
			 Iterator<int[]> indexes = thisSolIndexes.iterator();
			 while(indexes.hasNext())
			 	{
				 int[] thisIndex = indexes.next();
				 McsaConnection conn = connection_list[thisIndex[0]].get(thisIndex[1]);
				 conList.add(conn);
			 	}
			 result.add(conList);
		 	}
		 McsaResult res = null;
			if(result!=null)
				{
				 res = new McsaResult(result,this.departureTime,timetable.getSpecialNeeds(),timetable.getPassengerTransfer());
				}
			return res;
		}*/
}
