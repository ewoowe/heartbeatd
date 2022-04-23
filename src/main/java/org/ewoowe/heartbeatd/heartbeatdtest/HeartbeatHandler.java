package org.ewoowe.heartbeatd.heartbeatdtest;

import org.ewoowe.heartbeatd.HeartbeatLostHandler;
import org.ewoowe.heartbeatd.HeartbeatRecoverHandler;
import org.ewoowe.heartbeatd.HeartbeatTask;

public class HeartbeatHandler implements HeartbeatLostHandler, HeartbeatRecoverHandler {
    /**
     * 当心跳丢失，并且经过{@link HeartbeatTask#times}次监测次数仍然丢失则
     * 认为对目标的心跳监测异常，需要告知用户心跳丢失
     *
     * @param task 心跳监测任务
     */
    @Override
    public void doWhenLost(HeartbeatTask task) {
        System.out.println("心跳丢失");
    }

    /**
     * 当心跳恢复，并且经过{@link HeartbeatTask#times}次正常监测次数后
     * 认为对目标的心跳监测正常，需要告知用户心跳恢复
     *
     * @param task 心跳任务对象
     */
    @Override
    public void doWhenRecover(HeartbeatTask task) {
        System.out.println("心跳恢复");
    }
}
