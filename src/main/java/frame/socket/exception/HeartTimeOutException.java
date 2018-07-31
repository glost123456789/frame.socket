package frame.socket.exception;


import org.apache.log4j.Logger;

public class HeartTimeOutException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String reason;
	
	private Logger logger=Logger.getLogger(getClass());

	public HeartTimeOutException(String reason) {
		super();
		this.reason = reason;
	}
	
	@Override
	public void printStackTrace() {
		logger.error(reason);
		super.printStackTrace();
	}
	
	
}
