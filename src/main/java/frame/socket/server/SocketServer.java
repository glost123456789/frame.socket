package frame.socket.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import frame.socket.config.HeartCheckResult;
import frame.socket.entity.ClientSocket;
import frame.socket.exception.OutofIndexException;
import frame.socket.util.StringUtil;

public abstract class SocketServer {
	/**
	 * socket服务端
	 */
	private ServerSocket serverSocket;
	/**
	 * socket服务端端口
	 */
	private final int PORT;
	/**
	 * 单次读写长度
	 */
	private final int WR_size;
	/**
	 * 传输编码规则
	 */
	private final String WR_charset="utf-8";
	/**
	 * 正在连接的所有socket
	 */
	private Map<String,ClientSocket> socketmap=new HashMap<>();
	
	/**
	 * 是否开启心跳检测
	 */
	private boolean isOpenHeart=false;
	/**
	 * 心跳检测超时时间
	 */
	private Long Timeout=20000L;
	
	private Logger logger=Logger.getLogger(getClass());

	public SocketServer(int port,int size) {
		this.PORT = port;
		this.WR_size=size;
	}
	/**
	 * 服务端这是心跳参数
	 * @param Timeout 超时时间，如果使用默认，可以设为null
	 * @param isOpenHeart 是否打开心跳检测
	 */
	public void setHeartParams(Long Timeout,boolean isOpenHeart){
		if(Timeout!=null){
			this.Timeout=Timeout;
		}
		this.isOpenHeart=isOpenHeart;
	}
	
	public void openServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			// 记录连接过服务器的客户端数量
			int count = 0;
			System.out.println("***服务器即将启动，等待客户端的连接***");
			while (true) {// 循环侦听新的客户端的连接
				// 调用accept（）方法侦听，等待客户端的连接以获取Socket实例
				Socket socket=null;
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;
				System.out.println("服务器端被连接过的次数：" + count);
				InetAddress address = socket.getInetAddress();
				System.out.println("当前客户端的IP为：" + address.getHostAddress());
				final String socketid=UUID.randomUUID().toString();
				ClientSocket client=new ClientSocket(this,true,
						socket, socketid, Timeout, isOpenHeart);
				socketmap.put(socketid, client);
				Send("你好，服务端已接收到您的信息:socketid="+socketid,socketid);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void Send(String msg,String socketId) {
				try {
					OutputStream outputStream = socketmap.get(socketId).getSocket().getOutputStream();
					byte[] msgbuf=StringUtil.StringtoBytes(msg,WR_size,WR_charset);
//					logger.info("msgbuf.size="+msgbuf.length);
					outputStream.write(msgbuf);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OutofIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}

//	public void getMessage(String socketid) {
//		try {
//			InputStream inputStream = socketmap.get(socketid).getInputStream();
//			BufferedInputStream is=new BufferedInputStream(inputStream);
//			byte[] msgbuf=new byte[WR_size];
//			int size;
//			while ((size=is.read(msgbuf))!=-1) {
//				onMessage(new String(msgbuf, 0, size,WR_charset),socketid);
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}catch (IOException e) {
//			onError(e.getMessage(),socketid);
//		}
//	}
	
	/**
	 * 消息处理
	 * @param message
	 */
	public abstract void onMessage(String message,String socketId);
	/**
	 * 链接异常处理
	 * @param errmsg
	 */
	public abstract void onError(String errmsg,String socketId);
	
	/**
	 * 可以通过在子类中重写该方法自定义心跳检测的检测
	 * @param msg 收到来自客户端的内容
	 * @param socketId socket编号
	 * @return
	 */
	public HeartCheckResult checkHeart(String msg,String socketId){
		logger.debug("msg="+msg+"\tmsg.size="+msg.length());
//		logger.info("msg.size="+msg.length());
		if(msg.matches("\\+\\+\\+[0-9]*\\+\\+\\+")){
			return HeartCheckResult.CheckSuccess;
		}else{
			return HeartCheckResult.IsNotHeartMsg;
		}
	}
	/**
	 * 可以通过在子类中重写该方法自定义心跳检测的回复内容
	 * @param msg 收到来自客户端的内容
	 * @param socketId socket编号
	 * @return
	 */
	public String HeartCallback(String msg,String socketId){
		return msg;
	}
	


	public Long getTimeout() {
		return Timeout;
	}

	public void setTimeout(Long timeout) {
		Timeout = timeout;
	}

	public int getWR_size() {
		return WR_size;
	}

	public String getWR_charset() {
		return WR_charset;
	}
	
	public static void main(String[] args) {
		String msg="+++183+++";
		Logger logger=Logger.getLogger(ServerSocket.class);
		
		if(msg.matches("\\+\\+\\+[0-9]*\\+\\+\\+")){
			logger.info("aaa"+msg.matches("\\+\\+\\+[0-9]*\\+\\+\\+"));
		}else{
			logger.info("bbb"+msg.matches("\\+\\+\\+[0-9]*\\+\\+\\+"));
		}
	}
	
}
