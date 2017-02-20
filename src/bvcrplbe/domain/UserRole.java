package bvcrplbe.domain;

public class UserRole {
	private static final String ROLE_DRIVER= "driver";
	private static final String ROLE_PASSENGER = "passenger";
	private String role;
	
	public UserRole(){}

	public String getRole() {
		return role;
	}

	public void setDriver() {
		this.role = ROLE_DRIVER;
	}
	public void setPassenger(){
		this.role = ROLE_PASSENGER;
	}

	

}
