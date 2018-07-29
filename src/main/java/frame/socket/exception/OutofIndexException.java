package frame.socket.exception;

import java.io.PrintStream;

import org.apache.log4j.Logger;





@SuppressWarnings("serial")
public class OutofIndexException extends Exception{
	private String reason="";

	private Logger logger=Logger.getLogger(getClass());
	
	public OutofIndexException(String reason) {
		this.reason = reason;
	}
	
	@Override
	public void printStackTrace(PrintStream s) {
		// TODO Auto-generated method stub
		logger.error(reason);
		super.printStackTrace(s);
	}
}
