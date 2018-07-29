package frame.socket.test.server;

import frame.socket.server.SocketServer;

public class SocketServerTest {
	public static void main(String[] args) {
		SocketServer server=new SocketServer(18002,1024) {
			
			@Override
			public void onMessage(String message, String socketId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(String errmsg,String socketId) {
				// TODO Auto-generated method stub
				
			}
		};
		server.setHeartParams(null, true);
		server.openServer();
	}
}
