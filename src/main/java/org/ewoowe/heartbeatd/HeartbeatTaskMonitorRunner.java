package org.ewoowe.heartbeatd;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 心跳检测任务的执行器
 *
 * @author wangcheng2017@ict.ac.cn
 * @date 2021.01.21
 */
public class HeartbeatTaskMonitorRunner implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        HeartbeatTaskMonitor taskMonitor = (HeartbeatTaskMonitor) context.getJobDetail().getJobDataMap().get("heartbeat task monitor instance");
        HeartbeatdService heartbeatdService = (HeartbeatdService) context.getJobDetail().getJobDataMap().get("heartbeatd service");
        if (!taskMonitor.getCancel().get())
            heartbeatdService.lostHeartbeatOnce(taskMonitor.getTask());
    }
}
