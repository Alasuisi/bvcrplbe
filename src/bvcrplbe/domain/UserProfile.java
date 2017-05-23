package bvcrplbe.domain;

import com.fasterxml.jackson.databind.JsonNode;

public class UserProfile {
	private int profileID;
	private int userID;
	private String ntransit;
	private String tmeans;
	private String servicecar;
	private String prefclass;
	private String specialneeds;
	private String tottraveltime;
	private String confortlevel;
	private String emissionsens;
	private String walkdistance;
	private String rangedeptime;
	private String pricerange;
	private String scorepolicy;
	
	
	public UserProfile(){};
	
	
	public String getPricerange() {
		return pricerange;
	}


	public void setPricerange(String pricerange) {
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
	public String getNtransit() {
		return ntransit;
	}
	public void setNtransit(String ntransit) {
		this.ntransit = ntransit;
	}
	public String getTmeans() {
		return tmeans;
	}
	public void setTmeans(String tmeans) {
		this.tmeans = tmeans;
	}
	public String getServicecar() {
		return servicecar;
	}
	public void setServicecar(String servicecar) {
		this.servicecar = servicecar;
	}
	public String getPrefclass() {
		return prefclass;
	}
	public void setPrefclass(String prefclass) {
		this.prefclass = prefclass;
	}
	public String getSpecialneeds() {
		return specialneeds;
	}
	public void setSpecialneeds(String specialneeds) {
		this.specialneeds = specialneeds;
	}
	public String getTottraveltime() {
		return tottraveltime;
	}
	public void setTottraveltime(String tottraveltime) {
		this.tottraveltime = tottraveltime;
	}
	public String getConfortlevel() {
		return confortlevel;
	}
	public void setConfortlevel(String confortlevel) {
		this.confortlevel = confortlevel;
	}
	public String getEmissionsens() {
		return emissionsens;
	}
	public void setEmissionsens(String emissionsens) {
		this.emissionsens = emissionsens;
	}
	public String getWalkdistance() {
		return walkdistance;
	}
	public void setWalkdistance(String walkdistance) {
		this.walkdistance = walkdistance;
	}
	public String getRangedeptime() {
		return rangedeptime;
	}
	public void setRangedeptime(String rangedeptime) {
		this.rangedeptime = rangedeptime;
	}
	public String getScorepolicy() {
		return scorepolicy;
	}
	public void setScorepolicy(String scorepolicy) {
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


	@Override
	public String toString() {
		return "UserProfile [profileID=" + profileID + ", userID=" + userID + ", ntransit=" + ntransit + ", tmeans="
				+ tmeans + ", servicecar=" + servicecar + ", prefclass=" + prefclass + ", specialneeds=" + specialneeds
				+ ", tottraveltime=" + tottraveltime + ", confortlevel=" + confortlevel + ", emissionsens="
				+ emissionsens + ", walkdistance=" + walkdistance + ", rangedeptime=" + rangedeptime + ", pricerange="
				+ pricerange + ", scorepolicy=" + scorepolicy + "]";
	}
	
	
	
}