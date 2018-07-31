package frame.socket.util;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import frame.socket.exception.OutofIndexException;

public class StringUtil {
	
	@SuppressWarnings("unused")
	private static Logger logger=Logger.getLogger(StringUtil.class);
	
	public static byte[] StringtoBytes(String msg,int size,String charset) 
			throws OutofIndexException, UnsupportedEncodingException{
		byte[] msgbuf=msg.getBytes(charset);
		if(msgbuf.length>size){
			throw new OutofIndexException(msg+"长度:"+msgbuf.length+"大于size:"+size);
		}else{
			byte[] rs=new byte[size];
			for(int i=0;i<msgbuf.length;i++){
				rs[i]=msgbuf[i];
			}
			return rs;
		}
	}
	
	
	public static byte[] StringtoBytes(String msg,String charset) 
			throws UnsupportedEncodingException{
		byte[] msgbuf=msg.getBytes(charset);
		return msgbuf;
	}
	
	public static String BytestoString(byte[] msg,String charset) 
			throws UnsupportedEncodingException{
		int i=0;
		for(;i<msg.length;i++){
			if(msg[msg.length-1-i]!=0){
				break;
			}
		}
		int strlength=msg.length-i;
		return new String(msg,0,strlength,charset);
	}
	
//	public static void main(String[] args) {
//		String msg="abcd   000";
//		try {
//			byte[] msgbyte=StringtoBytes(msg,102, "utf-8");
//			String newmsg=StringtoBytes(msgbyte, "utf-8");
//			logger.info(newmsg+"\tnewmsg==msg:"+(newmsg.equals(msg)));
//		} catch (UnsupportedEncodingException | OutofIndexException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}
