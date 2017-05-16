package bvcrplbe.domain;

public class TranException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2000917499239635632L;
		
	public TranException(String msg)
	{
		super (new Exception(msg)); 
	}
}
