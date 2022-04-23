package org.ewoowe.heartbeatd;

/**
 * 心跳恢复处理器
 *
 * @author wangcheng2017@ict.ac.cn
 * @since 2021.01.20
 */
public interface HeartbeatRecoverHandler {
    /**
     * 当心跳恢复，并且经过{@link HeartbeatTask#times}次正常监测次数后
     * 认为对目标的心跳监测正常，需要告知用户心跳恢复
     *
     * @param task 心跳任务对象
     */
    void doWhenRecover(HeartbeatTask task);
}
