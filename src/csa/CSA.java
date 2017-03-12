package csa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import bvcrplbe.domain.Transfer;


public class CSA {
    public static final int MAX_STATIONS  = 100000;

    private Timetable timetable;
    private Connection in_connection[];
    private long earliest_arrival[];
    
    private Transfer passenger;
    private LinkedList<Transfer> drivers;

    public CSA(LinkedList<Transfer> drivers,Transfer passenger) {
    	this.drivers=drivers;
    	this.passenger=passenger;
        timetable = new Timetable(drivers,passenger);
    }

    void main_loop(int arrival_station) {
        long earliest = Long.MAX_VALUE;
        for (Connection connection: timetable.connections) {
            if (connection.departure_timestamp >= earliest_arrival[connection.departure_station] &&
                    connection.arrival_timestamp < earliest_arrival[connection.arrival_station]) {
                earliest_arrival[connection.arrival_station] = connection.arrival_timestamp;
                in_connection[connection.arrival_station] = connection;

                if(connection.arrival_station == arrival_station) {
                    earliest = Math.min(earliest, connection.arrival_timestamp);
                }
            } else if(connection.arrival_timestamp > earliest) {
                return;
            }
        }
    }

    void print_result(int arrival_station) {
        if(in_connection[arrival_station] == null) {
            System.out.println("NO_SOLUTION");
        } else {
            List<Connection> route = new ArrayList<Connection>();
            // We have to rebuild the route from the arrival station 
            Connection last_connection = in_connection[arrival_station];
            while (last_connection != null) {
                route.add(last_connection);
                last_connection = in_connection[last_connection.departure_station];
            }

            // And now print it out in the right direction
            Collections.reverse(route);
            for (Connection connection : route) {
                System.out.println(connection.departure_station + " " + connection.arrival_station + " " +
                        connection.departure_timestamp + " " + connection.arrival_timestamp + " " + connection.transferID +" ("+connection.first_point.getLatitude()+","+connection.first_point.getLongitude()+")-->"+" ("+connection.second_point.getLatitude()+","+connection.second_point.getLongitude()+") "+connection.transferID);
            }
        }
        System.out.println("");
        System.out.flush();
    }

    private void compute(int departure_station, int arrival_station, long departure_time) {
        in_connection = new Connection[MAX_STATIONS];
        earliest_arrival = new long[MAX_STATIONS];
        for(int i = 0; i < MAX_STATIONS; ++i) {
            in_connection[i] = null;
            earliest_arrival[i] = Long.MAX_VALUE;
        }
        earliest_arrival[departure_station] = departure_time;

        if (departure_station <= MAX_STATIONS && arrival_station <= MAX_STATIONS) {
            main_loop(arrival_station);
        }
        print_result(arrival_station);
    }

    
    
    public void aggregateAll(LinkedList<Transfer> drivers,Transfer passenger)
    	{
    	// this.drivers=drivers;
    	// this.passenger=passenger;
    	 CSA csa = new CSA(drivers,passenger);
    	 csa.compute(timetable.getSourceIndex(), timetable.getDestinationIndex(), passenger.getDep_time());
    	}
    public void computeCSA()
    	{
    	this.compute(timetable.getSourceIndex(), timetable.getDestinationIndex(), passenger.getDep_time());
    	long diocaro= 1416879902083L;
    	//this.compute(0, 86, diocaro);
    	}
    
  /*  public static void main(String[] args) {
    	 String FILENAME = "D:\\bench_data_48h.txt";
    	//String FILENAME = "D:\\diocane.txt";
    	// FileReader fr = null;
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	 BufferedReader in=null;
		try {
			in = new BufferedReader(new FileReader(FILENAME));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        CSA csa = new CSA(in);
        csa.compute(19899, 29390, 3000);

        String line;
        try {
            line = in.readLine();

            while (!line.isEmpty()) {
                String[] tokens = line.split(" ");
                csa.compute(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                line = in.readLine();
            }
        } catch( Exception e) {
            System.out.println("Something went wrong while reading the parameters: " + e.getMessage());
        }
    }*/
}
