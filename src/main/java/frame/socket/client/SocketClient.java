package frame.socket.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.log4j.Logger;

import frame.socket.config.HeartCheckResult;
import frame.socket.util.StringUtil;

public abstract class  SocketClient {
	/**
	 * 与服务端通讯的套接字
	 */
	private Socket socket;
	/**
	 * 服务端地址（ip或者域名）
	 */
	private final String Address;
	/**
	 * 服务端端口
	 */
	private final int PORT;
	/**
	 * 读写的长度，单位比特
	 */
	private final int WR_size;
	/**
	 * 读写的编码
	 */
	private final String WR_charset="utf-8";
	/**
	 * 连接状态，true为连接中，false为已断开
	 */
	private boolean isConnected=false;
	/**
	 * 数据写出流
	 */
    OutputStream os;
    /**
     * 数据读入流
     */
    InputStream is;
    /**
     * 是否开启心跳
     */
    private boolean isOpenHeart=false;
    /**
     * 心跳检测时间间隔
     */
    private Long heartIntervalTime=5000L;
    /**
     * 心跳检测使用的数据包
     */
    private String HeartRandom;
    /**
     * 当前心跳检测超时次数
     */
    private int TimeoutCount=0;
    /**
     *最大超时次数
     */
    private int MaxTimeoutCount=3;
    /**
     * 心跳检测是否有返回
     */
    private boolean HeartIsBack=true;
    /**
     * 数据读取线程
     */
    Thread readthread=new Thread(()-> {
			getMessage();
		});
    
    private Logger logger=Logger.getLogger(getClass());
    
	public SocketClient(final String Address,final int Port,int read_size) {
		this.Address=Address;
		this.PORT=Port;
		this.WR_size=read_size;
	}
	
	public void connect(){
		try {
			socket=new Socket(Address, PORT);
			isConnected=true;//设置连接状态为true
			readthread.start();//开启读取数据线程
			startHeartCheck();//心跳检测开启，必须提前设置心跳参数
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getMessage() {
		try {
			is = socket.getInputStream();
			BufferedInputStream bis=new BufferedInputStream(is);
			byte[] msgbuf=new byte[WR_size];
			int size;
			while (isConnected && (size=bis.read(msgbuf))!=-1) {
				String msg=StringUtil.BytestoString(msgbuf, WR_charset);
				if(isOpenHeart){//心跳检测
					HeartCheckResult rs=CheckHeart(msg);
					if(rs.equals(HeartCheckResult.CheckSuccess)){//如果心跳检测成功，则清空超时次数
						logger.info("心跳检测成功，心跳包为："+msg);
						HeartIsBack=true;
						TimeoutCount=0;
					}else if(rs.equals(HeartCheckResult.IsNotHeartMsg)){//该条不是心跳检测的数据，则继续执行数据处理
						onMessage(msg);//数据处理
					}else if(rs.equals(HeartCheckResult.CheckFail)){//如果是心跳检测超时，则增加一次超时次数，并判断是否超时了最大
						//超时次数，如果超过则认为已断开连接，改变连接状态参数，并跳出对服务端数据的监听
						TimeoutCount++;
						CheckSocketIsTimeout();
					}
				}else{
					onMessage(msg);//数据处理
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					onError(e.getMessage());
					e.printStackTrace();
				}
			}
		}catch (IOException e) {
			onError(e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * 消息处理
	 * @param message
	 */
	public abstract void onMessage(String message);
	/**
	 * 链接异常处理
	 * @param errmsg
	 */
	public abstract void onError(String errmsg);
	
	/**
	 * 发送数据
	 * @param msg
	 */
	public void send(String msg){
		try {
			os=socket.getOutputStream(); 
			os.write(StringUtil.StringtoBytes(msg, WR_size, WR_charset));
		} catch (Exception e) {
			onError(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 心跳包的参数设置
	 * @param isOpenHeart 是否开启心跳检测，true为开启，false为不开启
	 * @param MaxTimeoutCount 最大超时检测次数，默认为3次，如果使用默认此处可以为null
	 * @param heartIntervalTime 心跳检测时间间隔，如果使用默认值，此处可以为null
	 */
	public void setHeartParams(boolean isOpenHeart,Integer MaxTimeoutCount,Long heartIntervalTime){
		this.isOpenHeart=isOpenHeart;
		if(MaxTimeoutCount!=null){
			this.MaxTimeoutCount=MaxTimeoutCount;
		}
		if(heartIntervalTime!=null){
			this.heartIntervalTime=heartIntervalTime;
		}
	}
	
	/**
	 * 判断socket通信是否已经超时，超时则断开socket
	 */
	public void CheckSocketIsTimeout(){
		if(TimeoutCount>=MaxTimeoutCount){
			ClientcolseSocket();
		}
	}
	
	
	/**
	 * 客户端主动连接断开
	 */
	public void ClientcolseSocket(){
		isConnected=false;
		isOpenHeart=false;
		try {
			if(!socket.isClosed()){
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 开启心跳检测，必须事先设置好心跳的各类参数
	 */
	private void startHeartCheck(){
		new Thread(()->{
			while(isOpenHeart){
				if(!HeartIsBack){//心跳发送间隔即为客户端超时检测间隔，如果上一次的超时还没有返回则超时次数+1
					TimeoutCount++;
					System.out.println("心跳检测时，TimeoutCount="+TimeoutCount);
					CheckSocketIsTimeout();
				}else{
					HeartIsBack=false;
				}
				HeartRandom=getHeartMsg();
				send(HeartRandom);
				try {
					Thread.sleep(heartIntervalTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 用户可以通过在子类中重写该方法，设置心跳包
	 * @return
	 */
	protected String getHeartMsg(){
		Random random=new Random();
		int temp=random.nextInt(1000);
		String Heartmsg="+++"+temp+"+++";
		return Heartmsg;
	}
	
	/**
	 * 用户可以通过在子类中重写该方法，自定义心跳包检测
	 * @param msg
	 * @return
	 */
	protected HeartCheckResult CheckHeart(String msg){
		if(msg.equals(HeartRandom)){
			return HeartCheckResult.CheckSuccess;
		}else{
			return HeartCheckResult.IsNotHeartMsg;
		}
	}
}
