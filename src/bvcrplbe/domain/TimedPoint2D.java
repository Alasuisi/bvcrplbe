package bvcrplbe.domain;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class TimedPoint2D{

	
	private Point2D.Double latLng = new Point2D.Double();
	private long touchTime;
	
	public TimedPoint2D(){};
	
	public TimedPoint2D(Point2D.Double latLng, long touchTime) {
		super();
		this.latLng = latLng;
		this.touchTime = touchTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((latLng == null) ? 0 : latLng.hashCode());
		result = prime * result + (int) (touchTime ^ (touchTime >>> 32));
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
		TimedPoint2D other = (TimedPoint2D) obj;
		if (latLng == null) {
			if (other.latLng != null)
				return false;
		} else if (!latLng.equals(other.latLng))
			return false;
		if (touchTime != other.touchTime)
			return false;
		return true;
	}
	public Point2D.Double getLatLng() {
		return latLng;
	}
	public void setLatLng(Point2D.Double latLng) {
		this.latLng = latLng;
	}
	public long getTouchTime() {
		return touchTime;
	}
	public void setTouchTime(long touchTime) {
		this.touchTime = touchTime;
	}
	

}
