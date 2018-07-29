package frame.socket.entity;

import java.awt.SecondaryLoop;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import frame.socket.config.HeartCheckResult;
import frame.socket.server.SocketServer;
import frame.socket.util.StringUtil;

public class ClientSocket {
	/**
	 * 是否连接
	 */
	private boolean isConnected=false;
	/**
	 * 与客户端通讯套接字
	 */
	private Socket socket;
	/**
	 * uuid唯一标识
	 */
	private String socketId;
	/**
	 * 是否开启心跳检测
	 */
	private boolean isOpenHeartCheck=false;
	/**
	 * 超时时间
	 */
	private Long TimeOutCheckInterval=5000L;
	/**
	 * socket总服务端
	 */
	private SocketServer socketServer;
	/**
	 * 数据读取线程
	 */
	private Thread readThread=new Thread(()->{
		getMessage();
	});
	
	/**
	 * 
	 */
	private boolean isTimeOut=false;
	
	private Thread heartCheckThread=new Thread(()->{
		while(isOpenHeartCheck){
			if(isTimeOut){
				ServerCloseSocket();
				break;
			}else{
				isTimeOut=true;
			}
			try {
				Thread.sleep(TimeOutCheckInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	});
	
	public ClientSocket(SocketServer server, boolean isConnected, Socket socket, String socketId,
			Long TimeOutCheckInterval,boolean isOpenHeartCheck) {
		this.isConnected = isConnected;
		this.socket = socket;
		this.socketId = socketId;
		this.socketServer=server;
		this.TimeOutCheckInterval=TimeOutCheckInterval;
		this.isOpenHeartCheck=isOpenHeartCheck;
		readThread.start();
		heartCheckThread.start();
	}
	
	/**
	 * 每一个客户端的数据读取
	 */
	private void getMessage() {
		try {
			InputStream inputStream = socket.getInputStream();
			BufferedInputStream is=new BufferedInputStream(inputStream);
			byte[] msgbuf=new byte[socketServer.getWR_size()];
			int size;
			while (isConnected && (size=is.read(msgbuf))!=-1) {
				String msg=StringUtil.BytestoString(msgbuf, socketServer.getWR_charset());
				HeartCheckResult rs=socketServer.checkHeart(msg, socketId);
				if(isOpenHeartCheck){
					if(rs.equals(HeartCheckResult.CheckSuccess)){//心跳检测成功向前端返还内容
						isTimeOut=false;
						socketServer.Send(socketServer.HeartCallback(msg, socketId), socketId);
					}else if(rs.equals(HeartCheckResult.IsNotHeartMsg)){//如果不是心跳检测的内容则交给业务层处理
						socketServer.onMessage(msg,socketId);
					}else if(rs.equals(HeartCheckResult.CheckFail)){//超时，超时则断开soket
						ServerCloseSocket();
					}
				}else{
					socketServer.onMessage(msg,socketId);
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}catch (IOException e) {
			socketServer.onError(e.getMessage(),socketId);
		}
	}
	
	
	private void ServerCloseSocket(){
		isTimeOut=true;
		isConnected=false;
		isOpenHeartCheck=false;
		try {
			if(!socket.isClosed()){
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public String getSocketId() {
		return socketId;
	}
	public void setSocketId(String socketId) {
		this.socketId = socketId;
	}
	
}
