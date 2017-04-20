package bvcrplbe.domain;

import java.util.LinkedList;

public class Pool {
	public int poolID;
	public int driverID;
	LinkedList<TimedPoint2D> driverPath=new LinkedList<TimedPoint2D>();
	LinkedList<Passenger> passengerList=new LinkedList<Passenger>();
	public Pool(int poolID, int driverID, LinkedList<TimedPoint2D> driverPath, LinkedList<Passenger> passengerList) {
		super();
		this.poolID = poolID;
		this.driverID = driverID;
		this.driverPath = driverPath;
		this.passengerList = passengerList;
	}
	public int getPoolID() {
		return poolID;
	}
	public void setPoolID(int poolID) {
		this.poolID = poolID;
	}
	public int getDriverID() {
		return driverID;
	}
	public void setDriverID(int driverID) {
		this.driverID = driverID;
	}
	public LinkedList<TimedPoint2D> getDriverPath() {
		return driverPath;
	}
	public void setDriverPath(LinkedList<TimedPoint2D> driverPath) {
		this.driverPath = driverPath;
	}
	public LinkedList<Passenger> getPassengerList() {
		return passengerList;
	}
	public void setPassengerList(LinkedList<Passenger> passengerList) {
		this.passengerList = passengerList;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + driverID;
		result = prime * result + ((driverPath == null) ? 0 : driverPath.hashCode());
		result = prime * result + ((passengerList == null) ? 0 : passengerList.hashCode());
		result = prime * result + poolID;
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
		Pool other = (Pool) obj;
		if (driverID != other.driverID)
			return false;
		if (driverPath == null) {
			if (other.driverPath != null)
				return false;
		} else if (!driverPath.equals(other.driverPath))
			return false;
		if (passengerList == null) {
			if (other.passengerList != null)
				return false;
		} else if (!passengerList.equals(other.passengerList))
			return false;
		if (poolID != other.poolID)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Pool [poolID=" + poolID + ", driverID=" + driverID + ", driverPath=" + driverPath + ", passengerList="
				+ passengerList + "]";
	}
	
	

}
