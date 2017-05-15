package mcsa;

public class McsaException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5527827128071390273L;
	
	public McsaException(String msg)
	{
		super (new Exception(msg)); 
	}

}
