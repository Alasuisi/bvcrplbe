package bvcrplbe;

public class DaoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9007462374640049175L;

	public DaoException(String msg)
	{
		super (new Exception(msg)); 
	}
}

