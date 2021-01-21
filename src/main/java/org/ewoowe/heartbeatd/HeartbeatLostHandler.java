package org.ewoowe.heartbeatd;

/**
 * 心跳丢失处理器
 *
 * @author wangcheng217@ict.ac.cn
 * @date 2021.01.20
 */
public interface HeartbeatLostHandler
{
    /**
     * 当心跳丢失，并且经过{@link HeartbeatTask#times}次监测次数仍然丢失则
     * 认为对目标的心跳监测异常，需要告知用户心跳丢失
     *
     * @param task 心跳监测任务
     */
    void doWhenLost(HeartbeatTask task);
}
