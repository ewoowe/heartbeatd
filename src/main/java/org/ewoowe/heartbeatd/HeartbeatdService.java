package org.ewoowe.heartbeatd;

/**
 * 心跳监测框架对外API
 *
 * @author wangcheng2017@ict.ac.cn
 * @since 2021.01.20
 */
public interface HeartbeatdService {
	/**
	 * 注册心跳监测任务
	 *
	 * @param task 心跳监测任务
	 */
	void registerHeartbeatTask(HeartbeatTask task) throws InterruptedException;

	/**
	 * 注销心跳监测任务
	 *
	 * @param task 心跳监测任务
	 */
	void unregisterHeartbeatTask(HeartbeatTask task) throws InterruptedException;

	/**
	 * 心跳正常监测一次
	 *
	 * @param task 心跳监测任务
	 */
	void haveHeartbeatOnce(HeartbeatTask task);

	/**
	 * 心跳丢失监测一次
	 *
	 * @param task 心跳监测任务
	 */
	void lostHeartbeatOnce(HeartbeatTask task);
}
