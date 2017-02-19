package bvcrplbe.domain;

import com.google.gson.JsonObject;

public class UserProfile {
	private int profileID;
	private int userID;
	private JsonObject ntransit;
	private JsonObject tmeans;
	private JsonObject servicecar;
	private JsonObject prefclass;
	private JsonObject specialneeds;
	private JsonObject tottraveltime;
	private JsonObject confortlevel;
	private JsonObject emissionsens;
	private JsonObject walkdistance;
	private JsonObject rangedeptime;
	private JsonObject pricerange;
	private JsonObject scorepolicy;
	
	
	public UserProfile(){};
	
	
	public JsonObject getPricerange() {
		return pricerange;
	}


	public void setPricerange(JsonObject pricerange) {
		this.pricerange = pricerange;
	}


	public int getProfileID() {
		return profileID;
	}
	public void setProfileID(int profileID) {
		this.profileID = profileID;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public JsonObject getNtransit() {
		return ntransit;
	}
	public void setNtransit(JsonObject ntransit) {
		this.ntransit = ntransit;
	}
	public JsonObject getTmeans() {
		return tmeans;
	}
	public void setTmeans(JsonObject tmeans) {
		this.tmeans = tmeans;
	}
	public JsonObject getServicecar() {
		return servicecar;
	}
	public void setServicecar(JsonObject servicecar) {
		this.servicecar = servicecar;
	}
	public JsonObject getPrefclass() {
		return prefclass;
	}
	public void setPrefclass(JsonObject prefclass) {
		this.prefclass = prefclass;
	}
	public JsonObject getSpecialneeds() {
		return specialneeds;
	}
	public void setSpecialneeds(JsonObject specialneeds) {
		this.specialneeds = specialneeds;
	}
	public JsonObject getTottraveltime() {
		return tottraveltime;
	}
	public void setTottraveltime(JsonObject tottraveltime) {
		this.tottraveltime = tottraveltime;
	}
	public JsonObject getConfortlevel() {
		return confortlevel;
	}
	public void setConfortlevel(JsonObject confortlevel) {
		this.confortlevel = confortlevel;
	}
	public JsonObject getEmissionsens() {
		return emissionsens;
	}
	public void setEmissionsens(JsonObject emissionsens) {
		this.emissionsens = emissionsens;
	}
	public JsonObject getWalkdistance() {
		return walkdistance;
	}
	public void setWalkdistance(JsonObject walkdistance) {
		this.walkdistance = walkdistance;
	}
	public JsonObject getRangedeptime() {
		return rangedeptime;
	}
	public void setRangedeptime(JsonObject rangedeptime) {
		this.rangedeptime = rangedeptime;
	}
	public JsonObject getScorepolicy() {
		return scorepolicy;
	}
	public void setScorepolicy(JsonObject scorepolicy) {
		this.scorepolicy = scorepolicy;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + userID;
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
		UserProfile other = (UserProfile) obj;
		if (userID != other.userID)
			return false;
		return true;
	}
	
	
	
}