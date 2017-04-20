package bvcrplbe.domain;

public class Passenger {
	private int transferID;
	private TimedPoint2D boardingPoint;
	private TimedPoint2D getofPoint;
	
	public Passenger(int transferID, TimedPoint2D boardingPoint, TimedPoint2D getofPoint) {
		super();
		this.transferID = transferID;
		this.boardingPoint = boardingPoint;
		this.getofPoint = getofPoint;
	}

	public int getTransferID() {
		return transferID;
	}

	public void setTransferID(int transferID) {
		this.transferID = transferID;
	}

	public TimedPoint2D getBoardingPoint() {
		return boardingPoint;
	}

	public void setBoardingPoint(TimedPoint2D boardingPoint) {
		this.boardingPoint = boardingPoint;
	}

	public TimedPoint2D getGetofPoint() {
		return getofPoint;
	}

	public void setGetofPoint(TimedPoint2D getofPoint) {
		this.getofPoint = getofPoint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boardingPoint == null) ? 0 : boardingPoint.hashCode());
		result = prime * result + ((getofPoint == null) ? 0 : getofPoint.hashCode());
		result = prime * result + transferID;
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
		Passenger other = (Passenger) obj;
		if (boardingPoint == null) {
			if (other.boardingPoint != null)
				return false;
		} else if (!boardingPoint.equals(other.boardingPoint))
			return false;
		if (getofPoint == null) {
			if (other.getofPoint != null)
				return false;
		} else if (!getofPoint.equals(other.getofPoint))
			return false;
		if (transferID != other.transferID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Passenger [transferID=" + transferID + ", boardingPoint=" + boardingPoint + ", getofPoint=" + getofPoint
				+ "]";
	}
	
	

}
