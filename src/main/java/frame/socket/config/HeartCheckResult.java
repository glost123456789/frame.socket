package frame.socket.config;

public enum HeartCheckResult {
	/**
	 * 心跳检测成功
	 */
	CheckSuccess,
	/**
	 * 心跳检测失败
	 */
	CheckFail,
	/**
	 * 该条数据不是心跳检测数据
	 */
	IsNotHeartMsg;
}
