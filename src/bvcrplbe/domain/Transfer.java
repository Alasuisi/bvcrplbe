package bvcrplbe.domain;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.maps.model.LatLng;



public class Transfer {
	private int tran_id;
	private int user_id;
	private int prof_id;
	private int class_id;
	private int reser_id;
	private int pool_id;
	private String role; //JsonObject
	private String dep_addr;
	private String arr_addr;
	private Point2D.Double dep_gps;
	private Point2D.Double arr_gps;
	private Long dep_time;
	private String type;
	private int occ_seats;
	private int ava_seats;
	private boolean animal;
	private boolean handicap;
	private boolean smoke;
	private boolean luggage;
	private String status;
	private double price;
	private LinkedList<TimedPoint2D> path; //jsonobject
	private double det_range;
	private String ride_details;
	private String callback_uri;

	
	public String getCallback_uri() {
		return callback_uri;
	}

	public void setCallback_uri(String callback_uri) {
		this.callback_uri = callback_uri;
	}

	public Transfer(){}

	public double getDet_range() {
		return det_range;
	}

	public void setDet_range(double det_range) {
		this.det_range = det_range;
	}

	public String getRide_details() {
		return ride_details;
	}

	public void setRide_details(String ride_details) {
		this.ride_details = ride_details;
	}

	public int getTran_id() {
		return tran_id;
	}

	public void setTran_id(int tran_id) {
		this.tran_id = tran_id;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getProf_id() {
		return prof_id;
	}

	public void setProf_id(int prof_id) {
		this.prof_id = prof_id;
	}

	public int getClass_id() {
		return class_id;
	}

	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}

	public int getReser_id() {
		return reser_id;
	}

	public void setReser_id(int reser_id) {
		this.reser_id = reser_id;
	}

	public int getPool_id() {
		return pool_id;
	}

	public void setPool_id(int pool_id) {
		this.pool_id = pool_id;
	}

	public String getUser_role() {
		return role; 
		//return "{\"role\":\""+role+"\"}";
	}

	public void setUser_role(String user_role) throws JsonProcessingException {
		/*ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(user_role);
		this.role = jsonInString;*/
		this.role=user_role;
	}

	public String getDep_addr() {
		return dep_addr;
	}

	public void setDep_addr(String dep_addr) {
		this.dep_addr = dep_addr;
	}

	public String getArr_addr() {
		return arr_addr;
	}

	public void setArr_addr(String arr_addr) {
		this.arr_addr = arr_addr;
	}

	public Point2D.Double getDep_gps() {
		return dep_gps;
	}

	public void setDep_gps(Point2D.Double dep_gps) {
		this.dep_gps = dep_gps;
	}

	public Point2D.Double getArr_gps() {
		return arr_gps;
	}

	public void setArr_gps(Point2D.Double point2d) {
		this.arr_gps = point2d;
	}

	public Long getDep_time() {
		return dep_time;
	}

	public void setDep_time(Long dep_time) {
		this.dep_time = dep_time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getOcc_seats() {
		return occ_seats;
	}

	public void setOcc_seats(int occ_seats) {
		this.occ_seats = occ_seats;
	}

	public int getAva_seats() {
		return ava_seats;
	}

	public void setAva_seats(int ava_seats) {
		this.ava_seats = ava_seats;
	}

	public boolean isAnimal() {
		return animal;
	}

	public void setAnimal(boolean animal) {
		this.animal = animal;
	}

	public boolean isHandicap() {
		return handicap;
	}

	public void setHandicap(boolean handicap) {
		this.handicap = handicap;
	}

	public boolean isSmoke() {
		return smoke;
	}

	public void setSmoke(boolean smoke) {
		this.smoke = smoke;
	}

	public boolean isLuggage() {
		return luggage;
	}

	public void setLuggage(boolean luggage) {
		this.luggage = luggage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public LinkedList<TimedPoint2D> getPath() {
		/*ObjectMapper mapper = new ObjectMapper();
		LinkedList<Point2D> pathFromJson =mapper.readValue(path, new TypeReference<LinkedList<Point2D>>() {});*/
		return path;
	}

	public void setPath(LinkedList<TimedPoint2D> pathpp) {
		/*ObjectMapper mapper = new ObjectMapper();
		String pathInString = mapper.writeValueAsString(path);*/
		this.path = pathpp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tran_id;
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
		Transfer other = (Transfer) obj;
		if (tran_id != other.tran_id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transfer [tran_id=" + tran_id + ", user_id=" + user_id + ", prof_id=" + prof_id + ", class_id="
				+ class_id + ", reser_id=" + reser_id + ", pool_id=" + pool_id + ", role=" + role + ", dep_addr="
				+ dep_addr + ", arr_addr=" + arr_addr + ", dep_gps=" + dep_gps + ", arr_gps=" + arr_gps + ", dep_time="
				+ dep_time + ", type=" + type + ", occ_seats=" + occ_seats + ", ava_seats=" + ava_seats + ", animal="
				+ animal + ", handicap=" + handicap + ", smoke=" + smoke + ", luggage=" + luggage + ", status=" + status
				+ ", price=" + price + ", path=" + path + ", det_range=" + det_range + ", ride_details=" + ride_details
				+ ", callback_uri=" + callback_uri + "]";
	}

	
	
	
	

}
