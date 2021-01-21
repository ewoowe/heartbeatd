package org.ewoowe.heartbeatd;

import java.util.concurrent.TimeUnit;

/**
 * 心跳监测任务对象
 *
 * @author wangcheng2017@ict.ac.cn
 * @date 2021.01.20
 */
public class HeartbeatTask
{
    /**
     * 任务唯一id
     */
    private String id;

    /**
     * 任务执行周期单位
     */
    private TimeUnit timeUnit;

    /**
     * 任务执行周期时间，单位s
     */
    private int period;

    /**
     * 心跳丢失/恢复次数发起心跳丢失通知/心跳恢复通知
     */
    private int times;

    private HeartbeatLostHandler heartbeatLostHandler;

    private HeartbeatRecoverHandler heartbeatRecoverHandler;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    public int getPeriod()
    {
        return period;
    }

    public void setPeriod(int period)
    {
        this.period = period;
    }

    public int getTimes()
    {
        return times;
    }

    public void setTimes(int times)
    {
        this.times = times;
    }

    public HeartbeatLostHandler getHeartbeatLostHandler()
    {
        return heartbeatLostHandler;
    }

    public void setHeartbeatLostHandler(HeartbeatLostHandler heartbeatLostHandler)
    {
        this.heartbeatLostHandler = heartbeatLostHandler;
    }

    public HeartbeatRecoverHandler getHeartbeatRecoverHandler()
    {
        return heartbeatRecoverHandler;
    }

    public void setHeartbeatRecoverHandler(HeartbeatRecoverHandler heartbeatRecoverHandler)
    {
        this.heartbeatRecoverHandler = heartbeatRecoverHandler;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HeartbeatTask that = (HeartbeatTask) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
