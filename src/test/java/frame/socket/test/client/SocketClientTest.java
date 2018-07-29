package frame.socket.test.client;

import frame.socket.client.SocketClient;

public class SocketClientTest {
	public static void main(String[] args) {
		SocketClient client=new SocketClient("localhost",18002,1024) {
			
			@Override
			public void onMessage(String message) {
				// TODO Auto-generated method stub
				System.out.println("客户端收到的内容为："+message);
			}
			
			@Override
			public void onError(String errmsg) {
				// TODO Auto-generated method stub
				
			}
		};
		client.setHeartParams(true, null, null);
		client.connect();
	}
	
}
