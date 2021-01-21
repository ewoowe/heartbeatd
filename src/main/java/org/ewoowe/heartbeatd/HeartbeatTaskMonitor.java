package org.ewoowe.heartbeatd;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于监视一个HeartbeatTask
 *
 * @author wangcheng2017@ict.ac.cn
 * @date 2021.01.21
 */
public class HeartbeatTaskMonitor
{
    /**
     * 要监测的心跳任务
     */
    private HeartbeatTask task;

    /**
     * 心跳是否已经丢失，初始值为false
     * 当心跳丢失次数连续达到{@link HeartbeatTaskMonitor#getThreshold}次后
     * 即{@code remains <= 0}时，losted被置为true
     * 并且当心跳恢复监测次数达到{@link HeartbeatTaskMonitor#getThreshold()}次后
     * 即{@code remains == threhold}时，loted被置位false
     */
    private AtomicBoolean losted = new AtomicBoolean(false);

    /**
     * 心跳丢失次数上限，初始值为{@link HeartbeatTask#getTimes}
     * 当达到上限时触发{@link HeartbeatLostHandler#doWhenLost(HeartbeatTask)}
     * 也可表示为losted后心跳恢复次数达到上限，然后触发{@link HeartbeatRecoverHandler#doWhenRecover(HeartbeatTask)}
     */
    private AtomicInteger threshold;

    /**
     * 心跳丢失/恢复剩余次数，当{@code remains <= 0}时，置losted为true，
     * 并触发{@link HeartbeatLostHandler#doWhenLost(HeartbeatTask)}
     * 当losted为true并且{@code remains == threshold}时触发{@link HeartbeatRecoverHandler#doWhenRecover(HeartbeatTask)}
     */
    private AtomicInteger remains;

    /**
     * 任务是否被取消
     */
    private AtomicBoolean cancel = new AtomicBoolean(false);

    public HeartbeatTask getTask()
    {
        return task;
    }

    public void setTask(HeartbeatTask task)
    {
        this.task = task;
    }

    public AtomicBoolean getLosted()
    {
        return losted;
    }

    public void setLosted(AtomicBoolean losted)
    {
        this.losted = losted;
    }

    public AtomicInteger getThreshold()
    {
        return threshold;
    }

    public void setThreshold(AtomicInteger threshold)
    {
        this.threshold = threshold;
    }

    public AtomicInteger getRemains()
    {
        return remains;
    }

    public void setRemains(AtomicInteger remains)
    {
        this.remains = remains;
    }

    public AtomicBoolean getCancel()
    {
        return cancel;
    }

    public void setCancel(AtomicBoolean cancel)
    {
        this.cancel = cancel;
    }

    /**
     * 增加一次剩余丢失次数，说明收到心跳监测了，但是增加的上限为threshold
     */
    public int incrementRemainsAndGet()
    {
        int old = remains.get();
        int upd = old + 1;
        if (old < threshold.get())
            remains.compareAndSet(old, upd);
        return remains.get();
    }

    /**
     * 减少一次剩余丢失次数，说明丢失心跳监测了，但是减少的下限为0
     */
    public int decrementRemainsAndGet()
    {
        int old = remains.get();
        int upd = old - 1;
        if (upd >= 0)
            remains.compareAndSet(old, upd);
        return remains.get();
    }
}
